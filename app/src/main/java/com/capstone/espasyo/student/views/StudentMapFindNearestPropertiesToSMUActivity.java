package com.capstone.espasyo.student.views;

import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.Property;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Locale;

public class StudentMapFindNearestPropertiesToSMUActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient client;
    private SupportMapFragment mapFragment;
    private GoogleMap gMap;
    private Geocoder geocoder;

    private ArrayList<Property> propertyList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_activity_map_find_nearest_properties_to_smu);
        geocoder = new Geocoder(StudentMapFindNearestPropertiesToSMUActivity.this, Locale.getDefault());
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragmentFindNearestPropertiesToSMU);
        mapFragment.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(StudentMapFindNearestPropertiesToSMUActivity.this);

        Intent intent = getIntent();
        propertyList = intent.getParcelableArrayListExtra("properties");

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng SaintMarysUniversity = new LatLng(16.483022, 121.155538);
        gMap.addMarker(new MarkerOptions().position(SaintMarysUniversity).title("Saint Mary's University").icon(BitmapDescriptorFactory.fromResource(R.drawable.img_university))).showInfoWindow();
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SaintMarysUniversity, 16.0f));
        drawCircumferenceOfSMU(gMap, SaintMarysUniversity);

        displayPropertiesOnMap(propertyList);
    }

    public void drawCircumferenceOfSMU(GoogleMap gMap, LatLng location) {
        Circle nearest = gMap.addCircle(new CircleOptions()
                .center(location)
                .strokeWidth(0.7f)
                .radius(350)
                .fillColor(Color.parseColor("#2200FF00"))
                .strokeColor(Color.BLACK));
    }

    public void displayPropertiesOnMap(ArrayList<Property> propertyList) {
        for (Property property : propertyList) {
            String propertyName = property.getName();
            String propertyAddress = property.getAddress();
            double latitude = property.getLatitude();
            double longitude = property.getLongitude();

            LatLng propertyLocation = new LatLng(latitude, longitude);
            gMap.addMarker(new MarkerOptions()
                    .position(propertyLocation)
                    .title(propertyName)
                    .snippet(propertyAddress + "\n300 meters")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))).showInfoWindow();
        }
    }
}
