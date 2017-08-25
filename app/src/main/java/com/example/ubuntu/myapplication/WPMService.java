package com.example.ubuntu.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import java.util.ArrayList;

public class WPMService extends Service {
    Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    SpeechRecognizer speechRecognizer = null;
    public WPMService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        startListening();
    }

    public void startListening() {
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new listener());
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.example.ubuntu.myapplication");
        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 0);
        speechIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        speechRecognizer.startListening(speechIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private class listener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("wpmServiceAction");
            broadcastIntent.putExtra("Data", "ready");
            sendBroadcast(broadcastIntent);
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("wpmServiceAction");
            broadcastIntent.putExtra("Data", "end");
            sendBroadcast(broadcastIntent);
        }

        @Override
        public void onError(int i) {

        }

        public void onResults(Bundle results) {
            startListening();
            String str = "";
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (data != null) {
                str += data.get(0);
            }
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("wpmServiceAction");
            broadcastIntent.putExtra("Data", str);
            sendBroadcast(broadcastIntent);
        }

        public void onPartialResults(Bundle partialResults) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("wpmServiceAction");
            broadcastIntent.putExtra("Data", "partial");
            sendBroadcast(broadcastIntent);
        }

        public void onEvent(int eventType, Bundle params) {
        }
    }

}
