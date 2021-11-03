package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.VerificationRequest;

public class VerificationRequestDetailsActivity extends AppCompatActivity {

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

    //imageView for buttons edit and delete verification request
    private ImageView btnEditVerificationRequest,
                      btnDeleteVerificationRequest;

    //imageView for displaying business permits
    private ImageView barangayBPImageViewDisplay,
                      municipalBPImageViewDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_verification_request_details);

        Intent intent = getIntent();
        getDataFromIntent(intent);

    }

    public void getDataFromIntent(Intent intent) {
        verificationRequest = intent.getParcelableExtra("chosenVerificationRequest");
    }
}