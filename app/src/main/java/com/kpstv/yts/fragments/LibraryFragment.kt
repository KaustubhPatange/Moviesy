package com.kpstv.yts.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat

import com.kpstv.yts.R
import com.kpstv.yts.activities.MainActivity
import com.kpstv.yts.activities.SearchActivity
import kotlinx.android.synthetic.main.fragment_watchlist.view.*

class LibraryFragment : Fragment() {

    private var v: View? = null
    private lateinit var mainActivity: MainActivity
    private val TAG = "LibraryFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return v ?: inflater.inflate(R.layout.fragment_library, container, false).also { view ->
            v = view
            mainActivity = activity as MainActivity

            setToolBar(view)
        }
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
