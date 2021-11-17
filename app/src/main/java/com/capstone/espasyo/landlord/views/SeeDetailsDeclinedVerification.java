package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.VerificationRequest;

public class SeeDetailsDeclinedVerification extends AppCompatActivity {

    private VerificationRequest verificationRequest;

    private TextView declinedVerificationDescription;

    //for tips
    private CardView reason1Cardview, reason2Cardview, reason3Cardview, reason4Cardview, otherReasonCardview;
    private LinearLayout reason1Description, reason2Description, reason3Description, reason4Description, otherReasonDescription;

    private String reason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_see_details_declined_verification);

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);
        displayDeclinedVerificationReason();

        reason1Cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(reason1Description.getVisibility() == View.VISIBLE) {
                    reason1Description.setVisibility(View.GONE);
                } else {
                    reason1Description.setVisibility(View.VISIBLE);
                }
            }
        });

        reason2Cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(reason2Description.getVisibility() == View.VISIBLE) {
                    reason2Description.setVisibility(View.GONE);
                } else {
                    reason2Description.setVisibility(View.VISIBLE);
                }
            }
        });

        reason3Cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(reason3Description.getVisibility() == View.VISIBLE) {
                    reason3Description.setVisibility(View.GONE);
                } else {
                    reason3Description.setVisibility(View.VISIBLE);
                }
            }
        });

        reason4Cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(reason4Description.getVisibility() == View.VISIBLE) {
                    reason4Description.setVisibility(View.GONE);
                } else {
                    reason4Description.setVisibility(View.VISIBLE);
                }
            }
        });

        otherReasonCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(otherReasonDescription.getVisibility() == View.VISIBLE) {
                    otherReasonDescription.setVisibility(View.GONE);
                } else {
                    otherReasonDescription.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public void initializeViews() {
        //textviews
        declinedVerificationDescription = findViewById(R.id.declinedVerificationDescription);

        //cardviews for tips
        reason1Cardview = findViewById(R.id.reason1Cardview);
        reason2Cardview = findViewById(R.id.reason2Cardview);
        reason3Cardview = findViewById(R.id.reason3Cardview);
        reason4Cardview = findViewById(R.id.reason4Cardview);
        otherReasonCardview = findViewById(R.id.otherReasonCardview);
        //linearlayouts for tips description
        reason1Description = findViewById(R.id.reason1Description);
        reason2Description = findViewById(R.id.reason2Description);
        reason3Description = findViewById(R.id.reason3Description);
        reason4Description = findViewById(R.id.reason4Description);
        otherReasonDescription = findViewById(R.id.otherReasonDescription);
    }

    public void getDataFromIntent(Intent intent) {
        verificationRequest = intent.getParcelableExtra("verificationRequest");
    }

    public void displayDeclinedVerificationReason() {
        String reason = verificationRequest.getDeclinedVerificationDescription();
        declinedVerificationDescription.setText(reason);
    }
}