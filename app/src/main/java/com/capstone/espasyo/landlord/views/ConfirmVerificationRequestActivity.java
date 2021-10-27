package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.VerificationRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class ConfirmVerificationRequestActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseStorage storage;
    private StorageReference businessPermitStoragReference;

    private TextView displayPropertyNameConfirmVerification,
                     displayAddressConfirmVerification,
                     displayProprietorNameConfirmVerification,
                     displayLandlordNameConfirmVerification,
                     displayLandlordPhoneNumberConfirmVerification;

    private ImageView displayBarangayBusinessPermit,
                      displayMunicipalBusinessPermit;

    private Button btnConfirmVerificationRequest;

    private VerificationRequest verificationRequest;

    private Uri barangayBusinessPermitImageURI;
    private Uri municipalBusinessPermitImageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_confirm_verification_request);

        //initialize firebase connections
        firebaseConnection = FirebaseConnection.getInstance();
        storage = firebaseConnection.getFirebaseStorageInstance();
        businessPermitStoragReference = storage.getReference();

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);

        btnConfirmVerificationRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVerificationImages(barangayBusinessPermitImageURI);
            }
        });
    }

    public void uploadVerificationImages(Uri barangayBusinessPermit) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Business Permit Uploading...");
        progressDialog.show();

        String barangayBusinessPermitImageKey = UUID.randomUUID().toString();
        StorageReference barangayBusinessPermitRef = businessPermitStoragReference.child("images/" + barangayBusinessPermitImageKey);

        barangayBusinessPermitRef.putFile(barangayBusinessPermit)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Snackbar.make(findViewById(android.R.id.content), "Image Uploaded", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ConfirmVerificationRequestActivity.this, "Failed to Upload Image", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progressPercent  = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Percentage: " + (int) progressPercent + "%");
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
    }

    public void getDataFromIntent(Intent intent) {
        verificationRequest = intent.getParcelableExtra("initialVerificationRequest");

        String propertyName = verificationRequest.getPropertyName();
        String address = verificationRequest.getPropertyAddress();
        String proprietorName = verificationRequest.getProprietorName();
        String landlordName = verificationRequest.getLandlordName();
        String landlordPhoneNumber = verificationRequest.getLandlordContactNumber();

        //get the barangay and municipal businesspermit String URI from intent
        String barangayBusinessPermitStringURI = intent.getStringExtra("barangayBusinessPermitImageURI");
        barangayBusinessPermitImageURI = Uri.parse(barangayBusinessPermitStringURI);

        String municipalBusinessPermitStringURI = intent.getStringExtra("municipalBusinessPermitImageURI");
        municipalBusinessPermitImageURI = Uri.parse(municipalBusinessPermitStringURI);

        displayPropertyNameConfirmVerification.setText(propertyName);
        displayAddressConfirmVerification.setText(address);
        displayProprietorNameConfirmVerification.setText(proprietorName);
        displayLandlordNameConfirmVerification.setText(landlordName);
        displayLandlordPhoneNumberConfirmVerification.setText(landlordPhoneNumber);

        //set imageURI of barangay and municipal business permit
        displayBarangayBusinessPermit.setImageURI(barangayBusinessPermitImageURI);
        displayMunicipalBusinessPermit.setImageURI(municipalBusinessPermitImageURI);
    }

}