package com.example.ubuntu.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.internal.zzagz.runOnUiThread;

public class OrientationService extends Service implements SensorEventListener {

    private Sensor mRotationSensor;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float pitch, degree;
    private static final int FROM_RADS_TO_DEGS = -57;
    private Intent broadcastIntent;
    private SensorManager mSensorManager;
    private static final int SENSOR_DELAY = 500 * 1000; // 500ms
    private float standardDegreeLeft, standardDegreeRight;
    private Timer disorientedTimer, orientedTimer;
    private float disorientedTime = 0, orientedTime = 0;
    private boolean standardDegreeSet;
    private boolean pauseOrientedTimer = false;

    public OrientationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        standardDegreeSet = false;
        broadcastIntent = new Intent();
        broadcastIntent.setAction("orientationServiceAction");
        disorientedTimer = new Timer();
        orientedTimer = new Timer();
        disorientedTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pauseOrientedTimer) {
                            disorientedTime++;
                        }
                    }
                });
            }
        }, 1000, 1000);
        orientedTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!pauseOrientedTimer) {
                            orientedTime++;
                        }
                    }
                });
            }
        }, 1000, 1000);
        try {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            mSensorManager.registerListener(this, mRotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        } catch (Exception e) {
            Toast.makeText(this, "Hardware compatibility issue", Toast.LENGTH_LONG).show();
        }

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
            float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
            update(null, azimuthInDegrees, true);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void update(float[] vectors, float degreeInput, boolean isOrientation) {
        if (isOrientation) {
            degree = degreeInput;
            if (!standardDegreeSet) {
                standardDegreeSet = true;
                standardDegreeRight = degree + 60;
                if (standardDegreeRight > 360) {
                    standardDegreeRight -= 360;
                }
                standardDegreeLeft = degree - 60;
                if (standardDegreeLeft < 0) {
                    standardDegreeLeft += 360;
                }
            }

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
        if (pitch > 30 || pitch < -35 || degree > standardDegreeRight || degree < standardDegreeLeft) {
            pauseOrientedTimer = true;
        } else {
            pauseOrientedTimer = false;
        }
        broadcastIntent.putExtra("orientedTime", Float.toString(orientedTime));
        broadcastIntent.putExtra("disorientedTime", Float.toString(disorientedTime));
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }


}
