package com.capstone.espasyo.landlord.views;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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
            textInputLandlordNameLayout,
            textInputLandlordPhoneNumberLayout,
            textInputMinimumPriceLayout,
            textInputMaximumPriceLayout;

    private TextInputEditText textInputPropertyName,
            textInputCompleteAddress,
            textInputProprietorName,
            textInputLandlordName,
            textInputLandlordPhoneNumber;

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

    private ActivityResultLauncher<Intent> someActivityResultLauncher;

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
        someActivityResultLauncher = registerForActivityResult(
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


                            completeAddress = formatStringLocation(street, barangay, municipality, landmark);
                            Toast.makeText(AddPropertyActivity.this, "Location Picked: "  + completeAddress, Toast.LENGTH_SHORT).show();
                            textInputCompleteAddress.setText(completeAddress);
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
                String landlordName = textInputLandlordName.getText().toString().trim();
                String landlordPhoneNumber = textInputLandlordPhoneNumber.getText().toString().trim();
                String minPrice = textInputMinimumPrice.getText().toString().trim();
                String maxPrice = textInputMaximumPrice.getText().toString().trim();


                if (areInputsValid(propertyName, propertyType, completeAddress, proprietorName, landlordName, landlordPhoneNumber, minPrice, maxPrice)) {

                    int minimumPrice = Integer.parseInt(minPrice);
                    int maximumPrice = Integer.parseInt(maxPrice);


                    String newPropertyID = UUID.randomUUID().toString();
                    String propertyOwner = fAuth.getCurrentUser().getUid().toString();

                    getRentInclusions();

                    // CREATE SAMPLE PROPERTY OBJECT
                    Property newProperty = new Property(
                            newPropertyID,
                            propertyOwner,
                            latitude,
                            longitude,
                            false,
                            false,
                            propertyType,
                            propertyName,
                            completeAddress,
                            proprietorName,
                            landlordName,
                            landlordPhoneNumber,
                            minimumPrice,
                            maximumPrice,
                            isElectricityIncluded,
                            isWaterIncluded,
                            isInternetIncluded,
                            isGarbageCollectionIncluded
                    );

                    addNewProperty(newPropertyID, newProperty);

                } else {
                    Toast.makeText(AddPropertyActivity.this, "SOMETHING IS EMPTY", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancelAddProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: add cancel functionality
                startActivity(new Intent(AddPropertyActivity.this, LandlordMainActivity.class));
                finish();
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

    private boolean isLandlordNameValid(String landlordName) {
        if (!landlordName.isEmpty()) {
            textInputLandlordNameLayout.setError(null);
            Log.d(TAG, "LANDLORD NAME: NOT EMPTY");
            return true;
        } else {
            textInputLandlordNameLayout.setError("Landlord Name Required");
            Log.d(TAG, "LANDLORD NAME: EMPTY");
            return false;
        }
    }

    private boolean isLandlordPhoneNumberValid(String landlordPhoneNumbers) {
        if (!landlordPhoneNumbers.isEmpty()) {
            textInputLandlordPhoneNumberLayout.setError(null);
            Log.d(TAG, "LANDLORD PHONE NUMBER: NOT EMPTY");
            return true;
        } else {
            textInputLandlordPhoneNumberLayout.setError("Landlord Phone Number Required");
            Log.d(TAG, "LANDLORD PHONE NUMBER: EMPTY");
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

    public boolean areInputsValid(String propertyName, String propertyType, String completeAddress, String proprietorName, String landlordName, String landlordPhoneNumber, String minimumPrice, String maximumPrice) {

        boolean propertyNameResult = isPropertyNameValid(propertyName);
        boolean propertyTypeResult = isPropertyTypeValid(propertyType);
        boolean completeAddressResult = isCompleteAddressValid(completeAddress);
        boolean proprietorNameResult = isProprietorNameValid(proprietorName);
        boolean landlordNameResult = isLandlordNameValid(landlordName);
        boolean landlordPhoneNumberResult = isLandlordPhoneNumberValid(landlordPhoneNumber);
        boolean minimumPriceResult = isMinimumPriceValid(minimumPrice);
        boolean maximumPriceResult = isMaximumPriceValid(maximumPrice);

        if (propertyNameResult && propertyTypeResult && completeAddressResult && proprietorNameResult && landlordPhoneNumberResult && minimumPriceResult && maximumPriceResult) {
            Log.d(TAG, "CAN PROCEED: TRUE");
            return true;
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
        textInputLandlordNameLayout = findViewById(R.id.text_input_landlordName_layout);
        textInputLandlordPhoneNumberLayout = findViewById(R.id.text_input_landlord_phoneNumber_layout);
        textInputMinimumPriceLayout = findViewById(R.id.text_input_minimumPrice_layout);
        textInputMaximumPriceLayout = findViewById(R.id.text_input_maximumPrice_layout);

        textInputPropertyName = findViewById(R.id.text_input_propertyName);
        textInputPropertyType = findViewById(R.id.text_input_propertyType);
        textInputCompleteAddress = findViewById(R.id.text_input_completeAddress);
        textInputProprietorName = findViewById(R.id.text_input_proprietorName);
        textInputLandlordName = findViewById(R.id.text_input_landlordName);
        textInputLandlordPhoneNumber = findViewById(R.id.text_input_landlord_phoneNumber);
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
        if (electricityCheckBox.isChecked()) {
            isElectricityIncluded = true;
        } else {
            isElectricityIncluded = false;
        }

        if (waterCheckBox.isChecked()) {
            isWaterIncluded = true;
        } else {
            isWaterIncluded = false;
        }

        if (internetCheckBox.isChecked()) {
            isInternetIncluded = true;
        } else {
            isInternetIncluded = false;
        }

        if (garbageCheckBox.isChecked()) {
            isGarbageCollectionIncluded = true;
        } else {
            isGarbageCollectionIncluded = false;
        }
    }

    public void addNewProperty(String newPropertyID, Property newProperty) {
        // TESTING PURPOSES - Refactor  soon and put in Repository or Viewmodel
        propertiesDocumentReference = database.collection("properties").document(newPropertyID);

        propertiesDocumentReference.set(newProperty).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(AddPropertyActivity.this, "Property Successfully Added", Toast.LENGTH_SHORT).show();
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

    //get data from LocationPickerActivity and put latitude, longitude , address in property object
    //also put address data in complete address textbox
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                String completeAddress = data.getStringExtra("address");
                latitude = data.getDoubleExtra("latitude",0);
                longitude = data.getDoubleExtra("longitude",0);

                textInputCompleteAddress.setText(completeAddress);
            }
        }
    }*/

    public void openLocationPickerActivityForResult() {
        Intent intent = new Intent(AddPropertyActivity.this, LocationPickerActivity.class);
        someActivityResultLauncher.launch(intent);
    }

    //this will check if the landmark is blank, if so, format location string, if not return whole woth landmark
    public String formatStringLocation(String street, String barangay, String municipality, String landmark) {
        String formattedLocationString = "";
        if(landmark.equals("")) {
            formattedLocationString = street + " " + barangay + " " +municipality;
        } else {
            formattedLocationString = street + " " + barangay + " " + municipality + " " + landmark;
        }

        return  formattedLocationString;
    }


    // TODO: Handle Activity Life Cycle
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Toast.makeText(this, "Pressing back button", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}