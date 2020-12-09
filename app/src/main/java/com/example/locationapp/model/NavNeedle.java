package com.example.locationapp.model;

import java.util.Observable;

import com.example.locationapp.util.Geo;
import com.example.locationapp.util.Rotations;

public final class NavNeedle extends Observable {
    private float bearing;
    private final double[] destination = new double[2];
    private final double[] source = new double[2];

    {
        setDestination(48.858595716441805, 2.2944410188355775);
    }

    public float getBearing() {
        synchronized (this) {
            return bearing;
        }
    }



    public float getNormalizedBearingInDegrees() {
        synchronized (this) {
            return Rotations.normalizeRadToDegree(bearing);
        }
    }

    public void recalculateBearingFrom(double latitude, double longitude) {
        synchronized (this) {
            bearing = (float) Geo.getBearing(latitude, longitude, destination[0], destination[1]);
            source[0] = latitude;
            source[1] = longitude;
        }
        setChanged();
        notifyObservers(this);
        clearChanged();
    }
    public void recalculateBearingFromLastSource() {
        recalculateBearingFrom(source[0], source[1]);
    }

    public double getDestinationLongitude() {
        return destination[1];
    }

    public double getDestinationLatitude() {
        return destination[0];
    }

    public double getSourceLongitude() {
        return source[1];
    }

    public double getSourceLatitude() {
        return source[0];
    }

    public void setSourceLongitude(double longitude) {
        source[1] = longitude;
    }

    public void setSourceLatitude(double latitude) {
        source[0] = latitude;
    }

    public void setDestinationLongitude(double longitude) {
        destination[1] = longitude;
    }

    public void setDestinationLatitude(double latitude) {
        destination[0] = latitude;
    }

    public void setDestination(double latitude, double longitude) {
        setDestinationLatitude(latitude);
        setDestinationLongitude(longitude);
    }
    public void setSource(double latitude, double longitude) {
        setSourceLatitude(latitude);
        setSourceLongitude(longitude);
    }

}
