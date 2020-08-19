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

@AndroidEntryPoint
class LibraryFragment : Fragment(R.layout.fragment_library) {
    private val TAG = "LibraryFragment"

    private val viewModel by activityViewModels<MainViewModel>()

    private lateinit var mainActivity: MainActivity
    private lateinit var downloadAdapter: LibraryDownloadAdapter
    private var mediaRouteMenuItem: MenuItem? = null

    private val binding by viewBinding(FragmentLibraryBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = requireActivity() as MainActivity

        setToolBar()

        setRecyclerView()

        bindUI()

        mainActivity.castHelper.init(
            activity = mainActivity,
            onSessionDisconnected = { model, lastSavedPosition ->
                if (model == null) return@init
                viewModel.updateDownload(model.hash, true, lastSavedPosition)
            },
            onNeedToShowIntroductoryOverlay = {
                mainActivity.castHelper.showIntroductoryOverlay(mediaRouteMenuItem)
            }
        )
        mediaRouteMenuItem =
            mainActivity.castHelper.setMediaRouteMenu(requireContext(), binding.toolbar.menu)
    }

    override fun onDestroyView() {
        mainActivity.castHelper.unInit()
        super.onDestroyView()
    }

    private fun setRecyclerView() {
        binding.recyclerViewDownload.layoutManager = LinearLayoutManager(requireContext())
        downloadAdapter = LibraryDownloadAdapter(

            onClickListener = { model, _ ->
                val sheet = if (mainActivity.castHelper.isCastActive()) {
                    /** Show cast to device button */
                    BottomSheetLibraryDownload(mainActivity.castHelper, PlaybackType.REMOTE)
                } else {
                    /** Show local play button */
                    BottomSheetLibraryDownload(mainActivity.castHelper, PlaybackType.LOCAL)
                }
                val bundle = Bundle()
                bundle.putSerializable("model", model)
                sheet.arguments = bundle
                sheet.show(mainActivity.supportFragmentManager, "")
            },

            onMoreClickListener = { innerView, model, _ ->
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
        )
        binding.recyclerViewDownload.adapter = downloadAdapter
    }

    private fun bindUI() {
        viewModel.downloadMovieIds.observe(viewLifecycleOwner, Observer {
            downloadAdapter.submitList(it)
            if (it.isNotEmpty()) {
                binding.fragmentLibraryNoDownload.root.hide()
                binding.flDownloadLayout.show()
            } else {
                binding.fragmentLibraryNoDownload.root.show()
                binding.flDownloadLayout.hide()
            }

            /** Restore previous state of recyclerView */
            if (viewModel.libraryFragmentState.recyclerViewState != null) {
                binding.recyclerViewDownload.layoutManager?.onRestoreInstanceState(viewModel.libraryFragmentState.recyclerViewState)
                viewModel.libraryFragmentState.recyclerViewState = null
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

    /**
     * Save all your state here
     */
    override fun onStop() {
        super.onStop()
        viewModel.libraryFragmentState.recyclerViewState =
            binding.recyclerViewDownload.layoutManager?.onSaveInstanceState()
    }
}
