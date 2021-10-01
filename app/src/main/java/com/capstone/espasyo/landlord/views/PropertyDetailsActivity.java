package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.Property;

public class PropertyDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_property_details);

        Intent intent = getIntent();
        Property property = intent.getParcelableExtra("property");

        boolean isVerified = property.getIsVerified();
        String name = property.getName();
        String propertyType = property.getPropertyType();
        String address = property.getAddress();
        String landlordName = property.getLandlordName();
        String landlordPhoneNumber = property.getLandlordPhoneNumber();
        int minimumPrice = property.getMinimumPrice();
        int maximumPrice = property.getMaximumPrice();

        TextView propName = findViewById(R.id.propertyNameDisplay);
        TextView propType = findViewById(R.id.propertyTypeDisplay);
        TextView propAddress = findViewById(R.id.propertyAddressDisplay);
        TextView propLandlord = findViewById(R.id.propertyLandlordDisplay);
        TextView propLandlordPhoneNumber = findViewById(R.id.propertyLandlordPhoneNumberDisplay);
        TextView propMinimumPrice = findViewById(R.id.propertyMinimumPriceDisplay);
        TextView propMaximumPrice = findViewById(R.id.propertyMaximumPriceDisplay);
        LinearLayout verificationWarning = findViewById(R.id.verificationWarning);


        propName.setText(name);
        propType.setText(propertyType);
        propAddress.setText(address);
        propLandlord.setText(landlordName);
        propLandlordPhoneNumber.setText(landlordPhoneNumber);
        propMinimumPrice.setText(Integer.toString(minimumPrice));
        propMaximumPrice.setText(Integer.toString(maximumPrice));

        if(isVerified != true) {
            verificationWarning.setVisibility(View.VISIBLE);
        } else {
            verificationWarning.setVisibility(View.GONE);
        }

    }
}