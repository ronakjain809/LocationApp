package com.example.locationapp.util;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

public class Rotations {
    private Rotations() { }
    public static float normalizeRadToDegree(float rad) {
        return (float) ((Math.toDegrees(rad)));
    }

    public static void applySmoothRotationAnimation(View view, float from, float to) {
        Animation animation = new RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(750);
        animation.setRepeatCount(0);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    public static void applySmoothViewWindowRotation(View view, float from, float to) {
        Animation animation = new RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(750);
        animation.setRepeatCount(0);
        animation.setFillAfter(true);
        animation.setDetachWallpaper(true);
        view.startAnimation(animation);
    }
}
