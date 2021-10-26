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
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.VerificationRequest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadMunicipalBusinessPermitActivity extends AppCompatActivity {


    private final int CAMERA_PERMISSION_CODE = 101;
    private final int STORAGE_PERMISSION_CODE = 201;

    private ImageView  municipalBusinessPermitUploadImage;
    private Uri municipalBusinessPermitImageURI;

    private Button btnNextMunicipalBusinessPermit;

    private TextView propertyNameDisplay;

    private String currentImagePath;
    private String municipalBusinessPermitImageName = "";

    private ActivityResultLauncher<Intent> pickFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> pickFromCameraActivityResultLauncher;

    private VerificationRequest verificationRequest;
    private String barangayBusinessPermitImageName;
    private String barangayBusinessPermitImageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_upload_municipal_business_permit);

        initializeViews();
        requestCameraPermissions();
        //will handle all the data from the gallery
        pickFromGalleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                            Uri contentUri = result.getData().getData();

                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String imageFileName = "capture_" + timeStamp + "." + getFileExtenstion(contentUri);

                            //set imageName to global varaiable and image Uri to barangayBusinessPermitUploadImage
                            municipalBusinessPermitImageName = imageFileName;
                            municipalBusinessPermitImageURI = contentUri;
                            municipalBusinessPermitUploadImage.setImageURI(municipalBusinessPermitImageURI);

                        } else if(result.getResultCode() == Activity.RESULT_CANCELED) {
                            Toast.makeText(UploadMunicipalBusinessPermitActivity.this, "FROM GALLERY: Picture not picked", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        //will handle all the data from the camera
        pickFromCameraActivityResultLauncher  = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if (result.getResultCode() == Activity.RESULT_OK) {
                            File f = new File(currentImagePath);

                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri contentUri = Uri.fromFile(f);
                            mediaScanIntent.setData(contentUri);
                            sendBroadcast(mediaScanIntent);

                            //set the municipal business permit image to the image captured
                            municipalBusinessPermitImageName = f.getName();
                            municipalBusinessPermitImageURI = contentUri;
                            municipalBusinessPermitUploadImage.setImageURI(municipalBusinessPermitImageURI);

                        } else if(result.getResultCode() == Activity.RESULT_CANCELED) {
                            Toast.makeText(UploadMunicipalBusinessPermitActivity.this, "FROM CAMERA: Picture not picked", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        Intent intent = getIntent();
        getDataFromIntent(intent);

        municipalBusinessPermitUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageSource();
            }
        });

        btnNextMunicipalBusinessPermit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!municipalBusinessPermitImageName.equals("") && !municipalBusinessPermitImageURI.equals(Uri.EMPTY)) {

                    Intent intent = attachImageDataToIntent(barangayBusinessPermitImageName, barangayBusinessPermitImageURI, municipalBusinessPermitImageName, municipalBusinessPermitImageURI);
                    intent.putExtra("initialVerificationRequest", verificationRequest);
                    startActivity(intent);

                } else {
                    Toast.makeText(UploadMunicipalBusinessPermitActivity.this, "Please pick image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void initializeViews() {
        municipalBusinessPermitUploadImage = findViewById(R.id.municipal_business_permit_image);
        btnNextMunicipalBusinessPermit = findViewById(R.id.btn_next_municipalBusinessPermit);
        municipalBusinessPermitUploadImage = findViewById(R.id.municipal_business_permit_image);

        propertyNameDisplay = findViewById(R.id.propertyName_uploadBP);
    }

    public void getDataFromIntent(Intent intent) {
       /* Uri barangayImageUri = Uri.parse(intent.getStringExtra("barangayBusinessPermitImageURI"));
        municipalBusinessPermitUploadImage.setImageURI(barangayImageUri);*/

        verificationRequest = intent.getParcelableExtra("initialVerificationRequest");
        barangayBusinessPermitImageName = intent.getStringExtra("barangayBusinessPermitImageName");
        barangayBusinessPermitImageURI = intent.getStringExtra("barangayBusinessPermitImageURI");

        String propertyName = verificationRequest.getPropertyName();

        propertyNameDisplay.setText(propertyName);
    }

    //wwill open the dialog for the user to choose where they can get image
    public void chooseImageSource() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadMunicipalBusinessPermitActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.landlord_choose_image_source, null);
        builder.setView(dialogView);

        ImageView btnImageSelectFromGallery = dialogView.findViewById(R.id.btn_image_selectFromGallery);
        ImageView btnImageSelectFromCamera = dialogView.findViewById(R.id.btn_image_selectFromCamera);

        AlertDialog chooseImageSourceDialog = builder.create();
        chooseImageSourceDialog.show();

        //image button to select image in gallery
        btnImageSelectFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: check if user has granted the permission for accessing gallery
                openGallery();
                chooseImageSourceDialog.dismiss();
            }
        });
        //image button to select image in camera
        btnImageSelectFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: check if user has granted the permission for accessing camera
                openCamera();
                chooseImageSourceDialog.dismiss();
            }
        });
    }

    public void openGallery() {
        // will check if the storage access is granted by the user
        if(ContextCompat.checkSelfPermission(UploadMunicipalBusinessPermitActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickFromGalleryActivityResultLauncher.launch(openGalleryIntent);
        } else {
            requestStoragePermission();
        }
    }

    private void openCamera() {
        // will check if the camera access is granted by the user
        if(ContextCompat.checkSelfPermission(UploadMunicipalBusinessPermitActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //makes sure that there's a camera activity to handle the intent
            if(openCameraIntent.resolveActivity(getPackageManager()) != null) {
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
                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                    pickFromCameraActivityResultLauncher.launch(openCameraIntent);
                }
            } else {
                Toast.makeText(UploadMunicipalBusinessPermitActivity.this, "There is no program to handle this process.", Toast.LENGTH_LONG).show();
            }
        } else {
            requestCameraPermissions();
        }
    }

    //will create the image file when the user wants to get image from camera
    private File createImageFile() throws IOException {
        //create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "capture_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        //save a file: path for use with ACTION_VIEW intents
        currentImagePath = image.getAbsolutePath();
        return image;
    }

    //will get the file extension of the Uri being passed (.jpeg, .png etc.)
    public String getFileExtenstion(Uri contentUri) {
        ContentResolver c = getContentResolver();
        return MimeTypeMap.getFileExtensionFromUrl(c.getType(contentUri));
    }

    public void checkPermissions() {
        if(ContextCompat.checkSelfPermission(UploadMunicipalBusinessPermitActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            //do nothing because the permissions are granted
        } else {
            requestCameraPermissions();
        }
    }

    public Intent attachImageDataToIntent(String barangayBusinessPermitImageName, String barangayBusinessPermitImageURI, String municipalBusinessPermitImageName, Uri municipalBusinessPermitImageURI) {
        Intent intent = new Intent(UploadMunicipalBusinessPermitActivity.this, ConfirmVerificationRequestActivity.class);
        String municipalBusinessPermitStringImageURI = municipalBusinessPermitImageURI.toString();

        intent.putExtra("barangayBusinessPermitImageName", barangayBusinessPermitImageName);
        intent.putExtra("barangayBusinessPermitImageURI", barangayBusinessPermitImageURI);
        intent.putExtra("municipalBusinessPermitImageName", this.municipalBusinessPermitImageName);
        intent.putExtra("municipalBusinessPermitImageURI", municipalBusinessPermitStringImageURI);

        return  intent;
    }

    public void requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This permissions are needed to access the your gallery")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(UploadMunicipalBusinessPermitActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            STORAGE_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();

        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION_CODE);
        }
    }

    public void requestCameraPermissions() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This permissions are needed to access your camera.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(UploadMunicipalBusinessPermitActivity.this, new String[] {Manifest.permission.CAMERA,
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            CAMERA_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();

        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //will do nothing since camera and other permissions are granted
            } else {
                Toast.makeText(UploadMunicipalBusinessPermitActivity.this, "Camera Permission is required to Use Camera", Toast.LENGTH_LONG).show();
            }
        }

        if(requestCode == STORAGE_PERMISSION_CODE) {
            if(grantResults.length >  0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //will do nothing since storage permission is granted
            } else {
                Toast.makeText(UploadMunicipalBusinessPermitActivity.this, "Storage Permission is required to Access Storage", Toast.LENGTH_LONG).show();
            }
        }
    }
}