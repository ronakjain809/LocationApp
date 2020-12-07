package com.example.locationapp.util;

import android.location.Location;

public final class Geo {
    private Geo() {}

    private static final Location SOURCE = new Location("");
    private static final Location DESTINATION = new Location("");

    public static double getBearing(double latA, double longA, double latB, double longB) {
        SOURCE.setLatitude(latA); SOURCE.setLongitude(longA);
        DESTINATION.setLatitude(latB); DESTINATION.setLongitude(longB);
        return Math.toRadians(SOURCE.bearingTo(DESTINATION));
    }
}
