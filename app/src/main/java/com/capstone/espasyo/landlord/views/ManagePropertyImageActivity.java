package com.capstone.espasyo.landlord.views;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.models.ImageFolder;
import com.capstone.espasyo.models.Property;
import com.capstone.espasyo.student.repository.FirebaseConnection;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemChangeListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.UUID;

public class ManagePropertyImageActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;
    private FirebaseStorage storage;

    private ImageView btnDeleteImage, btnGotoUploadImage, btnFullScreen;
    private ImageView emptyImagesDisplay;

    //storing data
    private ArrayList<String> imageList = new ArrayList<>();

    //for display of images
    private ArrayList<String> downloadedURLs = new ArrayList<>();
    private ImageFolder propertyImageFolder;

    private Property property;
    private ImageSlider imageSlider;
    private CustomProgressDialog progressDialog;

    //for deleting images
    private int imageIndex;

    private ActivityResultLauncher<Intent> uploadImageActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_manage_property_image);

        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        storage = firebaseConnection.getFirebaseStorageInstance();
        propertyImageFolder = new ImageFolder();

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);

        //will handle all the data from the UploadPropertyImageActivity
        uploadImageActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // refresh the property images
                            getImageFolderOf(property);
                        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            Toast.makeText(ManagePropertyImageActivity.this, "Upload Image Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        imageSlider.setItemChangeListener(new ItemChangeListener() {
            @Override
            public void onItemChanged(int i) {
                //for deleting the image in its index and sending it to preview image activity
                imageIndex = i;
            }
        });

        btnGotoUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagePropertyImageActivity.this, UploadPropertyImageActivity.class);
                intent.putExtra("property", property);
                intent.putExtra("imageFolder", propertyImageFolder);
                uploadImageActivityLauncher.launch(intent);
            }
        });

        //todo: must include delete in firebase storage
        btnDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!downloadedURLs.isEmpty()) {
                    showConfirmDeleteImageDialog();
                } else {
                    Toast.makeText(ManagePropertyImageActivity.this, "No image to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!downloadedURLs.isEmpty()) {
                    Intent intent = new Intent(ManagePropertyImageActivity.this, PreviewImageActivity.class);
                    intent.putExtra("previewImage", downloadedURLs.get(imageIndex));
                    startActivity(intent);
                }
            }
        });
    }

    public void initializeViews() {
        btnGotoUploadImage = findViewById(R.id.btnGotoUploadImage);
        btnDeleteImage = findViewById(R.id.btnDeleteImage);
        btnFullScreen = findViewById(R.id.btnFullScreen);
        imageSlider = findViewById(R.id.image_slider);

        emptyImagesDisplay = findViewById(R.id.emptyImagesDisplay);

        progressDialog = new CustomProgressDialog(this);
    }

    public void getDataFromIntent(Intent intent) {
        property = intent.getParcelableExtra("property");
        getImageFolderOf(property);
    }

    //fetch the imageFolder from the property
    public void getImageFolderOf(Property property) {
        String imageFolderID = property.getImageFolder();
        //will check if the property has imageFolder, if not create an imageFolder
        if (imageFolderID != null) {
            DocumentReference imageFolderDocRef = database.collection("imageFolders").document(imageFolderID);
            progressDialog.showProgressDialog("Loading images...", false);
            imageFolderDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    propertyImageFolder = documentSnapshot.toObject(ImageFolder.class);
                    displayImagesFrom(propertyImageFolder);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ManagePropertyImageActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ManagePropertyImageActivity.this, "imageFolder of property is null", Toast.LENGTH_SHORT).show();
        }
    }

    //will display images from imageFolder of the property
    public void displayImagesFrom(ImageFolder imageFolder) {
        if (imageFolder != null) {
            downloadedURLs = imageFolder.getImages();
            ArrayList<SlideModel> imageSlides = new ArrayList<>();

            if (!downloadedURLs.isEmpty()) {
                emptyImagesDisplay.setVisibility(View.GONE);
                for (String url : downloadedURLs) {
                    imageSlides.add(new SlideModel(url, ScaleTypes.CENTER_INSIDE));
                }
                imageSlider.setImageList(imageSlides);
                progressDialog.dismissProgressDialog();
            } else {
                imageSlider.setImageList(imageSlides);
                emptyImagesDisplay.setVisibility(View.VISIBLE);
                progressDialog.dismissProgressDialog();
            }
        } else {
            Toast.makeText(ManagePropertyImageActivity.this, "NULL", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteImage() {
        //first is to get the imageURL from the selected image in the arraylist
        String imageToDelete = downloadedURLs.get(imageIndex);
        //second is to delete the image in firebase storage
        StorageReference imageToDeleteRef = storage.getReferenceFromUrl(imageToDelete);
        imageToDeleteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //delete it in the list of images in the imageFolder object
                downloadedURLs.remove(imageToDelete);
                propertyImageFolder.setImages(downloadedURLs);
                updateImageFolder(propertyImageFolder);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ManagePropertyImageActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void updateImageFolder(ImageFolder imageFolder) {
        DocumentReference imageFolderDocRef = database.collection("imageFolders").document(imageFolder.getFolderID());
        imageFolderDocRef.set(imageFolder).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //reload data
                getImageFolderOf(property);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ManagePropertyImageActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showConfirmDeleteImageDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteImage();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }
}