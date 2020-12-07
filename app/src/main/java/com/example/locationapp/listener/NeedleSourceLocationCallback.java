package com.example.locationapp.listener;

import android.location.Location;

import com.example.locationapp.model.NavNeedle;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;


public class NeedleSourceLocationCallback extends LocationCallback {
    private final NavNeedle needle;
    private final Runnable noLocCallback;
    public NeedleSourceLocationCallback(NavNeedle needle, Runnable noLocationCallback) {
        this.needle = needle;
        this.noLocCallback = noLocationCallback;
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        Location location = locationResult.getLastLocation();
        needle.recalculateBearingFrom(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
        super.onLocationAvailability(locationAvailability);
        if (!locationAvailability.isLocationAvailable()) {
            noLocCallback.run();
        }
    }
}
