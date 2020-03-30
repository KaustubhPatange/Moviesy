package com.kpstv.yts.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.kpstv.yts.R
import com.kpstv.yts.activities.MainActivity
import com.kpstv.yts.activities.SearchActivity
import com.kpstv.yts.activities.TorrentPlayerActivity
import com.kpstv.yts.adapters.LibraryDownloadAdapter
import com.kpstv.yts.dialogs.AlertNoIconDialog
import com.kpstv.yts.extensions.Coroutines
import com.kpstv.yts.extensions.deleteRecursive
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.show
import com.kpstv.yts.fragments.sheets.BottomSheetLibraryDownload
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_library.view.*
import kotlinx.android.synthetic.main.fragment_library_no_download.view.*
import kotlinx.android.synthetic.main.fragment_watchlist.view.toolbar
import java.io.File

class LibraryFragment : Fragment() {

    private var v: View? = null
    private lateinit var mainActivity: MainActivity
    private val TAG = "LibraryFragment"
    private lateinit var downloadAdapter: LibraryDownloadAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return v ?: inflater.inflate(R.layout.fragment_library, container, false).also { view ->
            v = view
            mainActivity = activity as MainActivity

            view.layout_noDownload.hide()

            setToolBar(view)

            setRecyclerView(view)

            bindUI(view)
        }
    }

    private fun setRecyclerView(view: View) {
        view.recyclerView_download.layoutManager = LinearLayoutManager(context)
        downloadAdapter = LibraryDownloadAdapter(context!!, ArrayList())
        downloadAdapter.OnClickListener = { model, i ->
            // TODO: Show related sub
            val sheet = BottomSheetLibraryDownload()
            val bundle = Bundle()
            bundle.putString("title", model.title)
            bundle.putString("imdbCode", model.imdbCode)
            bundle.putString("normalLink", model.videoPath)
            sheet.arguments = bundle
            sheet.show(mainActivity.supportFragmentManager, "")
           /* val intent = Intent(context, TorrentPlayerActivity::class.java)
            intent.putExtra("normalLink", model.videoPath)
            startActivity(intent)*/
        }
        downloadAdapter.OnMoreClickListener = { view, model, i ->
           val popupMenu = PopupMenu(context!!, view)
            popupMenu.inflate(R.menu.library_menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_show_location -> {
                        AlertNoIconDialog.Companion.Builder(context).apply {
                            setTitle("Location")
                            setMessage("${model.downloadPath}")
                            setPositiveButton("OK",null)
                        }.show()
                    }
                    R.id.action_delete -> {
                        AlertNoIconDialog.Companion.Builder(context).apply {
                            setTitle("Delete?")
                            setMessage("This can't be undone?")
                            setNegativeButton("Nope", object: AlertNoIconDialog.DialogListener {
                                override fun onClick() { }
                            })
                            setPositiveButton("Do It", object: AlertNoIconDialog.DialogListener {
                                override fun onClick() {
                                    val f = File(model.downloadPath!!)
                                    if (f.exists()) {
                                        f.deleteRecursive()
                                    }else {
                                        Toasty.error(context!!, "Path does not exist", Toasty.LENGTH_SHORT).show()
                                        mainActivity.viewModel.removeDownload(model.hash)
                                    }
                                }
                            })
                        }.show()
                    }
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }
        view.recyclerView_download.adapter = downloadAdapter
    }

    private fun bindUI(view: View) = Coroutines.main {
        mainActivity.viewModel.downloadMovieIds.await().observe(mainActivity, Observer {
            downloadAdapter.updateModels(it)
            if (it.isNotEmpty()) {
                view.layout_noDownload.hide()
                view.fl_downloadLayout.show()
            } else {
                view.layout_noDownload.show()
                view.fl_downloadLayout.hide()
            }
        })
    }

    private fun setToolBar(view: View) {
        view.toolbar.title = getString(R.string.library)
        view.toolbar.setNavigationIcon(R.drawable.ic_menu)
        view.toolbar.setNavigationOnClickListener {
            mainActivity.drawerLayout.openDrawer(GravityCompat.START)
        }
        view.toolbar.inflateMenu(R.menu.fragment_watchlist_menu)

        view.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_search) {
                val intent = Intent(mainActivity, SearchActivity::class.java)
                startActivity(intent)
            }
            return@setOnMenuItemClickListener true
        }
    }
}
