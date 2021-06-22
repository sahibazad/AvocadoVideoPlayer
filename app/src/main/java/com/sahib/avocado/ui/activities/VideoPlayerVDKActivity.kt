package com.sahib.avocado.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

        initView()
        initExoPlayer()
    }

    private fun initView() {
        playerView = findViewById<ExoPlayerView>(R.id.exoPlayerView)

        playbackPosition = MyApplication.prefHelper.customPrefs(Constants.SharedPrefNames.videoStatus.name).getLong(Constants.SharedPrefItemNames.position.name, 0)
    }

    private fun initExoPlayer() {
        playerView.setupDrm()
            .setWidevine()
            .apply()

        playerView.createPlaylist()
            .add(ExoMedia.Builder(intent.getStringExtra(Constants.IntentItems.videoUri.name)!!).build())
            .apply()
    }
}
