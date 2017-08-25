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
import android.widget.Chronometer;
import android.widget.Toast;

public class MainService extends Service {
    private boolean running = true;
    private Chronometer mChronometer;
    private Intent broadcastIntent;
    private IntentFilter intentFilter = new IntentFilter();
//    long speechLength = 0;
//    long pauseLength = 0;
//    float wps = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mChronometer = new Chronometer(this);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        broadcastIntent = new Intent();
        broadcastIntent.setAction("mainServiceAction");
        intentFilter.addAction("orientationServiceAction");
        registerReceiver(mReceiver, intentFilter);
        new Thread(new Runnable() {
            public void run() {
                while(running) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long elapsedMillis = SystemClock.elapsedRealtime() - mChronometer.getBase();
                    int hours = (int) (elapsedMillis / 3600000);
                    int minutes = (int) (elapsedMillis - hours * 3600000) / 60000;
                    int seconds = (int) (elapsedMillis - hours * 3600000 - minutes * 60000) / 1000;
                    int millis = (int) (elapsedMillis - hours * 3600000 - minutes * 60000 - seconds * 1000);
                    broadcastIntent = new Intent();
                    broadcastIntent.setAction("mainServiceAction");
                    broadcastIntent.putExtra("Data", hours + ":" + minutes + ":" + seconds + ":" + millis);
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
        mChronometer.stop();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Log data
            if (intent.getAction().equals("orientationServiceAction")) {
                Toast.makeText(getApplicationContext(), intent.getStringExtra("Data"),
                        Toast.LENGTH_SHORT).show();
            }
//                speechLength = intent.getIntExtra("speechLength", 0);
//                pauseLength = intent.getIntExtra("pauseLength", 0);
//                wps = intent.getFloatExtra("wps", 0);
//                Toast.makeText(
//                    getApplicationContext(),
//                    "spelength:" + speechLength + ", paulength:" + pauseLength + ", wps:" + wps,
//                    Toast.LENGTH_LONG
//                ).show();
//            }
        }
    };

}