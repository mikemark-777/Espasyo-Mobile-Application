package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Property;
import com.capstone.espasyo.models.VerificationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class VerificationRequestDetailsActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;
    private DocumentReference propertyDocRef;

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

    //imageView for buttons edit and delete verification request
    private ImageView btnEditVerificationRequest,
                      btnDeleteVerificationRequest;

    //imageView for displaying business permits
    private ImageView barangayBPImageViewDisplay,
                      municipalBPImageViewDisplay;

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

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);

        btnPreviewBarangayBP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(VerificationRequestDetailsActivity.this, PreviewBusinessPermitImage.class);
               intent.putExtra("businessPermit", barangayBPUrl);
               startActivity(intent);
            }
        });

        btnPreviewMunicipalBP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(VerificationRequestDetailsActivity.this, "MBP clicked", Toast.LENGTH_SHORT).show();
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

        btnPreviewBarangayBP = findViewById(R.id.btnPreviewBarangayBP);
        btnPreviewMunicipalBP = findViewById(R.id.btnPreviewMunicipalBP);
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

        if(!isVerified) {
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
        propertyDocRef = database.collection("properties").document(propertyID);

        propertyDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        property = documentSnapshot.toObject(Property.class);
                    }
                });
    }
}