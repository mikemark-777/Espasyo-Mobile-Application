package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.customdialogs.ConfirmPickedPropertyLocationDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback, ConfirmPickedPropertyLocationDialog.ConfirmLocationDialogListener {

    private FusedLocationProviderClient client;
    private SupportMapFragment mapFragment;

    private int LOCATION_PERMISSION_CODE = 1;
    private ConnectivityManager connectivityManager;
    private NetworkInfo  mobileConnection;
    private NetworkInfo wifiConnection;

    private GoogleMap gMap;
    private Geocoder geocoder;

    private double selectedLat, selectedLong;

    private List<Address> listOfAddresses;

    private String street, barangay, municipality, landmark;

    private SearchView locationSearchView;
    private ImageView btnBackToAddPropertyActivity;
    private Button btnGetPickedPropertyLocation;
    private FloatingActionButton FABChangeMapType, FABGetCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_location_picker);


        if(!isConnectedToInternet()) {
            showNoInternetConnectionDialog();
        }

        initializeViews();
        checkPermission();

        geocoder = new Geocoder(LocationPickerActivity.this, Locale.getDefault());
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(LocationPickerActivity.this);

        //search for location
        locationSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(ActivityCompat.checkSelfPermission(LocationPickerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if(isConnectedToInternet()) {
                        String location = locationSearchView.getQuery().toString();
                        listOfAddresses = null;

                        if (location != null || !location.equals("")) {
                            try {
                                listOfAddresses = geocoder.getFromLocationName(location, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            gMap.clear();

                            if(!listOfAddresses.isEmpty()) {
                                Address addressResult = listOfAddresses.get(0);
                                LatLng searchedLocation = new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
                                gMap.addMarker(new MarkerOptions().position(searchedLocation).title(location));
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, gMap.getMaxZoomLevel()));
                                btnGetPickedPropertyLocation.setEnabled(false);
                            } else {
                                Toast.makeText(LocationPickerActivity.this, "No Location Found. Please be more specific", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            showNoInternetConnectionDialog();
                        }
                    }
                } else {
                    requestLocationPermission();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        btnBackToAddPropertyActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                gMap = null;
                finish();
            }
        });

        FABChangeMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                    gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (gMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
                    gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else if (gMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
                    gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });

        //get current location automatically
        FABGetCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isConnectedToInternet()) {
                    getCurrentLocation();
                } else {
                    showNoInternetConnectionDialog();
                }

            }
        });

        btnGetPickedPropertyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmPickedPropertyLocationDialog();
            }
        });
    }

    public void initializeViews() {
        locationSearchView = findViewById(R.id.locationSearchView);

        btnBackToAddPropertyActivity = findViewById(R.id.btnBackToAddPropertyActivity);
        FABChangeMapType = findViewById(R.id.FABChangeMapType);
        FABGetCurrentLocation = findViewById(R.id.FABGetCurrentLocation);
        btnGetPickedPropertyLocation = findViewById(R.id.btnGetPickedPropertyLocation);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        gMap = googleMap;
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        setPolyLineOfMap(gMap);

        LatLng SaintMarysUniversity = new LatLng(16.483022, 121.155538);
        gMap.addMarker(new MarkerOptions().position(SaintMarysUniversity).title("Saint Mary's University")).showInfoWindow();
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SaintMarysUniversity, 16.0f));

        //get location via click
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
              if(ActivityCompat.checkSelfPermission(LocationPickerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                  if (isConnectedToInternet()) {
                      double latitude = latLng.latitude;
                      double longitude = latLng.longitude;

                      //resets/clears everytime user pick a location
                      gMap.clear();

                      //display the marker on the location where the user clicks
                      LatLng usersLocation = new LatLng(latitude, longitude);
                      MarkerOptions markerOptions = new MarkerOptions().position(usersLocation).title("Is this your property's location?");
                      gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(usersLocation, gMap.getMaxZoomLevel()));
                      gMap.addMarker(markerOptions).showInfoWindow();

                      getAddress(latitude, longitude);
                  } else {
                      showNoInternetConnectionDialog();
                  }
              } else {
                  requestLocationPermission();
              }
            }
        });

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

    public void getCurrentLocation() {

        //check if the user has granted the permission for this functionality to access location
        if (ActivityCompat.checkSelfPermission(LocationPickerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(isConnectedToInternet()) {
                Task<Location> task = client.getLastLocation();

                task.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            if (location != null) {

                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                gMap.clear();

                                LatLng usersLocation = new LatLng(latitude, longitude);
                                MarkerOptions markerOptions = new MarkerOptions().position(usersLocation).title("You are here");
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(usersLocation, gMap.getMaxZoomLevel()));
                                gMap.addMarker(markerOptions).showInfoWindow();

                                getAddress(latitude, longitude);


                            } else {
                                Toast.makeText(LocationPickerActivity.this, "Location is null", Toast.LENGTH_LONG).show();
                                //location of device is disabled
                                showEnableLocationInSettingsDialog();
                            }
                        } else {
                            Toast.makeText(LocationPickerActivity.this, "Task not successfull", Toast.LENGTH_LONG).show();
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

    //get the specific address of the specific coordinates of a place
    private void getAddress(double latitude, double longitude) {

        if (latitude != 0 && longitude != 0) {
            try {
                listOfAddresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (listOfAddresses != null) {
                //get the first address in the listOfAddress
                Address selectedAddress = listOfAddresses.get(0);

                if (selectedAddress != null) {

                    //set the address information from the selected address
                    String street = selectedAddress.getThoroughfare();
                    String municipality = selectedAddress.getLocality();

                    btnGetPickedPropertyLocation.setEnabled(true);
                    setConfirmedLocation(street, "", municipality, "", latitude, longitude);

                } else {
                    Toast.makeText(LocationPickerActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(LocationPickerActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(LocationPickerActivity.this, "LatLng Null", Toast.LENGTH_LONG).show();
        }

    }

    private void setConfirmedLocation(String pickedStreet, String pickedBarangay, String pickedMunicipality, String pickedLandmark, double pickedLatitude, double pickedLongitude) {

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

        Bundle args = new Bundle();
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

    /*------------------------- Check and Request Location Permissions ----------------------------------*/

    // check for permissions for location access
    public void checkPermission() {
        if(ContextCompat.checkSelfPermission(LocationPickerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //do nothing because the permissions are granted
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
                            ActivityCompat.requestPermissions(LocationPickerActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(LocationPickerActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(LocationPickerActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    /*------------------------- Check Internet Connections ----------------------------------*/

    //this will check the connection of the user (network connection)
    private boolean isConnectedToInternet() {
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        mobileConnection = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        wifiConnection = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(mobileConnection != null && mobileConnection.isConnected() || wifiConnection != null && wifiConnection.isConnected()) {
            return true;
        } else {
            return false;
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