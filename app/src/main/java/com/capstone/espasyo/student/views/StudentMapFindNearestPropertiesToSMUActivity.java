package com.capstone.espasyo.student.views;

import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.capstone.espasyo.MainActivity;
import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class StudentMapFindNearestPropertiesToSMUActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient client;
    private SupportMapFragment mapFragment;
    private GoogleMap gMap;
    private Geocoder geocoder;

    private ArrayList<Property> propertyList;

    private FloatingActionButton getCurrentLocationStudent_FindNearest, changeMapType_FindNearest;
    private CustomProgressDialog progressDialog;
    private ImageView btnBackToMainStudentMap;

    //final value of property location on map coordinates
    private final LatLng SaintMarysUniversity = new LatLng(16.483022, 121.155538);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_activity_map_find_nearest_properties_to_smu);
        geocoder = new Geocoder(StudentMapFindNearestPropertiesToSMUActivity.this, Locale.getDefault());
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragmentFindNearestPropertiesToSMU);
        mapFragment.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(StudentMapFindNearestPropertiesToSMUActivity.this);
        progressDialog = new CustomProgressDialog(this);

        getCurrentLocationStudent_FindNearest = findViewById(R.id.getCurrentLocationStudent_FindNearest) ;
        changeMapType_FindNearest = findViewById(R.id.changeMapType_FindNearest);
        btnBackToMainStudentMap = findViewById(R.id.btnBackToMainStudentMap);

        Intent intent = getIntent();
        propertyList = intent.getParcelableArrayListExtra("properties");

        getCurrentLocationStudent_FindNearest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        changeMapType_FindNearest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnBackToMainStudentMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        gMap.addMarker(new MarkerOptions().position(SaintMarysUniversity).title("Saint Mary's University").icon(BitmapDescriptorFactory.fromResource(R.drawable.img_university))).showInfoWindow();
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SaintMarysUniversity, 16.0f));
        drawCircumferenceOfSMU(gMap, SaintMarysUniversity);
        setPolyLineOfMap(gMap);

        displayPropertiesOnMap(propertyList);


    }

    public void drawCircumferenceOfSMU(GoogleMap gMap, LatLng location) {
        Circle nearest = gMap.addCircle(new CircleOptions()
                .center(location)
                .strokeWidth(0.7f)
                .radius(350)
                .zIndex(2)
                .fillColor(Color.parseColor("#2200FF00"))
                .strokeColor(Color.BLACK));

        Circle slightlyNear = gMap.addCircle(new CircleOptions()
                .center(location)
                .strokeWidth(0.7f)
                .radius(500)
                .zIndex(1)
                .fillColor(Color.parseColor("#22ff0000"))
                .strokeColor(Color.BLACK));
    }


    public void setPolyLineOfMap(GoogleMap gMap) {
        //to specify the area of main location
        gMap.addPolyline(new PolylineOptions().clickable(true).color(Color.BLACK).width(2.0f).add(
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

    public void displayPropertiesOnMap(ArrayList<Property> propertyList) {
        //this will render the property locations in the map
        for (Property property : propertyList) {
            String propertyName = property.getName();
            String propertyAddress = property.getAddress();
            double latitude = property.getLatitude();
            double longitude = property.getLongitude();

            LatLng propertyLocation = new LatLng(latitude, longitude);

            double distanceFromSMU = solveForDistance(propertyLocation);

            if(distanceFromSMU < 350.0) {
                //will make the pin blue if property coordinates is within the 350m radius Of nearest properties in SMU
                gMap.addMarker(new MarkerOptions()
                        .position(propertyLocation)
                        .title(propertyName)
                        .snippet(propertyAddress)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
            } else {
                //will make the pin blue if property coordinates is greater than the 350m radius of nearest properties in SMU
                gMap.addMarker(new MarkerOptions()
                        .position(propertyLocation)
                        .title(propertyName)
                        .snippet(propertyAddress)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))).showInfoWindow();
            }



            //add onclicklistener to all property location
            gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    progressDialog.showProgressDialog("Preparing property...", false);
                    Property clickedProperty = getPropertyClicked(marker.getTitle());

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog.isShowing()) {
                                Intent intent = new Intent(StudentMapFindNearestPropertiesToSMUActivity.this, StudentViewPropertyDetailsActivity.class);
                                intent.putExtra("property", clickedProperty);
                                startActivity(intent);
                                progressDialog.dismissProgressDialog();
                            }
                        }
                    }, 1500);
                    return false;
                }
            });
        }
    }

    public Property getPropertyClicked(String clickedPropertyName) {

        Property pickedProperty = null;
        for (Property property : propertyList) {
            if (property.getName().equals(clickedPropertyName)) {
                pickedProperty = property;
            }
        }
        return pickedProperty;
    }


    public double solveForDistance(LatLng propertyCoordinate) {
        double R = 6378.137; // Radius of earth in KM
        double dLat = propertyCoordinate.latitude * Math.PI / 180 - SaintMarysUniversity.latitude * Math.PI / 180;
        double dLon = propertyCoordinate.longitude * Math.PI / 180 - SaintMarysUniversity.longitude * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(SaintMarysUniversity.latitude * Math.PI / 180) * Math.cos(propertyCoordinate.latitude * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d * 1000; // meters
    }

}
