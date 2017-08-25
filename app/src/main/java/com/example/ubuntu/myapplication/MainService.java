package com.example.ubuntu.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.Toast;

public class MainService extends Service implements SensorEventListener {
    private boolean running = true;
    private Chronometer mChronometer;

    private SensorManager mSensorManager;
    private Sensor mRotationSensor;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    private static final int SENSOR_DELAY = 500 * 1000; // 500ms
    private static final int FROM_RADS_TO_DEGS = -57;
    private float pitch, degree;
    private Intent broadcastIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        mChronometer = new Chronometer(this);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        broadcastIntent = new Intent();
        broadcastIntent.setAction("mainServiceAction");
        try {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY);
            mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY);
            mSensorManager.registerListener(this, mMagnetometer, SENSOR_DELAY);
        } catch (Exception e) {
            Toast.makeText(this, "Hardware compatibility issue", Toast.LENGTH_LONG).show();
        }
//        new Thread(new Runnable() {
//            public void run() {
//                while(running) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    long elapsedMillis = SystemClock.elapsedRealtime()
//                            - mChronometer.getBase();
//                    int hours = (int) (elapsedMillis / 3600000);
//                    int minutes = (int) (elapsedMillis - hours * 3600000) / 60000;
//                    int seconds = (int) (elapsedMillis - hours * 3600000 - minutes * 60000) / 1000;
//                    int millis = (int) (elapsedMillis - hours * 3600000 - minutes * 60000 - seconds * 1000);
//                    Intent broadcastIntent = new Intent();
//                    broadcastIntent.setAction("mainServiceAction");
////                    broadcastIntent.putExtra("Data", hours + ":" + minutes + ":" + seconds + ":" + millis);
//                    broadcastIntent.putExtra("Data", mAccelerometerReading[0]);
//                    sendBroadcast(broadcastIntent);
//                }
//            }
//        }).start();

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
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mRotationSensor) {
            if (event.values.length > 4) {
                float[] truncatedRotationVector = new float[4];
                System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4);
                update(truncatedRotationVector, 0, false);
            } else {
                update(event.values, 0, false);
            }
        } else if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegrees = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            update(null, azimuthInDegrees, true);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    private void update(float[] vectors, float degreeInput, boolean isOrientation) {
        if (isOrientation) {
            degree = degreeInput;
        } else {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);
            int worldAxisX = SensorManager.AXIS_X;
            int worldAxisZ = SensorManager.AXIS_Z;
            float[] adjustedRotationMatrix = new float[9];
            SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix);
            float[] orientation = new float[3];
            SensorManager.getOrientation(adjustedRotationMatrix, orientation);
            pitch = orientation[1] * FROM_RADS_TO_DEGS;
        }
        broadcastIntent.putExtra("Data", "Pitch: " + pitch + "  " + "Degree: " + degree );
        sendBroadcast(broadcastIntent);
    }
}