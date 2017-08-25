package com.example.ubuntu.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.Toast;

public class MainService extends Service {
    private boolean running = true;
    public MainService() {
    }

    private Chronometer mChronometer;

    @Override
    public void onCreate() {
        super.onCreate();
        mChronometer = new Chronometer(this);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        new Thread(new Runnable() {
            public void run() {
                while(running) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    long elapsedMillis = SystemClock.elapsedRealtime()
                            - mChronometer.getBase();
                    int hours = (int) (elapsedMillis / 3600000);
                    int minutes = (int) (elapsedMillis - hours * 3600000) / 60000;
                    int seconds = (int) (elapsedMillis - hours * 3600000 - minutes * 60000) / 1000;
                    int millis = (int) (elapsedMillis - hours * 3600000 - minutes * 60000 - seconds * 1000);
                    Intent broadcastIntent = new Intent();
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

}