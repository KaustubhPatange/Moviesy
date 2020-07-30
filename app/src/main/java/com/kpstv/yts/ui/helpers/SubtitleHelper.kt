package com.kpstv.yts.ui.helpers

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.adapters.SelectSubAdapter
import com.kpstv.yts.data.models.SelectSubtitle
import com.kpstv.yts.interfaces.listener.SingleClickListener
import com.kpstv.yts.ui.fragments.sheets.BottomSheetSubtitles
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class SubtitleHelper {
    private lateinit var activity: FragmentActivity
    private lateinit var parentView: View
    private lateinit var addLayout: LinearLayout
    private lateinit var title: String
    private lateinit var imdbCode: String

    private var bottomSheet: BottomSheetDialogFragment? = null

    private var singleAdapter: SelectSubAdapter? = null

    fun getSelectedSubtitle(): File? {
        singleAdapter?.models?.forEach {
            if (it.isChecked) {
                return File(AppInterface.SUBTITLE_LOCATION, it.text)
            }
        }
        return null
    }

    fun populateSubtitleView() {
        if (AppInterface.SUBTITLE_LOCATION.listFiles()?.isNotEmpty() == true) {

            /** Filter subtitle to check whether subtitle for current movie exist */

            val titleSpan = title.split(" ")[0]

            val onlySuchFiles =
                AppInterface.SUBTITLE_LOCATION.listFiles()?.filter { f ->
                    f?.name?.contains(titleSpan) == true && f.extension.toLowerCase(
                        Locale.ROOT
                    ) == "srt"
                }?.map { it.name }
            if (onlySuchFiles?.isNotEmpty() == true) {
                val cssView = LayoutInflater.from(activity)
                    .inflate(R.layout.custom_select_subtitle, addLayout)
                val recyclerView = cssView.findViewById<RecyclerView>(R.id.recyclerView)

                val list = ArrayList<SelectSubtitle>()
                onlySuchFiles.mapTo(list) { SelectSubtitle(it) }

                singleAdapter = SelectSubAdapter(list)
                singleAdapter?.setOnClickListener(object :
                    SingleClickListener {
                    override fun onClick(obj: Any, i: Int) {
                        onlySuchFiles.indices.forEach { c ->
                            if (list[c].isChecked && i != c) {
                                list[c].isChecked = false
                                singleAdapter?.notifyItemChanged(c)
                            }
                        }

                        list[i].isChecked = !list[i].isChecked
                        singleAdapter?.notifyItemChanged(i)
                    }
                })

                recyclerView.layoutManager = LinearLayoutManager(activity.applicationContext)
                recyclerView.adapter = singleAdapter
            } else commonNoSubtitle()
        } else commonNoSubtitle()
    }

    private fun commonNoSubtitle() = with(activity) {
        CommonTipHelper.Builder(this)
            .setTitle(getString(R.string.noSubs))
            .setButtonText(getString(R.string.download))
            .setParentLayout(addLayout)
            .setButtonClickListener {
                val bottomSheetSubtitles =
                    BottomSheetSubtitles()
                bottomSheetSubtitles.show(supportFragmentManager, imdbCode)
                bottomSheet?.dismiss()
            }
            .build()
            .populateView()
    }

    data class Builder(
        private val activity: FragmentActivity
    ) {
        private val helper = SubtitleHelper()

        init {
            helper.activity = activity
        }

        fun setTitle(title: String): Builder {
            helper.title = title
            return this
        }

        fun setImdbCode(value: String): Builder {
            helper.imdbCode = value
            return this
        }

        fun setParentView(value: View): Builder {
            helper.parentView = value
            return this
        }

        fun setAddLayout(value: LinearLayout): Builder {
            helper.addLayout = value
            return this
        }

        /** Optional parameter to pass if you want to automatically dismiss
         *  bottomSheet View on Download subtitle button clicked. */
        fun setParentBottomSheet(value: BottomSheetDialogFragment): Builder {
            helper.bottomSheet = value
            return this
        }

        fun build() =
            helper
    }

}