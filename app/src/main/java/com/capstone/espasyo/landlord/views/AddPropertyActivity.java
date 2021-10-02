package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();
    private DocumentReference dbProperties;

    private TextInputLayout textInputPropertyNameLayout,
            textInputPropertyTypeLayout,
            textInputCompleteAddressLayout,
            textInputLandlordNameLayout,
            textInputLandlordPhoneNumberLayout,
            textInputMinimumPriceLayout,
            textInputMaximumPriceLayout;

    private TextInputEditText textInputPropertyName,
            textInputCompleteAddress,
            textInputLandlordName,
            textInputLandlordPhoneNumber;

    private AutoCompleteTextView textInputPropertyType,
            textInputMinimumPrice,
            textInputMaximumPrice;

    private CheckBox electrictiyCheckBox,
                     waterCheckBox,
                     internetCheckBox,
                     garbageCheckBox;

    private boolean isElectricityIncluded,
                    isWaterIncluded,
                    isInternetIncluded,
                    isGarbageCollectionIncluded;

    List<String> rentInclusions = new ArrayList<>();

    String[] propertyType = {"Apartment", "Boarding House", "Dormitory"};
    String[] minimumPrices = {"500", "1000", "2000", "3000", "4000", "5000", "6000", "7000", "8000", "9000", "10000", "11000", "12000"};
    String[] maximumPrices = {"500", "1000", "2000", "3000", "4000", "5000", "6000", "7000", "8000", "9000", "10000", "11000", "12000"};
    ArrayAdapter<String> propertyTypeAdapter;
    ArrayAdapter<String> minimumPriceAdapter;
    ArrayAdapter<String> maximumPriceAdapter;

    private Button btnAddProperty,
                   btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_add_property);

        //Initialize textInputLayouts, textInputEditTexts, autoCompleteTextView, checkBoxes and buttons

        textInputPropertyNameLayout = findViewById(R.id.text_input_propertyName_layout);
        textInputPropertyTypeLayout = findViewById(R.id.text_input_propertyType_layout);
        textInputCompleteAddressLayout = findViewById(R.id.text_input_completeAddress_layout);
        textInputLandlordNameLayout = findViewById(R.id.text_input_landlordName_layout);
        textInputLandlordPhoneNumberLayout = findViewById(R.id.text_input_landlord_phoneNumber_layout);
        textInputMinimumPriceLayout = findViewById(R.id.text_input_minimumPrice_layout);
        textInputMaximumPriceLayout = findViewById(R.id.text_input_maximumPrice_layout);

        textInputPropertyName = findViewById(R.id.text_input_propertyName);
        textInputPropertyType = findViewById(R.id.text_input_propertyType);
        textInputCompleteAddress = findViewById(R.id.text_input_completeAddress);
        textInputLandlordName = findViewById(R.id.text_input_landlordName);
        textInputLandlordPhoneNumber = findViewById(R.id.text_input_landlord_phoneNumber);
        textInputMinimumPrice = findViewById(R.id.text_input_minimumPrice);
        textInputMaximumPrice = findViewById(R.id.text_input_maximumPrice);

        electrictiyCheckBox = findViewById(R.id.electricityCheckBox);
        waterCheckBox = findViewById(R.id.waterCheckBox);
        internetCheckBox = findViewById(R.id.internetCheckBox);
        garbageCheckBox = findViewById(R.id.garbageCheckBox);

        btnAddProperty = findViewById(R.id.btnAddProperty);
        btnCancel = findViewById(R.id.btnCancel);


        propertyTypeAdapter = new ArrayAdapter<String>(this, R.layout.landlord_property_type_list_item, propertyType);
        textInputPropertyType.setAdapter(propertyTypeAdapter);

        minimumPriceAdapter = new ArrayAdapter<String>(this, R.layout.landlord_minimum_price_list_item, minimumPrices);
        textInputMinimumPrice.setAdapter(minimumPriceAdapter);
        minimumPriceAdapter.notifyDataSetChanged();

        maximumPriceAdapter = new ArrayAdapter<String>(this, R.layout.landlord_maximum_price_list_item, maximumPrices);
        textInputMaximumPrice.setAdapter(maximumPriceAdapter);
        maximumPriceAdapter.notifyDataSetChanged();



        btnAddProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String propertyName = textInputPropertyName.getText().toString().trim();
                String propertyType = textInputPropertyType.getText().toString().trim();
                String completeAddress = textInputCompleteAddress.getText().toString().trim();
                String landlordName = textInputLandlordName.getText().toString().trim();
                String landlordPhoneNumber = textInputLandlordPhoneNumber.getText().toString().trim();
                String minPrice = textInputMinimumPrice.getText().toString().trim();
                String maxPrice = textInputMaximumPrice.getText().toString().trim();


                if (areInputsValid(propertyName, propertyType, completeAddress, landlordName, landlordPhoneNumber, minPrice, maxPrice)) {

                    int minimumPrice = Integer.parseInt(minPrice);
                    int maximumPrice = Integer.parseInt(maxPrice);


                    String newPropertyID = UUID.randomUUID().toString();
                    String propertyOwner = fAuth.getCurrentUser().getUid().toString();

                    getRentInclusions();

                    // CREATE SAMPLE PROPERTY OBJECT
                    Property newProperty = new Property(
                            newPropertyID,
                            propertyOwner,
                            false,
                            propertyType,
                            propertyName,
                            completeAddress,
                            landlordName,
                            landlordPhoneNumber,
                            minimumPrice,
                            maximumPrice,
                            isElectricityIncluded,
                            isWaterIncluded,
                            isInternetIncluded,
                            isGarbageCollectionIncluded
                    );

                    // TESTING PURPOSES - Refactor  soon and put in Repository or Viewmodel
                    dbProperties = database.collection("properties").document(newPropertyID);

                    dbProperties.set(newProperty).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                } else {
                    Toast.makeText(AddPropertyActivity.this, "SOMETHING IS EMPTY", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddPropertyActivity.this, LandlordMainActivity.class));
                finish();
            }
        });

    }

    // Functions
    //TODO: Add input validations

    /* ------------- Input Validations ------------*/
    public final String TAG = "[ADD PROPERTY TESTING]";

    private Boolean isPropertyNameValid(String propertyName) {
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

    private Boolean isPropertyTypeValid(String propertyType) {
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

    private Boolean isCompleteAddressValid(String completeAddress) {
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

    private Boolean isLandlordNameValid(String landlordName) {
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

    private Boolean isLandlordPhoneNumberValid(String landlordPhoneNumbers) {
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

    private Boolean isMinimumPriceValid(String minimumPrice) {
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

    private Boolean isMaximumPriceValid(String maximumPrice) {
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

    public boolean areInputsValid(String propertyName, String propertyType, String completeAddress, String landlordName, String landlordPhoneNumber, String minimumPrice, String maximumPrice) {

        boolean propertyNameResult = isPropertyNameValid(propertyName);
        boolean propertyTypeResult = isPropertyTypeValid(propertyType);
        boolean completeAddressResult = isCompleteAddressValid(completeAddress);
        boolean landlordNameResult = isLandlordNameValid(landlordName);
        boolean landlordPhoneNumberResult = isLandlordPhoneNumberValid(landlordPhoneNumber);
        boolean minimumPriceResult = isMinimumPriceValid(minimumPrice);
        boolean maximumPriceResult = isMaximumPriceValid(maximumPrice);

        if (propertyNameResult && propertyTypeResult && completeAddressResult && landlordNameResult && landlordPhoneNumberResult && minimumPriceResult && maximumPriceResult) {
            Log.d(TAG, "CAN PROCEED: TRUE");
            return true;
        } else {
            Log.d(TAG, "CAN PROCEED: FALSE");
            return false;
        }

    }

    public void getRentInclusions() {
        if(electrictiyCheckBox.isChecked()) {
          isElectricityIncluded = true;
        } else {
            isElectricityIncluded = false;
        }

        if(waterCheckBox.isChecked()) {
            isWaterIncluded = true;
        } else {
            isWaterIncluded = false;
        }

        if(internetCheckBox.isChecked()) {
           isInternetIncluded = true;
        } else {
            isInternetIncluded = false;
        }

        if(garbageCheckBox.isChecked()) {
           isGarbageCollectionIncluded = true;
        } else {
            isGarbageCollectionIncluded = false;
        }
    }


    // Activity Life Cycle
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