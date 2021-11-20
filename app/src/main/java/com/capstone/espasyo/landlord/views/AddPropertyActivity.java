package com.capstone.espasyo.landlord.views;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.LandlordMainActivity;
import com.capstone.espasyo.models.Property;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddPropertyActivity extends AppCompatActivity {

    //TODO: get fAuth and database instance in FirebaseConnection
    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();
    private DocumentReference propertiesDocumentReference;

    private TextInputLayout textInputPropertyNameLayout,
            textInputPropertyTypeLayout,
            textInputCompleteAddressLayout,
            textInputProprietorNameLayout,
            textInputMinimumPriceLayout,
            textInputMaximumPriceLayout;

    private TextInputEditText textInputPropertyName,
            textInputCompleteAddress,
            textInputProprietorName;

    private AutoCompleteTextView textInputPropertyType,
            textInputMinimumPrice,
            textInputMaximumPrice;

    private CheckBox electricityCheckBox,
            waterCheckBox,
            internetCheckBox,
            garbageCheckBox;

    private boolean isElectricityIncluded,
            isWaterIncluded,
            isInternetIncluded,
            isGarbageCollectionIncluded;

    private String completeAddress;
    private double latitude, longitude;

    private ActivityResultLauncher<Intent> LocationPickerActivityResultLauncher;

    List<String> rentInclusions = new ArrayList<>();

    String[] propertyType = {"Apartment", "Boarding House", "Dormitory"};
    String[] minimumPrices = {"500", "1000", "1500", "2000", "2500", "3000", "3500", "4000", "4500", "5000", "5500", "6000", "6500", "7000", "7500", "8000"};
    String[] maximumPrices = {"500", "1000", "1500", "2000", "2500", "3000", "3500", "4000", "4500", "5000", "5500", "6000", "6500", "7000", "7500", "8000"};
    ArrayAdapter<String> propertyTypeAdapter;
    ArrayAdapter<String> minimumPriceAdapter;
    ArrayAdapter<String> maximumPriceAdapter;

    private Button btnGetMapLocation,
            btnAddProperty,
            btnCancelAddProperty;//TODO: add cancel functionality

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_add_property);

        initializeViews();

        //will handle all the data from the LocationPickerActivity
        LocationPickerActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            String street = data.getStringExtra("street");
                            String barangay = data.getStringExtra("barangay");
                            String municipality = data.getStringExtra("municipality");
                            String landmark = data.getStringExtra("landmark");
                            latitude = data.getDoubleExtra("latitude", 0);
                            longitude = data.getDoubleExtra("longitude", 0);

                            //will enable the textBox since it has captured its location and get the latitude and longitude
                            textInputCompleteAddress.setEnabled(true);
                            textInputCompleteAddressLayout.setEnabled(true);
                            completeAddress = formatStringLocation(street, barangay, municipality, landmark);
                            Toast.makeText(AddPropertyActivity.this, "Location Picked: " + completeAddress, Toast.LENGTH_SHORT).show();
                            textInputCompleteAddress.setText(completeAddress);
                        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            Toast.makeText(AddPropertyActivity.this, "Location and address not set", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        btnGetMapLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationPickerActivityForResult();
            }
        });

        btnAddProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String propertyName = textInputPropertyName.getText().toString().trim();
                String propertyType = textInputPropertyType.getText().toString().trim();
                String completeAddress = textInputCompleteAddress.getText().toString().trim();
                String proprietorName = textInputProprietorName.getText().toString().trim();
                String minPrice = textInputMinimumPrice.getText().toString().trim();
                String maxPrice = textInputMaximumPrice.getText().toString().trim();


                if (areInputsValid(propertyName, propertyType, completeAddress, proprietorName, minPrice, maxPrice)) {

                    int minimumPrice = Integer.parseInt(minPrice);
                    int maximumPrice = Integer.parseInt(maxPrice);

                    String newPropertyID = UUID.randomUUID().toString();
                    String propertyOwner = fAuth.getCurrentUser().getUid().toString();

                    getRentInclusions();

                    // CREATE SAMPLE PROPERTY OBJECT
                    Property newProperty = new Property();
                    newProperty.setPropertyID(newPropertyID);
                    newProperty.setOwner(propertyOwner);
                    newProperty.setLatitude(latitude);
                    newProperty.setLongitude(longitude);
                    newProperty.setIsVerified(false);
                    newProperty.setIsLocked(false);
                    newProperty.setPropertyType(propertyType);
                    newProperty.setName(propertyName);
                    newProperty.setAddress(completeAddress);
                    newProperty.setProprietorName(proprietorName);
                    newProperty.setMinimumPrice(minimumPrice);
                    newProperty.setMaximumPrice(maximumPrice);
                    newProperty.setIsElectricityIncluded(isElectricityIncluded);
                    newProperty.setIsWaterIncluded(isWaterIncluded);
                    newProperty.setIsInternetIncluded(isInternetIncluded);
                    newProperty.setIsGarbageCollectionIncluded(isGarbageCollectionIncluded);

                    addNewProperty(newPropertyID, newProperty);
                    btnAddProperty.setEnabled(false);

                } else {
                    Toast.makeText(AddPropertyActivity.this, "Please fill out everything", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancelAddProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInputsIfCanDiscard();
            }
        });
    }

    /*----------------------------------------------------------- functions ---------------------------------------------------------------*/

    /*----------- input validations ----------*/
    public final String TAG = "[ADD PROPERTY TESTING]";

    private boolean isPropertyNameValid(String propertyName) {
        if (!propertyName.isEmpty()) {
            textInputPropertyNameLayout.setError(null);
            Log.d(TAG, "PROPERTY NAME: NOT EMPTY");
            return true;
        } else {
            textInputPropertyNameLayout.setError("Property Name Required");
            Log.d(TAG, "PROPERTY NAME: EMPTY");
            return false;
        }
    }

    private boolean isPropertyTypeValid(String propertyType) {
        if (!propertyType.isEmpty()) {
            textInputPropertyTypeLayout.setError(null);
            Log.d(TAG, "PROPERTY TYPE: NOT EMPTY");
            return true;
        } else {
            textInputPropertyTypeLayout.setError("Property Type Required");
            Log.d(TAG, "PROPERTY TYPE: EMPTY");
            return false;
        }
    }

    private boolean isCompleteAddressValid(String completeAddress) {
        if (!completeAddress.isEmpty()) {
            textInputCompleteAddressLayout.setError(null);
            Log.d(TAG, "COMPLETE ADDRESS: NOT EMPTY");
            return true;
        } else {
            textInputCompleteAddressLayout.setError("Complete Address Required");
            Log.d(TAG, "COMPLETE ADDRESS: EMPTY");
            return false;
        }
    }

    private boolean isProprietorNameValid(String proprietorName) {
        if (!proprietorName.isEmpty()) {
            textInputProprietorNameLayout.setError(null);
            Log.d(TAG, "PROPRIETOR NAME: NOT EMPTY");
            return true;
        } else {
            textInputProprietorNameLayout.setError("Proprietor Name Required");
            Log.d(TAG, "PROPRIETOR NAME: EMPTY");
            return false;
        }
    }

    private boolean isMinimumPriceValid(String minimumPrice) {
        if (!minimumPrice.isEmpty()) {
            textInputMinimumPriceLayout.setError(null);
            Log.d(TAG, "MINIMUM PRICE: NOT EMPTY");
            return true;
        } else {
            textInputMinimumPriceLayout.setError("Required");
            Log.d(TAG, "MINIMUM PRICE: EMPTY");
            return false;
        }
    }

    private boolean isMaximumPriceValid(String maximumPrice) {
        if (!maximumPrice.isEmpty()) {
            textInputMaximumPriceLayout.setError(null);
            Log.d(TAG, "MAXIMUM PRICE: NOT EMPTY");
            return true;
        } else {
            textInputMaximumPriceLayout.setError("Required");
            Log.d(TAG, "MAXIMUM PRICE: EMPTY");
            return false;
        }
    }

    private boolean isMinimumPriceLessThanMaximumPrice(int minimumPrice, int maximumPrice) {
        if (minimumPrice <= maximumPrice) {
            return true;
        } else {
            textInputMinimumPriceLayout.setError("Must be less than maximum price");
            Log.d(TAG, "MINIMUM PRICE: GREATER THAN MAXIMUM");
            return false;
        }
    }

    public boolean areInputsValid(String propertyName, String propertyType, String completeAddress, String proprietorName, String minimumPrice, String maximumPrice) {

        boolean propertyNameResult = isPropertyNameValid(propertyName);
        boolean propertyTypeResult = isPropertyTypeValid(propertyType);
        boolean completeAddressResult = isCompleteAddressValid(completeAddress);
        boolean proprietorNameResult = isProprietorNameValid(proprietorName);
        boolean minimumPriceResult = isMinimumPriceValid(minimumPrice);
        boolean maximumPriceResult = isMaximumPriceValid(maximumPrice);


        if (propertyNameResult && propertyTypeResult && completeAddressResult && proprietorNameResult && minimumPriceResult && maximumPriceResult) {
            //will check if the minimum is greater than maximum
            int minPrice = Integer.parseInt(minimumPrice);
            int maxPrice = Integer.parseInt(maximumPrice);
            boolean checkMinimumMaximumResult = isMinimumPriceLessThanMaximumPrice(minPrice, maxPrice);

            if (checkMinimumMaximumResult) {
                Log.d(TAG, "CAN PROCEED: TRUE");
                return true;
            } else {
                return false;
            }
        } else {
            Log.d(TAG, "CAN PROCEED: FALSE");
            return false;
        }
    }

    /*----------- other functions ----------*/

    public void initializeViews() {
        //Initialize textInputLayouts, textInputEditTexts, autoCompleteTextView, checkBoxes, buttons and adapters
        textInputPropertyNameLayout = findViewById(R.id.text_input_propertyName_layout);
        textInputPropertyTypeLayout = findViewById(R.id.text_input_propertyType_layout);
        textInputCompleteAddressLayout = findViewById(R.id.text_input_completeAddress_layout);
        textInputProprietorNameLayout = findViewById(R.id.text_input_proprietorName_layout);
        textInputMinimumPriceLayout = findViewById(R.id.text_input_minimumPrice_layout);
        textInputMaximumPriceLayout = findViewById(R.id.text_input_maximumPrice_layout);

        textInputPropertyName = findViewById(R.id.text_input_propertyName);
        textInputPropertyType = findViewById(R.id.text_input_propertyType);
        textInputCompleteAddress = findViewById(R.id.text_input_completeAddress);
        textInputProprietorName = findViewById(R.id.text_input_proprietorName);
        textInputMinimumPrice = findViewById(R.id.text_input_minimumPrice);
        textInputMaximumPrice = findViewById(R.id.text_input_maximumPrice);

        electricityCheckBox = findViewById(R.id.electricityCheckBox);
        waterCheckBox = findViewById(R.id.waterCheckBox);
        internetCheckBox = findViewById(R.id.internetCheckBox);
        garbageCheckBox = findViewById(R.id.garbageCheckBox);

        btnGetMapLocation = findViewById(R.id.getMapLocation);
        btnAddProperty = findViewById(R.id.btnAddProperty);
        btnCancelAddProperty = findViewById(R.id.btnCancelAddProperty);


        propertyTypeAdapter = new ArrayAdapter<String>(this, R.layout.landlord_property_type_list_item, propertyType);
        textInputPropertyType.setAdapter(propertyTypeAdapter);

        minimumPriceAdapter = new ArrayAdapter<String>(this, R.layout.landlord_minimum_price_list_item, minimumPrices);
        textInputMinimumPrice.setAdapter(minimumPriceAdapter);
        minimumPriceAdapter.notifyDataSetChanged();

        maximumPriceAdapter = new ArrayAdapter<String>(this, R.layout.landlord_maximum_price_list_item, maximumPrices);
        textInputMaximumPrice.setAdapter(maximumPriceAdapter);
        maximumPriceAdapter.notifyDataSetChanged();
    }

    public void getRentInclusions() {
        isElectricityIncluded = electricityCheckBox.isChecked();
        isWaterIncluded = waterCheckBox.isChecked();
        isInternetIncluded = internetCheckBox.isChecked();
        isGarbageCollectionIncluded = garbageCheckBox.isChecked();
    }

    public void addNewProperty(String newPropertyID, Property newProperty) {
        // TESTING PURPOSES - Refactor  soon and put in Repository or Viewmodel
        propertiesDocumentReference = database.collection("properties").document(newPropertyID);

        propertiesDocumentReference.set(newProperty).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(AddPropertyActivity.this, "Property Successfully Added", Toast.LENGTH_SHORT).show();
                btnAddProperty.setEnabled(true);
                startActivity(new Intent(AddPropertyActivity.this, LandlordMainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddPropertyActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openLocationPickerActivityForResult() {
        Intent intent = new Intent(AddPropertyActivity.this, LocationPickerActivity.class);
        LocationPickerActivityResultLauncher.launch(intent);
    }

    //this will check if the landmark is blank, if so, format location string, if not return whole with landmark
    public String formatStringLocation(String street, String barangay, String municipality, String landmark) {
        String formattedLocationString = "";
        if (landmark.equals("")) {
            formattedLocationString = street + ", " + barangay + ", " + municipality;
        } else {
            formattedLocationString = street + ", " + barangay + ", " + municipality + ", " + landmark;
        }

        return formattedLocationString;
    }

    public void showDiscardDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Discard draft")
                .setMessage("Are you sure you want to discard your inputs?")
                .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    public boolean areInputsEmpty(String propertyName, String propertyType, String completeAddress, String proprietorName, String minimumPrice, String maximumPrice) {
        boolean propertyNameResult = propertyName.isEmpty();
        boolean propertyTypeResult = propertyType.isEmpty();
        boolean completeAddressResult = completeAddress.isEmpty();
        boolean proprietorNameResult = proprietorName.isEmpty();
        boolean minimumPriceResult = minimumPrice.isEmpty();
        boolean maximumPriceResult = maximumPrice.isEmpty();

        return propertyNameResult & propertyTypeResult & completeAddressResult & proprietorNameResult & minimumPriceResult & maximumPriceResult;
    }

    //will check inputs if empty
    public void checkInputsIfCanDiscard() {
        String propertyName = textInputPropertyName.getText().toString().trim();
        String propertyType = textInputPropertyType.getText().toString().trim();
        String completeAddress = textInputCompleteAddress.getText().toString().trim();
        String proprietorName = textInputProprietorName.getText().toString().trim();
        String minPrice = textInputMinimumPrice.getText().toString().trim();
        String maxPrice = textInputMaximumPrice.getText().toString().trim();

        if (areInputsEmpty(propertyName, propertyType, completeAddress, proprietorName, minPrice, maxPrice)) {
            finish();
        } else {
            showDiscardDialog();
        }
    }

    // TODO: Handle Activity Life Cycle
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        checkInputsIfCanDiscard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}