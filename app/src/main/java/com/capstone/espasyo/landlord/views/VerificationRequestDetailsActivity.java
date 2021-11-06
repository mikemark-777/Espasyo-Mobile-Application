package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Property;
import com.capstone.espasyo.models.VerificationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class VerificationRequestDetailsActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;
    private FirebaseStorage storage;

    //verification Object
    private VerificationRequest verificationRequest;

    //textViews for displaying propertyDetails and verificatioRequestDetails;
    private TextView propertyNameDisplay,
            propertyAddressDisplay,
            landlordNameDisplay,
            landlordPhoneNumberDisplay,
            dateSubmittedDisplay,
            dateVerifiedDisplay,
            isVerifiedDisplay;

    //textviews for previewing business permit images
    TextView btnPreviewBarangayBP,
            btnPreviewMunicipalBP;

    //button for viewing property details
    private Button btnVisitProperty;

    //imageView for buttons edit and delete verification request
    private ImageView btnDeleteVerificationRequest,
            btnBackToVerificationFragment;

    //imageView for displaying business permits
    private ImageView barangayBPImageViewDisplay,
            municipalBPImageViewDisplay;
    private CustomProgressDialog progressDialog;

    private String barangayBPUrl;
    private String municipalBPUrl;

    //this is the ID of the property linked to this verification request
    private String propertyID;
    private Property property;

    private final String VERIFIED = "Verified";
    private final String UNVERIFIED = "Unverified";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_verification_request_details);

        //initialize firebase connections
        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        storage = firebaseConnection.getFirebaseStorageInstance();

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);
        getProperty(propertyID);

        //visit property that is linked to the verification request
        btnVisitProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VerificationRequestDetailsActivity.this, PropertyDetailsActivity.class);
                intent.putExtra("property", property);
                startActivity(intent);
            }
        });

        //preview barangay business permit (able to zoom in and out)
        btnPreviewBarangayBP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VerificationRequestDetailsActivity.this, PreviewImageActivity.class);
                intent.putExtra("previewImage", barangayBPUrl);
                startActivity(intent);
            }
        });
        //preview municipal business permit (able to zoom in and out)
        btnPreviewMunicipalBP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VerificationRequestDetailsActivity.this, PreviewImageActivity.class);
                intent.putExtra("previewImage", municipalBPUrl);
                startActivity(intent);
            }
        });

        btnDeleteVerificationRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDeleteDialog();
            }
        });

        btnBackToVerificationFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void initializeViews() {
        propertyNameDisplay = findViewById(R.id.propertyName_display_VRDetails);
        propertyAddressDisplay = findViewById(R.id.propertyAddress_display_VRDetails);
        dateSubmittedDisplay = findViewById(R.id.dateSubmitted_display_VRDetails);
        dateVerifiedDisplay = findViewById(R.id.dateVerified_display_VRDetails);
        isVerifiedDisplay = findViewById(R.id.isVerified_display_VRDetails);
        barangayBPImageViewDisplay = findViewById(R.id.barangayBP_display_VRDetails);
        municipalBPImageViewDisplay = findViewById(R.id.municipalBP_display_VRDetails);

        //buttons
        btnVisitProperty = findViewById(R.id.btnVisitProperty_VRDetails);
        btnDeleteVerificationRequest = findViewById(R.id.imageButtonDeleteVerificationRequest);
        btnBackToVerificationFragment = findViewById(R.id.imageButtonBackToVerificationFragment);
        btnPreviewBarangayBP = findViewById(R.id.btnPreviewBarangayBP);
        btnPreviewMunicipalBP = findViewById(R.id.btnPreviewMunicipalBP);

        //progressDialog
        progressDialog = new CustomProgressDialog(this);
    }

    public void getDataFromIntent(Intent intent) {
        verificationRequest = intent.getParcelableExtra("chosenVerificationRequest");

        //set propertyID linked to this verificaton request
        propertyID = verificationRequest.getPropertyID();

        String propertyName = verificationRequest.getPropertyName();
        String address = verificationRequest.getPropertyAddress();
        /* String landlord = verificationRequest.getLandlord();
        String landlordPhoneNumber = verificationRequest.getLandlordPhoneNumber();*/
        String dateSubmitted = verificationRequest.getDateSubmitted();
        String dateVerified = verificationRequest.getDateVerified();
        boolean isVerified = verificationRequest.isVerified();
        barangayBPUrl = verificationRequest.getBarangayBusinessPermitImageURL();
        municipalBPUrl = verificationRequest.getMunicipalBusinessPermitImageURL();

        propertyNameDisplay.setText(propertyName);
        propertyAddressDisplay.setText(address);
        /*landlordNameDisplay.setText(landlord);
        landlordPhoneNumberDisplay.setText(landlordPhoneNumber);*/
        dateSubmittedDisplay.setText(dateSubmitted);
        dateVerifiedDisplay.setText(dateVerified);

        if (!isVerified) {
            isVerifiedDisplay.setText(UNVERIFIED);
            isVerifiedDisplay.setTextColor(this.getResources().getColor(R.color.espasyo_red_200));
        } else {
            isVerifiedDisplay.setText(VERIFIED);
            isVerifiedDisplay.setTextColor(this.getResources().getColor(R.color.espasyo_green_200));
        }

        //will display the images of barangay and municipal business permit
        Picasso.get()
                .load(barangayBPUrl)
                .placeholder(R.drawable.img_upload_business_permit)
                .into(barangayBPImageViewDisplay);
        Picasso.get()
                .load(municipalBPUrl)
                .placeholder(R.drawable.img_upload_business_permit)
                .into(municipalBPImageViewDisplay);
    }

    public void getProperty(String propertyID) {
        DocumentReference propertyDocRef = database.collection("properties").document(propertyID);

        propertyDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                property = documentSnapshot.toObject(Property.class);
            }
        });
    }

    private void deleteVerificationRequest(String verificationRequestID) {
        //first is to delete the images from the storage
        StorageReference barangayBPRef = storage.getReferenceFromUrl(barangayBPUrl);
        StorageReference municipalBPRef = storage.getReferenceFromUrl(municipalBPUrl);

        barangayBPRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                municipalBPRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });
            }
        });


        //next is to clear the verificationID attached to the property
        DocumentReference propertyDocRef = database.collection("properties").document(propertyID);

        //clear the verificationID and set the property state to unverified
        property.setVerificationID(null);
        property.setIsVerified(false);
        propertyDocRef.set(property).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //lastly is to delete the verification request itself from the verificationRequests collection
                database.collection("verificationRequests").document(verificationRequestID)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(VerificationRequestDetailsActivity.this, "Verification Request Successfully Deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
            }
        });
    }

    public void showConfirmationDeleteDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.landlord_delete_verification_confirmation_dialog, null);

        Button btnConfirmDelete = view.findViewById(R.id.btnConfirmDeleteProperty);
        Button btnCancelDelete = view.findViewById(R.id.btnCancelDeleteProperty);

        AlertDialog confirmationDialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnConfirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.showProgressDialog("Deleting Verification Request...", false);
                String verificationRequestID = verificationRequest.getVerificationRequestID();
                deleteVerificationRequest(verificationRequestID);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismissProgressDialog();
                        confirmationDialog.cancel();
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

}