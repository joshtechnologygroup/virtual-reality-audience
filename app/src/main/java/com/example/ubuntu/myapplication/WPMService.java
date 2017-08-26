package com.example.ubuntu.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.abs;

public class WPMService extends Service {
    Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    SpeechRecognizer speechRecognizer = null;
    long speechLength = 0; // in milliseconds
    long pauseLength = 0; // in milliseconds
    long startTimeStamp, endTimeStamp;
    public String mError;
    private static final String TAG = "WPMService";
    public ArrayList<String> wordList = new ArrayList<>();
    public static final int wordCountInterVal = 1000 * 20; // 20 seconds
    AudioManager mAudioManager;
    int mStreamVolume;

    public WPMService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startTimeStamp = System.currentTimeMillis();
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 0);
        speechIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        startListening();
        Timer wpmTimer = new Timer();
        wpmTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Calculate Words per minute and broadcast the value
                float wpm = (wordList.size() * 1000 * 60 / wordCountInterVal);
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("wpmServiceAction");
                broadcastIntent.putExtra("wpm", wpm);
                broadcastIntent.putExtra("onlyWpm", true);
                sendBroadcast(broadcastIntent);
                // Reset the word list so that new words can be stored in the list;
                wordList = new ArrayList<String>();
            }
        }, 1000, wordCountInterVal);

    }

    public void startListening() {
        // Getting system volume into var for later un-muting
        mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
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
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);
        }

        @Override
        public void onBeginningOfSpeech() {
            // Calculate the time for which the speaker was silent.
            long currentTimeStamp = System.currentTimeMillis();
            pauseLength = abs(startTimeStamp - currentTimeStamp);
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
            speechLength = (endTimeStamp - startTimeStamp);
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
                    speechRecognizer.destroy();
                    speechRecognizer = null;
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
            wordList.addAll(new ArrayList<String>(Arrays.asList(str.split(" "))));
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("wpmServiceAction");
            broadcastIntent.putExtra("Data", str);
            broadcastIntent.putExtra("speechLength", speechLength);
            broadcastIntent.putExtra("pauseLength", pauseLength);
            sendBroadcast(broadcastIntent);
        }

        public void onPartialResults(Bundle partialResults) {

        }

        public void onEvent(int eventType, Bundle params) {
        }
    }

}
