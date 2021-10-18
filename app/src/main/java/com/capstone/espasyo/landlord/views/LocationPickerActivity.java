package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.customdialogs.ConfirmPickedPropertyLocationDialog;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback, ConfirmPickedPropertyLocationDialog.ConfirmLocationDialogListener {

    private FusedLocationProviderClient client;
    private SupportMapFragment mapFragment;
    private SearchView barangaySearchView;
    private int REQUEST_CODE = 111;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    private GoogleMap gMap;
    private Geocoder geocoder;


    private double selectedLat,
                   selectedLong;

    private List<Address> addresses;
    private String selectedAddress;
    private String houseNumber,
                   street,
                   barangay,
                   municipality,
                   landmark;

    private EditText textInputLocation_street,
            textInputLocation_barangay,
            textInputLocation_municipality,
            textInputLocation_landmark;


    private Button btnGetPickedPropertyLocation;
    private FloatingActionButton
                    FABChangeMapType,
                    FABGetCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_location_picker);

        barangaySearchView = findViewById(R.id.locationSearchView_edit);

        textInputLocation_street = findViewById(R.id.text_input_location_street);
        textInputLocation_barangay = findViewById(R.id.text_input_location_barangay);
        textInputLocation_municipality = findViewById(R.id.text_input_location_municipality);
        textInputLocation_landmark = findViewById(R.id.text_input_location_landmark);

        FABChangeMapType = findViewById(R.id.FABChangeMapType);
        FABGetCurrentLocation = findViewById(R.id.FABGetCurrentLocation);
        btnGetPickedPropertyLocation = findViewById(R.id.btnGetPickedPropertyLocation);


        geocoder = new Geocoder(LocationPickerActivity.this, Locale.getDefault());
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(LocationPickerActivity.this);

        //search for location
        barangaySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = barangaySearchView.getQuery().toString();
                addresses = null;

                if (location != null || !location.equals("")) {
                    try {
                        addresses = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Address address = addresses.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    gMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, gMap.getMaxZoomLevel()));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        FABChangeMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                    gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if(gMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
                    gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });

        //get current location automatically
        FABGetCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
            }
        });

        btnGetPickedPropertyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmPickedPropertyLocationDialog();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //getCurrentLocation();
            }
        } else {
            Toast.makeText(LocationPickerActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        gMap = googleMap;
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng BayombongDefault = new LatLng(16.4845001, 121.1563895);
        gMap.addMarker(new MarkerOptions().position(BayombongDefault).title("Bayombong")).showInfoWindow();
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BayombongDefault, 16.0f));

        //get location via click
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

                checkConnection();

                if (networkInfo.isAvailable() && networkInfo.isConnected()) {
                    double latitude = latLng.latitude;
                    double longitude = latLng.longitude;

                    //resets/clears everytime user pick a location
                    gMap.clear();
                    getAddress(latitude, longitude);
                } else {
                    Toast.makeText(LocationPickerActivity.this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    public void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(LocationPickerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LocationPickerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {

        }

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
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(usersLocation, gMap.getMaxZoomLevel()));
                        gMap.addMarker(markerOptions).showInfoWindow();
                        getAddress(latitude, longitude);


                    } else {
                        Toast.makeText(LocationPickerActivity.this, "Location Null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LocationPickerActivity.this, "Task not successfull", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //get the specific address of the specific coordinates of a place
    private void getAddress(double latitude, double longitude) {

        if (latitude != 0) {
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null) {
                String address = addresses.get(0).getAddressLine(0);

                //set the location information
                String street = addresses.get(0).getThoroughfare();
                String municipality = addresses.get(0).getLocality();
                String postalCode = addresses.get(0).getPremises();

                // setLatitudeLongitude(latitude, longitude);
                selectedAddress = street + ", " + municipality;

                setLatitudeLongitude(latitude, longitude);
                getConfirmedLocation( street, "", municipality, "", selectedLat, selectedLong);


                if (address != null) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(latitude, longitude);
                    markerOptions.position(latLng).title(postalCode);
                    gMap.addMarker(markerOptions).showInfoWindow();
                    btnGetPickedPropertyLocation.setEnabled(true);
                    Toast.makeText(LocationPickerActivity.this, "Location: " + street + " ," + municipality, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LocationPickerActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LocationPickerActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(LocationPickerActivity.this, "LatLng Null", Toast.LENGTH_SHORT).show();
        }

    }

    private void checkConnection() {
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
    }

    //must delete
    private void setLatitudeLongitude(double latitude, double longitude) {
        selectedLat = latitude;
        selectedLong = longitude;
    }

    private void getConfirmedLocation(String pickedStreet, String pickedBarangay, String pickedMunicipality, String pickedLandmark, double pickedLatitude, double pickedLongitude) {

        //location information
        street = pickedStreet;
        barangay = pickedBarangay;
        municipality = pickedMunicipality;
        landmark = pickedLandmark;

        //map coordinates
        selectedLat = pickedLatitude;
        selectedLong = pickedLongitude;

    }

    public void showConfirmPickedPropertyLocationDialog() {
        //TODO: Must have Input validations
        //TODO: Must check if user has set the latitude and longitude, if not, inform
        Bundle args = new Bundle();
        args.putString("houseNumber", houseNumber);
        args.putString("street", street);
        args.putString("barangay", barangay);
        args.putString("municipality", municipality);
        args.putString("landmark", landmark);
        args.putDouble("latitude", selectedLat);
        args.putDouble("longitude", selectedLong);

        //create an instance of the custom confirm dialog
        ConfirmPickedPropertyLocationDialog confirmLocationDialog = new ConfirmPickedPropertyLocationDialog();
        confirmLocationDialog.setArguments(args);
        confirmLocationDialog.show(getSupportFragmentManager(), "confirmLocationDialog");
    }


    @Override
    public void getConfirmedLocationData(String street, String barangay, String municipality, String landmark, double latitude, double longitude) {
        Intent intent = new Intent();
        intent.putExtra("street", street);
        intent.putExtra("barangay", barangay);
        intent.putExtra("municipality", municipality);
        intent.putExtra("landmark", landmark);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        setResult(RESULT_OK, intent);
        gMap = null;
        finish();
    }

    @Override
    public void changeLocationData() {
        //will reset all the map data, picked location data and other data
        gMap.clear();
        LatLng BayombongDefault = new LatLng(16.4845001, 121.1563895);
        gMap.addMarker(new MarkerOptions().position(BayombongDefault).title("Bayombong")).showInfoWindow();
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(BayombongDefault, 16.0f));
        btnGetPickedPropertyLocation.setEnabled(false);
    }

}