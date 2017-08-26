package com.example.ubuntu.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class WPMService extends Service {
    Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    SpeechRecognizer speechRecognizer = null;
    long speechLength = 0;
    long pauseLength = 0;
    long startTimeStamp, endTimeStamp;
    float wps = 0;
    public String mError;
    private static final String TAG = "WPMService";

    public WPMService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startTimeStamp = System.currentTimeMillis();
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.example.ubuntu.myapplication");
        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 0);
        speechIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        startListening();
    }

    public void startListening() {
        if(speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new listener());
        }
        speechRecognizer.startListening(speechIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private class listener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle bundle) {

        }

        @Override
        public void onBeginningOfSpeech() {
            long currentTimeStamp = System.currentTimeMillis();
            pauseLength = Math.abs(startTimeStamp - currentTimeStamp)/1000;
            startTimeStamp = currentTimeStamp;
        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            endTimeStamp = System.currentTimeMillis();
            // Calculate the time for which the speaker was speaking.
            speechLength = (endTimeStamp - startTimeStamp)/1000;
            startTimeStamp = endTimeStamp;
        }

        @Override
        public void onError(int error) {
            mError = "";
            switch (error) {
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    mError = " network timeout";
                    startListening();
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    mError = " network" ;
                    break;
                case SpeechRecognizer.ERROR_AUDIO:
                    mError = " audio";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    mError = " server";
                    startListening();
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    mError = " client";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    mError = " speech time out" ;
                    startListening();
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    mError = " no match" ;
                    startListening();
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    mError = " recogniser busy" ;
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    mError = " insufficient permissions" ;
                    break;

            }
            Log.e(TAG,  "Error: " +  error + " - " + mError);
        }

        public void onResults(Bundle results) {
            startListening();
            String str = "";
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (data != null) {
                str += data.get(0);
            }
            String [] spoken_words = str.split(" ");
            if (speechLength != 0) {
                wps = spoken_words.length / speechLength;
            }
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("wpmServiceAction");
            broadcastIntent.putExtra("speechLength", String.valueOf(speechLength));
            broadcastIntent.putExtra("pauseLength", String.valueOf(pauseLength));
            broadcastIntent.putExtra("wps", Float.toString(wps));
            sendBroadcast(broadcastIntent);
        }

        public void onPartialResults(Bundle partialResults) {

        }

        public void onEvent(int eventType, Bundle params) {
        }
    }

}
