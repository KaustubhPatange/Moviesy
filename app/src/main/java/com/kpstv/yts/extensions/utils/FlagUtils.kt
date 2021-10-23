package com.kpstv.yts.extensions.utils

import android.annotation.SuppressLint
import android.util.Log
import com.kpstv.yts.AppInterface.Companion.COUNTRY_FLAG_JSON_URL
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Callable
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("CheckResult")
@Singleton
class FlagUtils @Inject constructor(
    private val retrofitUtils: RetrofitUtils
) {
    private val TAG = javaClass.simpleName

    fun getFlagUrl(country: String): String? {
        return try {
            val obj =JSONObject(DATA_JSON)
            if (obj.has(country)) {
                obj.getString(country)
            } else if (country.contains(" ")) {
                obj.keys().asSequence().find { it.startsWith(country) }
            } else null
        }catch (e: JSONException) {
            Log.e(TAG,e.message,e)
            null
        }
    }

    fun getMatchingFlagUrl(country: String): String? {
        return try {
            val obj = JSONObject(DATA_JSON)
            val key = obj.keys().asSequence().find { it.startsWith(country) }
            if (key != null) {
                obj.getString(key)
            } else null
        } catch (e: JSONException) {
            Log.e(TAG,e.message,e)
            null
        }
    }

    private var DATA_JSON: String = """
            {
                "Albanian": "https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/Flag_of_Albania.svg/320px-Flag_of_Albania.svg.png",
                "American": "https://upload.wikimedia.org/wikipedia/en/thumb/a/a4/Flag_of_the_United_States.svg/330px-Flag_of_the_United_States.svg.png",
                "Arabic": "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Flag_of_Saudi_Arabia.svg/640px-Flag_of_Saudi_Arabia.svg.png",
                "Bengali": "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/Flag_of_Bangladesh.svg/640px-Flag_of_Bangladesh.svg.png",
                "Brazilian portuguese": "https://upload.wikimedia.org/wikipedia/en/thumb/0/05/Flag_of_Brazil.svg/640px-Flag_of_Brazil.svg.png",
                "Brazillian portuguese": "https://upload.wikimedia.org/wikipedia/en/thumb/0/05/Flag_of_Brazil.svg/640px-Flag_of_Brazil.svg.png",
                "Bulgarian": "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9a/Flag_of_Bulgaria.svg/640px-Flag_of_Bulgaria.svg.png",
                "Chinese": "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/Flag_of_the_People%27s_Republic_of_China.svg/640px-Flag_of_the_People%27s_Republic_of_China.svg.png",
                "Croatian": "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Flag_of_Croatia.svg/640px-Flag_of_Croatia.svg.png",
                "Czech": "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cb/Flag_of_the_Czech_Republic.svg/640px-Flag_of_the_Czech_Republic.svg.png",
                "Danish": "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9c/Flag_of_Denmark.svg/634px-Flag_of_Denmark.svg.png",
                "Dutch": "https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/Flag_of_the_Netherlands.svg/640px-Flag_of_the_Netherlands.svg.png",
                "English": "https://upload.wikimedia.org/wikipedia/en/thumb/a/ae/Flag_of_the_United_Kingdom.svg/640px-Flag_of_the_United_Kingdom.svg.png",
                "Farsi/persian": "https://www.worldatlas.com/r/w728-h425-c728x425/upload/9c/16/6f/ir-flag.jpg",
                "Finnish": "https://upload.wikimedia.org/wikipedia/commons/thumb/b/bc/Flag_of_Finland.svg/640px-Flag_of_Finland.svg.png",
                "French": "https://upload.wikimedia.org/wikipedia/en/thumb/c/c3/Flag_of_France.svg/640px-Flag_of_France.svg.png",
                "German": "https://upload.wikimedia.org/wikipedia/en/thumb/b/ba/Flag_of_Germany.svg/640px-Flag_of_Germany.svg.png",
                "Greek": "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f1/Merchant_Navy_of_Greece_flag_%281822-1828%29.svg/640px-Merchant_Navy_of_Greece_flag_%281822-1828%29.svg.png",
                "Hebrew": "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Flag_of_Israel.svg/640px-Flag_of_Israel.svg.png",
                "Hindi": "https://upload.wikimedia.org/wikipedia/en/thumb/4/41/Flag_of_India.svg/120px-Flag_of_India.svg.png",
                "Hungarian": "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c1/Flag_of_Hungary.svg/640px-Flag_of_Hungary.svg.png",
                "Indonesian": "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9f/Flag_of_Indonesia.svg/255px-Flag_of_Indonesia.svg.png",
                "Italian": "https://upload.wikimedia.org/wikipedia/en/thumb/0/03/Flag_of_Italy.svg/640px-Flag_of_Italy.svg.png",
                "Japanese": "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Flag_of_Japan_%281870%E2%80%931999%29.svg/220px-Flag_of_Japan_%281870%E2%80%931999%29.svg.png",
                "Korean": "https://upload.wikimedia.org/wikipedia/commons/thumb/0/09/Flag_of_South_Korea.svg/640px-Flag_of_South_Korea.svg.png",
                "Lithuanian": "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Flag_of_Lithuania.svg/640px-Flag_of_Lithuania.svg.png",
                "Macedonian": "https://render.fineartamerica.com/images/rendered/medium/print/images/artworkimages/medium/1/flag-of-macedonia-unknown.jpg",
                "Malay": "https://upload.wikimedia.org/wikipedia/commons/thumb/6/66/Flag_of_Malaysia.svg/320px-Flag_of_Malaysia.svg.png",
                "Nepali": "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9b/Flag_of_Nepal.svg/120px-Flag_of_Nepal.svg.png",
                "Norwegian": "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Flag_of_Norway.svg/640px-Flag_of_Norway.svg.png",
                "Polish": "https://upload.wikimedia.org/wikipedia/en/thumb/1/12/Flag_of_Poland.svg/640px-Flag_of_Poland.svg.png",
                "Portuguese": "https://st.depositphotos.com/1482106/3814/i/450/depositphotos_38141049-stock-photo-portugal-flag.jpg",
                "Romanian": "https://upload.wikimedia.org/wikipedia/commons/thumb/7/73/Flag_of_Romania.svg/255px-Flag_of_Romania.svg.png",
                "Russian": "https://upload.wikimedia.org/wikipedia/en/thumb/f/f3/Flag_of_Russia.svg/640px-Flag_of_Russia.svg.png",
                "Serbian": "https://upload.wikimedia.org/wikipedia/commons/thumb/f/ff/Flag_of_Serbia.svg/640px-Flag_of_Serbia.svg.png",
                "Slovenian": "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f0/Flag_of_Slovenia.svg/320px-Flag_of_Slovenia.svg.png",
                "Spanish": "https://upload.wikimedia.org/wikipedia/en/thumb/9/9a/Flag_of_Spain.svg/640px-Flag_of_Spain.svg.png",
                "Swedish": "https://upload.wikimedia.org/wikipedia/en/thumb/4/4c/Flag_of_Sweden.svg/640px-Flag_of_Sweden.svg.png",
                "Thai": "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Flag_of_Thailand.svg/640px-Flag_of_Thailand.svg.png",
                "Taiwanese": "https://upload.wikimedia.org/wikipedia/commons/thumb/7/72/Flag_of_the_Republic_of_China.svg/188px-Flag_of_the_Republic_of_China.svg.png",
                "Turkish": "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/Flag_of_Turkey.svg/640px-Flag_of_Turkey.svg.png",
                "Urdu": "https://upload.wikimedia.org/wikipedia/en/thumb/4/41/Flag_of_India.svg/640px-Flag_of_India.svg.png",
                "Vietnamese": "https://upload.wikimedia.org/wikipedia/commons/thumb/2/21/Flag_of_Vietnam.svg/320px-Flag_of_Vietnam.svg.png",
                "United States": "https://upload.wikimedia.org/wikipedia/en/thumb/a/a4/Flag_of_the_United_States.svg/330px-Flag_of_the_United_States.svg.png",
                "Ukrainian": "https://upload.wikimedia.org/wikipedia/commons/thumb/4/49/Flag_of_Ukraine.svg/640px-Flag_of_Ukraine.svg.png",
                "Canadian": "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Flag_of_Canada_%28Pantone%29.svg/188px-Flag_of_Canada_%28Pantone%29.svg.png",
                "Thailand": "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Flag_of_Thailand.svg/188px-Flag_of_Thailand.svg.png"
            }
        """.trimIndent()

    @Deprecated("Better to create a DATA_JSON instead of calling callback"
        , level = DeprecationLevel.ERROR)
    fun setUp(listener: FlagCallbacks) {
        if (DATA_JSON.isBlank()) {
            Observable.fromCallable(Callable<String> {
                return@Callable retrofitUtils.getHttpClient().newCall(
                    Request.Builder().url(
                        COUNTRY_FLAG_JSON_URL
                    ).build()
                ).execute().body?.string()
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    parseData(it,listener)
                })
        }else parseData(DATA_JSON,listener)
    }

    private fun parseData(it: String?, listener: FlagCallbacks) {
        if (it==null) {
            DATA_JSON = "{ }"
            listener.onError()
        }
        else {
            DATA_JSON = it
            listener.onSuccess(it)
        }
    }

    interface FlagCallbacks {
        fun onSuccess(text: String)
        fun onError()
    }
}
