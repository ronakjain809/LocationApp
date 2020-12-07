package com.example.locationapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.locationapp.constant.RequestCode;
import com.example.locationapp.navigation.NavigationHandler;
import com.example.locationapp.util.Permissions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.Constants;
import com.sucho.placepicker.PlacePicker;

import java.util.List;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class MainActivity3 extends AppCompatActivity {
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private ImageView needleImage;
    private ImageView compassImage;
    private Button locationSearchBtn;
    private TextView curPickedPlace;

    private NavigationHandler navigationHandler;
    private FusedLocationProviderClient locationProviderClient;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        needleImage = findViewById(R.id.needle);
        compassImage = findViewById(R.id.compass);
        locationSearchBtn = findViewById(R.id.locationSearch);
        curPickedPlace = findViewById(R.id.curPickedPlace);



        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        navigationHandler = new NavigationHandler(this, locationProviderClient, sensorManager, needleImage, compassImage);

        if (!Permissions.getDeniedPermissions(getApplicationContext(), PERMISSIONS).isEmpty()) {
            Permissions.requestPermissions(this, RequestCode.PERMISSIONS, PERMISSIONS);
        } else {
            navigationHandler .startNavigation();
        }

        locationSearchBtn.setOnClickListener((view) -> {

            locationProviderClient.getLastLocation().onSuccessTask((bestLocation) -> {
                navigationHandler.checkSettings();
                PlacePicker.IntentBuilder placePickerBuilder = new PlacePicker.IntentBuilder();
                placePickerBuilder.setLatLong(bestLocation.getLatitude(), bestLocation.getLongitude());
                // placePickerBuilder.setPlaceSearchBar(true, BuildConfig.MAPS_API_KEY);
                Intent intent = placePickerBuilder.build(this);
                startActivityForResult(intent, RequestCode.PLACE_PICKER);
                return null;
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RequestCode.PERMISSIONS:
                if (permissions.length < PERMISSIONS.length) {
                    Toast.makeText(this, "Permissions not granted! App may not work properly", Toast.LENGTH_SHORT).show();
                } else {
                    navigationHandler.startNavigation();
                }
                break;
            default:
                break;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RequestCode.PLACE_PICKER:
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        Toast.makeText(getApplicationContext(), "Invalid result from map selection!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String name;
                    AddressData addressData = data.getParcelableExtra(Constants.ADDRESS_INTENT);
                    List<Address> addressList = addressData.getAddressList();
                    if (addressList == null || addressList.isEmpty()) {
                        name = String.format(Locale.getDefault(),"(%.3f,%.3f)", addressData.getLatitude(), addressData.getLongitude());
                    } else {
                        name = addressList.get(0).getAddressLine(0);
                    }
                    curPickedPlace.setText(name);
                    locationProviderClient.getLastLocation().onSuccessTask((curLocation) -> {
                        navigationHandler.getNeedle().setDestination(addressData.getLatitude(), addressData.getLongitude());
                        navigationHandler.getNeedle().recalculateBearingFrom(curLocation.getLatitude(), curLocation.getLongitude());
                        Toast.makeText(getApplicationContext(), "Now pointing to target location!", Toast.LENGTH_SHORT).show();
                        return null;
                    });
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationHandler.startNavigation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        navigationHandler.stopNavigation();
    }


}