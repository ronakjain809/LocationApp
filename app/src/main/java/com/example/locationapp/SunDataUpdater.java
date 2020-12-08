package com.example.locationapp;

import android.location.Location;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import org.shredzone.commons.suncalc.SunTimes;

import java.time.ZonedDateTime;

public class SunDataUpdater extends LocationCallback {
    private final SunData sunData;
    private final Runnable onFailRun;

    public SunDataUpdater(SunData sunData, Runnable onFailRun) {
        this.sunData = sunData;
        this.onFailRun = onFailRun;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        Location lastLocation = locationResult.getLastLocation();
        if (lastLocation == null) return;

        double latitude = lastLocation.getLatitude();
        double longitude = lastLocation.getLongitude();

        ZonedDateTime currentSystemTime = ZonedDateTime.now();
        SunTimes times = SunTimes.compute().on(currentSystemTime).at(latitude, longitude).execute();

        sunData.setSunrise(times.getRise());
        sunData.setNoon(times.getNoon());
        sunData.setSunset(times.getSet());

        sunData.alertObservers();
    }

    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
        super.onLocationAvailability(locationAvailability);
        if (!locationAvailability.isLocationAvailable()) {
            onFailRun.run();
        }
    }
}
