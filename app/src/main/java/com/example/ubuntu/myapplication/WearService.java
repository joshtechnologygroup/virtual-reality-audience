package com.example.ubuntu.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearService  extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        showToast(messageEvent.getPath());
    }

    private void showToast(String message) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("wpmServiceAction");
        broadcastIntent.putExtra("Data", message);
        sendBroadcast(broadcastIntent);
    }

}
