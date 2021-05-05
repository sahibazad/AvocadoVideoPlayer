package com.sahib.avocado.ui.activities

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import com.sahib.avocado.Constants
import com.sahib.avocado.R
import com.sahib.avocado.app.MyApplication
import com.sahib.avocado.utils.swipper.SwipperGestureDetection


class VideoPlayerActivity : AppCompatActivity(){

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var videoDuration: Long = 0
    private var currentBrightness: Float = 0.1F
    private lateinit var playerView: PlayerView
    private var simpleExoPlayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        initView();
        initExoPlayer()
    }

    private fun initView() {
        playerView = findViewById(R.id.videoView)
        currentBrightness =  MyApplication.prefHelper.customPrefs(Constants.SharedPrefNames.videoStatus.name).getFloat(Constants.SharedPrefItemNames.brightness.name, this.window.attributes.screenBrightness)
        playbackPosition =  MyApplication.prefHelper.customPrefs(Constants.SharedPrefNames.videoStatus.name).getLong(Constants.SharedPrefItemNames.position.name, 0)
        videoDuration = intent.getLongExtra(Constants.IntentItems.videoDuration.name, 1000)
        setDefaultBrightness()
    }

    private fun initExoPlayer() {
        if (simpleExoPlayer == null) {
            simpleExoPlayer = SimpleExoPlayer.Builder(this).build()
            playerView.player = simpleExoPlayer

            val mediaItem: MediaItem =
                MediaItem.fromUri(Uri.parse(intent.getStringExtra(Constants.IntentItems.videoUri.name)))
            simpleExoPlayer?.setMediaItem(mediaItem)
            simpleExoPlayer?.playWhenReady = playWhenReady
            simpleExoPlayer?.seekTo(currentWindow, playbackPosition)
            simpleExoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            simpleExoPlayer?.prepare()

            playerView.setOnTouchListener(SwipperGestureDetection(this, currentBrightness, videoDuration, simpleExoPlayer))
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initExoPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Util.SDK_INT < 24 || simpleExoPlayer == null) {
            initExoPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer!!.stop()
            playWhenReady = simpleExoPlayer!!.playWhenReady
            playbackPosition = simpleExoPlayer!!.currentPosition
            currentWindow = simpleExoPlayer!!.currentWindowIndex
            simpleExoPlayer!!.release()

            MyApplication.prefHelper.customPrefs(Constants.SharedPrefNames.videoStatus.name).edit {
                putLong(Constants.SharedPrefItemNames.position.name, playbackPosition).commit()
            }

            simpleExoPlayer = null
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun setDefaultBrightness() {
        val attr = this.window.attributes
        attr.screenBrightness = currentBrightness
        this.window.attributes = attr
    }

}