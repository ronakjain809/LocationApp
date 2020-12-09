package com.example.locationapp.navigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.locationapp.constant.Interval;
import com.example.locationapp.constant.RequestCode;
import com.example.locationapp.listener.CompassRotationListener;
import com.example.locationapp.listener.NeedleSourceLocationCallback;
import com.example.locationapp.model.Compass;
import com.example.locationapp.model.NavNeedle;
import com.example.locationapp.util.Permissions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public final class NavigationHandler {
    private boolean running = false;
    private boolean usingAccelerometer = false;
    private boolean usingMagneticFieldSensor = false;
    private boolean usingRotationVectorSensor = false;


    private final NavNeedle needle = new NavNeedle();
    private final Compass compass = new Compass();

    private final CompassRotationListener rotationListener;
    private final NeedleSourceLocationCallback sourceLocationCallback;

    private final SensorManager sensorManager;
    private final Sensor accelerometerSensor;
    private final Sensor magneticFieldSensor;
    private final Sensor rotationVectorSensor;

    private final ImageView needleView;
    private final ImageView compassView;
    private final Context context;
    private final Activity activity;

    private final FusedLocationProviderClient locationProviderClient;
    private final LocationRequest locationRequest;
    private final SettingsClient settingsClient;
    private final LocationSettingsRequest locationSettingsRequest;

    public NavigationHandler(Activity activity, FusedLocationProviderClient locationProviderClient, SensorManager sensorManager, ImageView needleView, ImageView compassView, ImageView targetView) {
        this.locationProviderClient = locationProviderClient;
        this.needleView = needleView;
        this.compassView = compassView;
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.sensorManager = sensorManager;
        this.accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        rotationListener = new CompassRotationListener(compass);
        sourceLocationCallback = new NeedleSourceLocationCallback(needle, this::checkSettings);
        compass.addObserver(new ViewRotationHandler(compassView, needleView, targetView, compass, needle));

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(Interval.LOCATION_UPDATE);
        locationRequest.setFastestInterval(Interval.FASTEST_LOCATION_UPDATE);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
        settingsClient = LocationServices.getSettingsClient(context);
    }


    @SuppressLint("MissingPermission")
    public boolean startNavigation() {
        if (running) return true;
        checkSettings();
        if (!Permissions.getDeniedPermissions(context, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).isEmpty()) {
            return false;
        }
        locationProviderClient.getLastLocation().onSuccessTask((res) -> {
            if (res != null) {
                if (needle.getSourceLatitude() == 0 && needle.getDestinationLongitude() == 0) {
                    needle.recalculateBearingFrom(res.getLatitude(), res.getLongitude());
                }
            }
            return null;
        });
        locationProviderClient.requestLocationUpdates(locationRequest, sourceLocationCallback, Looper.myLooper());
        startSensors();
        return true;
    }

    public void stopNavigation() {
        if (!running) return;
        running = false;
        locationProviderClient.removeLocationUpdates(sourceLocationCallback);
        stopSensors();
    }

    public NavNeedle getNeedle() {
        return needle;
    }

    public Compass getCompass() {
        return compass;
    }


    public void checkSettings() {
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnFailureListener((fail) -> {
            if (!(fail instanceof ApiException)) return;
            switch (((ApiException)fail).getStatusCode()) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    ResolvableApiException resolvable = (ResolvableApiException) fail;
                    try {
                        resolvable.startResolutionForResult(activity, RequestCode.GPS_REQUEST);
                    } catch (IntentSender.SendIntentException e) {
                        Toast.makeText(context, "Unable to get location! App may not function properly!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Toast.makeText(context, "Unable to request for location setting changes! App may not function properly!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startSensors() {
        if (rotationVectorSensor == null) {
            if (accelerometerSensor != null) {
                usingAccelerometer = sensorManager.registerListener(rotationListener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_UI);
            }
            if (magneticFieldSensor != null) {
                usingMagneticFieldSensor = sensorManager.registerListener(rotationListener, magneticFieldSensor, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_UI);
            }
        } else {
            usingRotationVectorSensor = sensorManager.registerListener(rotationListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_UI);
        }
        running = true;
    }

    private void stopSensors() {
        usingAccelerometer = false;
        usingRotationVectorSensor = false;
        usingMagneticFieldSensor = false;
        sensorManager.unregisterListener(rotationListener);
    }
}
