package com.sahib.avocado.ui.activities

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import com.sahib.avocado.Constants
import com.sahib.avocado.R
import com.sahib.avocado.app.MyApplication
import hu.accedo.commons.widgets.exowrapper.ExoPlayerView
import hu.accedo.commons.widgets.exowrapper.mediasource.model.ExoMedia


class VideoPlayerVDKActivity : AppCompatActivity() {

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private lateinit var playerView: ExoPlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player_vdk)

        initView();
        initExoPlayer()
    }

    private fun initView() {
        playerView = findViewById<ExoPlayerView>(R.id.exoPlayerView)

        playbackPosition =  MyApplication.prefHelper.customPrefs(Constants.SharedPrefNames.videoStatus.name).getLong(Constants.SharedPrefItemNames.position.name, 0)
    }

    private fun initExoPlayer() {
        playerView.setupDrm()
            .setWidevine()
            .apply();

        playerView.createPlaylist()
            .add(ExoMedia.Builder(intent.getStringExtra(Constants.IntentItems.videoUri.name)!!).build())
            .apply()
    }

}