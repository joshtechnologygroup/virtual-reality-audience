package com.example.ubuntu.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;

public class MainService extends Service {
    private boolean running = true;
    private Intent broadcastIntent;
    private IntentFilter intentFilter = new IntentFilter();
    private float orientationPercentage;

    @Override
    public void onCreate() {
        super.onCreate();
        intentFilter = new IntentFilter();
        broadcastIntent = new Intent();
        broadcastIntent.setAction("mainServiceAction");
        intentFilter.addAction("orientationServiceAction");
        registerReceiver(mReceiver, intentFilter);
        new Thread(new Runnable() {
            public void run() {
                while (running) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    broadcastIntent = new Intent();
                    broadcastIntent.setAction("mainServiceAction");
                    broadcastIntent.putExtra("data", orientationPercentage);
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
            // Log data
            if (intent.getAction().equals("orientationServiceAction")) {
                float orientedTime = Float.parseFloat(intent.getStringExtra("orientedTime"));
                float disorientedTime = Float.parseFloat(intent.getStringExtra("disorientedTime"));
                if (orientedTime == 0) {
                    orientedTime = 1;
                }
                orientationPercentage = (orientedTime / (orientedTime + disorientedTime)) * 100;
            }
        }
    };

}