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

public class EditLocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback, ConfirmPickedPropertyLocationDialog.ConfirmLocationDialogListener {

    private FusedLocationProviderClient client;
    private SupportMapFragment mapFragment_edit;

    private int LOCATION_PERMISSION_CODE = 1;
    private ConnectivityManager connectivityManager;
    private NetworkInfo mobileConnection;
    private NetworkInfo wifiConnection;

    private GoogleMap gMapEdit;
    private Geocoder geocoder;

    private double selectedLat,
            selectedLong;

    private List<Address> listOfAddresses;

    private String street,
            barangay,
            municipality,
            landmark;

    private String propertyName;
    private String address;

    private SearchView locationSearchView_edit;
    private ImageView btnBackToEditPropertyActivity;
    private Button btnGetPickedPropertyLocation_edit;
    private FloatingActionButton
            FABChangeMapType_edit,
            FABGetCurrentLocation_edit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_edit_location_picker);

        if(!isConnectedToInternet()) {
            showNoInternetConnectionDialog();
        }

        initializeViews();
        checkPermission();

        Intent intent = getIntent();
        getDataFromIntent(intent);

        geocoder = new Geocoder(EditLocationPickerActivity.this, Locale.getDefault());
        mapFragment_edit = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment_edit);
        mapFragment_edit.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(EditLocationPickerActivity.this);

        //search for location
        locationSearchView_edit.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if(ActivityCompat.checkSelfPermission(EditLocationPickerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if(isConnectedToInternet()) {
                        String location = locationSearchView_edit.getQuery().toString();
                        listOfAddresses = null;

                        if (location != null || !location.equals("")) {
                            try {
                                listOfAddresses = geocoder.getFromLocationName(location, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if(!listOfAddresses.isEmpty()) {
                                Address addressResult = listOfAddresses.get(0);
                                LatLng searchedLocation = new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
                                gMapEdit.addMarker(new MarkerOptions().position(searchedLocation).title(location));
                                gMapEdit.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, gMapEdit.getMaxZoomLevel()));
                            } else {
                                //TODO: Must create a dialog saying no address found
                                Toast.makeText(EditLocationPickerActivity.this, "No Location Found. Please be more specific", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        showNoInternetConnectionDialog();
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

        btnBackToEditPropertyActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                gMapEdit = null;
                finish();
            }
        });

        FABChangeMapType_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gMapEdit.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                    gMapEdit.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (gMapEdit.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
                    gMapEdit.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });

        //get current location automatically
        FABGetCurrentLocation_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnectedToInternet()) {
                    getCurrentLocation();
                } else {
                    showNoInternetConnectionDialog();
                }

            }
        });

        btnGetPickedPropertyLocation_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmPickedPropertyLocationDialog();
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMapEdit = googleMap;

        gMapEdit.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng previousLocation = new LatLng(selectedLat, selectedLong);
        gMapEdit.addMarker(new MarkerOptions().position(previousLocation)
                .title(propertyName)
                .snippet(address)).showInfoWindow();
        gMapEdit.moveCamera(CameraUpdateFactory.newLatLngZoom(previousLocation, gMapEdit.getMaxZoomLevel()));

        gMapEdit.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
             if(ActivityCompat.checkSelfPermission(EditLocationPickerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                 if (isConnectedToInternet()) {
                     double latitude = latLng.latitude;
                     double longitude = latLng.longitude;

                     //resets/clears everytime user pick a location
                     gMapEdit.clear();

                     //display the marker on the location where the user clicks
                     LatLng usersLocation = new LatLng(latitude, longitude);
                     MarkerOptions markerOptions = new MarkerOptions().position(usersLocation).title("Is this your property's location?");
                     gMapEdit.animateCamera(CameraUpdateFactory.newLatLngZoom(usersLocation, gMapEdit.getMaxZoomLevel()));
                     gMapEdit.addMarker(markerOptions).showInfoWindow();

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

    //this will initialize all views : searchViews, buttons, imageViews and floating action buttons
    public void initializeViews() {
        locationSearchView_edit = findViewById(R.id.locationSearchView_edit);

        btnBackToEditPropertyActivity = findViewById(R.id.btnBackToEditPropertyActivity);
        FABChangeMapType_edit = findViewById(R.id.FABChangeMapType_edit);
        FABGetCurrentLocation_edit = findViewById(R.id.FABGetCurrentLocation_edit);
        btnGetPickedPropertyLocation_edit = findViewById(R.id.btnGetPickedPropertyLocation_edit);
    }

    //this will get the data from the intent passed by the edit property activity
    public void getDataFromIntent(Intent intent) {

        propertyName = intent.getStringExtra("propertyName");
        address = intent.getStringExtra("address");
        selectedLat = intent.getDoubleExtra("latitude", 0);
        selectedLong = intent.getDoubleExtra("longitude", 0);
    }

    //this will get the current location of the user (not always precise)
    public void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(EditLocationPickerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(isConnectedToInternet()) {
                //will get the location of the user
                Task<Location> task = client.getLastLocation();

                task.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            if (location != null) {

                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                //display the marker on the users current location
                                LatLng usersLocation = new LatLng(latitude, longitude);
                                MarkerOptions markerOptions = new MarkerOptions().position(usersLocation).title("You are here!");
                                gMapEdit.animateCamera(CameraUpdateFactory.newLatLngZoom(usersLocation, gMapEdit.getMaxZoomLevel()));
                                gMapEdit.addMarker(markerOptions).showInfoWindow();

                                getAddress(latitude, longitude);

                            } else {
                                Toast.makeText(EditLocationPickerActivity.this, "Location is null", Toast.LENGTH_SHORT).show();
                                showEnableLocationInSettingsDialog();
                            }
                        } else {
                            Toast.makeText(EditLocationPickerActivity.this, "Task not successful", Toast.LENGTH_SHORT).show();
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

                    btnGetPickedPropertyLocation_edit.setEnabled(true);
                    setConfirmedLocation(street, "", municipality, "", latitude, longitude);

                } else {
                    Toast.makeText(EditLocationPickerActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(EditLocationPickerActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(EditLocationPickerActivity.this, "Coordinates are null", Toast.LENGTH_LONG).show();
        }

    }

    //this will set the global variables of the address and location based on the picked location
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

    //will show the the dialog confirming the picked location of the user
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


    //a callback from a interface in the ConfirmPickedPropertyLocationDialog that will provide the data (from ConfirmPickedPropertyLocationDialog)
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
        gMapEdit = null;
        finish();
    }
    //a callback from a interface in the ConfirmPickedPropertyLocationDialog that will clear the selected location for the user to pick another location
    @Override
    public void changeLocationData() {
        //will reset all the map data, picked location data and other data
        gMapEdit.clear();
        LatLng currentLocation = new LatLng(selectedLat, selectedLong);
        gMapEdit.addMarker(new MarkerOptions().position(currentLocation)
                .title(propertyName)
                .snippet(address)).showInfoWindow();
        gMapEdit.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16.0f));
        btnGetPickedPropertyLocation_edit.setEnabled(false);
    }

    /*------------------------- Check and Request Permissions ----------------------------------*/

    // check for permissions for location access
    public void checkPermission() {
        if(ContextCompat.checkSelfPermission(EditLocationPickerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                            ActivityCompat.requestPermissions(EditLocationPickerActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
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
                Toast.makeText(EditLocationPickerActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(EditLocationPickerActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
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

    public void enableLocationInSettings() {
        Intent openLocationInSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(openLocationInSettingsIntent);
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

}