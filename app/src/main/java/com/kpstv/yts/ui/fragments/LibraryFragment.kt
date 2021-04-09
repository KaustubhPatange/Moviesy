package com.kpstv.yts.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.AppInterface.Companion.MOVIE_ID
import com.kpstv.yts.R
import com.kpstv.yts.adapters.LibraryDownloadAdapter
import com.kpstv.yts.databinding.FragmentLibraryBinding
import com.kpstv.yts.extensions.deleteRecursive
import com.kpstv.common_moviesy.extensions.hide
import com.kpstv.common_moviesy.extensions.show
import com.kpstv.yts.AppSettings
import com.kpstv.yts.cast.CastHelper
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.extensions.common.CustomTipLayout
import com.kpstv.yts.ui.activities.AbstractBottomNavActivity
import com.kpstv.yts.ui.activities.FinalActivity
import com.kpstv.yts.ui.activities.MainActivity
import com.kpstv.yts.ui.activities.SearchActivity
import com.kpstv.yts.ui.dialogs.AlertNoIconDialog
import com.kpstv.yts.ui.fragments.sheets.BottomSheetLibraryDownload
import com.kpstv.yts.ui.fragments.sheets.PlaybackType
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import java.io.File
import java.lang.ref.WeakReference

@Deprecated("Use v2")
@AndroidEntryPoint
class LibraryFragment : Fragment(R.layout.fragment_library), AbstractBottomNavActivity.BottomNavFragmentSelection {
    private val TAG = "LibraryFragment"

    private val viewModel by activityViewModels<MainViewModel>()

    private lateinit var mainActivity: MainActivity
    private lateinit var downloadAdapter: WeakReference<LibraryDownloadAdapter>
    private var mediaRouteMenuItem: WeakReference<MenuItem?>? = null

    private val binding by viewBinding(FragmentLibraryBinding::bind)
    private val isCastingSupported by lazy { CastHelper.isCastingSupported(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = requireActivity() as MainActivity

        setToolBar()

        setRecyclerView()

        bindUI()

        addDownloadTip()

        if (isCastingSupported) {
            mainActivity.castHelper.init(
                activity = mainActivity,
                onSessionDisconnected = { model, lastSavedPosition ->
                    if (model == null) return@init
                    viewModel.updateDownload(model.hash, true, lastSavedPosition)
                },
                onNeedToShowIntroductoryOverlay = {
                    mainActivity.castHelper.showIntroductoryOverlay(mediaRouteMenuItem?.get())
                }
            )
            mediaRouteMenuItem = WeakReference(mainActivity.castHelper.setMediaRouteMenu(requireContext(), binding.toolbar.menu))
        }
    }

    override fun onReselected() {
        binding.recyclerViewDownload.smoothScrollToPosition(0)
    }

    override fun onDestroyView() {
        if (isCastingSupported) mainActivity.castHelper.unInit()
        super.onDestroyView()
    }

    private fun setRecyclerView() {
        binding.recyclerViewDownload.layoutManager = LinearLayoutManager(requireContext())
        downloadAdapter = WeakReference(
            LibraryDownloadAdapter(
                onClickListener = { model, _ ->
                    adapterOnClickListener(model)
                },
                onMoreClickListener = { innerView, model, _ ->
                    adapterOnMoreListener(innerView, model)
                }
            )
        )
        binding.recyclerViewDownload.adapter = downloadAdapter.get()
    }

    private fun bindUI() {
        viewModel.downloadMovieIds.observe(viewLifecycleOwner, Observer {
            downloadAdapter.get()?.submitList(it)
            if (it.isNotEmpty()) {
                binding.fragmentLibraryNoDownload.root.hide()
                binding.flDownloadLayout.show()
            } else {
                binding.fragmentLibraryNoDownload.root.show()
                binding.flDownloadLayout.hide()
            }

            /** Restore previous state of recyclerView */
            if (viewModel.uiState_old.libraryFragmentState.recyclerViewState != null) {
                binding.recyclerViewDownload.layoutManager?.onRestoreInstanceState(viewModel.uiState_old.libraryFragmentState.recyclerViewState)
                viewModel.uiState_old.libraryFragmentState.recyclerViewState = null
            }
        })
    }

    private fun setToolBar() {
        binding.toolbar.title = getString(R.string.library)
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu)
        binding.toolbar.setNavigationOnClickListener {
            mainActivity.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.toolbar.inflateMenu(R.menu.fragment_library_menu)

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_search) {
                val intent = Intent(mainActivity, SearchActivity::class.java)
                startActivity(intent)
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun adapterOnClickListener(model: Model.response_download) {
        val sheet = if (isCastingSupported && mainActivity.castHelper.isCastActive()) {
            /** Show cast to device button */
            BottomSheetLibraryDownload(mainActivity.castHelper, PlaybackType.REMOTE)
        } else {
            /** Show local play button */
            BottomSheetLibraryDownload(playbackType = PlaybackType.LOCAL)
        }
        val bundle = Bundle()
        bundle.putSerializable("model", model)
        sheet.arguments = bundle
        sheet.show(mainActivity.supportFragmentManager, "")
    }

    private fun adapterOnMoreListener(innerView: View, model: Model.response_download) {
        val popupMenu = PopupMenu(requireContext(), innerView)
        popupMenu.inflate(R.menu.library_menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_details -> startActivity(
                    Intent(requireContext(), FinalActivity::class.java).apply {
                        putExtra(MOVIE_ID, model.movieId)
                    }
                )
                R.id.action_show_location -> {
                    AlertNoIconDialog.Companion.Builder(context).apply {
                        setTitle(getString(R.string.location))
                        setMessage("${model.downloadPath}")
                        setPositiveButton("OK", null)
                    }.show()
                }
                R.id.action_delete -> {
                    AlertNoIconDialog.Companion.Builder(context).apply {
                        setTitle("Delete?")
                        setMessage(getString(R.string.delete_undone))
                        setNegativeButton(getString(R.string.no)) { }
                        setPositiveButton(getString(R.string.yes)) {
                            val f = File(model.downloadPath!!)
                            if (f.exists()) {
                                f.deleteRecursive()
                                viewModel.removeDownload(model.hash)
                            } else {
                                Toasty.error(
                                    requireContext(),
                                    getString(R.string.error_path_exist),
                                    Toasty.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }.show()
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

    private fun addDownloadTip() {
        CustomTipLayout.Builder(binding.addLayout)
            .setTitle("Existing downloads")
            .setMessage("In any case, your existing download doesn't show up here you can go to Settings > Storage > Scan existing downloads.")
            .show(AppSettings.SHOW_DOWNLOAD_TIP_PREF)
    }

    /**
     * Save all your state here
     */
    override fun onStop() {
        super.onStop()
        viewModel.uiState_old.libraryFragmentState.recyclerViewState =
            binding.recyclerViewDownload.layoutManager?.onSaveInstanceState()
    }
}
