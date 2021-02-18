package com.kpstv.yts.ui.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kpstv.yts.ui.activities.FinalActivity

object DeepLinksHelper {
    fun handle(context: Context, intent: Intent?): Boolean {
        if (intent == null) return false
        val data: Uri? = intent.data
        if (intent.action == Intent.ACTION_VIEW && data != null) {
            when {
                data.pathSegments.contains("movies") -> {
                    // Pass the data to Final Activity then
                    Intent(context, FinalActivity::class.java).apply {
                        putExtra(FinalActivity.MOVIE_URL, data.toString())
                        context.startActivity(this)
                    }
                    return true
                }
            }
        }
        return false
    }
}