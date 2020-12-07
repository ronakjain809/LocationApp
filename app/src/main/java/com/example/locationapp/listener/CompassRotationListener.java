package com.example.locationapp.listener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.locationapp.model.Compass;


public class CompassRotationListener implements SensorEventListener {
    private static final int SAMPLE_SIZE = 1;

    private final Compass compass;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private final float[] samples = new float[SAMPLE_SIZE];
    private int sampleIndex = 0;

    public CompassRotationListener(Compass compass) {
        this.compass = compass;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);
        } else {
            return;
        }
        recalculateAzimuth();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void recalculateAzimuth() {
        if (sampleIndex >= SAMPLE_SIZE - 1) {
            compass.setAzimuth(averageSample());
            sampleIndex = 0;
        }
        samples[sampleIndex++] = orientationAngles[0];
    }

    private float averageSample() {
        float sampleSum = 0;
        for (float sample: samples) {
            sampleSum += sample;
        }
        return sampleSum/SAMPLE_SIZE;
    }
}
