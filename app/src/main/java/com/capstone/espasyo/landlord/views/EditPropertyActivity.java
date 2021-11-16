package com.capstone.espasyo.landlord.views;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.LandlordMainActivity;
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Property;
import com.capstone.espasyo.models.VerificationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditPropertyActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private VerificationRequest verificationRequest;
    private CustomProgressDialog progressDialog;

    private TextInputLayout textEditPropertyNameLayout,
            textEditPropertyTypeLayout,
            textEditCompleteAddressLayout,
            textEditProprietorNameLayout,
            textEditLandlordNameLayout,
            textEditLandlordPhoneNumberLayout,
            textEditMinimumPriceLayout,
            textEditMaximumPriceLayout;

    private TextInputEditText textEditPropertyName,
            textEditCompleteAddress,
            textEditProprietorName,
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
            btnCancelEditProperty;

    private ImageButton btnEditMapLocation;
    private ImageView btnDeleteProperty,
                      imageButtonBackToChooseEdit;

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

    private String completeAddress;
    private double latitude, longitude;

    private ActivityResultLauncher<Intent> EditLocationPickerActivityResultLauncher;

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
        storage = firebaseConnection.getFirebaseStorageInstance();

        //initialize all views
        initializeViews();

        /*get the intent passed by ChooseEditActivity (contains the chosen Property Object)
        and load the property data to the views that is initialized*/
        Intent intent = getIntent();
        loadPropertyData(intent);

        if(property.getVerificationID() != null) {
            preLoadVerificationRequest();
        }
        
        //will handle all the data from the LocationPickerActivity
        EditLocationPickerActivityResultLauncher = registerForActivityResult(
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
                            Toast.makeText(EditPropertyActivity.this, "Location Picked: Lat("  + latitude + ") , (" + longitude + ")", Toast.LENGTH_SHORT).show();
                            textEditCompleteAddress.setText(completeAddress);
                        } else if (result.getResultCode() == Activity.RESULT_CANCELED){
                            //if the user don't change the location, the location saved retains
                            latitude = property.getLatitude();
                            longitude = property.getLongitude();
                        }
                    }
                });

        btnEditProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String editedPropertyName = textEditPropertyName.getText().toString().trim();
                String editedPropertyType = textEditPropertyType.getText().toString().trim();
                String editedPropertyAddress = textEditCompleteAddress.getText().toString().trim();
                double editedLatitude = latitude;
                double editedLongitude = longitude;
                String editedProprietorName = textEditProprietorName.getText().toString().trim();
                String editedLandlordName = textEditLandlordName.getText().toString().trim();
                String editedLandlordPhoneNumber = textEditLandlordPhoneNumber.getText().toString().trim();
                String editedMinimumPrice = textEditMinimumPrice.getText().toString().trim();
                String editedMaximumPrice = textEditMaximumPrice.getText().toString().trim();
                boolean editedIsElectricityIncluded = electrictiyEditCheckBox.isChecked();
                boolean editedIsWaterIncluded = waterEditCheckBox.isChecked();
                boolean editedIsInternetIncluded = internetEditCheckBox.isChecked();
                boolean editedIsGarbageCollectionIncluded = garbageEditCheckBox.isChecked();

                //TODO: Must include input validations

                if (areInputsValid(editedPropertyName, editedPropertyType, editedPropertyAddress, editedProprietorName, editedLandlordName, editedLandlordPhoneNumber, editedMinimumPrice, editedMaximumPrice)) {

                    property.setIsVerified(false);
                    property.setName(editedPropertyName);
                    property.setPropertyType(editedPropertyType);
                    property.setAddress(editedPropertyAddress);
                    property.setLatitude(editedLatitude);
                    property.setLongitude(editedLongitude);
                    property.setProprietorName(editedProprietorName);
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
            }
        });

        btnCancelEditProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnEditMapLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditLocationPickerActivityForResult();
            }
        });

        btnDeleteProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDeleteDialog();
            }
        });

        imageButtonBackToChooseEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /*----------------------------------------------------------- functions ---------------------------------------------------------------*/

    /*----------- input validations ----------*/
    public final String TAG = "[EDIT PROPERTY TESTING]";

    public boolean isPropertyNameValid(String propertyName) {
        if (!propertyName.isEmpty()) {
            textEditPropertyNameLayout.setError(null);
            Log.d(TAG, "PROPERTY NAME: NOT EMPTY");
            return true;
        } else {
            textEditPropertyNameLayout.setError("Property Name Required");
            Log.d(TAG, "PROPERTY NAME: EMPTY");
            return false;
        }
    }

    public boolean isPropertyTypeValid(String propertyType) {
        if (!propertyType.isEmpty()) {
            textEditPropertyTypeLayout.setError(null);
            Log.d(TAG, "PROPERTY TYPE: NOT EMPTY");
            return true;
        } else {
            textEditPropertyTypeLayout.setError("Property Type Required");
            Log.d(TAG, "PROPERTY TYPE: EMPTY");
            return false;
        }
    }

    public boolean isCompleteAddressValid(String completeAddress) {
        if (!completeAddress.isEmpty()) {
            textEditCompleteAddressLayout.setError(null);
            Log.d(TAG, "COMPLETE ADDRESS: NOT EMPTY");
            return true;
        } else {
            textEditCompleteAddressLayout.setError("Complete Address Required");
            Log.d(TAG, "COMPLETE ADDRESS: EMPTY");
            return false;
        }
    }

    public boolean isProprietorNameValid(String proprietorName) {
        if (!proprietorName.isEmpty()) {
            textEditProprietorNameLayout.setError(null);
            Log.d(TAG, "PROPRIETOR NAME: NOT EMPTY");
            return true;
        } else {
            textEditProprietorNameLayout.setError("Proprietor Name Required");
            Log.d(TAG, "PROPRIETOR NAME: EMPTY");
            return false;
        }
    }

    public boolean isLandlordNameValid(String landlordName) {
        if (!landlordName.isEmpty()) {
            textEditLandlordNameLayout.setError(null);
            Log.d(TAG, "LANDLORD NAME: NOT EMPTY");
            return true;
        } else {
            textEditLandlordNameLayout.setError("Landlord Name Required");
            Log.d(TAG, "LANDLORD NAME: EMPTY");
            return false;
        }
    }

    public boolean isLandlordPhoneNumberValid(String landlordPhoneNumber) {
        if (!landlordPhoneNumber.isEmpty()) {
            if(landlordPhoneNumber.length() == 10) {
                textEditLandlordPhoneNumberLayout.setError(null);
                Log.d(TAG, "LANDLORD PHONE NUMBER: NOT EMPTY");
                return true;
            } else {
                textEditLandlordPhoneNumberLayout.setError("Phone number must be 12 digit");
                Log.d(TAG, "LANDLORD PHONE NUMBER: NOT EMPTY");
                return false;
            }
        } else {
            textEditLandlordPhoneNumberLayout.setError("Landlord Number Required");
            Log.d(TAG, "LANDLORD NUMBER: EMPTY");
            return false;
        }
    }

    public boolean isMinimumPriceValid(String minimumPrice) {
        if (!minimumPrice.isEmpty()) {
            textEditMinimumPriceLayout.setError(null);
            Log.d(TAG, "MINIMUM PRICE: NOT EMPTY");
            return true;
        } else {
            textEditMinimumPriceLayout.setError("Minimum Price Required");
            Log.d(TAG, "MINIMUM PRICE: EMPTY");
            return false;
        }
    }

    public boolean isMaximumPriceValid(String maximumPrice) {
        if (!maximumPrice.isEmpty()) {
            textEditMaximumPriceLayout.setError(null);
            Log.d(TAG, "MAXIMUM PRICE: NOT EMPTY");
            return true;
        } else {
            textEditMaximumPriceLayout.setError("Maximum Price Required");
            Log.d(TAG, "MAXIMUM PRICE: EMPTY");
            return false;
        }
    }

    private boolean isMinimumPriceLessThanMaximumPrice(int minimumPrice, int maximumPrice) {
        if(minimumPrice <= maximumPrice) {
            return true;
        } else {
            textEditMinimumPriceLayout.setError("Must be less than maximum price");
            Log.d(TAG, "MINIMUM PRICE: GREATER THAN MAXIMUM");
            return false;
        }
    }

    private boolean areInputsValid(String propertyName, String propertyType, String completeAddress, String proprietorName, String landlordName, String landlordPhoneNumber, String minimumPrice, String maximumPrice) {

        boolean propertyNameResult = isPropertyNameValid(propertyName);
        boolean propertyTypeResult = isPropertyTypeValid(propertyType);
        boolean completeAddressResult = isCompleteAddressValid(completeAddress);
        boolean proprietorNameResult = isProprietorNameValid(proprietorName);
        boolean landlordNameResult = isLandlordNameValid(landlordName);
        boolean landlordPhoneNumberResult = isLandlordPhoneNumberValid(landlordPhoneNumber);
        boolean minimumPriceResult = isMinimumPriceValid(minimumPrice);
        boolean maximumPriceResult = isMaximumPriceValid(maximumPrice);

        int minPrice = Integer.parseInt(minimumPrice);
        int maxPrice = Integer.parseInt(maximumPrice);

        boolean checkMinimumMaximumResult = isMinimumPriceLessThanMaximumPrice(minPrice, maxPrice);

        if (propertyNameResult && propertyTypeResult && completeAddressResult && proprietorNameResult && landlordNameResult && landlordPhoneNumberResult && minimumPriceResult && maximumPriceResult && checkMinimumMaximumResult) {
            Log.d(TAG, "CAN PROCEED: TRUE");
            return true;
        } else {
            Log.d(TAG, "CAN PROCEED: FALSE");
            return false;
        }
    }

    /*----------- other functions ----------*/

    //Initialize textInputLayouts, textInputEditTexts, autoCompleteTextView, checkBoxes, buttons and adapters
    private void initializeViews() {
        //textInputLayouts
        textEditPropertyNameLayout = findViewById(R.id.text_edit_propertyName_layout);
        textEditPropertyTypeLayout = findViewById(R.id.text_edit_propertyType_layout);
        textEditCompleteAddressLayout = findViewById(R.id.text_edit_completeAddress_layout);
        textEditProprietorNameLayout = findViewById(R.id.text_edit_proprietorName_layout);
        textEditLandlordNameLayout = findViewById(R.id.text_edit_landlordName_layout);
        textEditLandlordPhoneNumberLayout = findViewById(R.id.text_edit_landlord_phoneNumber_layout);
        textEditMinimumPriceLayout = findViewById(R.id.text_edit_minimumPrice_layout);
        textEditMaximumPriceLayout = findViewById(R.id.text_edit_maximumPrice_layout);
        //textInputEditTexts
        textEditPropertyName = findViewById(R.id.text_edit_propertyName);
        textEditPropertyType = findViewById(R.id.text_edit_propertyType);
        textEditCompleteAddress = findViewById(R.id.text_edit_completeAddress);
        textEditProprietorName = findViewById(R.id.text_edit_proprietorName);
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
        btnEditMapLocation = findViewById(R.id.btnEditMapLocation);
        btnCancelEditProperty = findViewById(R.id.btnCancelEditProperty);
        btnDeleteProperty = findViewById(R.id.imageViewDeleteProperty);
        imageButtonBackToChooseEdit = findViewById(R.id.imageButtonBackToChooseEdit);
        //progressDialog
        progressDialog = new CustomProgressDialog(this);

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
        String proprietorName = property.getProprietorName();
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
        textEditProprietorName.setText(proprietorName);
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
                if (task.isSuccessful()) {
                    Toast.makeText(EditPropertyActivity.this, "Property Successfully Edited!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditPropertyActivity.this, "Error saving edited property: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
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
                progressDialog.showProgressDialog("Deleting Property...", false);
                deleteProperty(property.getPropertyID());
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismissProgressDialog();
                        confirmationDialog.cancel();
                        Intent intent = new Intent(EditPropertyActivity.this, LandlordMainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }, 4000);

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

    public void deleteProperty(String propertyID) {

        String verificationID = property.getVerificationID();
        String barangayBusinessPermitURL = "";
         String municipalBusinessPermitURL = "";

        //first delete the rooms of this property
        database.collection("properties/" + propertyID + "/rooms")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot room : task.getResult()) {
                            database.collection("properties/" + propertyID + "/rooms")
                                    .document(room.getId())
                                    .delete();
                        }
                    }
                });

        if(verificationRequest != null) {
            //next is to delete the images of the barangay and municipal business permit from storage
            municipalBusinessPermitURL = verificationRequest.getMunicipalBusinessPermitImageURL();

            StorageReference municipalBPRef = storage.getReferenceFromUrl(municipalBusinessPermitURL);

            municipalBPRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    //business permit has been deleted
                }
            });


            //next is to delete the verification request linked to this property (if issued)
            if(!verificationID.equals("")) {
                database.collection("verificationRequests").document(verificationID)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        });
            }
        }

        //lastly is to delete the property itself
        database.collection("properties").document(propertyID)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditPropertyActivity.this, "Property Successfully Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void openEditLocationPickerActivityForResult() {
        Intent intent = new Intent(EditPropertyActivity.this, EditLocationPickerActivity.class);

        //get property name, address,  and address coordinates (latitude and longitude to be passed in EditLocationPickerActivity)
        String propertyName = property.getName();
        String address = property.getAddress();
        double latitude = property.getLatitude();
        double longitude = property.getLongitude();

        intent.putExtra("propertyName", propertyName);
        intent.putExtra("address", address);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);

        EditLocationPickerActivityResultLauncher.launch(intent);
    }

    //this will check if the landmark is blank, if so, format location string, if not return whole woth landmark
    public String formatStringLocation(String street, String barangay, String municipality, String landmark) {
        String formattedLocationString = "";
        if(landmark.equals("")) {
            formattedLocationString = street + ", " + barangay + ", " +municipality;
        } else {
            formattedLocationString = street + ", " + barangay + ", " + municipality + ", " + landmark;
        }

        return  formattedLocationString;
    }

    public void preLoadVerificationRequest() {
        String verificationID = property.getVerificationID();
        database.collection("verificationRequests").document(verificationID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //save the verificationRequest data from the database to the verificationRequest object
                        verificationRequest = documentSnapshot.toObject(VerificationRequest.class);
                    }
                });
    }


    // TODO: Handle Activity Life Cycle
}