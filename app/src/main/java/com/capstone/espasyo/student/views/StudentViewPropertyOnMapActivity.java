package com.capstone.espasyo.student.views;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.Property;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class StudentViewPropertyOnMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private SupportMapFragment mapFragment;

    private int LOCATION_PERMISSION_CODE = 1;
    private ConnectivityManager connectivityManager;
    private NetworkInfo mobileConnection;
    private NetworkInfo wifiConnection;

    private GoogleMap gMap;

    private Property chosenProperty;

    private String propertyName;
    private String propertyAddress;
    private double propertyLatitude,
            propertyLongitude;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_view_property_on_map);

        Intent intent = getIntent();
        getPropertyDataFromIntent(intent);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment_viewPropertyOnMap);
        mapFragment.getMapAsync(this);
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
    }
}