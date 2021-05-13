package com.sahib.avocado.utils.swipper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.sahib.avocado.Constants;
import com.sahib.avocado.app.MyApplication;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class SwipperGestureDetection implements View.OnTouchListener{

    private int mActivePointerId = INVALID_POINTER_ID;
    private SwipperProgressBar progressBarBrightness;
    private SwipperProgressBar progressBarAudio;
    private SwipperSeekView seekView;

    private final Activity activity;
    private final SimpleExoPlayer simpleExoPlayer;
    private final PlayerView playerView;
    private float brightness;
    private long videoDuration;
    private AudioManager audio;
    private int maxVolume;
    private int currentVolume;
    private int numberOfTaps = 0;
    private long lastTapTimeMs = 0;
    private long touchDownMs = 0;
    private float seekDistance = 0;
    private int ACTION_IN_PROCESS = 0;
    private int SEEK_THRESHOLD = 0;
    private int HALF_WIDTH = 0;
    private int DISTANCE_THRESHOLD = 1;

    public SwipperGestureDetection(Activity activity, float brightness, long videoDuration, SimpleExoPlayer simpleExoPlayer, PlayerView playerView) {
        this.activity = activity;
        this.brightness = brightness;
        this.videoDuration = videoDuration;
        this.simpleExoPlayer = simpleExoPlayer;
        this.playerView = playerView;

        setBrightnessProgress();
        setAudioProgress();
        setSeekView();
    }

    private void setBrightnessProgress() {
        WindowManager.LayoutParams layout = activity.getWindow().getAttributes();
        layout.screenBrightness = brightness;
        activity.getWindow().setAttributes(layout);
        progressBarBrightness = new SwipperProgressBar(activity);
        progressBarBrightness.setProgress((int) ((brightness / 255) * 100));
        progressBarBrightness.setProgressText(((int) (brightness / 255)) + "%");
    }

    private void setAudioProgress() {
        audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        progressBarAudio = new SwipperProgressBar(activity);
        progressBarAudio.setMaxProgress(maxVolume);
        progressBarAudio.setProgress(currentVolume);
        progressBarAudio.setProgressText("" + currentVolume);
    }

    public void setNewVideoDuration(long videoDuration) {
        this.videoDuration = videoDuration;
        SEEK_THRESHOLD = (int) (videoDuration / 100);
    }

    private void setSeekView() {
        seekView = new SwipperSeekView(activity);
        SEEK_THRESHOLD = (int) (videoDuration / 100);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (HALF_WIDTH == 0) {
            v.post(() -> {
                int width = v.getWidth();
                HALF_WIDTH = width / 2;
            });
        }

        final int action = event.getActionMasked();

        float distanceCovered = 0;
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                seekDistance = 0;
                distanceCovered = 0;
                touchDownMs = System.currentTimeMillis();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX();
                final float y = event.getY();
                distanceCovered = getDistance(x, y, event);
                if (distanceCovered > DISTANCE_THRESHOLD) {
                    try {
                        switch (ACTION_IN_PROCESS) {
                            case 1:
                                changeBrightness(event.getHistoricalX(0, 0), event.getHistoricalY(0, 0), x, y, distanceCovered);
                                break;
                            case 2:
                                changeVolume(event.getHistoricalX(0, 0), event.getHistoricalY(0, 0), x, y, distanceCovered);
                                break;
                            case 3:
                                changeSeek(event.getHistoricalX(0, 0), event.getHistoricalY(0, 0), x, y, distanceCovered);
                                break;
                            default:
                                int newX = (int) x;
                                int newY = (int) y;
                                int oldX = (int) event.getHistoricalX(0, 0);
                                int oldY = (int) event.getHistoricalY(0, 0);
                                int deltaX = oldX - newX;
                                int deltaY = oldY - newY;

                                if (Math.abs(deltaY) > Math.abs(deltaX)) {
                                    changeBrightness(event.getHistoricalX(0, 0), event.getHistoricalY(0, 0), x, y, distanceCovered);
                                    changeVolume(event.getHistoricalX(0, 0), event.getHistoricalY(0, 0), x, y, distanceCovered);
                                } else {
                                    changeSeek(event.getHistoricalX(0, 0), event.getHistoricalY(0, 0), x, y, distanceCovered);
                                }
                                break;
                        }
                    } catch (Exception e) {
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (progressBarBrightness.isVisible())
                            progressBarBrightness.hide();
                        if (progressBarAudio.isVisible())
                            progressBarAudio.hide();
                        if (seekView.isVisible())
                            seekView.hide();
                    }
                }, 2000);

                ACTION_IN_PROCESS = 0;

                if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
                    numberOfTaps = 0;
                    lastTapTimeMs = 0;
                    break;
                }

                if (numberOfTaps > 0 && (System.currentTimeMillis() - lastTapTimeMs) < ViewConfiguration.getDoubleTapTimeout()) {
                    numberOfTaps += 1;
                } else {
                    numberOfTaps = 1;
                }
                lastTapTimeMs = System.currentTimeMillis();
                if (numberOfTaps == 2) {
                    //TODO tap listener if required
                }
                mActivePointerId = INVALID_POINTER_ID;
                toggleControllerVisibility();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    private void toggleControllerVisibility() {
        if (playerView.isControllerVisible()) {
            playerView.hideController();
        } else {
            playerView.showController();
        }
    }

    public void changeBrightness(float X, float Y, float x, float y, float distance) {
        if (HALF_WIDTH == 0 || x > HALF_WIDTH) return;
        ACTION_IN_PROCESS = 1;
        progressBarBrightness.setTitle("Brightness");
        distance = distance / 270;
        if (y < Y) {
            commonBrightness(distance);
        } else {
            commonBrightness(-distance);
        }
    }

    public void commonBrightness(float distance) {
        int brightnessInt;
        Window window = activity.getWindow();
        WindowManager.LayoutParams layout = window.getAttributes();
        if (window.getAttributes().screenBrightness + distance <= 1 && window.getAttributes().screenBrightness + distance >= 0) {
            progressBarBrightness.show();
            progressBarAudio.hide();
            seekView.hide();

            if ((int) ((window.getAttributes().screenBrightness + distance) * 100) > 100) {
                brightnessInt = 100;
            } else {
                brightnessInt = Math.max((int) ((window.getAttributes().screenBrightness + distance) * 100), 0);
            }

            progressBarBrightness.setProgress(brightnessInt);
            progressBarBrightness.setProgressText(brightnessInt + "%");

            brightness = window.getAttributes().screenBrightness + distance;
            layout.screenBrightness = brightness;

            MyApplication.prefHelper.customPrefs(Constants.SharedPrefNames.general.name()).edit().
                    putFloat(Constants.SharedPrefItemNames.brightness.name(), brightness).apply();

            window.setAttributes(layout);
        }
    }

    public void changeVolume(float X, float Y, float x, float y, float distance) {
        if (HALF_WIDTH == 0 || x < HALF_WIDTH) return;
        ACTION_IN_PROCESS = 2;
        progressBarAudio.setTitle("  Volume  ");
        if (y < Y) {
            distance = distance / 100;
            commonVolume(distance);
        } else {
            distance = distance / 150;
            commonVolume(-distance);
        }
    }

    public void commonVolume(float distance) {
        progressBarAudio.show();
        progressBarBrightness.hide();
        seekView.hide();
        if (distance > 0) {
            if (currentVolume < maxVolume) {
                currentVolume = currentVolume + 1;
            }
        } else{
            if (currentVolume > 0) {
                currentVolume = currentVolume - 1;
            } else {
                currentVolume = 0;
            }
        }
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
        progressBarAudio.setProgress(currentVolume);
        progressBarAudio.setProgressText("" + currentVolume);
    }

    public void changeSeek(float X, float Y, float x, float y, float distance) {
        ACTION_IN_PROCESS = 3;
        distance = distance / 200;
        if (x > X) {
            seekCommon(distance);
        } else {
            seekCommon(-distance);
        }
    }

    public void seekCommon(float distance) {
        progressBarAudio.hide();
        progressBarBrightness.hide();
        seekView.show();

        seekDistance += distance * SEEK_THRESHOLD;
        long currentPosition = simpleExoPlayer.getCurrentPosition();

        if (currentPosition + (int) (distance * SEEK_THRESHOLD) > 0 && currentPosition + (int) (distance * SEEK_THRESHOLD) < simpleExoPlayer.getDuration() + 10) {
            simpleExoPlayer.seekTo(currentPosition + (int) (distance * SEEK_THRESHOLD));
            if (seekDistance > 0)
                seekView.setText("+" + Math.abs((int) (seekDistance / SEEK_THRESHOLD)) + ":" + String.valueOf(Math.abs((int) ((seekDistance) % SEEK_THRESHOLD))).substring(0, 2) + "(" + (int) ((currentPosition + (int) (distance * SEEK_THRESHOLD)) / SEEK_THRESHOLD) + ":" + String.valueOf((int) ((currentPosition + (int) (distance * SEEK_THRESHOLD)) % SEEK_THRESHOLD)).substring(0, 2) + ")");
            else
                seekView.setText("-" + Math.abs((int) (seekDistance / SEEK_THRESHOLD)) + ":" + String.valueOf(Math.abs((int) ((seekDistance) % SEEK_THRESHOLD))).substring(0, 2) + "(" + (int) ((currentPosition + (int) (distance * SEEK_THRESHOLD)) / SEEK_THRESHOLD) + ":" + String.valueOf((int) ((currentPosition + (int) (distance * SEEK_THRESHOLD)) % SEEK_THRESHOLD)).substring(0, 2) + ")");
        }
    }

    float getDistance(float startX, float startY, MotionEvent ev) {
        float distanceSum = 0;
        final int historySize = ev.getHistorySize();
        for (int h = 0; h < historySize; h++) {
            float hx = ev.getHistoricalX(0, h);
            float hy = ev.getHistoricalY(0, h);
            float dx = (hx - startX);
            float dy = (hy - startY);
            distanceSum += Math.sqrt(dx * dx + dy * dy);
            startX = hx;
            startY = hy;
        }
        float dx = (ev.getX(0) - startX);
        float dy = (ev.getY(0) - startY);
        distanceSum += Math.sqrt(dx * dx + dy * dy);
        return distanceSum;
    }
}
