package com.kpstv.yts.cast

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.framework.*
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import com.kpstv.yts.AppInterface.Companion.APP_IMAGE_URL
import com.kpstv.yts.R
import com.kpstv.yts.cast.service.WebService
import com.kpstv.yts.cast.ui.ExpandedControlsActivity
import com.kpstv.yts.cast.utils.SubtitleConverter
import com.kpstv.yts.cast.utils.Utils
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.extensions.Coroutines
import com.kpstv.yts.extensions.SessionCallback
import com.kpstv.yts.extensions.toFile
import es.dmoral.toasty.Toasty
import io.github.dkbai.tinyhttpd.nanohttpd.webserver.SimpleWebServer
import java.io.File

/**
 * A helper created to manage cast in the app.
 *
 * Instead of putting all the codes in activity/fragment, better to
 * separate the logic to a different class.
 */
class CastHelper {
    companion object {
        const val PORT = "8081"
        var deviceIpAddress: String? = null
    }

    private lateinit var mCastContext: CastContext
    private lateinit var mediaRouteMenuItem: MenuItem
    private var mCastSession: CastSession? = null
    private lateinit var mSessionManagerListener: SessionManagerListener<CastSession>

    private var mIntroductoryOverlay: IntroductoryOverlay? = null

    private lateinit var mActivity: AppCompatActivity
    private lateinit var mApplicationContext: Context
    private lateinit var mToolbar: Toolbar

    private var model: Model.response_download? = null

    private var onSessionDisconnected: SessionCallback? = null

    fun isCastActive() =
        mCastSession?.castDevice != null

    /**
     * You need to call this method in the parent activity if you are setting up
     * fragment. Otherwise you can call [init] method.
     */
    fun initCastSession(activity: AppCompatActivity) {
        mActivity = activity
        mCastContext = CastContext.getSharedInstance(mActivity)
    }

    /**
     * Set the activity along with the toolbar must be called in [onPostCreate]
     * of activity.
     */
    fun init(
        activity: AppCompatActivity,
        toolbar: Toolbar,
        /** Use this to save last play position, Integer value returns the last
         *  played position. */
        onSessionDisconnected: SessionCallback
    ) {
        mToolbar = toolbar
        mActivity = activity
        mApplicationContext = mActivity.applicationContext
        this.onSessionDisconnected = onSessionDisconnected

        deviceIpAddress = Utils.findIPAddress(mApplicationContext)
        if (deviceIpAddress == null) {
            Toasty.error(
                mApplicationContext,
                mApplicationContext.getString(R.string.error_cast_wifi)
            ).show()
            return
        }

        setUpCastListener()

        initCastSession(activity)
        mCastContext.sessionManager.addSessionManagerListener(
            mSessionManagerListener, CastSession::class.java
        )

        mCastContext.addCastStateListener { state ->
            if (state != CastState.NO_DEVICES_AVAILABLE)
                showIntroductoryOverlay()
            if (state == CastState.NOT_CONNECTED) {
                /** When casting is disconnected we post updateLastModel */
                postUpdateLastModel()
                SimpleWebServer.stopServer()
            }
        }

        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(
            mApplicationContext,
            mToolbar.menu,
            R.id.media_route_menu_item
        )
    }

    fun loadMedia(
        downloadModel: Model.response_download,
        playFromLastPosition: Boolean,
        srtFile: File?,
        onLoadComplete: (Exception?) -> Unit
    ) {
        /**
         * Suppose if a media is already casting and user decided to cast another media,
         * in such case we still've last [Model.response_download] model which needs to
         * be updated.
         *
         * Here we post such a similar update.
         */
        postUpdateLastModel()

        this.model = downloadModel

        val mediaFile = model?.videoPath.toFile()!!
        val bannerImage = model?.imagePath.toFile()

        /** Get the remote names of the file */
        val remoteFileName = Utils.getRemoteFileName(deviceIpAddress, mediaFile)
            ?.replace(" ", "%20")
        val remoteImageFileName =
            if (bannerImage != null)
                Utils.getRemoteFileName(deviceIpAddress, bannerImage)?.replace(" ", "%20")
            else APP_IMAGE_URL

        /** Generate media metadata */
        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        movieMetadata.putString(MediaMetadata.KEY_TITLE, mediaFile.nameWithoutExtension)
        movieMetadata.addImage(WebImage(Uri.parse(remoteImageFileName)))
        movieMetadata.addImage(WebImage(Uri.parse(remoteImageFileName)))

        buildSubtitle(srtFile) { mediaTracks ->
            val mediaInfo = MediaInfo.Builder(remoteFileName)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/mp4")
                .setMetadata(movieMetadata)
                .setStreamDuration(downloadModel.total_video_length)
                .setMediaTracks(mediaTracks)
                .build()

            /** Start a local HTTP server */
            mApplicationContext.startService(Intent(mApplicationContext, WebService::class.java))

            /** Play the file on the device. */
            if (mCastSession?.remoteMediaClient == null) {
                onLoadComplete.invoke(Exception("Client is null"))
                return@buildSubtitle
            }
            val remoteMediaClient = mCastSession?.remoteMediaClient ?: return@buildSubtitle

            remoteMediaClient.registerCallback(object : RemoteMediaClient.Callback() {
                override fun onStatusUpdated() {
                    /** OnLoaded */
                    onLoadComplete.invoke(null)

                    /** When media loaded we will start the fullscreen player activity. */
                    val intent =
                        Intent(mApplicationContext, ExpandedControlsActivity::class.java)
                    mActivity.startActivity(intent)
                    remoteMediaClient.unregisterCallback(this)
                }
            })
            remoteMediaClient.load(
                MediaLoadRequestData.Builder()
                    .setMediaInfo(mediaInfo)
                    .setAutoplay(true)
                    .setCurrentTime(
                        if (playFromLastPosition) model?.lastSavedPosition?.toLong() ?: 0L else 0L
                    )
                    .build()
            )
        }
    }

    fun stopCast() {
        mCastSession?.remoteMediaClient?.stop()
        SimpleWebServer.stopServer()
    }

    /**
     * This will post a callback to the subscriber when session is disconnected along
     * with the [Model.response_download] and last saved position [Long].
     */
    private fun postUpdateLastModel() {
        mCastSession?.remoteMediaClient?.approximateStreamPosition?.let {
            onSessionDisconnected?.invoke(
                model,
                it.toInt()
            )
            this@CastHelper.model = null
        }
    }

    private fun buildSubtitle(srtFile: File?, onComplete: (List<MediaTrack>) -> Unit) {
        if (srtFile == null) return onComplete.invoke(listOf())
        Coroutines.io {
            val vttFile =
                File(mApplicationContext.externalCacheDir, srtFile.nameWithoutExtension + ".vtt")
            val remoteSrtFileName = Utils.getRemoteFileName(deviceIpAddress, vttFile)
            /** Convert srt to vtt on background thread */
            SubtitleConverter.convertFromSrtToVtt(srtFile, vttFile)
            /** Post subtitles on main thread */
            Coroutines.main {
                onComplete.invoke(
                    listOf(
                        MediaTrack.Builder(1, MediaTrack.TYPE_TEXT)
                            .setName(srtFile.name)
                            .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                            .setContentId(remoteSrtFileName)
                            .setLanguage("en-US")
                            .build()
                    )
                )
            }
        }


    }

    private fun setUpCastListener() {
        mSessionManagerListener = object : SessionManagerListener<CastSession> {
            override fun onSessionStarted(session: CastSession?, p1: String?) {
                onApplicationConnected(session)
            }

            override fun onSessionResumeFailed(p0: CastSession?, p1: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionEnded(p0: CastSession?, p1: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionResumed(session: CastSession?, p1: Boolean) {
                onApplicationConnected(session)
            }

            override fun onSessionStartFailed(p0: CastSession?, p1: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionSuspended(p0: CastSession?, p1: Int) {}

            override fun onSessionStarting(castSession: CastSession?) {
                mCastSession = castSession
            }

            override fun onSessionResuming(castSession: CastSession?, p1: String?) {
                mCastSession = castSession
            }

            override fun onSessionEnding(p0: CastSession?) {}

            private fun onApplicationConnected(castSession: CastSession?) {
                mCastSession = castSession
                mActivity.invalidateOptionsMenu()
                //    mToolbar.invalidate()
            }

            private fun onApplicationDisconnected() {
                mActivity.invalidateOptionsMenu()
                //  mToolbar.invalidate()
            }
        }
    }

    private fun showIntroductoryOverlay() {
        mIntroductoryOverlay?.remove()

        if (::mediaRouteMenuItem.isInitialized && mediaRouteMenuItem.isVisible) {
            Handler().post {
                mIntroductoryOverlay = IntroductoryOverlay.Builder(
                    mActivity, mediaRouteMenuItem
                )
                    .setTitleText(mApplicationContext.getString(R.string.cast_media_to_device))
                    .setSingleTime()
                    .setOnOverlayDismissedListener { mIntroductoryOverlay = null }
                    .build()
                mIntroductoryOverlay?.show()
            }
        }
    }
}