package com.example.ubuntu.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.internal.zzagz.runOnUiThread;

public class SoundFeedbackService extends Service {
    private MediaPlayer mediaPlayer;
    private int maxClap = 5, maxBoo = 3;
    private Timer timer;
    private int time = 0;
    private float totalPercentage;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("mainServiceAction");
        intentFilter.addAction("wpmServiceAction");
        registerReceiver(mReceiver, intentFilter);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        time++;
                    }
                });
            }
        }, 1000, 1000);
        startPlayer();

    }

    void startPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.background);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("mainServiceAction")) {
                totalPercentage = intent.getFloatExtra("totalPercentage", 0);
            } else if (intent.getAction().equals("wpmServiceAction")){
                if (totalPercentage > 70 && time > 10 && maxClap > 0) {
                    time = 0;
                    maxClap--;
                    mediaPlayer.stop();
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.applause);
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            startPlayer();
                        }
                    });
                } else if (totalPercentage < 50 && time > 10 && maxBoo > 0){
                    time = 0;
                    maxBoo--;
                    mediaPlayer.stop();
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.chatter);
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            startPlayer();
                        }
                    });
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        unregisterReceiver(mReceiver);
    }
}
