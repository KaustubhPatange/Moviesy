package com.kpstv.yts.ui.helpers

import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import com.kpstv.yts.R
import com.kpstv.yts.ui.fragments.sheets.CustomBottomSheet
import org.json.JSONObject

class ChangelogHelper(private val context: FragmentActivity) {
    private val preference = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Loads & show changelog from R.raw.changelog.json file when the
     * appropriate app versions are matched.
     */
    fun show() {
        val stream = context.resources.openRawResource(R.raw.changelog)
        val json = stream.bufferedReader().readText()
        val obj = JSONObject(json)
        val v1 = obj.getString("version")
        if (!preference.getBoolean(v1, false)) {
            val data = obj.getJSONArray("summary")
            val builder = StringBuilder()
            for(i in 0 until data.length()) {
                builder.apply {
                    append(data.getString(i))
                }
            }
            showBottomSheet(builder.toString())
            preference.edit {
                putBoolean(v1, true)
            }
        }
    }

    private fun showBottomSheet(text: String) = with(context) {
        CustomBottomSheet.show(
            fragmentManager = supportFragmentManager,
            title = getString(R.string.changelog),
            subtitle = text
        )
    }
}