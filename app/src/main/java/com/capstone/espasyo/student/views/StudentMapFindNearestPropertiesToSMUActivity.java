package com.capstone.espasyo.student.views;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class StudentMapFindNearestPropertiesToSMUActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient client;
    private SupportMapFragment mapFragment;
    private GoogleMap gMap;
    private Geocoder geocoder;

    private int LOCATION_PERMISSION_CODE = 1;
    private ConnectivityManager connectivityManager;
    private NetworkInfo mobileConnection;
    private NetworkInfo wifiConnection;

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

        changeMapType_FindNearest = findViewById(R.id.changeMapType_FindNearest);
        getCurrentLocationStudent_FindNearest = findViewById(R.id.getCurrentLocationStudent_FindNearest);
        btnBackToMainStudentMap = findViewById(R.id.btnBackToMainStudentMap);

        Intent intent = getIntent();
        propertyList = intent.getParcelableArrayListExtra("properties");

        getCurrentLocationStudent_FindNearest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        changeMapType_FindNearest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMapType();
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
                .strokeWidth(0.0f)
                .radius(500)
                .zIndex(1)
                .fillColor(Color.parseColor("#22ff0000")));
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

                    if(clickedProperty == null) {
                        Toast.makeText(StudentMapFindNearestPropertiesToSMUActivity.this, "This is not a property", Toast.LENGTH_SHORT).show();
                        progressDialog.dismissProgressDialog();
                    } else {
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
                    }
                    return false;
                }
            });
        }
    }

    public void getCurrentLocation() {
        //check if the user has granted the permission for this functionality to access location
        if (ActivityCompat.checkSelfPermission(StudentMapFindNearestPropertiesToSMUActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (isConnectedToInternet()) {
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
                                MarkerOptions markerOptions = new MarkerOptions().position(usersLocation).title("You are here").icon(BitmapDescriptorFactory.fromResource(R.drawable.img_walking_person));
                                gMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(usersLocation, 16.0f, 0, 0)));
                                gMap.addMarker(markerOptions).showInfoWindow();

                            } else {
                                //location of device is disabled
                                showEnableLocationInSettingsDialog();
                            }
                        } else {
                            Toast.makeText(StudentMapFindNearestPropertiesToSMUActivity.this, "Task not successfull", Toast.LENGTH_LONG).show();
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

    public void toggleMapType() {
        if (gMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (gMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (gMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
            gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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

    //====== check internet connections =============

    //this will check the connection of the user (network connection)
    private boolean isConnectedToInternet() {
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        mobileConnection = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        wifiConnection = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mobileConnection != null && mobileConnection.isConnected() || wifiConnection != null && wifiConnection.isConnected()) {
            return true;
        } else {
            return false;
        }

    }

    public void showNoInternetConnectionDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.no_internet_connection_dialog, null);

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

    public void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Location permission is needed to access your location.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(StudentMapFindNearestPropertiesToSMUActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
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

    public void enableLocationInSettings() {
        Intent openLocationInSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(openLocationInSettingsIntent);
    }

}
