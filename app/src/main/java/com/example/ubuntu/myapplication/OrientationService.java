package com.example.ubuntu.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

public class OrientationService extends Service implements SensorEventListener{

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

    public OrientationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastIntent = new Intent();
        broadcastIntent.setAction("orientationServiceAction");
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }


}
