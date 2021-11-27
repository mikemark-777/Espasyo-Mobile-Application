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
import android.app.ProgressDialog;
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
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Property;
import com.capstone.espasyo.models.VerificationRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadMunicipalBusinessPermitActivity extends AppCompatActivity {

    //for the firebase connections
    private FirebaseConnection firebaseConnection;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    //this will hold the initial verification request from STEP-1
    private VerificationRequest verificationRequest;
    private Property chosenProperty;

    private ProgressDialog progressDialog;

    private final int CAMERA_PERMISSION_CODE = 101;
    private final int STORAGE_PERMISSION_CODE = 201;

    private ImageView municipalBusinessPermitImageView;
    private String municipalBusinessPermitImageName = "";
    private Uri municipalBusinessPermitImageURI;

    private Button btnBack;
    private Button btnNext;
    private Button btnChooseImage;
    private TextView propertyNameDisplay;

    private String currentImagePath;

    private ActivityResultLauncher<Intent> pickFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> pickFromCameraActivityResultLauncher;

    private String currentMunicipalBPImageName;
    private Uri currentMunicipalBPImageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_upload_municipal_business_permit);

        //initialize firebase connections
        firebaseConnection = FirebaseConnection.getInstance();
        storage = firebaseConnection.getFirebaseStorageInstance();
        storageReference = storage.getReference();

        initializeViews();
        requestCameraPermissions();
        Intent intent = getIntent();
        getDataFromIntent(intent);

        //will handle all the data from the gallery
        pickFromGalleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                            Uri contentUri = result.getData().getData();

                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String imageFileName = "espasyo_image_" + timeStamp + "." + getFileExtenstion(contentUri);

                            //set municipalBusinessPermitImageName to global variable and image Uri to municipalBusinessPermitImageView
                            municipalBusinessPermitImageName = imageFileName;
                            municipalBusinessPermitImageURI = contentUri;
                            municipalBusinessPermitImageView.setImageURI(municipalBusinessPermitImageURI);

                        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            Toast.makeText(UploadMunicipalBusinessPermitActivity.this, "FROM GALLERY: Picture not picked", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        //will handle all the data from the camera
        pickFromCameraActivityResultLauncher = registerForActivityResult(
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
                            municipalBusinessPermitImageView.setImageURI(municipalBusinessPermitImageURI);

                        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            Toast.makeText(UploadMunicipalBusinessPermitActivity.this, "FROM CAMERA: Picture not picked", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        //will open choice where to get the image
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageSource();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!municipalBusinessPermitImageName.equals("") && !municipalBusinessPermitImageURI.equals(Uri.EMPTY)) {
                    showDiscardDialog();
                } else {
                    String municipalBPRUrl = verificationRequest.getMunicipalBusinessPermitImageURL();
                    Intent intent = new Intent();
                    intent.putExtra("municipalBPRUrl", municipalBPRUrl);
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
            }
        });

        //will open confirmation dialog to confirm that the image will be attached
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!municipalBusinessPermitImageName.equals("") && !municipalBusinessPermitImageURI.equals(Uri.EMPTY)) {
                    Intent intent = new Intent(UploadMunicipalBusinessPermitActivity.this, ConfirmVerificationRequestActivity.class);
                    intent.putExtra("initialVerificationRequest", verificationRequest);
                    intent.putExtra("chosenProperty", chosenProperty);
                    intent.putExtra("imageName", municipalBusinessPermitImageName);
                    intent.putExtra("imageURL", municipalBusinessPermitImageURI.toString());
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    Toast.makeText(UploadMunicipalBusinessPermitActivity.this, "Please pick an image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //will preview the image
        municipalBusinessPermitImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (municipalBusinessPermitImageURI != null) {
                    String municipalBPUrl = municipalBusinessPermitImageURI.toString();
                    Intent intent = new Intent(UploadMunicipalBusinessPermitActivity.this, PreviewImageActivity.class);
                    intent.putExtra("previewImage", municipalBPUrl);
                    startActivity(intent);
                }

            }
        });
    }

    public void initializeViews() {
        municipalBusinessPermitImageView = findViewById(R.id.imageview_municipalBP);
        btnBack = findViewById(R.id.btn_back_to_step2);
        btnNext = findViewById(R.id.btn_next_to_confirmation);
        btnChooseImage = findViewById(R.id.btnChooseImage_municipalBP);

        propertyNameDisplay = findViewById(R.id.propertyName_municipalBP);

        //initialize the progressDialog for the uploading of business permits
        progressDialog = new ProgressDialog(UploadMunicipalBusinessPermitActivity.this);
    }

    public void getDataFromIntent(Intent intent) {
        verificationRequest = intent.getParcelableExtra("initialVerificationRequest");
        chosenProperty = intent.getParcelableExtra("chosenProperty");
        String propertyName = verificationRequest.getPropertyName();

        propertyNameDisplay.setText(propertyName);
    }

    //will let user choose where to get the image
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
                openGallery();
                chooseImageSourceDialog.dismiss();
            }
        });

        //image button to select image in camera
        btnImageSelectFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
                chooseImageSourceDialog.dismiss();
            }
        });
    }

    public void openGallery() {
        // will check if the storage access is granted by the user
        if (ContextCompat.checkSelfPermission(UploadMunicipalBusinessPermitActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickFromGalleryActivityResultLauncher.launch(openGalleryIntent);
        } else {
            requestStoragePermission();
        }
    }

    private void openCamera() {
        // will check if the camera access is granted by the user
        if (ContextCompat.checkSelfPermission(UploadMunicipalBusinessPermitActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //makes sure that there's a camera activity to handle the intent
            if (openCameraIntent.resolveActivity(getPackageManager()) != null) {
                //create the file where the photo will go
                File imageFile = null;
                try {
                    imageFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //continue only if the File was successfully created
                if (imageFile != null) {
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
        String imageFileName = "espasyo_image_" + timeStamp;
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
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    public void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This permissions are needed to access the your gallery")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(UploadMunicipalBusinessPermitActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }

    public void requestCameraPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This permissions are needed to access your camera.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(UploadMunicipalBusinessPermitActivity.this, new String[]{Manifest.permission.CAMERA,
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //will do nothing since camera and other permissions are granted
            } else {
                Toast.makeText(UploadMunicipalBusinessPermitActivity.this, "Camera Permission is required to Use Camera", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //will do nothing since storage permission is granted
            } else {
                Toast.makeText(UploadMunicipalBusinessPermitActivity.this, "Storage Permission is required to Access Storage", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void showDiscardDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Discard Image")
                .setMessage("Are you sure you want to discard the chosen image?")
                .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    @Override
    public void onBackPressed() {
        if (!municipalBusinessPermitImageName.equals("") && !municipalBusinessPermitImageURI.equals(Uri.EMPTY)) {
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }
}