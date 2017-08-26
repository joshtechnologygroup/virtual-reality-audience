package com.example.ubuntu.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    boolean mainServiceRunning = false;
    private IntentFilter intentFilter = new IntentFilter();
    private TextView textView;

    private void requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String requiredPermission = Manifest.permission.RECORD_AUDIO;
            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 101);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        intentFilter.addAction("mainServiceAction");
        requestRecordAudioPermission();
    }

    protected void start() {
        Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, OrientationService.class);
        startService(intent);
//        intent = new Intent(this, WPMService.class);
//        startService(intent);
        intent = new Intent(this, WearService.class);
        startService(intent);
        intent = new Intent(this, MainService.class);
        startService(intent);
        mainServiceRunning = true;
    }

    protected void stop() {
        Toast.makeText(getApplicationContext(), "Stop", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainService.class);
        stopService(intent);
        intent = new Intent(this, OrientationService.class);
        stopService(intent);
//        intent = new Intent(this, WPMService.class);
//        stopService(intent);
        intent = new Intent(this, WearService.class);
        stopService(intent);

        mainServiceRunning = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        textView = (TextView) findViewById(R.id.text);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mainServiceRunning) {
                    stop();
                }
                else {
                    start();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mReceiver, intentFilter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            textView.setText(intent.getStringExtra("data"));
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
        unregisterReceiver(mReceiver);
    }
}
