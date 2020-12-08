package com.example.locationapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;


public class MainActivity2 extends AppCompatActivity {
    private static final int LOCATION_UPDATE_INTERVAL = 15 * 10000;
    private static final int FASTEST_LOCATION_UPDATE_INTERVAL = 10 * 10000;
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int PERMISSION_REQUEST = 100;
    private static final int GPS_REQUEST = 101;
    private FusedLocationProviderClient locationProviderClient;
    private LocationRequest locationRequest;
    private SunData data;
    private ProgressBar progressBar;
    private LocationCallback sunDataUpdater;
    private SettingsClient settingsClient;
    private LocationSettingsRequest locationSettingsRequest;
    private DateTimeFormatter formatter;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        data = new SunData();
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        sunDataUpdater = new SunDataUpdater(data, this::checkSettings);
        settingsClient = LocationServices.getSettingsClient(getApplicationContext());
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_LOCATION_UPDATE_INTERVAL);
        locationRequest.setSmallestDisplacement(1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();

        TextView sunriseText = findViewById(R.id.sunrise);
        TextView sunAtTopText = findViewById(R.id.sunattop);
        TextView sunsetText = findViewById(R.id.sunset);

        data.addObserver((observable, obj) -> {
            sunriseText.setText(data.getSunrise().format(formatter));
            sunAtTopText.setText(data.getNoon().format(formatter));
            sunsetText.setText(data.getSunset().format(formatter));
        });
        checkSettings();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST);
            return;
        }

        requestSunData();
    }

    public void requestSunData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationProviderClient.requestLocationUpdates(locationRequest, sunDataUpdater, Looper.myLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationProviderClient.removeLocationUpdates(sunDataUpdater);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestSunData();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST:
                if (Arrays.stream(grantResults).allMatch((a) -> a>=0)) {
                    requestSunData();
                } else {
                    Toast.makeText(getApplicationContext(), "Permissions not provided, App may not work properly!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void checkSettings() {
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnFailureListener((fail) -> {
            if (!(fail instanceof ApiException)) return;
            switch (((ApiException)fail).getStatusCode()) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    ResolvableApiException resolvable = (ResolvableApiException) fail;
                    try {
                        resolvable.startResolutionForResult(this, GPS_REQUEST);
                    } catch (IntentSender.SendIntentException e) {
                        Toast.makeText(getApplicationContext(), "Unable to get location! App may not function properly!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Toast.makeText(getApplicationContext(), "Unable to request for location setting changes! App may not function properly!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}





