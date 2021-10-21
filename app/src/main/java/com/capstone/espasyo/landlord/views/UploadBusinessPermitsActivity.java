package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.VerificationRequest;

public class UploadBusinessPermitsActivity extends AppCompatActivity {

    TextView propertyNameDisplay,
             proprietorNameDisplay,
             landlordNameDisplay,
             landlordPhoneNumberDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_upload_business_permits);

        initializeView();

        Intent intent = getIntent();
        getDataFromIntent(intent);


    }

    public void initializeView() {
        propertyNameDisplay = findViewById(R.id.propertyName_uploadBP);
        proprietorNameDisplay = findViewById(R.id.proprietorName_uploadBP);
        landlordNameDisplay = findViewById(R.id.landlordName_uploadBP);
        landlordPhoneNumberDisplay = findViewById(R.id.landlordPhoneNumber_uploadBP);
    }

    public void getDataFromIntent(Intent intent) {

        VerificationRequest verificationRequest = intent.getParcelableExtra("initialVerificationRequest");
        String propertyName = verificationRequest.getPropertyName();
        String proprietorName = verificationRequest.getProprietorName();
        String landlordName = verificationRequest.getLandlordName();
        String landlordPhoneNumber = verificationRequest.getLandlordContactNumber();

        propertyNameDisplay.setText(propertyName);
        proprietorNameDisplay.setText(proprietorName);
        landlordNameDisplay.setText(landlordName);
        landlordPhoneNumberDisplay.setText(landlordPhoneNumber);

    }


}