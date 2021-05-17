package com.sahib.avocado.ui.activities

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.Player.EventListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.sahib.avocado.Constants
import com.sahib.avocado.R
import com.sahib.avocado.app.MyApplication
import com.sahib.avocado.model.VideoContent
import com.sahib.avocado.utils.swipper.SwipperGestureDetection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File

class VideoPlayerActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var swipeGestureDetection: SwipperGestureDetection
    private var playWhenReady = true
    private var playbackPosition: Long = 0
    private var currentBrightness: Float = 0.1F
    private lateinit var playerView: PlayerView
    private lateinit var customController: LinearLayout
    private lateinit var exo_video_fit: ImageButton
    private lateinit var exo_video_fill: ImageButton
    private var simpleExoPlayer: SimpleExoPlayer ?= null
    private var position: Int ?= null
    private var list: ArrayList<VideoContent> ?= null
    private var currentVideo: VideoContent ?= null
    private var playbackProgressObservable: Observable<Long>? =null
    private var playbackDisposable: Disposable ?= null
    private var currentScale: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        initView()
        initExoPlayer()
        handleVideoAspectRatio()
    }

    private fun initView() {
        playerView = findViewById(R.id.videoView)
        customController = findViewById(R.id.layout_custom_controller)
        exo_video_fit = findViewById(R.id.exo_video_fit)
        exo_video_fill = findViewById(R.id.exo_video_fill)
        currentBrightness =  MyApplication.prefHelper.customPrefs(Constants.SharedPrefNames.general.name).getFloat(Constants.SharedPrefItemNames.brightness.name, this.window.attributes.screenBrightness)

        list = intent.getParcelableArrayListExtra(Constants.IntentItems.videoList.name)
        position = intent.getIntExtra(Constants.IntentItems.position.name, 0)
        currentVideo = list?.get(position!!)

        playbackPosition =  MyApplication.prefHelper.customPrefs(Constants.SharedPrefNames.videoStatus.name).getLong(currentVideo?.assetFileStringUri, 0)

        setDefaultBrightness()
        observerProgress()
    }

    private fun observerProgress() {
        //RxKotlin to observe progress update
        playbackDisposable = playbackProgressObservable?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe {
            if (simpleExoPlayer!!.currentWindowIndex == position) {
                playbackPosition = it
            }
        }
    }

    private fun initExoPlayer() {
        if (simpleExoPlayer == null) {
            simpleExoPlayer = SimpleExoPlayer.Builder(this).build()
            playerView.player = simpleExoPlayer

            val mediaSourcesList : ArrayList<MediaSource> = ArrayList()
            for (videoContent:VideoContent in list!!) {
                val mediaSource = ExtractorMediaSource(Uri.parse(videoContent.assetFileStringUri), DefaultDataSourceFactory(this), DefaultExtractorsFactory(), null, null, null)
                val subtitleUri = videoContent.path!!.substring(0, videoContent.path!!.lastIndexOf(".")) + ".srt"
                if (File(subtitleUri).exists()) {
                    val mergedSource: MergingMediaSource
                    val subtitleFormat: Format = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, Format.NO_VALUE, "en")
                    val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, this.packageName), DefaultBandwidthMeter())
                    val subtitleSource = SingleSampleMediaSource.Factory(dataSourceFactory) .createMediaSource(Uri.parse(subtitleUri), subtitleFormat, C.TIME_UNSET)
                    mergedSource = MergingMediaSource(mediaSource, subtitleSource)
                    mediaSourcesList.add(mergedSource)
                } else {
                    mediaSourcesList.add(mediaSource)
                }
            }
            simpleExoPlayer?.setMediaSources(mediaSourcesList)
            simpleExoPlayer?.playWhenReady = playWhenReady
            simpleExoPlayer?.seekTo(position!!, playbackPosition)
            simpleExoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            simpleExoPlayer?.prepare()

            swipeGestureDetection = SwipperGestureDetection(this, currentBrightness, currentVideo!!.videoDuration, simpleExoPlayer, playerView)
            playerView.setOnTouchListener(swipeGestureDetection)

            //To change current seek position in case the track is changed
            simpleExoPlayer!!.addListener(object: EventListener {
                override fun onPositionDiscontinuity(reason: Int) {
                    val latestWindowIndex: Int = simpleExoPlayer!!.currentWindowIndex
                    if (latestWindowIndex != position) {
                        //Save the old tracks playback position
                        MyApplication.prefHelper.customPrefs(Constants.SharedPrefNames.videoStatus.name).edit {
                            putLong(currentVideo?.assetFileStringUri, playbackPosition).commit()
                        }

                        //Load the new tracks playback position
                        position = latestWindowIndex
                        currentVideo = list?.get(position!!)
                        swipeGestureDetection.setNewVideoDuration(currentVideo!!.videoDuration)
                        playbackPosition =  MyApplication.prefHelper.customPrefs(Constants.SharedPrefNames.videoStatus.name).getLong(currentVideo?.assetFileStringUri, 0)
                        simpleExoPlayer!!.seekTo(playbackPosition)
                    }
                }
            })
        }
    }

    private fun handleVideoAspectRatio() {
        currentScale = MyApplication.prefHelper.customPrefs(Constants.SharedPrefNames.general.name).getInt(Constants.SharedPrefItemNames.scale.name, AspectRatioFrameLayout.RESIZE_MODE_FIT)
        playerView.resizeMode = currentScale

        if (currentScale == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
            exo_video_fit.visibility = View.GONE
            exo_video_fill.visibility = View.VISIBLE
        } else {
            exo_video_fill.visibility = View.GONE
            exo_video_fit.visibility = View.VISIBLE
        }

        exo_video_fit.setOnClickListener(this)
        exo_video_fill.setOnClickListener(this)
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
            simpleExoPlayer!!.release()

            MyApplication.prefHelper.customPrefs(Constants.SharedPrefNames.videoStatus.name).edit {
                putLong(currentVideo?.assetFileStringUri, playbackPosition).commit()
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

    override fun onClick(v: View?) {
        when {
            v!!.id == R.id.exo_video_fit -> {
                currentScale = AspectRatioFrameLayout.RESIZE_MODE_FIT
                exo_video_fit.visibility = View.GONE
                exo_video_fill.visibility = View.VISIBLE
            }
            v.id == R.id.exo_video_fill -> {
                currentScale = AspectRatioFrameLayout.RESIZE_MODE_FILL
                exo_video_fill.visibility = View.GONE
                exo_video_fit.visibility = View.VISIBLE
            }
        }
        playerView.resizeMode = currentScale
        MyApplication.prefHelper.customPrefs(Constants.SharedPrefNames.general.name).edit {
            putInt(Constants.SharedPrefItemNames.scale.name, currentScale).commit()
        }
    }

}