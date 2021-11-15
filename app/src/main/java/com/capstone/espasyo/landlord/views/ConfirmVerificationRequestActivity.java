package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.LandlordMainActivity;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Property;
import com.capstone.espasyo.models.VerificationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class ConfirmVerificationRequestActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private DocumentReference verificationRequestsDocumentReference;

    private TextView displayPropertyNameConfirmVerification,
            displayAddressConfirmVerification,
            displayProprietorNameConfirmVerification,
            displayLandlordNameConfirmVerification,
            displayLandlordPhoneNumberConfirmVerification;

    private ImageView displayMunicipalBusinessPermit,
            btnBackToStep3;

    private Button btnConfirmVerificationRequest,
            btnDiscardVerificationRequest;

    private ProgressDialog progressDialog;

    // this is the verification request that has the data from the steps 1-3 of the compose verification process
    private VerificationRequest verificationRequest;
    private Property chosenProperty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_confirm_verification_request);

        //initialize firebase connections
        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        storage = firebaseConnection.getFirebaseStorageInstance();

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);

        btnConfirmVerificationRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dateSubmitted = getDateSubmitted();
                verificationRequest.setDateSubmitted(dateSubmitted);
                uploadVerificationRequest(verificationRequest);
            }
        });

        btnDiscardVerificationRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiscardConfirmationDialog();
            }
        });


        displayMunicipalBusinessPermit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String municipalBPUrl = verificationRequest.getMunicipalBusinessPermitImageURL();
                Intent intent = new Intent(ConfirmVerificationRequestActivity.this, PreviewImageActivity.class);
                intent.putExtra("previewImage", municipalBPUrl);
                startActivity(intent);
            }
        });

        btnBackToStep3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void initializeViews() {
        displayPropertyNameConfirmVerification = findViewById(R.id.propertyName_confirmVerification);
        displayAddressConfirmVerification = findViewById(R.id.propertyAddress_confirmVerification);
        displayProprietorNameConfirmVerification = findViewById(R.id.proprietorName_confirmVerification);
        displayLandlordNameConfirmVerification = findViewById(R.id.landlordName_confirmVerification);
        displayLandlordPhoneNumberConfirmVerification = findViewById(R.id.landlordPhoneNumber_confirmVerification);

        //business permit imageviews
        displayMunicipalBusinessPermit = findViewById(R.id.displayMunicipalBusinessPermit_confirmVerification);

        //image view back button
        btnBackToStep3 = findViewById(R.id.btn_back_to_step3);

        //initialize buttons
        btnConfirmVerificationRequest = findViewById(R.id.btnConfirmVerificationRequest);
        btnDiscardVerificationRequest = findViewById(R.id.btnDiscardVerificationRequest);

        //initialize the progressDialog for the uploading of business permits
        progressDialog = new ProgressDialog(ConfirmVerificationRequestActivity.this);
    }

    public void getDataFromIntent(Intent intent) {
        verificationRequest = intent.getParcelableExtra("initialVerificationRequest");
        chosenProperty = intent.getParcelableExtra("chosenProperty"); //for displaying purposes

        String propertyName = chosenProperty.getName();
        String address = chosenProperty.getAddress();
        String proprietorName = chosenProperty.getProprietorName();
        String landlordName = chosenProperty.getLandlordName();
        String landlordPhoneNumber = chosenProperty.getLandlordPhoneNumber();
        String municipalBusinessPermitURL = verificationRequest.getMunicipalBusinessPermitImageURL();

        //will display the image of municipal business permit based on its given URL from firebase storage
        Picasso.get()
                .load(municipalBusinessPermitURL)
                .placeholder(R.drawable.img_upload_business_permit)
                .into(displayMunicipalBusinessPermit);

        displayPropertyNameConfirmVerification.setText(propertyName);
        displayAddressConfirmVerification.setText(address);
        displayProprietorNameConfirmVerification.setText(proprietorName);
        displayLandlordNameConfirmVerification.setText(landlordName);
        displayLandlordPhoneNumberConfirmVerification.setText(landlordPhoneNumber);
    }

    public void uploadVerificationRequest(VerificationRequest newVerificationRequest) {

        String newVerificationRequestID = newVerificationRequest.getVerificationRequestID();
        verificationRequestsDocumentReference = database.collection("verificationRequests").document(newVerificationRequestID);
        progressDialog.setTitle("Sending Verification Request...");
        progressDialog.show();

        verificationRequestsDocumentReference.set(newVerificationRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                attachVerificationRequestIDToProperty(newVerificationRequestID);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ConfirmVerificationRequestActivity.this, "Failed to Upload Verification Request.", Toast.LENGTH_SHORT).show();
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 3500);
    }

    public String getDateSubmitted() {
        Date currentDate = Calendar.getInstance().getTime();
        return DateFormat.getDateInstance(DateFormat.FULL).format(currentDate);
    }

    public void attachVerificationRequestIDToProperty(String verificationID) {

        String propertyID = chosenProperty.getPropertyID();
        chosenProperty.setVerificationID(verificationID);

        DocumentReference propertyDocumentReference = database.collection("properties").document(propertyID);
        propertyDocumentReference.set(chosenProperty).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ConfirmVerificationRequestActivity.this, "Verification Request has been sent to the admin", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ConfirmVerificationRequestActivity.this, LandlordMainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ConfirmVerificationRequestActivity.this, "Error saving verification ID to property: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showDiscardConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Discard Draft")
                .setMessage("Do you want to discard your drafted verification request?")
                .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        discardVerificationRequest();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    //will clear all the data of the drafted verification request
    public void discardVerificationRequest() {
        progressDialog.setTitle("Cancelling Verification Request...");
        progressDialog.show();
        String municipalBPUrl = verificationRequest.getMunicipalBusinessPermitImageURL();

        StorageReference municipalBPRef = storage.getReferenceFromUrl(municipalBPUrl);

        municipalBPRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //business permit image has been deleted
            }
        });

        // finish this activity
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ConfirmVerificationRequestActivity.this, LandlordMainActivity.class);
                progressDialog.dismiss();
                verificationRequest = null;
                Toast.makeText(ConfirmVerificationRequestActivity.this, "Verification Request cancelled", Toast.LENGTH_SHORT).show();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }, 3500);
    }

    @Override
    public void onBackPressed() {
        //back button disabled
    }
}