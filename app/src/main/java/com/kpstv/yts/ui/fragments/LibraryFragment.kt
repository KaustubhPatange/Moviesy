package com.kpstv.yts.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.AppInterface.Companion.MOVIE_ID
import com.kpstv.yts.R
import com.kpstv.yts.adapters.LibraryDownloadAdapter
import com.kpstv.yts.databinding.FragmentLibraryBinding
import com.kpstv.yts.extensions.deleteRecursive
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.show
import com.kpstv.yts.ui.activities.FinalActivity
import com.kpstv.yts.ui.activities.MainActivity
import com.kpstv.yts.ui.activities.SearchActivity
import com.kpstv.yts.ui.dialogs.AlertNoIconDialog
import com.kpstv.yts.ui.fragments.sheets.BottomSheetLibraryDownload
import com.kpstv.yts.ui.fragments.sheets.PlaybackType
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_library.view.*
import java.io.File

@AndroidEntryPoint
class LibraryFragment : Fragment() {
    private val TAG = "LibraryFragment"

    private val viewModel by viewModels<MainViewModel>(
        ownerProducer = { requireActivity() }
    )

    private lateinit var mainActivity: MainActivity
    private lateinit var downloadAdapter: LibraryDownloadAdapter

    private lateinit var binding: FragmentLibraryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity

        viewModel.libraryView?.let {
            binding = FragmentLibraryBinding.bind(it)
        } ?: run {
            binding = FragmentLibraryBinding.bind(
                inflater.inflate(R.layout.fragment_library, container, false)
            )

            binding.fragmentLibraryNoDownload.root.hide()

            setToolBar()

            setRecyclerView()

            bindUI()

            viewModel.libraryView = binding.root
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.castHelper.init(
            activity = mainActivity,
            toolbar = view.toolbar,
            onSessionDisconnected = { model, lastSavedPosition ->
                if (model == null) return@init
                viewModel.updateDownload(model.hash, true, lastSavedPosition)
            }
        )
    }

    private fun setRecyclerView() {
        binding.recyclerViewDownload.layoutManager = LinearLayoutManager(context)
        downloadAdapter = LibraryDownloadAdapter(ArrayList())
        downloadAdapter.onClickListener = { model, _ ->
            /** OnClick for download item */
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
        }
        downloadAdapter.onMoreClickListener = { innerView, model, _ ->
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
                                } else {
                                    Toasty.error(
                                        requireContext(),
                                        getString(R.string.error_path_exist),
                                        Toasty.LENGTH_SHORT
                                    ).show()
                                    viewModel.removeDownload(model.hash)
                                }
                            }
                        }.show()
                    }
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }
        binding.recyclerViewDownload.adapter = downloadAdapter
    }

    private fun bindUI() = Coroutines.main {
        viewModel.downloadMovieIds.await().observe(mainActivity, Observer {
            downloadAdapter.updateModels(it)
            if (it.isNotEmpty()) {
                binding.fragmentLibraryNoDownload.root.hide()
                binding.flDownloadLayout.show()
            } else {
                binding.fragmentLibraryNoDownload.root.show()
                binding.flDownloadLayout.hide()
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
}
