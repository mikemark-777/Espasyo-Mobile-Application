package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.Property;

import java.util.ArrayList;

public class ViewReasonLockedPropertyActivity extends AppCompatActivity {

    //Property object
    private Property property;
    private TextView reasonLockedDisplay;

    //for tips
    private CardView reason1Cardview, reason2Cardview, otherReasonCardview;
    private LinearLayout reason1Description, reason2Description, otherReasonDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_view_reason_locked_property);

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);
        displayReasonLocked();


        reason1Cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reason1Description.getVisibility() == View.VISIBLE) {
                    reason1Description.setVisibility(View.GONE);
                } else {
                    reason1Description.setVisibility(View.VISIBLE);
                }
            }
        });

        reason2Cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reason2Description.getVisibility() == View.VISIBLE) {
                    reason2Description.setVisibility(View.GONE);
                } else {
                    reason2Description.setVisibility(View.VISIBLE);
                }
            }
        });

        otherReasonCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (otherReasonDescription.getVisibility() == View.VISIBLE) {
                    otherReasonDescription.setVisibility(View.GONE);
                } else {
                    otherReasonDescription.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public void initializeViews() {

        //for displaying reason for locking the property
        reasonLockedDisplay = findViewById(R.id.reasonLockedDisplay);

        //cardviews for tips
        reason1Cardview = findViewById(R.id.reason1Cardview_locked);
        reason2Cardview = findViewById(R.id.reason2Cardview_locked);
        otherReasonCardview = findViewById(R.id.otherReasonCardview_locked);

        //linearlayouts for tips description
        reason1Description = findViewById(R.id.reason1Description_locked);
        reason2Description = findViewById(R.id.reason2Description_locked);
        otherReasonDescription = findViewById(R.id.otherReasonDescription_locked);
    }

    public void getDataFromIntent(Intent intent) {
        property = intent.getParcelableExtra("property");
    }

    public void displayReasonLocked() {
        ArrayList<String> reasonLocked = property.getReasonLocked();

        String concatReasons = "";
        for(String reason : reasonLocked) {
            concatReasons += reason + "\n";
        }

        reasonLockedDisplay.setText(concatReasons);
    }


}