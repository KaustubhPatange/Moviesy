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
import com.kpstv.yts.ui.dialogs.AlertNoIconDialog
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

    companion object {
        fun doesSubtitleExist(fileName: String): Boolean {
            var name = fileName.replace("(.zip)".toRegex(), "")
            if (!name.endsWith(".srt")) name = "$name.srt"
            AppInterface.SUBTITLE_LOCATION.listFiles()?.let { files ->
                files.forEach { if (it.name == name)
                    return true
                }
            }
            return false
        }
        fun String.removeSpecialCharacters() =
            replace("[':]".toRegex(), "")
    }

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

            val onlySuchFiles =
                AppInterface.SUBTITLE_LOCATION.listFiles()
                    ?.filter { f -> applySubtitleFilter(title.removeSpecialCharacters(), f) }
                    ?.map { it.name }
            if (onlySuchFiles?.isNotEmpty() == true) {
                val cssView = LayoutInflater.from(activity)
                    .inflate(R.layout.custom_select_subtitle, addLayout)
                val recyclerView = cssView.findViewById<RecyclerView>(R.id.recyclerView)

                val list = ArrayList<SelectSubtitle>()
                onlySuchFiles.mapTo(list) { SelectSubtitle(it) }

                singleAdapter = SelectSubAdapter(
                    models = list,
                    adapterOnSingleClick = { _, i ->
                        onlySuchFiles.indices.forEach { c ->
                            if (list[c].isChecked && i != c) {
                                list[c].isChecked = false
                                singleAdapter?.notifyItemChanged(c)
                            }
                        }

                        list[i].isChecked = !list[i].isChecked
                        singleAdapter?.notifyItemChanged(i)
                    },
                    adapterOnLongClick = { selectSubtitle, i ->
                        showAlertAndDeleteSubtitles(selectSubtitle.text, i)
                    }
                )
                recyclerView.layoutManager = LinearLayoutManager(activity.applicationContext)
                recyclerView.adapter = singleAdapter
            } else commonNoSubtitle()
        } else commonNoSubtitle()
    }

    private fun applySubtitleFilter(title: String, f: File?): Boolean {
        if (f == null) return false
        val fileName = f.name.removeSpecialCharacters()
        return (fileName.contains(title) ||
                fileName.contains(title.replace("\\s".toRegex(), "."))) &&
                f.extension.toLowerCase(Locale.ROOT) == "srt"
    }

    private fun showAlertAndDeleteSubtitles(fileName: String, pos: Int) = with(activity) {
        AlertNoIconDialog.Companion.Builder(this)
            .setTitle(fileName)
            .setMessage(getString(R.string.remove_subtitle))
            .setPositiveButton(getString(R.string.alright)) {
                AppInterface.SUBTITLE_LOCATION.listFiles()?.find { it.name == fileName }
                    ?.delete()
                singleAdapter?.models?.removeAt(pos)
                singleAdapter?.notifyItemRemoved(pos)
            }
            .setNegativeButton(getString(R.string.no)) { }
            .show()
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