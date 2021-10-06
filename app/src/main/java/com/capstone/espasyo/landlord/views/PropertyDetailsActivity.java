package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.Property;

import java.util.List;

public class PropertyDetailsActivity extends AppCompatActivity {

    private String propertyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_property_details);

        Intent intent = getIntent();
        Property property = intent.getParcelableExtra("property");

        propertyID = property.getPropertyID();

/*
        boolean isVerified = property.getIsVerified();
        String name = property.getName();
        String propertyType = property.getPropertyType();
        String address = property.getAddress();
        String landlordName = property.getLandlordName();
        String landlordPhoneNumber = property.getLandlordPhoneNumber();
        int minimumPrice = property.getMinimumPrice();
        int maximumPrice = property.getMaximumPrice();
        boolean isElectricityIncluded = property.getIsElectricityIncluded();
        boolean isWaterIncluded = property.getIsWaterIncluded();
        boolean isInternetIncluded = property.getIsInternetIncluded();
        boolean isGarbageCollectionIncluded = property.getIsGarbageCollectionIncluded();


        Button btnAddRoom;


        TextView propName = findViewById(R.id.propertyNameDisplay);
        TextView propType = findViewById(R.id.propertyTypeDisplay);
        TextView propAddress = findViewById(R.id.propertyAddressDisplay);
        TextView propLandlord = findViewById(R.id.propertyLandlordDisplay);
        TextView propLandlordPhoneNumber = findViewById(R.id.propertyLandlordPhoneNumberDisplay);
        TextView propMinimumPrice = findViewById(R.id.propertyMinimumPriceDisplay);
        TextView propMaximumPrice = findViewById(R.id.propertyMaximumPriceDisplay);
        CheckBox electricityRentInclusion = findViewById(R.id.electricityRentInclusion);
        CheckBox waterRentInclusion = findViewById(R.id.waterRentInclusion);
        CheckBox internetRentInclusion = findViewById(R.id.internetRentInclusion);
        CheckBox garbageCollectionRentInclusion = findViewById(R.id.garbageCollectionRentInclusion);
        LinearLayout verificationWarning = findViewById(R.id.verificationWarning);
        btnAddRoom = findViewById(R.id.sampleAddRoom);


        propName.setText(name);
        propType.setText(propertyType);
        propAddress.setText(address);
        propLandlord.setText(landlordName);
        propLandlordPhoneNumber.setText(landlordPhoneNumber);
        propMinimumPrice.setText(Integer.toString(minimumPrice));
        propMaximumPrice.setText(Integer.toString(maximumPrice));
        electricityRentInclusion.setChecked(isElectricityIncluded);
        waterRentInclusion.setChecked(isWaterIncluded);
        internetRentInclusion.setChecked(isInternetIncluded);
        garbageCollectionRentInclusion.setChecked(isGarbageCollectionIncluded);

        if(isVerified != true) {
            verificationWarning.setVisibility(View.VISIBLE);
        } else {
            verificationWarning.setVisibility(View.GONE);
        }

        btnAddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PropertyDetailsActivity.this, AddRoomActivity.class));
            }
        });*/

        Button btnAddRoom;
        btnAddRoom = findViewById(R.id.addRoomButton);

        btnAddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Must pass propertyID to addRoomActivity
                Intent intent = new Intent(PropertyDetailsActivity.this, AddRoomActivity.class);
                intent.putExtra("propertyID", propertyID);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Toast.makeText(PropertyDetailsActivity.this, "PropertyDetailsActivity is onRestart()", Toast.LENGTH_SHORT).show();
    }
}