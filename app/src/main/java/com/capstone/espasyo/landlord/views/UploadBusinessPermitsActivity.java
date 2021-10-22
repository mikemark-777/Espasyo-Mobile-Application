package com.capstone.espasyo.landlord.views;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.VerificationRequest;

public class UploadBusinessPermitsActivity extends AppCompatActivity {

    private ImageView barangayBusinessPermitUploadImage;
    private Uri barangayBusinessPermitImageURI;

    private TextView propertyNameDisplay,
             proprietorNameDisplay,
             landlordNameDisplay,
             landlordPhoneNumberDisplay;

    private ActivityResultLauncher<Intent> PicturePickerActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_upload_business_permits);

        initializeViews();

        Intent intent = getIntent();
        getDataFromIntent(intent);


        //will handle all the data from the LocationPickerActivity
        PicturePickerActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            barangayBusinessPermitImageURI = result.getData().getData();
                            barangayBusinessPermitUploadImage.setImageURI(barangayBusinessPermitImageURI);
                        } else if(result.getResultCode() == Activity.RESULT_CANCELED) {
                            Toast.makeText(UploadBusinessPermitsActivity.this, "Location and address not set", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        barangayBusinessPermitUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });


    }

    public void initializeViews() {

        barangayBusinessPermitUploadImage = findViewById(R.id.barangay_business_permit_image);

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

    public void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        PicturePickerActivityResultLauncher.launch(intent);
    }


}