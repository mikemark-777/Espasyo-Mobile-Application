package com.capstone.espasyo.student.views;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.capstone.espasyo.R;
import com.capstone.espasyo.connectivityUtil.InternetConnectionUtil;
import com.capstone.espasyo.landlord.views.LocationPickerActivity;
import com.capstone.espasyo.models.Property;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class StudentViewPropertyOnMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private SupportMapFragment mapFragment;

    private int LOCATION_PERMISSION_CODE = 1;
    private InternetConnectionUtil internetChecker;
    private ConnectivityManager connectivityManager;
    private FusedLocationProviderClient client;

    private GoogleMap gMap;

    private Property chosenProperty;

    private String propertyName;
    private String propertyAddress;
    private double propertyLatitude,
            propertyLongitude;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_activity_view_property_on_map);

        Intent intent = getIntent();
        getPropertyDataFromIntent(intent);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment_viewPropertyOnMap);
        mapFragment.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(StudentViewPropertyOnMapActivity.this);
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        internetChecker = new InternetConnectionUtil(connectivityManager);
    }

    public void getPropertyDataFromIntent(Intent intent) {
        chosenProperty = intent.getParcelableExtra("chosenProperty");
        propertyName = chosenProperty.getName();
        propertyAddress = chosenProperty.getAddress();
        propertyLatitude = chosenProperty.getLatitude();
        propertyLongitude = chosenProperty.getLongitude();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng propertyLocation = new LatLng(propertyLatitude, propertyLongitude);
        gMap.addMarker(new MarkerOptions().position(propertyLocation)
                .title(propertyName)
                .snippet(propertyAddress)).showInfoWindow();
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(propertyLocation, gMap.getMaxZoomLevel()));
        getCurrentLocation();
    }

    public void getCurrentLocation() {

        //check if the user has granted the permission for this functionality to access location
        if (ActivityCompat.checkSelfPermission(StudentViewPropertyOnMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(internetChecker.isConnectedToInternet()) {
                Task<Location> task = client.getLastLocation();

                task.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            if (location != null) {

                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                LatLng usersLocation = new LatLng(latitude, longitude);
                                MarkerOptions markerOptions = new MarkerOptions().position(usersLocation).title("You are here");
                                gMap.addMarker(markerOptions).showInfoWindow();

                            } else {
                                Toast.makeText(StudentViewPropertyOnMapActivity.this, "Location is null", Toast.LENGTH_LONG).show();
                                //location of device is disabled
                                showEnableLocationInSettingsDialog();
                            }
                        } else {
                            Toast.makeText(StudentViewPropertyOnMapActivity.this, "Task not successfull", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                showNoInternetConnectionDialog();
            }
        } else {
            requestLocationPermission();
        }
    }

    public void requestLocationPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Location permission is needed to access your location.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(StudentViewPropertyOnMapActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();

        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    public void showNoInternetConnectionDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.landlord_no_internet_connection_dialog, null);

        Button btnOkayInternetConnection = view.findViewById(R.id.btnOkayInternetConnection);

        AlertDialog noInternetDialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnOkayInternetConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noInternetDialog.dismiss();
            }
        });

        noInternetDialog.show();
    }

    public void showEnableLocationInSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Use location?")
                .setMessage("To continue, you need to turn on location in your device.")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        enableLocationInSettings();
                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    public void enableLocationInSettings() {
        Intent openLocationInSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(openLocationInSettingsIntent);
    }
}