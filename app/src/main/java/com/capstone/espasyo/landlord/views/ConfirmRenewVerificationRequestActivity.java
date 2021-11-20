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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ConfirmRenewVerificationRequestActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DocumentReference verificationRequestsDocumentReference;

    private VerificationRequest verificationRequest;
    private Property chosenProperty;

    private String municipalBusinessPermitImageName;
    private String municipalBusinessPermitImageURI;

    private TextView displayPropertyNameConfirmRenewVerification,
            displayAddressConfirmRenewVerification,
            displayProprietorNameConfirmRenewVerification,
            displayLandlordNameConfirmRenewVerification,
            displayLandlordPhoneNumberConfirmRenewVerification;

    private ImageView displayMunicipalBusinessPermit_Renew,
            btnBackToUploadImage;

    private Button btnConfirmRenewVerificationRequest,
            btnDiscardRenewVerificationRequest;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_confirm_renew_verification_request);

        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        storage = firebaseConnection.getFirebaseStorageInstance();

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);

        btnConfirmRenewVerificationRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachBusinessPermit(municipalBusinessPermitImageName, Uri.parse(municipalBusinessPermitImageURI));
            }
        });

    }

    public void initializeViews() {
        displayPropertyNameConfirmRenewVerification = findViewById(R.id.propertyName_confirmRenewVerification);
        displayAddressConfirmRenewVerification = findViewById(R.id.propertyAddress_confirmRenewVerification);
        displayProprietorNameConfirmRenewVerification = findViewById(R.id.proprietorName_confirmRenewVerification);
        displayLandlordNameConfirmRenewVerification = findViewById(R.id.landlordName_confirmRenewVerification);
        displayLandlordPhoneNumberConfirmRenewVerification = findViewById(R.id.landlordPhoneNumber_confirmRenewVerification);

        //business permit imageviews
        displayMunicipalBusinessPermit_Renew = findViewById(R.id.displayMunicipalBusinessPermit_confirmRenewVerification);

        //image view back button
        btnBackToUploadImage = findViewById(R.id.btn_back_to_uploadNewBP);

        //initialize buttons
        btnConfirmRenewVerificationRequest = findViewById(R.id.btnConfirmRenewVerificationRequest);
        btnDiscardRenewVerificationRequest = findViewById(R.id.btnDiscardRenewVerificationRequest);

        //initialize the progressDialog for the uploading of business permits
        progressDialog = new ProgressDialog(ConfirmRenewVerificationRequestActivity.this);
    }

    public void getDataFromIntent(Intent intent) {
        verificationRequest = intent.getParcelableExtra("initialVerificationRequest");
        chosenProperty = intent.getParcelableExtra("chosenProperty");
        municipalBusinessPermitImageName = intent.getStringExtra("imageName");
        municipalBusinessPermitImageURI = intent.getStringExtra("imageURL");

        String propertyName = chosenProperty.getName();
        String address = chosenProperty.getAddress();
        String proprietorName = chosenProperty.getProprietorName();
        //String landlordName = chosenProperty.getLandlordName();
        //String landlordPhoneNumber = chosenProperty.getLandlordPhoneNumber();

        //will display the image of municipal business permit based on the newly picked municipal business permit image
        Picasso.get()
                .load(municipalBusinessPermitImageURI)
                .placeholder(R.drawable.img_upload_business_permit)
                .into(displayMunicipalBusinessPermit_Renew);

        displayPropertyNameConfirmRenewVerification.setText(propertyName);
        displayAddressConfirmRenewVerification.setText(address);
        displayProprietorNameConfirmRenewVerification.setText(proprietorName);
        //displayLandlordNameConfirmRenewVerification.setText(landlordName);
        //displayLandlordPhoneNumberConfirmRenewVerification.setText(landlordPhoneNumber);
    }

    public void attachBusinessPermit(String municipalBusinessPermitImageName, Uri municipalBusinessPermitImageURI) {

        progressDialog.setTitle("Attaching Municipal Business Permit to Verification Request...");
        progressDialog.show();

        String requesteeID = verificationRequest.getRequesteeID();
        String propertyID = verificationRequest.getPropertyID();
        storageReference = storage.getReference("landlords/" + requesteeID + "/" + propertyID + "/verificationRequest");
        final StorageReference businessPermitRef = storageReference.child(municipalBusinessPermitImageName);
        businessPermitRef.putFile(municipalBusinessPermitImageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                businessPermitRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String municipalBusinessPermitImageURL = uri.toString();
                        //attach the image url to verification request, set classification to 'renew' and status to 'unverified'
                        verificationRequest.setMunicipalBusinessPermitImageURL(municipalBusinessPermitImageURL);
                        verificationRequest.setClassification("renew");
                        verificationRequest.setStatus("unverified");
                        updateVerificationRequest(verificationRequest);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ConfirmRenewVerificationRequestActivity.this, "Failed to Upload Image", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setMessage("Percentage: " + (int) progressPercent + "%");
            }
        });
    }

    public void updateVerificationRequest(VerificationRequest RenewedVerificationRequest) {

        String renewedVerificationRequestID = RenewedVerificationRequest.getVerificationRequestID();
        verificationRequestsDocumentReference = database.collection("verificationRequests").document(renewedVerificationRequestID);
        progressDialog.setTitle("Sending Verification Request...");
        progressDialog.show();

        verificationRequestsDocumentReference.set(RenewedVerificationRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ConfirmRenewVerificationRequestActivity.this, "Verification Request has been sent to the admin for renewal", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ConfirmRenewVerificationRequestActivity.this, LandlordMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ConfirmRenewVerificationRequestActivity.this, "Failed to Upload Verification Request.", Toast.LENGTH_SHORT).show();
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 3500);
    }
}