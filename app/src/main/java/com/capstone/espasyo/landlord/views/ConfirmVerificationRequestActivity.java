package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ConfirmVerificationRequestActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;
    private DocumentReference verificationRequestsDocumentReference;

    private TextView displayPropertyNameConfirmVerification,
                     displayAddressConfirmVerification,
                     displayProprietorNameConfirmVerification,
                     displayLandlordNameConfirmVerification,
                     displayLandlordPhoneNumberConfirmVerification;

    private ImageView displayBarangayBusinessPermit,
                      displayMunicipalBusinessPermit;

    private Button btnConfirmVerificationRequest;

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
        database = FirebaseConnection.getInstance().getFirebaseFirestoreInstance();

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
    }

    public void initializeViews() {
        displayPropertyNameConfirmVerification = findViewById(R.id.propertyName_confirmVerification);
        displayAddressConfirmVerification = findViewById(R.id.propertyAddress_confirmVerification);
        displayProprietorNameConfirmVerification = findViewById(R.id.proprietorName_confirmVerification);
        displayLandlordNameConfirmVerification = findViewById(R.id.landlordName_confirmVerification);
        displayLandlordPhoneNumberConfirmVerification = findViewById(R.id.landlordPhoneNumber_confirmVerification);

        //business permit imageviews
        displayBarangayBusinessPermit = findViewById(R.id.displayBarangayBusinessPermit_confirmVerification);
        displayMunicipalBusinessPermit = findViewById(R.id.displayMunicipalBusinessPermit_confirmVerification);

        btnConfirmVerificationRequest = findViewById(R.id.btnConfirmVerificationRequest);
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
        String barangayBusinessPermitURL = verificationRequest.getBarangayBusinessPermitImageURL();
        String municipalBusinessPermitURL = verificationRequest.getMunicipalBusinessPermitImageURL();

        //will display the images of barangay and municipal business permit based on their given URLs from firebase storage
        Picasso.get()
                .load(barangayBusinessPermitURL)
                .placeholder(R.drawable.img_upload_business_permit)
                .into(displayBarangayBusinessPermit);

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
                //landlordMainActivity has been set its launchMode to singleTask, which will clear everything on its top once it has been started again
                Toast.makeText(ConfirmVerificationRequestActivity.this, "Verification Request has been sent to the admin", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ConfirmVerificationRequestActivity.this, LandlordMainActivity.class);
                startActivity(intent);
                finish();
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

}