package com.kpstv.yts.ui.helpers

import android.util.Log
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.AppInterface
import com.kpstv.yts.extensions.utils.RetrofitUtils
import com.kpstv.yts.interfaces.api.TMdbApi
import com.kpstv.yts.ui.activities.SearchActivity
import okhttp3.Request
import okhttp3.internal.wait
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class is created for [SearchActivity] to handle search
 * suggestions here.
 *
 * This all methods should not be run on mainThread otherwise
 * you will get NetworkOnMainThread exception.
 *
 * This class should not exist in first place but some implementation
 * in [SearchActivity] with RxJava gave rise to it.
 *
 * TODO: Should be removed when SearchActivity is refactored.
 */
@Singleton
class SuggestionHelper @Inject constructor(
    private val retrofitUtils: RetrofitUtils,
    private val tMdbApi: TMdbApi
) {
    fun getGoogleSuggestions(text: CharSequence): List<String> {
        val list = ArrayList<String>()

        val response = retrofitUtils.getHttpClient().newCall(
            Request.Builder()
                .url("${AppInterface.SUGGESTION_URL}$text")
                .build()
        ).execute()

        val json = response.body?.string()
        response.body?.close() // Always close the stream

        if (json?.isNotEmpty() == true) {
            val jsonArray = JSONArray(json).getJSONArray(1)
            for (i in 0 until jsonArray.length())
                list.add(jsonArray.getString(i))
        }

        return list
    }

    fun getTMDBSuggestions(text: CharSequence): List<String> {
        val list = ArrayList<String>()
        Coroutines.io {
            val results = tMdbApi.getSearch(text.toString())
            list.addAll(results.results.map { it.original_title })
            Log.e(javaClass.simpleName, "Results: $results")
        }.wait()
        Log.e(javaClass.simpleName, "Returning")
        //val results = tMdbApi.getSearch(text.toString())
        //return emptyList()
        return list
    }
}