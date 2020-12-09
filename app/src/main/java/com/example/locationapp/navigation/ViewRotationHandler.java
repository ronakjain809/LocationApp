package com.example.locationapp.navigation;

import android.widget.ImageView;

import java.util.Observable;
import java.util.Observer;

import com.example.locationapp.model.Compass;
import com.example.locationapp.model.NavNeedle;
import com.example.locationapp.util.Rotations;

public final class ViewRotationHandler implements Observer {
    private static final float SMOOTHING_FACTOR = 0.2f;
    private final ImageView compassView;
    private final ImageView needleView;
    private final ImageView targetView;
    private final Compass compass;
    private final NavNeedle needle;

    private final float[] prevLowPassOutputCompass = new float[2];
    private final float[] prevLowPassOutputNeedle = new float[2];


    public ViewRotationHandler(ImageView compassView, ImageView needleView, ImageView targetView, Compass compass, NavNeedle needle) {
        this.compassView = compassView;
        this.needleView = needleView;
        this.compass = compass;
        this.needle = needle;
        this.targetView = targetView;
    }



    @Override
    public void update(Observable o, Object arg) {
        float prevCompassRotation = Rotations.normalizeRadToDegree((float) Math.atan2(prevLowPassOutputCompass[0], prevLowPassOutputCompass[1]));
        float newCompassRotation = Rotations.normalizeRadToDegree(getSmoothedCompassAngle(prevLowPassOutputCompass, -compass.getAzimuth()));

        Rotations.applySmoothRotationAnimation(compassView, prevCompassRotation, newCompassRotation);

        float prevNeedleRotation = Rotations.normalizeRadToDegree((float) Math.atan2(prevLowPassOutputNeedle[0], prevLowPassOutputNeedle[1]));
        float newNeedleRotation = Rotations.normalizeRadToDegree((getSmoothedCompassAngle(prevLowPassOutputNeedle, needle.getBearing()-compass.getAzimuth())));

        Rotations.applySmoothRotationAnimation(needleView, prevNeedleRotation, newNeedleRotation);
        Rotations.applySmoothRotationAnimation(targetView, prevNeedleRotation, newNeedleRotation);

        /*compassView.setRotation(Rotations.normalizeRadToDegree(getSmoothedCompassAngle(prevLowPassOutputCompass, -compass.getAzimuth())));
        needleView.setRotation(Rotations.normalizeRadToDegree((getSmoothedCompassAngle(prevLowPassOutputNeedle, -needle.getBearing()-compass.getAzimuth()))));*/
    }

    private float getSmoothedCompassAngle(float[] prevFilterResult, float angle) {
        float filteredSin = (float) (SMOOTHING_FACTOR * prevFilterResult[0] + (1-SMOOTHING_FACTOR) * Math.sin(angle));
        float filteredCos = (float) (SMOOTHING_FACTOR * prevFilterResult[1] + (1-SMOOTHING_FACTOR) * Math.cos(angle));
        prevFilterResult[0] = filteredSin;
        prevFilterResult[1] = filteredCos;
        return (float) Math.atan2(filteredSin, filteredCos);
    }

}
