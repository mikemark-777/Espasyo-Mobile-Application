package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.capstone.espasyo.models.Landlord;
import com.capstone.espasyo.models.Property;
import com.capstone.espasyo.models.VerificationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class ConfirmVerificationRequestActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DocumentReference verificationRequestsDocumentReference;

    private TextView displayPropertyNameConfirmVerification, displayAddressConfirmVerification, displayLandlordNameConfirmVerification, displayLandlordPhoneNumberConfirmVerification;
    private ImageView displayMunicipalBusinessPermit, btnBackToStep3;
    private String municipalBusinessPermitImageName = "";
    private String municipalBusinessPermitImageURI;
    private Button btnConfirmVerificationRequest, btnDiscardVerificationRequest;
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
                attachBusinessPermit(municipalBusinessPermitImageName, Uri.parse(municipalBusinessPermitImageURI));
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
                Intent intent = new Intent(ConfirmVerificationRequestActivity.this, PreviewImageActivity.class);
                intent.putExtra("previewImage", municipalBusinessPermitImageURI);
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
        chosenProperty = intent.getParcelableExtra("chosenProperty");
        municipalBusinessPermitImageName = intent.getStringExtra("imageName");
        municipalBusinessPermitImageURI = intent.getStringExtra("imageURL");

        String propertyName = chosenProperty.getName();
        String address = chosenProperty.getAddress();
        String landlordID = chosenProperty.getOwner();

        //display all data

        getLandlord(landlordID);

        //will display the image of municipal business permit based on its given URL from firebase storage
        Picasso.get()
                .load(municipalBusinessPermitImageURI)
                .placeholder(R.drawable.img_upload_business_permit)
                .into(displayMunicipalBusinessPermit);

        displayPropertyNameConfirmVerification.setText(propertyName);
        displayAddressConfirmVerification.setText(address);
    }

    public String getDateSubmitted() {
        Date currentDate = Calendar.getInstance().getTime();
        return DateFormat.getDateInstance(DateFormat.FULL).format(currentDate);
    }

    public void attachBusinessPermit(String municipalBusinessPermitImageName, Uri municipalBusinessPermitImageURI) {

        progressDialog.setTitle("Attaching Municipal Business Permit to Verification Request...");
        progressDialog.show();
        btnConfirmVerificationRequest.setEnabled(false);
        btnDiscardVerificationRequest.setEnabled(false);

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
                        //attach the image url to verification request
                        verificationRequest.setMunicipalBusinessPermitImageURL(municipalBusinessPermitImageURL);
                        verificationRequest.setClassification("new");
                        verificationRequest.setStatus("unverified");
                        verificationRequest.setDateSubmitted(getDateSubmitted());
                        uploadVerificationRequest(verificationRequest);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                btnConfirmVerificationRequest.setEnabled(true);
                btnDiscardVerificationRequest.setEnabled(true);
                Toast.makeText(ConfirmVerificationRequestActivity.this, "Failed to Upload Image", Toast.LENGTH_LONG).show();
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

    public void uploadVerificationRequest(VerificationRequest newVerificationRequest) {

        String newVerificationRequestID = newVerificationRequest.getVerificationRequestID();
        verificationRequestsDocumentReference = database.collection("verificationRequests").document(newVerificationRequestID);
        progressDialog.setTitle("Sending Verification Request...");
        progressDialog.show();

        verificationRequestsDocumentReference.set(newVerificationRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                attachVerificationRequestToProperty(newVerificationRequest);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                btnConfirmVerificationRequest.setEnabled(true);
                btnDiscardVerificationRequest.setEnabled(true);
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

    public void attachVerificationRequestToProperty(VerificationRequest verificationRequest) {

        String verificationID = verificationRequest.getVerificationRequestID();

        String propertyID = chosenProperty.getPropertyID();
        chosenProperty.setVerificationID(verificationID);

        DocumentReference propertyDocumentReference = database.collection("properties").document(propertyID);
        propertyDocumentReference.set(chosenProperty).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ConfirmVerificationRequestActivity.this, "Verification Request has been sent to the admin", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ConfirmVerificationRequestActivity.this, LandlordMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    btnConfirmVerificationRequest.setEnabled(true);
                    btnDiscardVerificationRequest.setEnabled(true);
                    Toast.makeText(ConfirmVerificationRequestActivity.this, "Error saving verification ID to property: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                btnConfirmVerificationRequest.setEnabled(true);
                btnDiscardVerificationRequest.setEnabled(true);
                Toast.makeText(ConfirmVerificationRequestActivity.this, "Error saving verification ID to property: " + e.toString(), Toast.LENGTH_SHORT).show();
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

    public void getLandlord(String landlordID) {
        DocumentReference landlordDocRef = database.collection("landlords").document(landlordID);
        landlordDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Landlord landlord = documentSnapshot.toObject(Landlord.class);
                displayLandlordDetails(landlord);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ConfirmVerificationRequestActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayLandlordDetails(Landlord landlord) {

        displayLandlordNameConfirmVerification = findViewById(R.id.landlordName_confirmVerification);
        displayLandlordPhoneNumberConfirmVerification = findViewById(R.id.landlordPhoneNumber_confirmVerification);

        String landlordName = landlord.getFirstName() + " " + landlord.getLastName();
        String landlordPhoneNumber = landlord.getPhoneNumber();

        displayLandlordNameConfirmVerification.setText(landlordName);
        displayLandlordPhoneNumberConfirmVerification.setText(landlordPhoneNumber);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}