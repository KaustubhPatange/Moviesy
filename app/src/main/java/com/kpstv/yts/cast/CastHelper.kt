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
import com.kpstv.yts.extensions.Coroutines
import es.dmoral.toasty.Toasty
import java.io.File

/**
 * A helper created to manage cast in the app.
 *
 * Steps: To setup this class
 * 1. You need to add [tinyhttpd] module to your app.
 * 2. Copy this cast folder to your device.
 * 3. Update the manifest with the required permissions, metadata tag
 *    for [CastOptionsProvider] & service required by cast.
 * 4. Call [initCastSession] method in the parent activity if you are hosting a fragment
 *    inside it. Otherwise call [init] method
 * 5. (Optional) Call [init] method in the fragment onViewCreated() where you want to cast.
 * 6. Call [loadMedia] to play a local media file
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

    fun isCastActive() =
        mCastSession?.isConnected != null

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
    fun init(activity: AppCompatActivity, toolbar: Toolbar) {
        mToolbar = toolbar
        mActivity = activity
        mApplicationContext = mActivity.applicationContext

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
        }

        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(
            mApplicationContext,
            mToolbar.menu,
            R.id.media_route_menu_item
        )
    }

    fun loadMedia(
        mediaFile: File,
        videoLength: Long = 0,
        lastSavedPosition: Long = 0,
        bannerImage: File? = null,
        srtFile: File? = null,
        onComplete: (Exception?) -> Unit
    ) {

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
                .setStreamDuration(videoLength)
                .setMediaTracks(mediaTracks)
                .build()

            /** Start a local HTTP server */
            mApplicationContext.startService(Intent(mApplicationContext, WebService::class.java))

            /** Play the file on the device. */
            if (mCastSession?.remoteMediaClient == null) {
                onComplete.invoke(Exception("Client is null"))
                return@buildSubtitle
            }
            val remoteMediaClient = mCastSession?.remoteMediaClient ?: return@buildSubtitle
            remoteMediaClient.registerCallback(object : RemoteMediaClient.Callback() {
                override fun onStatusUpdated() {
                    /** OnLoaded */
                    onComplete.invoke(null)

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
                    .setCurrentTime(lastSavedPosition)
                    .build()
            )
        }
    }

    fun stopCast() {
        mCastSession?.remoteMediaClient?.stop()
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