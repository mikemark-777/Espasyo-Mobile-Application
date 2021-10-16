package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity {

    private FusedLocationProviderClient client;
    private SupportMapFragment mapFragment;
    private int REQUEST_CODE = 111;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    private GoogleMap gMap;
    private Geocoder geocoder;
    private double selectedLat, selectedLong;
    private List<Address> addresses;
    private String selectedAddress;


    private Button btnCancelPickPropertyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_location_picker);

        btnCancelPickPropertyLocation = findViewById(R.id.btnCancelPickPropertyLocation);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        client = LocationServices.getFusedLocationProviderClient(LocationPickerActivity.this);

        if (ActivityCompat.checkSelfPermission(LocationPickerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(LocationPickerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

        btnCancelPickPropertyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gMap = null;
                finish();
            }
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(LocationPickerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LocationPickerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

        Task<Location> task = client.getLastLocation();

        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {

                            gMap = googleMap;
                            //gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

                            double sampleLat = 16.4845001;
                            double sampleLong = 121.1563895;

                            LatLng BayombongDefault = new LatLng(16.4845001, 121.1563895);

                            /*double latitude = location.getLatitude();
                            double longitude = location.getLatitude();*/

                            double latitude = sampleLat;
                            double longitude = sampleLong;

                            LatLng latLng = new LatLng(latitude, longitude);
                            MarkerOptions markerOptions = new MarkerOptions().position(BayombongDefault).title("You are here");
                            //googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                            gMap.addMarker(markerOptions);

                            gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(@NonNull LatLng latLng) {
                                    checkConnection();

                                    if(networkInfo.isConnected() && networkInfo.isAvailable()) {
                                        selectedLat = latLng.latitude;
                                        selectedLong = latLng.longitude;
                                        getAddress(selectedLat, selectedLong);

                                        Toast.makeText(LocationPickerActivity.this, "Location: " + selectedAddress, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LocationPickerActivity.this, "Please Check Connection", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        } else {
            Toast.makeText(LocationPickerActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkConnection() {
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
    }

    private void getAddress(double latitude, double longitude) {
        geocoder = new Geocoder(LocationPickerActivity.this, Locale.getDefault());

        if(latitude!= 0) {
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(addresses != null) {
                String address = addresses.get(0).getAddressLine(0);

                //set the location information
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();

                selectedAddress = city + ", " + state + ", " + country + ", " + postalCode + ", " + knownName;

                if(address != null) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(latitude, longitude);
                    markerOptions.position(latLng).title(selectedAddress);
                    gMap.addMarker(markerOptions).showInfoWindow();
                } else {
                    Toast.makeText(LocationPickerActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LocationPickerActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(LocationPickerActivity.this, "LatLng Null", Toast.LENGTH_SHORT).show();
        }

    }
}