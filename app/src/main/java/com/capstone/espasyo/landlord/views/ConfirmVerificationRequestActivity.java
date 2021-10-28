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
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.VerificationRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class ConfirmVerificationRequestActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseStorage storage;
    private StorageReference storageReference;

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

    //this will hold the barangay and municipal business permit to be uploaded synchronously
    private ArrayList<Uri> businessPermitURIs;

    private ArrayList<String> downloadURLs;

    // this will hold the barangay business permit-image-name and URIs
    private String barangayBusinessPermitImageName;
    private String municipalBusinessPermitImageName;
    private Uri barangayBusinessPermitImageURI;
    private Uri municipalBusinessPermitImageURI;

    //this will be the URLs of the barangay and municipal business permits that will be saved to the VerificationRequest object
    private String barangayBusinessPermitImageURL;
    private String municipalBusinessPermitImageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_confirm_verification_request);

        //initialize the progressDialog for the uploading of business permits
        progressDialog = new ProgressDialog(ConfirmVerificationRequestActivity.this);

        //initialize firebase connections
        firebaseConnection = FirebaseConnection.getInstance();
        storage = firebaseConnection.getFirebaseStorageInstance();
        storageReference = storage.getReference();

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);


        btnConfirmVerificationRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadBusinessPermits();
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

        //get the barangay and municipal businesspermit String URI and imageNames from intent
        barangayBusinessPermitImageName = intent.getStringExtra("barangayBusinessPermitImageName");
        String barangayBusinessPermitStringURI = intent.getStringExtra("barangayBusinessPermitImageURI");
        barangayBusinessPermitImageURI = Uri.parse(barangayBusinessPermitStringURI);

        municipalBusinessPermitImageName = intent.getStringExtra("barangayBusinessPermitImageName");
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

    public void uploadBusinessPermits() {
        
        Uri[] businessPermitImageURIs = new Uri[2];
        businessPermitImageURIs[0] = barangayBusinessPermitImageURI;
        businessPermitImageURIs[1] = municipalBusinessPermitImageURI;

        String[] businessPermitImageNames = new String[2];
        businessPermitImageNames[0] = barangayBusinessPermitImageName;
        businessPermitImageNames[1] = municipalBusinessPermitImageName;

            progressDialog.setTitle("Uploading Business Permits ...");
            progressDialog.show();
            downloadURLs = new ArrayList<String>();

            for (int i = 0; i < 2; i++) {
                storageReference = storage.getReference("images");
                final StorageReference businessPermitRef = storageReference.child(businessPermitImageNames[i]);
                businessPermitRef.putFile(businessPermitImageURIs[i]).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        businessPermitRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageURL = uri.toString();
                                downloadURLs.add(imageURL);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ConfirmVerificationRequestActivity.this, "Failed to Upload Image", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progressPercent  = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Percentage: " + (int) progressPercent + "%");
                    }
                });
            }

        //progressDialog.dismiss();
    }

}