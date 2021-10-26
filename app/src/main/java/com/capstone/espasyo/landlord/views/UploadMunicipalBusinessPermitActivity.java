package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.capstone.espasyo.R;

public class UploadMunicipalBusinessPermitActivity extends AppCompatActivity {

    private ImageView  municipalBusinessPermitUploadImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_upload_municipal_business_permit);

        initializeViews();

        Intent intent = getIntent();
        getDataFromIntent(intent);
    }

    public void getDataFromIntent(Intent intent) {
        Uri barangayImageUri = Uri.parse(intent.getStringExtra("barangayBusinessPermitImageURI"));
        municipalBusinessPermitUploadImage.setImageURI(barangayImageUri);
    }

    public void initializeViews() {
        municipalBusinessPermitUploadImage = findViewById(R.id.municipal_business_permit_image);
    }
}