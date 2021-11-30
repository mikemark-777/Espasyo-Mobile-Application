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
import com.capstone.espasyo.models.Property;
import com.capstone.espasyo.models.VerificationRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ConfirmReuploadBusinessPermitImageActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DocumentReference verificationRequestsDocumentReference;

    private VerificationRequest verificationRequest;
    private Property chosenProperty;

    private String municipalBusinessPermitImageName;
    private String municipalBusinessPermitImageURI;

    private TextView displayPropertyNameConfirmReupload;

    private ImageView displayMunicipalBusinessPermit_Reupload;

    private Button btnConfirmReuploadBP, btnDiscardReuploadBP;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_confirm_reupload_business_permit_image);

        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        storage = firebaseConnection.getFirebaseStorageInstance();

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);

        btnConfirmReuploadBP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnConfirmReuploadBP.setEnabled(false);
                btnDiscardReuploadBP.setEnabled(false);
                detachOldBusinessPermit();
            }
        });

        btnDiscardReuploadBP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiscardConfirmationDialog();
            }
        });

        displayMunicipalBusinessPermit_Reupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfirmReuploadBusinessPermitImageActivity.this, PreviewImageActivity.class);
                intent.putExtra("previewImage", municipalBusinessPermitImageURI);
                startActivity(intent);
            }
        });

    }

    public void initializeViews() {
        displayPropertyNameConfirmReupload = findViewById(R.id.propertyName_municipalBP_reupload);

        //business permit imageviews
        displayMunicipalBusinessPermit_Reupload = findViewById(R.id.displayMunicipalBusinessPermit_confirmReupload);

        //initialize buttons
        btnConfirmReuploadBP = findViewById(R.id.btnConfirmReuploadBusinessPermit);
        btnDiscardReuploadBP = findViewById(R.id.btnDiscardReuploadBusinessPermit);

        //initialize the progressDialog for the uploading of business permits
        progressDialog = new ProgressDialog(ConfirmReuploadBusinessPermitImageActivity.this);
    }

    public void getDataFromIntent(Intent intent) {
        verificationRequest = intent.getParcelableExtra("initialVerificationRequest");
        municipalBusinessPermitImageName = intent.getStringExtra("imageName");
        municipalBusinessPermitImageURI = intent.getStringExtra("imageURL");

        String propertyName = verificationRequest.getPropertyName();

        //will display the image of municipal business permit based on the newly picked municipal business permit image
        Picasso.get()
                .load(municipalBusinessPermitImageURI)
                .placeholder(R.drawable.img_upload_business_permit)
                .into(displayMunicipalBusinessPermit_Reupload);

        displayPropertyNameConfirmReupload.setText(propertyName);
    }

    public void detachOldBusinessPermit() {
        String oldBusinessPermitImageURL = verificationRequest.getMunicipalBusinessPermitImageURL();
        //first is to delete the image from the storage
        StorageReference municipalBPRef = storage.getReferenceFromUrl(oldBusinessPermitImageURL);

        municipalBPRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                attachReuploadedBusinessPermit(municipalBusinessPermitImageName, Uri.parse(municipalBusinessPermitImageURI));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                btnConfirmReuploadBP.setEnabled(true);
                btnDiscardReuploadBP.setEnabled(true);
                Toast.makeText(ConfirmReuploadBusinessPermitImageActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void attachReuploadedBusinessPermit(String municipalBusinessPermitImageName, Uri municipalBusinessPermitImageURI) {

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
                        String reuploadedBusinessPermitImageURL = uri.toString();
                        //attach the image url to verification request, set classification to 'renew' and status to 'unverified'
                        verificationRequest.setMunicipalBusinessPermitImageURL(reuploadedBusinessPermitImageURL);
                        verificationRequest.setStatus("unverified");
                        verificationRequest.setDeclinedVerificationDescription(null);
                        updateVerificationRequest(verificationRequest);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                btnConfirmReuploadBP.setEnabled(true);
                btnDiscardReuploadBP.setEnabled(true);
                Toast.makeText(ConfirmReuploadBusinessPermitImageActivity.this, "Failed to Upload Image", Toast.LENGTH_LONG).show();
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

    public void updateVerificationRequest(VerificationRequest verificationRequest) {

        String verificationID = verificationRequest.getVerificationRequestID();
        verificationRequestsDocumentReference = database.collection("verificationRequests").document(verificationID);
        progressDialog.setTitle("Sending Verification Request...");
        progressDialog.show();

        verificationRequestsDocumentReference.set(verificationRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                btnConfirmReuploadBP.setEnabled(true);
                btnDiscardReuploadBP.setEnabled(true);
                Toast.makeText(ConfirmReuploadBusinessPermitImageActivity.this, "Business Permit successfully updated", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ConfirmReuploadBusinessPermitImageActivity.this, LandlordMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                btnConfirmReuploadBP.setEnabled(true);
                btnDiscardReuploadBP.setEnabled(true);
                Toast.makeText(ConfirmReuploadBusinessPermitImageActivity.this, "Failed to Upload Verification Request.", Toast.LENGTH_SHORT).show();
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 3500);
    }

    public void showDiscardConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Discard Draft")
                .setMessage("Do you want to discard re-uploaded business permit?")
                .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        discardReuploadBusinessPermit();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    public void discardReuploadBusinessPermit() {
        progressDialog.setTitle("Discarding...");
        progressDialog.show();
        // finish this activity
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(ConfirmReuploadBusinessPermitImageActivity.this, "Re-Upload Business Permit cancelled", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        }, 1500);
    }
}