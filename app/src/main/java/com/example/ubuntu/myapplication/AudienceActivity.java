/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.ubuntu.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.IOException;
import java.util.Locale;

/**
 * Fragment for the Gorilla tab.
 */
public class AudienceActivity extends AppCompatActivity{
    private static final String TAG = "AudienceActivity";
    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_VIDEO_DURATION = "videoDuration";
    private static final String STATE_PROGRESS_TIME = "progressTime";
    private int badVideoCount = 1;
    private boolean videoGood = true;
    private boolean videoOK = false;
    private String data;
    private BroadcastReceiver mReceiver;
    private IntentFilter intentFilter = new IntentFilter();
    /**
     * The video view and its custom UI elements.
     */
    private VrVideoView videoWidgetView;

    /**
     * Seeking UI & progress indicator. The seekBar's progress value represents milliseconds in the
     * video.
     */
    private SeekBar seekBar;
    private TextView statusText;

    /**
     * By default, the video will start playing as soon as it is loaded.
     */
    private boolean isPaused = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audience_fragment);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        statusText = (TextView) findViewById(R.id.status_text);
        videoWidgetView = (VrVideoView) findViewById(R.id.video_view);
        intentFilter.addAction("mainServiceAction");
        try {
            if (videoWidgetView.getDuration() <= 0) {
                videoWidgetView.loadVideoFromAsset("start.mp4",
                        new VrVideoView.Options());
            }
        } catch (Exception e) {
            Log.d("Exception raised", "on video view");
        }

        // initialize based on the saved state
        if (savedInstanceState != null) {
            long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
            videoWidgetView.seekTo(progressTime);
            seekBar.setMax((int)savedInstanceState.getLong(STATE_VIDEO_DURATION));
            seekBar.setProgress((int) progressTime);

            isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
            if (isPaused) {
                videoWidgetView.pauseVideo();
            }
        } else {
            seekBar.setEnabled(false);
        }

        // Add the seekbar listener here.

        // Add the VrVideoView listener here
        // initialize the seekbar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // if the user changed the position, seek to the new position.
                if (fromUser) {
                    videoWidgetView.seekTo(progress);
                    updateStatusText();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // ignore for now.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // ignore for now.
            }
        });

        // initialize the video listener
        videoWidgetView.setEventListener(new VrVideoEventListener() {
            /**
             * Called by video widget on the UI thread when it's done loading the video.
             */
            @Override
            public void onLoadSuccess() {
                Log.i(TAG, "Successfully loaded video " + videoWidgetView.getDuration());
                seekBar.setMax((int) videoWidgetView.getDuration());
                seekBar.setEnabled(true);
                updateStatusText();
            }

            /**
             * Called by video widget on the UI thread on any asynchronous error.
             */
            @Override
            public void onLoadError(String errorMessage) {
                Log.e(TAG, "Error loading video: " + errorMessage);
            }

            @Override
            public void onClick() {
                if (isPaused) {
                    videoWidgetView.playVideo();
                } else {
                    videoWidgetView.pauseVideo();
                }

                isPaused = !isPaused;
                updateStatusText();
            }

            /**
             * Update the UI every frame.
             */
            @Override
            public void onNewFrame() {
                updateStatusText();
                seekBar.setProgress((int) videoWidgetView.getCurrentPosition());
            }

            /**
             * Make the video play in a loop. This method could also be used to move to the next video in
             * a playlist.
             */
            @Override
            public void onCompletion() {
                videoWidgetView.seekTo(0);
            }
        });

    }

    private void updateStatusText() {
        String status = (isPaused ? "Paused: " : "Playing: ") +
                String.format(Locale.getDefault(), "%.2f", videoWidgetView.getCurrentPosition() / 1000f) +
                " / " +
                videoWidgetView.getDuration() / 1000f +
                " seconds.";
        statusText.setText(status);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(STATE_PROGRESS_TIME, videoWidgetView.getCurrentPosition());
        savedInstanceState.putLong(STATE_VIDEO_DURATION, videoWidgetView.getDuration());
        savedInstanceState.putBoolean(STATE_IS_PAUSED, isPaused);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Prevent the view from rendering continuously when in the background.
        videoWidgetView.pauseRendering();
        // If the video was playing when onPause() is called, the default behavior will be to pause
        // the video and keep it paused when onResume() is called.
        isPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume the 3D rendering.
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("mainServiceAction")) {
                    float totalPercentage = Float.parseFloat(intent.getStringExtra("totalPercentage"));
                    Log.d("Video", ""+videoWidgetView);
                    if(totalPercentage < 30 && badVideoCount > 0){
                        try {
                            videoWidgetView.loadVideoFromAsset("boo.mp4", new VrVideoView.Options());
                            badVideoCount--;
                        } catch (IOException e) {
                            Log.d("Exception raised", "on video view");
                        }
                    }
                    else if(totalPercentage < 70 && !videoOK){
                        try {
                            if(videoWidgetView.getDuration() < 7000)
                                videoWidgetView.loadVideoFromAsset("medium.mp4", new VrVideoView.Options());
                        } catch (IOException e) {
                            Log.d("Exception raised", "on video view");
                        }
                    }
                    else if(!videoGood){
                        try {
                            if(videoWidgetView.getDuration() < 7000)
                                videoWidgetView.loadVideoFromAsset("start.mp4", new VrVideoView.Options());
                        } catch (IOException e) {
                            Log.d("Exception raised", "on video view");
                        }
                    }
                }

            }
        };
        registerReceiver(mReceiver, intentFilter);
        videoWidgetView.resumeRendering();
        videoWidgetView.setDisplayMode(
                VrVideoView.DisplayMode.FULLSCREEN_STEREO
        );
        // Update the text to account for the paused video in onPause().
        updateStatusText();
    }

    @Override
    public void onDestroy() {
        // Destroy the widget and free memory.
        videoWidgetView.shutdown();
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

}
