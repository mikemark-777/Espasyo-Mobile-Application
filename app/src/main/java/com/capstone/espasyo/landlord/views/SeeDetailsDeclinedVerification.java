package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.VerificationRequest;

public class SeeDetailsDeclinedVerification extends AppCompatActivity {

    private VerificationRequest verificationRequest;

    private TextView declinedVerificationDescription;

    private String reason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_see_details_declined_verification);

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);
        displayDeclinedVerificationReason();
    }

    public void initializeViews() {
        declinedVerificationDescription = findViewById(R.id.declinedVerificationDescription);
    }

    public void getDataFromIntent(Intent intent) {
        verificationRequest = intent.getParcelableExtra("verificationRequest");
    }

    public void displayDeclinedVerificationReason() {
        String reason = verificationRequest.getDeclinedVerificationDescription();
        declinedVerificationDescription.setText(reason);
    }
}