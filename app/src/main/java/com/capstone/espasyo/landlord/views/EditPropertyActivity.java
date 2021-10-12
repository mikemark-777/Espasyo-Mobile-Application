package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.LandlordMainActivity;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Property;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class EditPropertyActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;

    private TextInputLayout textEditPropertyNameLayout,
            textEditPropertyTypeLayout,
            textEditCompleteAddressLayout,
            textEditLandlordNameLayout,
            textEditLandlordPhoneNumberLayout,
            textEditMinimumPriceLayout,
            textEditMaximumPriceLayout;

    private TextInputEditText textEditPropertyName,
            textEditCompleteAddress,
            textEditLandlordName,
            textEditLandlordPhoneNumber;

    private AutoCompleteTextView textEditPropertyType,
            textEditMinimumPrice,
            textEditMaximumPrice;

    private CheckBox electrictiyEditCheckBox,
            waterEditCheckBox,
            internetEditCheckBox,
            garbageEditCheckBox;

    private Button btnEditProperty,
            btnCancelEditProperty,
            btnDeleteProperty;

    String[] propertyType = {"Apartment", "Boarding House", "Dormitory"};
    String[] minimumPrices = {"500", "1000", "1500", "2000", "2500", "3000", "3500", "4000", "4500", "5000", "5500", "6000", "6500", "7000", "7500", "8000"};
    String[] maximumPrices = {"500", "1000", "1500", "2000", "2500", "3000", "3500", "4000", "4500", "5000", "5500", "6000", "6500", "7000", "7500", "8000"};
    ArrayAdapter<String> propertyTypeAdapter;
    ArrayAdapter<String> minimumPriceAdapter;
    ArrayAdapter<String> maximumPriceAdapter;

    private boolean isElectricityIncluded,
            isWaterIncluded,
            isInternetIncluded,
            isGarbageCollectionIncluded;

    private Property property;
    private String propertyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_edit_property);

        //Initialize FirebaseConnection, FirebaseAuth and FirebaseFirestore
        firebaseConnection = FirebaseConnection.getInstance();
        firebaseAuth = firebaseConnection.getFirebaseAuthInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();

        //initialize all views
        initializeViews();

        /*get the intent passed by ChooseEditActivity (contains the chosen Property Object)
        and load the property data to the views that is initialized*/
        Intent intent = getIntent();
        loadPropertyData(intent);

        btnEditProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editedPropertyName = textEditPropertyName.getText().toString().trim();
                String editedPropertyType = textEditPropertyType.getText().toString().trim();
                String editedPropertyAddress = textEditCompleteAddress.getText().toString().trim();
                String editedLandlordName = textEditLandlordName.getText().toString().trim();
                String editedLandlordPhoneNumber = textEditLandlordPhoneNumber.getText().toString().trim();
                String editedMinimumPrice = textEditMinimumPrice.getText().toString().trim();
                String editedMaximumPrice = textEditMaximumPrice.getText().toString().trim();
                boolean editedIsElectricityIncluded = electrictiyEditCheckBox.isChecked();
                boolean editedIsWaterIncluded = waterEditCheckBox.isChecked();
                boolean editedIsInternetIncluded = internetEditCheckBox.isChecked();
                boolean editedIsGarbageCollectionIncluded = garbageEditCheckBox.isChecked();

                //TODO: Must include input validations
                property.setName(editedPropertyName);
                property.setPropertyType(editedPropertyType);
                property.setAddress(editedPropertyAddress);
                property.setLandlordName(editedLandlordName);
                property.setLandlordPhoneNumber(editedLandlordPhoneNumber);
                property.setMinimumPrice(Integer.parseInt(editedMinimumPrice));
                property.setMaximumPrice(Integer.parseInt(editedMaximumPrice));
                property.setIsElectricityIncluded(editedIsElectricityIncluded);
                property.setIsWaterIncluded(editedIsWaterIncluded);
                property.setIsInternetIncluded(editedIsInternetIncluded);
                property.setIsGarbageCollectionIncluded(editedIsGarbageCollectionIncluded);

                saveChangesToProperty(property);

            }
        });

        btnDeleteProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDeleteDialog();
            }
        });

    }

    private void initializeViews() {
        //Initialize textInputLayouts, textInputEditTexts, autoCompleteTextView, checkBoxes, buttons and adapters

        //textInputLayouts
        textEditPropertyNameLayout = findViewById(R.id.text_edit_propertyName_layout);
        textEditPropertyTypeLayout = findViewById(R.id.text_edit_propertyType_layout);
        textEditCompleteAddressLayout = findViewById(R.id.text_edit_completeAddress_layout);
        textEditLandlordNameLayout = findViewById(R.id.text_edit_landlordName_layout);
        textEditLandlordPhoneNumberLayout = findViewById(R.id.text_edit_landlord_phoneNumber_layout);
        textEditMinimumPriceLayout = findViewById(R.id.text_edit_minimumPrice_layout);
        textEditMaximumPriceLayout = findViewById(R.id.text_edit_maximumPrice_layout);
        //textInputEditTexts
        textEditPropertyName = findViewById(R.id.text_edit_propertyName);
        textEditPropertyType = findViewById(R.id.text_edit_propertyType);
        textEditCompleteAddress = findViewById(R.id.text_edit_completeAddress);
        textEditLandlordName = findViewById(R.id.text_edit_landlordName);
        textEditLandlordPhoneNumber = findViewById(R.id.text_edit_landlord_phoneNumber);
        textEditMinimumPrice = findViewById(R.id.text_edit_minimumPrice);
        textEditMaximumPrice = findViewById(R.id.text_edit_maximumPrice);
        //checkBoxes
        electrictiyEditCheckBox = findViewById(R.id.electricityEditCheckBox);
        waterEditCheckBox = findViewById(R.id.waterEditCheckBox);
        internetEditCheckBox = findViewById(R.id.internetEditCheckBox);
        garbageEditCheckBox = findViewById(R.id.garbageEditCheckBox);
        //buttons
        btnEditProperty = findViewById(R.id.btnEditProperty);
        btnCancelEditProperty = findViewById(R.id.btnCancelEditProperty);
        btnDeleteProperty = findViewById(R.id.btnDeleteProperty);

        propertyTypeAdapter = new ArrayAdapter<String>(this, R.layout.landlord_property_type_list_item, propertyType);
        textEditPropertyType.setAdapter(propertyTypeAdapter);

        minimumPriceAdapter = new ArrayAdapter<String>(this, R.layout.landlord_minimum_price_list_item, minimumPrices);
        textEditMinimumPrice.setAdapter(minimumPriceAdapter);
        minimumPriceAdapter.notifyDataSetChanged();

        maximumPriceAdapter = new ArrayAdapter<String>(this, R.layout.landlord_maximum_price_list_item, maximumPrices);
        textEditMaximumPrice.setAdapter(maximumPriceAdapter);
        maximumPriceAdapter.notifyDataSetChanged();
    }

    private void loadPropertyData(Intent intent) {
        property = intent.getParcelableExtra("property");

        String name = property.getName();
        String propertyType = property.getPropertyType();
        String address = property.getAddress();
        String landlordName = property.getLandlordName();
        String landlordPhoneNumber = property.getLandlordPhoneNumber();
        int minimumPrice = property.getMinimumPrice();
        int maximumPrice = property.getMaximumPrice();
        boolean isElectricityIncluded = property.getIsElectricityIncluded();
        boolean isWaterIncluded = property.getIsWaterIncluded();
        boolean isInternetIncluded = property.getIsInternetIncluded();
        boolean isGarbageCollectionIncluded = property.getIsGarbageCollectionIncluded();

        //get propertyTypePosition, minimumPricePosition and maximumPricePosition in adapter
        int propertyTypePosition = propertyTypeAdapter.getPosition(propertyType);
        int minimumPricePosition = minimumPriceAdapter.getPosition(String.valueOf(minimumPrice));
        int maximumPricePosition = maximumPriceAdapter.getPosition(String.valueOf(maximumPrice));


        textEditPropertyName.setText(name);
        textEditPropertyType.setText(textEditPropertyType.getAdapter().getItem(propertyTypePosition).toString(), false);
        textEditCompleteAddress.setText(address);
        textEditLandlordName.setText(landlordName);
        textEditLandlordPhoneNumber.setText(landlordPhoneNumber);
        textEditMinimumPrice.setText(textEditMinimumPrice.getAdapter().getItem(minimumPricePosition).toString(), false);
        textEditMaximumPrice.setText(textEditMaximumPrice.getAdapter().getItem(maximumPricePosition).toString(), false);
        electrictiyEditCheckBox.setChecked(isElectricityIncluded);
        waterEditCheckBox.setChecked(isWaterIncluded);
        internetEditCheckBox.setChecked(isInternetIncluded);
        garbageEditCheckBox.setChecked(isGarbageCollectionIncluded);
    }

    public void saveChangesToProperty(Property editedProperty) {
        propertyID = editedProperty.getPropertyID();
        DocumentReference propertyDocumentReference = database.collection("properties").document(propertyID);
        
        propertyDocumentReference.set(editedProperty).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(EditPropertyActivity.this, "Property Successfully Edited!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditPropertyActivity.this, "Error saving edited property: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void deleteProperty(String propertyID) {

        //delete first the rooms of this property
        database.collection("properties/" + propertyID + "/rooms")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot room: task.getResult()) {
                            database.collection("properties/" + propertyID + "/rooms")
                                    .document(room.getId())
                                    .delete();
                        }
                    }
                });

        database.collection("properties").document(propertyID)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(EditPropertyActivity.this, "Property Successfully Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void showConfirmationDeleteDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.landlord_delete_property_confirmation_dialog, null);

        Button btnConfirmDelete = view.findViewById(R.id.btnConfirmDeleteProperty);
        Button btnCancelDelete = view.findViewById(R.id.btnCancelDeleteProperty);

        AlertDialog confirmationDialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnConfirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               deleteProperty(property.getPropertyID());
               confirmationDialog.cancel();
               Intent intent = new Intent(EditPropertyActivity.this, LandlordMainActivity.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               startActivity(intent);
               finish();
            }
        });

        btnCancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationDialog.cancel();
            }
        });

        confirmationDialog.show();
    }


}