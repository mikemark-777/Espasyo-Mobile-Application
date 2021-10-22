package com.capstone.espasyo.landlord.views;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.VerificationRequest;

public class UploadBusinessPermitsActivity extends AppCompatActivity {

    private final int CAMERA_PERMISSION_CODE = 101;

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
                            Toast.makeText(UploadBusinessPermitsActivity.this, "Picture not picked", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        barangayBusinessPermitUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageSource();
                /*checkPermissions();
                choosePicture();*/
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

    public void checkPermissions() {
        if(ContextCompat.checkSelfPermission(UploadBusinessPermitsActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            //do nothing because the permissions are granted
        } else {
            requestCameraPermission();
        }
    }

    public void chooseImageSource() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadBusinessPermitsActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.landlord_choose_image_source, null);
        builder.setView(dialogView);

        ImageView btnImageSelectFromGallery = dialogView.findViewById(R.id.btn_image_selectFromGallery);
        ImageView btnImageSelectFromCamera = dialogView.findViewById(R.id.btn_image_selectFromCamera);

        AlertDialog chooseImagewSourceDialog = builder.create();
        chooseImagewSourceDialog.show();

        btnImageSelectFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UploadBusinessPermitsActivity.this, "Will select from gallery", Toast.LENGTH_SHORT).show();
            }
        });

        btnImageSelectFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UploadBusinessPermitsActivity.this, "Will select from camera", Toast.LENGTH_SHORT).show();
            }
        });

    }


    public void requestCameraPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This permissions are needed for this functionality.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(UploadBusinessPermitsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, CAMERA_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();

        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == CAMERA_PERMISSION_CODE) {
            if(grantResults.length < 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //openCamera();
            } else {
                Toast.makeText(UploadBusinessPermitsActivity.this, "Camera Permission is required to Use Camera", Toast.LENGTH_LONG).show();
            }
        }

    }
}