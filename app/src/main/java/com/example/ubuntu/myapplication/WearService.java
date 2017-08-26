package com.example.ubuntu.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;


public class WearService extends WearableListenerService {

    private ArrayList<Integer> heartRates;
    private boolean running;

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        heartRates = new ArrayList<Integer>();
        new Thread(new Runnable() {
            public void run() {
                while (running) {
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int heartRate = 0;
                    if (heartRates.size() > 0) {
                        for (int hr : heartRates) {
                            heartRate += hr;
                        }
                        heartRate /= heartRates.size();
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction("wpmServiceAction");
                        broadcastIntent.putExtra("heartRate", heartRate);
                        sendBroadcast(broadcastIntent);
                        heartRates.clear();
                        heartRates.add(heartRate);
                    }
                }
            }
        }).start();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        heartRates.add(Integer.parseInt(messageEvent.getPath()));
    }

}
