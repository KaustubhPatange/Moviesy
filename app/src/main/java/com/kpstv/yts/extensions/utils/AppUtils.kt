package com.kpstv.yts.extensions.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.kpstv.common_moviesy.extensions.colorFrom
import com.kpstv.common_moviesy.extensions.utils.CommonUtils
import com.kpstv.yts.AppInterface.Companion.handleRetrofitError
import com.kpstv.yts.R
import com.kpstv.yts.databinding.CustomPurchaseDialogBinding
import es.dmoral.toasty.Toasty
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.reflect.KClass
import kotlin.system.exitProcess

class AppUtils {

    companion object {

        fun getBulletSymbol(): Spanned {
            return CommonUtils.getHtmlText("&#8226;")
        }

        @SuppressLint("ResourceType")
        fun launchUrl(context: Context, url: String, dark: Boolean = true) = with(context) {
            val builder = CustomTabsIntent.Builder()
            var color: Int = R.color.colorPrimary_New
            if (!dark) color = R.color.colorPrimary
            builder.setToolbarColor(colorFrom(color))
            val customTabsIntent = builder.build()
            try {
                customTabsIntent.intent.setPackage("com.android.chrome")
                customTabsIntent.launchUrl(this, Uri.parse(url))
            } catch (e: Exception) {
                Log.e("Chrome", "Chrome not installed: " + e.message)
                launchUrlIntent(this, url)
            }
        }

        fun launchUrlIntent(context: Context, url: String?) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            context.startActivity(i)
        }

        fun shareApp(activity: Activity) = with(activity) {
            try {
                ShareCompat.IntentBuilder.from(this)
                    .setChooserTitle(getString(R.string.share_app))
                    .setType("text/plain")
                    .setText("${getString(R.string.share_text)}, ${getString(R.string.app_link)}")
                    .startChooser()
            } catch (e: Exception) {
                Toasty.warning(this, "Failed: ${e.message}").show()
                Log.w(TAG, "Failed: ${e.message}", e)
            }
        }

        fun CafebarToast(context: Activity, message: String) {
            handleRetrofitError(
                context,
                Exception(message)
            )
        }

        fun getImdbUrl(id: String): String {
            return "https://www.imdb.com/title/${id}"
        }

        fun getMagnetUrl(hash: String, url_encoded_text: String): String {
            return """magnet:?xt=urn:btih:${hash}&dn=${url_encoded_text}
                |&tr=http://125.227.35.196:6969/announce
                |&tr=http://125.227.35.196:6969/announce
                |&tr=http://210.244.71.25:6969/announce
                |&tr=http://210.244.71.26:6969/announce
                |&tr=http://213.159.215.198:6970/announce
                |&tr=http://37.19.5.139:6969/announce
                |&tr=http://37.19.5.155:6881/announce
                |&tr=http://46.4.109.148:6969/announce
                |&tr=http://87.248.186.252:8080/announce
                |&tr=http://asmlocator.ru:34000/1hfZS1k4jh/announce
                |&tr=http://bt.evrl.to/announce
                |&tr=http://bt.pornolab.net/ann?uk=G7n8vXUdTt
                |&tr=http://bt.rutracker.org/ann
                |&tr=http://mgtracker.org:6969/announce
                |&tr=http://pubt.net:2710/announce
                |&tr=http://tracker.baravik.org:6970/announce
                |&tr=http://tracker.dler.org:6969/announce
                |&tr=http://tracker.filetracker.pl:8089/announce
                |&tr=http://tracker.grepler.com:6969/announce
                |&tr=http://tracker.mg64.net:6881/announce
                |&tr=http://tracker.tiny-vps.com:6969/announce
                |&tr=http://tracker.torrentyorg.pl/announce
                |&tr=http://tracker1.wasabii.com.tw:6969/announce
                |&tr=http://tracker2.wasabii.com.tw:6969/announce
                |&tr=udp://168.235.67.63:6969
                |&tr=udp://182.176.139.129:6969
                |&tr=udp://37.19.5.155:2710
                |&tr=udp://46.148.18.250:2710
                |&tr=udp://46.4.109.148:6969
                |&tr=udp://[2001:67c:28f8:92::1111:1]:2710
                |&tr=udp://bt.xxx-tracker.com:2710
                |&tr=udp://ipv6.leechers-paradise.org:6969
                |&tr=udp://opentor.org:2710
                |&tr=udp://public.popcorn-tracker.org:6969
                |&tr=udp://tracker.blackunicorn.xyz:6969
                |&tr=udp://tracker.ccc.de:80
                |&tr=udp://tracker.coppersurfer.tk:6969
                |&tr=udp://tracker.filetracker.pl:8089
                |&tr=udp://tracker.grepler.com:6969
                |&tr=udp://tracker.leechers-paradise.org:6969
                |&tr=udp://tracker.openbittorrent.com:80
                |&tr=udp://tracker.opentrackr.org:1337
                |&tr=udp://tracker.publicbt.com:80
                |&tr=udp://tracker.tiny-vps.com:6969""".trimMargin()
        }

        fun saveImageFromUrl(src: String, file: File) {
            try {
                val url = URL(src)
                val connection: HttpURLConnection = url
                    .openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                file.outputStream().use { input.copyTo(it) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun getVideoDuration(context: Context, videoFile: File): Long? {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.fromFile(videoFile))
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            return time?.toLong()
        }

        fun join(list: ArrayList<String>, separator: String): String {
            var builder = StringBuilder()
            for (i in 0 until list.size) {
                builder.append(list[i]).append(separator)
            }
            builder.append(list[list.size - 1])
            return builder.toString()
        }

        // TODO: Remove this unused method
        fun downloadFile(url: String, outputFile: File) {
            try {
                val u = URL(url);
                val conn = u.openConnection();
                val contentLength = conn.contentLength;

                val stream = DataInputStream(u.openStream());

                val buffer = ByteArray(contentLength)
                stream.readFully(buffer);
                stream.close();

                val fos = DataOutputStream(FileOutputStream(outputFile));
                fos.write(buffer);
                fos.flush();
                fos.close();
            } catch (e: FileNotFoundException) {
                Log.e(TAG, "downloadFile: ${e.message}}")
                return
            } catch (e: IOException) {
                Log.e(TAG, "downloadFile: ${e.message}}")
                return
            }
        }

        fun checkIfServiceIsRunning(context: Context, serviceClass: KClass<*>) = with(context) {
            val manager: ActivityManager =
                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.qualifiedName == service.service.className) {
                    return@with true
                }
            }
            return@with false
        }

        fun installApp(context: Context, apk: File) {
            val install = Intent(Intent.ACTION_INSTALL_PACKAGE)
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                install.data =
                    FileProvider.getUriForFile(context, context.packageName + ".provider", apk)
            } else {
                apk.setReadable(true, false)
                install.data = Uri.fromFile(apk)
            }
            context.startActivity(install)
        }

        @SuppressLint("SetTextI18n")
        fun doOnVersionDeprecated(context: Context): Unit = with(context) {
            /** Use same customPurchaseDialog to display it. */

            val binding = CustomPurchaseDialogBinding.inflate(LayoutInflater.from(this))

            binding.title.text = getString(R.string.deprecated_title)
            binding.message.text = getString(R.string.deprecated_text)
            binding.btnDetails.text = getString(R.string.deprecated_update)

            binding.lottieView.setAnimation(R.raw.deprecated)

            binding.btnDetails.setOnClickListener {
                launchUrlIntent(this, getString(R.string.app_link))
            }
            binding.btnClose.setOnClickListener {
                if (context is Activity)
                    context.finish()
                else exitProcess(0)
            }

            AlertDialog.Builder(this)
                .setView(binding.root)
                .setCancelable(false)
                .show()
        }

        @Suppress("DEPRECATION")
        fun isLastActivity(activity: Activity): Boolean = with(activity) {
            val mngr =
                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            val taskList = mngr.getRunningTasks(10)

            return taskList[0].numActivities == 1 && taskList[0].topActivity!!.className == activity.javaClass.name
        }

        private val TAG = "Utils"
    }
}
