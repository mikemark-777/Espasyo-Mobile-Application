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
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.VerificationRequest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadBusinessPermitsActivity extends AppCompatActivity {

    private final int CAMERA_PERMISSION_CODE = 101;
    private final int CAMERA_REQUEST_CODE = 102;

    private ImageView barangayBusinessPermitUploadImage;
    private Uri barangayBusinessPermitImageURI;

    private TextView propertyNameDisplay,
            proprietorNameDisplay,
            landlordNameDisplay,
            landlordPhoneNumberDisplay;

    private String currentImagePath;
    private final int REQUEST_TAKE_PHOTO = 1;

    //will handle all the data from the LocationPickerActivity
    private ActivityResultLauncher<Intent> pickFromGalleryActivityResultLauncher;

    private ActivityResultLauncher<Intent> pickFromCameraActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_upload_business_permits);

        initializeViews();

        pickFromGalleryActivityResultLauncher = registerForActivityResult(
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

        pickFromCameraActivityResultLauncher  = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                            Toast.makeText(UploadBusinessPermitsActivity.this, "getResultCode != null and is okay", Toast.LENGTH_SHORT).show();
                            //get request code from intent
                            Intent i = result.getData();
                            int requestCode = i.getIntExtra("requestCode", 0);

                            if(requestCode == CAMERA_REQUEST_CODE) {
                                File f = new File(currentImagePath);
                                barangayBusinessPermitUploadImage.setImageURI(Uri.fromFile(f));
                            } else {
                                Toast.makeText(UploadBusinessPermitsActivity.this, "Wrong requestCode", Toast.LENGTH_SHORT).show();
                            }
                        } else if(result.getResultCode() == Activity.RESULT_CANCELED) {
                            Toast.makeText(UploadBusinessPermitsActivity.this, "FROM CAMERA: Picture not picked", Toast.LENGTH_SHORT).show();
                        } else {
                            if(result.getData() != null) {
                                Toast.makeText(UploadBusinessPermitsActivity.this, "getData: NOT NULL", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UploadBusinessPermitsActivity.this, currentImagePath, Toast.LENGTH_SHORT).show();
                            }

                        }


                    }
                });

        Intent intent = getIntent();
        getDataFromIntent(intent);

        checkPermissions();

        barangayBusinessPermitUploadImage.setOnClickListener(v -> {
            chooseImageSource();
            /*checkPermissions();
            choosePicture();*/
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

    public void openGallery() {
        Intent openGallery = new Intent();
        openGallery.setType("image/*");
        openGallery.setAction(Intent.ACTION_GET_CONTENT);
        pickFromGalleryActivityResultLauncher.launch(openGallery);
    }

    public void chooseImageSource() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadBusinessPermitsActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.landlord_choose_image_source, null);
        builder.setView(dialogView);

        ImageView btnImageSelectFromGallery = dialogView.findViewById(R.id.btn_image_selectFromGallery);
        ImageView btnImageSelectFromCamera = dialogView.findViewById(R.id.btn_image_selectFromCamera);

        AlertDialog chooseImageSourceDialog = builder.create();
        chooseImageSourceDialog.show();

        btnImageSelectFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: check if user has granted the permission for accessing gallery
                chooseImageSourceDialog.dismiss();
                openGallery();
                Toast.makeText(UploadBusinessPermitsActivity.this, "Will select from gallery", Toast.LENGTH_SHORT).show();
            }
        });

        btnImageSelectFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                Toast.makeText(UploadBusinessPermitsActivity.this, "Will select from camera", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //makes sure that there's a camera activity to handle the intent
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //create the file where the photo will go
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //continue only if the File was successfully created
            if(imageFile != null) {
                Uri imageURI = FileProvider.getUriForFile(this,
                        "com.capstone.android.fileprovider",
                        imageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                takePictureIntent.putExtra("requestCode", CAMERA_REQUEST_CODE);
                pickFromCameraActivityResultLauncher.launch(takePictureIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        //create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        //save a file: path for use with ACTION_VIEW intents
        currentImagePath = image.getAbsolutePath();
        return image;
    }
    
    public void checkPermissions() {
        if(ContextCompat.checkSelfPermission(UploadBusinessPermitsActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            //do nothing because the permissions are granted
        } else {
            requestCameraPermission();
        }
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