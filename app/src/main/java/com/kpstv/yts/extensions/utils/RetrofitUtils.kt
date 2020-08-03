package com.kpstv.yts.extensions.utils

import android.util.Log
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.security.cert.CertificateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.*

/**
 * A class made for my project "Moviesy" https://github.com/KaustubhPatange/Moviesy
 *
 * Now serves as a general purpose for managing retrofit builder and
 * http logging utils.
 */
@Singleton
class RetrofitUtils @Inject constructor(
    private val interceptor: com.kpstv.yts.extensions.interceptors.NetworkConnectionInterceptor
) {
    private var retrofitBuilder: Retrofit.Builder? = null
    private var httpBuilder: OkHttpClient.Builder? = null
    private val TAG = javaClass.simpleName

    fun getRetrofitBuilder(): Retrofit.Builder {
        return retrofitBuilder ?: Retrofit.Builder().apply {
            addCallAdapterFactory(CoroutineCallAdapterFactory())
            addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().serializeNulls().create()
                )
            )
            client(getHttpClient())
        }.also { retrofitBuilder = it }
    }

    fun getHttpBuilder(): OkHttpClient.Builder {
        return httpBuilder
            ?: OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .also { httpBuilder = it }
    }

    /**
     * @param addLoggingInterceptor If true logcat will display all the Http request messages
     */
    fun getHttpClient(addLoggingInterceptor: Boolean = false): OkHttpClient {
        val client = getHttpBuilder()
        if (addLoggingInterceptor) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level =
                HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(loggingInterceptor)
        }
        return client.build()
    }

    /**
     * https://gist.github.com/maiconhellmann/c61a533eca6d41880fd2b3f8459c07f7
     */
   /* private fun unsafeHttpBuilder(): OkHttpClient.Builder {
       try {
           val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
               @Throws(CertificateException::class)
               override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) { }

               @Throws(CertificateException::class)
               override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) { }

               override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                   return arrayOf()
               }
           })
           val sslContext = SSLContext.getInstance("SSL")
           sslContext.init(null, trustAllCerts, java.security.SecureRandom())
           val sslSocketFactory = sslContext.socketFactory
           return getHttpBuilder()
               .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
               .hostnameVerifier(HostnameVerifier { _, _ -> true })
               .also { httpBuilder = it }
       }catch (e: Exception) {
           Log.w(TAG, "unsafeHttpBuilder: ${e.message}", e)
       }
        return getHttpBuilder()
    }*/
}