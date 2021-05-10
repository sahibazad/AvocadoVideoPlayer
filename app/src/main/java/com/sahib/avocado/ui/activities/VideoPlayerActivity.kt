package com.sahib.avocado.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.EventListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
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

class VideoPlayerActivity : AppCompatActivity(){

    private lateinit var swipeGestureDetection: SwipperGestureDetection
    private var playWhenReady = true
    private var playbackPosition: Long = 0
    private var currentBrightness: Float = 0.1F
    private lateinit var playerView: PlayerView
    private var simpleExoPlayer: SimpleExoPlayer ?= null
    private var position: Int ?= null
    private var list: ArrayList<VideoContent> ?= null
    private var currentVideo: VideoContent ?= null
    private var playbackProgressObservable: Observable<Long>? =null
    private var playbackDisposable: Disposable ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        initView();
        initExoPlayer()
    }

    private fun initView() {
        playerView = findViewById(R.id.videoView)
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

            val mediaItemList : ArrayList<MediaItem> = ArrayList()

            for (videoContent:VideoContent in list!!) {
                mediaItemList.add(MediaItem.fromUri(videoContent.assetFileStringUri!!))
            }

            simpleExoPlayer?.setMediaItems(mediaItemList)
            simpleExoPlayer?.playWhenReady = playWhenReady
            simpleExoPlayer?.seekTo(position!!, playbackPosition)
            simpleExoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            simpleExoPlayer?.prepare()

            playerView.controllerAutoShow = true
            playerView.controllerHideOnTouch = true
            swipeGestureDetection = SwipperGestureDetection(this, currentBrightness, currentVideo!!.videoDuration, simpleExoPlayer)
            playerView.setOnTouchListener(swipeGestureDetection)
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

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

}