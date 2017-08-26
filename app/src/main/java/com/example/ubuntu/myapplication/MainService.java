package com.example.ubuntu.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class MainService extends Service {
    private boolean running = true;
    private Intent broadcastIntent;
    private IntentFilter intentFilter;
    long speechLength = 0; // in milliseconds
    long pauseLength = 0; // in milliseconds
    float wpm = 0; // Words per minute
    private float orientationPercentage = 100;
    private int heartRatePercentage = 100;
    private int wpmPercentage = 100;
    private int speechPercentage = 100;
    private float totalPercentage = 100;
    @Override
    public void onCreate() {
        super.onCreate();
        broadcastIntent = new Intent();
        broadcastIntent.setAction("mainServiceAction");

        intentFilter = new IntentFilter();
        intentFilter.addAction("orientationServiceAction");
        intentFilter.addAction("wpmServiceAction");
        intentFilter.addAction("wearServiceAction");
        registerReceiver(mReceiver, intentFilter);
        new Thread(new Runnable() {
            public void run() {
                while (running) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    broadcastIntent = new Intent("mainServiceAction");
                    broadcastIntent.putExtra("totalPercentage", totalPercentage);
                    sendBroadcast(broadcastIntent);
                }
            }
        }).start();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("orientationServiceAction")) {
                float orientedTime = Float.parseFloat(intent.getStringExtra("orientedTime"));
                float disorientedTime = Float.parseFloat(intent.getStringExtra("disorientedTime"));
                if (orientedTime == 0) {
                    orientedTime = 1;
                }
                Log.d("MainService", "-> OrientationService -> Oriented Time" + orientedTime);
                Log.d("MainService", "-> OrientationService -> DisOriented Time" + disorientedTime);
                orientationPercentage = (orientedTime / (orientedTime + disorientedTime)) * 100;
            } else if (intent.getAction().equals("wearServiceAction")) {
                heartRatePercentage = Integer.parseInt(intent.getStringExtra("heartRate"));
                Log.d("MainService", "-> WearService -> HeartRatePercentage" + heartRatePercentage);
                if (heartRatePercentage >= 140) {
                    heartRatePercentage = 20;
                } else if (heartRatePercentage >= 125) {
                    heartRatePercentage = 40;
                } else if (heartRatePercentage >= 115) {
                    heartRatePercentage = 60;
                } else if (heartRatePercentage >= 95) {
                    heartRatePercentage = 80;
                } else {
                    heartRatePercentage = 100;
                }
            } else if (intent.getAction().equals("wpmServiceAction")) {
                speechLength = intent.getLongExtra("speechLength", 0);
                pauseLength = intent.getLongExtra("pauseLength", 0);
                boolean onlyWpm = intent.getBooleanExtra("onlyWpm", false);
                if (onlyWpm) {
                    wpm = intent.getFloatExtra("wpm", 0);
                    if (wpm > 170 || wpm < 10) {
                        wpmPercentage = 20;
                    } else if (wpm > 160 || wpm < 25) {
                        wpmPercentage = 40;
                    } else if (wpm > 140 || wpm < 60) {
                        wpmPercentage = 60;
                    } else if (wpm > 120 || wpm < 80) {
                        wpmPercentage = 80;
                    } else {
                        wpmPercentage = 100;
                    }
                } else {
                    if (speechLength > 15000 || pauseLength > 5000) {
                        speechPercentage = 20;
                    } else if (speechLength > 13000 || pauseLength > 4000 || pauseLength < 1500) {
                        speechPercentage = 40;
                    } else if (speechLength > 12000 || pauseLength > 3500 || pauseLength < 2000) {
                        speechPercentage = 60;
                    } else if (speechLength > 10000 || pauseLength > 3000 || pauseLength < 2500) {
                        speechPercentage = 80;
                    } else {
                        speechPercentage = 100;
                    }
                }
                Log.d("MainService", "-> WpmService:" + wpm);
                Log.d("MainService", "-> SpeechService -> speechlength:" + speechLength);
                Log.d("MainService", "-> SpeechService -> pauseLength:" + pauseLength);
            }
            totalPercentage = ((6 * heartRatePercentage) + (4 * orientationPercentage) + (10 * wpmPercentage) + (10 * speechPercentage))/30;
            Log.d("MainService", "Hr: " + heartRatePercentage + "   Or: " + orientationPercentage + " wpm: "+ wpmPercentage + " sp: " + speechPercentage);
            Log.d("MainService", "Receiver -> totalPercentage:"  + totalPercentage);
        }
    };

}