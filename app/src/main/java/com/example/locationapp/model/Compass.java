package com.example.locationapp.model;

import java.util.Observable;

import com.example.locationapp.util.Rotations;

public final class Compass extends Observable {
    private float azimuth;

    public float getAzimuth() {
        synchronized (this) {
            return azimuth;
        }
    }

    public float getNormalizedAzimuthInDegrees() {
        synchronized (this) {
            return Rotations.normalizeRadToDegree(azimuth);
        }
    }

    public void setAzimuth(float azimuth) {
        synchronized (this) {
            this.azimuth = azimuth;
        }
        setChanged();
        notifyObservers(this);
        clearChanged();
    }

}
