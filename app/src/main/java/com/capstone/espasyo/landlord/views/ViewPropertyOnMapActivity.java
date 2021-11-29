package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.connectivityUtil.InternetConnectionUtil;
import com.capstone.espasyo.models.Property;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ViewPropertyOnMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private SupportMapFragment mapFragment;

    private int LOCATION_PERMISSION_CODE = 1;
    private InternetConnectionUtil internetChecker;
    private ConnectivityManager connectivityManager;

    private FloatingActionButton changeMapType;

    private GoogleMap gMap;

    private Property chosenProperty;
    private String propertyName;
    private String propertyAddress;
    private double propertyLatitude, propertyLongitude;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_view_property_on_map);

        Intent intent = getIntent();
        getPropertyDataFromIntent(intent);
        checkPermission();

        changeMapType = findViewById(R.id.changeMapType_propertyOnMap);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment_viewPropertyOnMap);
        mapFragment.getMapAsync(this);

        changeMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMapType();
            }
        });
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
        setPolyLineOfMap(gMap);

        LatLng previousLocation = new LatLng(propertyLatitude, propertyLongitude);
        gMap.addMarker(new MarkerOptions().position(previousLocation)
                .title(propertyName)
                .snippet(propertyAddress)).showInfoWindow();
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(previousLocation, gMap.getMaxZoomLevel()));
    }

    public void setPolyLineOfMap(GoogleMap gMap) {
        //to specify the area of main location
        gMap.addPolyline(new PolylineOptions().clickable(true).color(Color.LTGRAY).add(
                new LatLng(16.4814312, 121.1542103),
                new LatLng(16.4826409, 121.1572306),
                new LatLng(16.4834756, 121.1572032),
                new LatLng(16.4843089, 121.15693),
                new LatLng(16.4845001, 121.1563895),
                new LatLng(16.4848, 121.1561731),
                new LatLng(16.4845163, 121.155666),
                new LatLng(16.4847609, 121.1552218),
                new LatLng(16.4838617, 121.1541471),
                new LatLng(16.4826408, 121.1531345),
                new LatLng(16.4814312, 121.1542103)
        ));
    }

    public void toggleMapType() {
        if (gMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (gMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (gMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
            gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }


    /*------------------------- Check and Request Location Permissions ----------------------------------*/

    // check for permissions for location access
    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(ViewPropertyOnMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //do nothing because the permissions are granted
        } else {
            requestLocationPermission();
        }
    }

    public void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Location permission is needed to access your location.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ViewPropertyOnMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(ViewPropertyOnMapActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ViewPropertyOnMapActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }
}