package com.example.ubuntu.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

public class MainService extends Service {
    private boolean running = true;
    private Intent broadcastIntent;
    private IntentFilter intentFilter = new IntentFilter();
    long speechLength = 0;
    long pauseLength = 0;
    float wps = 0;
    private float orientationPercentage;
    private int heartRatePercentage;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        broadcastIntent = new Intent();
        broadcastIntent.setAction("mainServiceAction");
        intentFilter.addAction("orientationServiceAction");
        intentFilter.addAction("wpmServiceAction");
        intentFilter.addAction("wearServiceAction");
        registerReceiver(mReceiver, intentFilter);
        new Thread(new Runnable() {
            public void run() {
                while (running) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    broadcastIntent = new Intent("mainServiceAction");
                    broadcastIntent.putExtra("orientationPercentage", orientationPercentage);
                    broadcastIntent.putExtra("heartRatePercentage", orientationPercentage);
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
                orientationPercentage = (orientedTime / (orientedTime + disorientedTime)) * 100;
            } else if (intent.getAction().equals("wearServiceAction")) {
                heartRatePercentage = Integer.parseInt(intent.getStringExtra("heartRate"));
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
            }
            else if (intent.getAction().equals("wpmServiceAction")) {
                speechLength = intent.getIntExtra("speechLength", 0);
                pauseLength = intent.getIntExtra("pauseLength", 0);
                wps = intent.getFloatExtra("wps", 0);
                Toast.makeText(
                    getApplicationContext(),
                        broadcastIntent.getStringExtra("Data") + "spelength:" + speechLength + ", paulength:" + pauseLength + ", wps:" + wps,
                    Toast.LENGTH_LONG
                ).show();
            }
        }
    };

}