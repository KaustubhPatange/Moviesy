package com.kpstv.yts.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kpstv.yts.R
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import kotlinx.android.synthetic.main.activity_final_previews.*

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: YouTubePlayer

    override fun onDestroy() {
        youtube_player_view.release()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        youtube_player_view.enterFullScreen()
        youtube_player_view.initialize(object: YouTubePlayerListener{

            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(intent.extras?.getString("videoId")!!,0f)
            }

            override fun onApiChange(youTubePlayer: YouTubePlayer) {

            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
            }

            override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer,playbackQuality: PlayerConstants.PlaybackQuality) {
            }

            override fun onPlaybackRateChange(
                youTubePlayer: YouTubePlayer,
                playbackRate: PlayerConstants.PlaybackRate) {}
            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
            }

            override fun onVideoLoadedFraction(
                youTubePlayer: YouTubePlayer,
                loadedFraction: Float
            ) {
            }
        })
    }
}
