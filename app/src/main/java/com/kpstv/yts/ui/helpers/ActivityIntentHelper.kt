package com.kpstv.yts.ui.helpers

import android.content.Intent
import android.net.Uri
import com.kpstv.yts.ui.activities.StartActivity
import com.kpstv.yts.ui.fragments.DetailFragment
import com.kpstv.yts.ui.fragments.MainFragment
import com.kpstv.yts.ui.viewmodels.StartViewModel

class ActivityIntentHelper(private val navViewModel: StartViewModel) {
    /**
     * Handle intent actions & deep-links for the parent activity.
     *
     * @return True if the intent is handled.
     */
    fun handle(intent: Intent?): Boolean {
        if (intent == null) return false
        val data: Uri? = intent.data
        if (intent.action == Intent.ACTION_VIEW && data != null) {
            when {
                data.pathSegments.contains("movies") -> {
                    // Pass the data to Final Activity then
                    navViewModel.navigateTo(
                        screen = StartActivity.Screen.MAIN,
                        args = MainFragment.Args(
                            moveToDetail = DetailFragment.Args(movieUrl = data.toString())
                        )
                    )
                    return true
                }
            }
        } else if (intent.action == ACTION_FORCE_CHECK_UPDATE) {
            navViewModel.navigateTo(
                screen = StartActivity.Screen.MAIN,
                args = MainFragment.Args(
                    forceUpdateCheck = true
                )
            )
            return true
        } else if (intent.action == ACTION_LAUNCH_MOVIE) {
            val movieId = intent.getIntExtra(PAYLOAD, -1)
            if (movieId != -1) {
                navViewModel.navigateTo(
                    screen = StartActivity.Screen.MAIN,
                    args = MainFragment.Args(
                        moveToDetail = DetailFragment.Args(ytsId = movieId)
                    )
                )
                return true
            }
        } else if (intent.action == ACTION_MOVE_TO_LIBRARY) {
            navViewModel.navigateTo(
                screen = StartActivity.Screen.MAIN,
                args = MainFragment.Args(
                    moveToLibrary = true
                )
            )
        }

        // Return false will start normal execution process.
        return false
    }

    companion object {
        const val ACTION_MOVE_TO_LIBRARY = "action_move_to_library"
        const val ACTION_FORCE_CHECK_UPDATE = "action_force_check_update"
        const val ACTION_LAUNCH_MOVIE = "action_launch_movie"

        const val PAYLOAD = "intent_payload"
    }
}