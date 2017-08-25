package com.example.ubuntu.myapplication;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    boolean mainServiceRunning = false;
    private IntentFilter intentFilter = new IntentFilter();
    private Context context;
    private TextView textView;

    @Override
    protected void onStart() {
        super.onStart();
        intentFilter.addAction("mainServiceAction");

    }
    protected void start() {
        Toast.makeText(getApplicationContext(), "Start", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainService.class);
        startService(intent);
        mainServiceRunning = true;
    }

    protected void stop() {
        Toast.makeText(getApplicationContext(), "Stop", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainService.class);
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

            if (intent.getAction().equals("mainServiceAction")) {
                textView.setText(intent.getStringExtra("Data"));

            }
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
}
