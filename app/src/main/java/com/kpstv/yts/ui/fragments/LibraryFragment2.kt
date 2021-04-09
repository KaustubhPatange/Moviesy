package com.kpstv.yts.ui.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.common_moviesy.extensions.applyTopInsets
import com.kpstv.common_moviesy.extensions.hide
import com.kpstv.common_moviesy.extensions.show
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.navigation.Navigator
import com.kpstv.yts.AppSettings
import com.kpstv.yts.R
import com.kpstv.yts.adapters.LibraryDownloadAdapter
import com.kpstv.yts.cast.CastHelper
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.databinding.FragmentLibraryBinding
import com.kpstv.yts.extensions.common.CustomTipLayout
import com.kpstv.yts.extensions.deleteRecursive
import com.kpstv.yts.ui.dialogs.AlertNoIconDialog
import com.kpstv.yts.ui.fragments.sheets.BottomSheetLibraryDownload
import com.kpstv.yts.ui.fragments.sheets.PlaybackType
import com.kpstv.yts.ui.viewmodels.MainViewModel
import com.kpstv.yts.ui.viewmodels.StartViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import java.io.File

@AndroidEntryPoint
class LibraryFragment2 : Fragment(R.layout.fragment_library), Navigator.BottomNavigation.Callbacks {
    interface Callbacks {
        fun getCastHelper() : CastHelper
    }

    private val binding by viewBinding(FragmentLibraryBinding::bind)
    private val viewModel by viewModels<MainViewModel>(
        ownerProducer = ::requireParentFragment
    )
    private val navViewModel by activityViewModels<StartViewModel>()
    private val isCastingSupported by lazy { CastHelper.isCastingSupported(requireContext()) }
    private val castHelper by lazy { (requireParentFragment() as Callbacks).getCastHelper() }
    private lateinit var downloadAdapter: LibraryDownloadAdapter
    private var mediaRouteMenuItem: MenuItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appBarLayout.applyTopInsets()
        setToolBar()
        bindUI()
        setRecyclerView()
        addDownloadTip()

        if (isCastingSupported) {
            castHelper.init(
                activity = requireActivity(),
                onSessionDisconnected = { model, lastSavedPosition ->
                    if (model == null) return@init
                    viewModel.updateDownload(model.hash, true, lastSavedPosition)
                },
                onNeedToShowIntroductoryOverlay = {
                    castHelper.showIntroductoryOverlay(mediaRouteMenuItem)
                }
            )
            mediaRouteMenuItem = castHelper.setMediaRouteMenu(requireContext(), binding.toolbar.menu)
        }
    }

    private fun bindUI() {
        viewModel.downloadMovieIds.observe(viewLifecycleOwner) { downloads: List<Model.response_download> ->
            downloadAdapter.submitList(downloads)
            if (downloads.isNotEmpty()) {
                binding.fragmentLibraryNoDownload.root.hide()
                binding.flDownloadLayout.show()
                binding.downloadHeader.hide()
            } else {
                binding.fragmentLibraryNoDownload.root.show()
                binding.flDownloadLayout.hide()
                binding.downloadHeader.show()
            }

            // Restore previous state of recyclerView
            if (viewModel.uiState.libraryFragmentState.recyclerViewState != null) {
                binding.recyclerViewDownload.layoutManager?.onRestoreInstanceState(viewModel.uiState.libraryFragmentState.recyclerViewState)
                viewModel.uiState.libraryFragmentState.recyclerViewState = null
            }
        }
    }

    override fun onReselected() {
        binding.recyclerViewDownload.smoothScrollToPosition(0)
    }

    override fun onDestroyView() {
        //if (isCastingSupported) mainActivity.castHelper.unInit() // TODO:
        super.onDestroyView()
    }

    private fun setRecyclerView() {
        downloadAdapter = LibraryDownloadAdapter(
            onClickListener = { model, _ ->
                adapterOnClickListener(model)
            },
            onMoreClickListener = { innerView, model, _ ->
                adapterOnMoreListener(innerView, model)
            }
        )
        binding.recyclerViewDownload.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = downloadAdapter
        }
    }

    private fun adapterOnClickListener(model: Model.response_download) {
        val sheet = if (isCastingSupported && castHelper.isCastActive()) {
            /** Show cast to device button */
            BottomSheetLibraryDownload(castHelper, PlaybackType.REMOTE)
        } else {
            /** Show local play button */
            BottomSheetLibraryDownload(playbackType = PlaybackType.LOCAL)
        }
        val bundle = Bundle()
        bundle.putSerializable("model", model)
        sheet.arguments = bundle
        sheet.show(childFragmentManager, "")
    }

    private fun adapterOnMoreListener(innerView: View, model: Model.response_download) {
        val popupMenu = PopupMenu(requireContext(), innerView)
        popupMenu.inflate(R.menu.library_menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_details -> navViewModel.goToDetail(ytsId = model.movieId)
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
                                Toasty.error(requireContext(), getString(R.string.error_path_exist), Toasty.LENGTH_SHORT).show()
                            }
                        }
                    }.show()
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

    private fun setToolBar() {
        val caller = parentFragment as MainFragmentDrawerCallbacks

        binding.toolbar.title = getString(R.string.library)
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu)
        binding.toolbar.setNavigationOnClickListener {
            caller.openDrawer()
        }
        binding.toolbar.inflateMenu(R.menu.fragment_library_menu)

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_search) {
                navViewModel.goToSearch()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun addDownloadTip() {
        binding.root.doOnPreDraw {
            CustomTipLayout.Builder(binding.addLayout)
                .setTitle(getString(R.string.download_tip_title))
                .setMessage(getString(R.string.download_tip_text))
                .show(AppSettings.SHOW_DOWNLOAD_TIP_PREF)
        }
    }

    /**
     * Save all your state here
     */
    override fun onStop() {
        super.onStop()
        viewModel.uiState.libraryFragmentState.recyclerViewState = binding.recyclerViewDownload.layoutManager?.onSaveInstanceState()
    }
}
