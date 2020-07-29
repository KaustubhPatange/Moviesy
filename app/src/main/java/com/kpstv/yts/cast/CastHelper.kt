package com.kpstv.yts.cast

import android.content.Context
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.cast.framework.*
import com.kpstv.yts.R
import com.kpstv.yts.cast.utils.Utils
import es.dmoral.toasty.Toasty

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

    fun loadMedia() {}

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

            override fun onSessionStarting(p0: CastSession?) {}

            override fun onSessionResuming(p0: CastSession?, p1: String?) {}

            override fun onSessionEnding(p0: CastSession?) {}

            private fun onApplicationConnected(castSession: CastSession?) {
                mCastSession = castSession
                mActivity.invalidateOptionsMenu()
                mToolbar.invalidate()
            }

            private fun onApplicationDisconnected() {
                mActivity.invalidateOptionsMenu()
                mToolbar.invalidate()
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