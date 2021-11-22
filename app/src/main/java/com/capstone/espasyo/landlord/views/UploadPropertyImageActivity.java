package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.ImageFolder;
import com.capstone.espasyo.student.repository.FirebaseConnection;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemChangeListener;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UploadPropertyImageActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;

    private Button btnDeleteImage, btnUploadImages, btnDisplayURLs;

    private ArrayList<String> imageList = new ArrayList<>();
    private ArrayList<String> downloadedURLs = new ArrayList<>();
    private ImageFolder propertyImageFolder;

    private ImageSlider imageSlider;

    private int imageIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_upload_property_image);

        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        propertyImageFolder = new ImageFolder();

        btnDeleteImage = findViewById(R.id.btnDeleteImage);
        btnUploadImages = findViewById(R.id.btnUploadImages);
        btnDisplayURLs = findViewById(R.id.btnDisplayURLs);
        imageSlider = findViewById(R.id.image_slider);

        btnUploadImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String folderID = UUID.randomUUID().toString();
                propertyImageFolder.setFolderID(folderID);
                imageList.add("https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Image_created_with_a_mobile_phone.png/1200px-Image_created_with_a_mobile_phone.png");
                imageList.add("https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg");
                imageList.add("https://mediadesknm.com/wp-content/uploads/2018/09/photographer-698908_960_720.jpg");
                propertyImageFolder.setImages(imageList);

                DocumentReference imageFolderDocRef = database.collection("imageFolders").document(folderID);
                imageFolderDocRef.set(propertyImageFolder).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(UploadPropertyImageActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadPropertyImageActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        btnDisplayURLs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String folderID = "3621be8b-bb6f-4186-9067-fdb860053c62";
                DocumentReference imageFolderDocRef = database.collection("imageFolders").document(folderID);
                imageFolderDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ImageFolder imageFolder = documentSnapshot.toObject(ImageFolder.class);
                        display(imageFolder);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadPropertyImageActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        imageSlider.setItemChangeListener(new ItemChangeListener() {
            @Override
            public void onItemChanged(int i) {
                imageIndex = i;
                Toast.makeText(UploadPropertyImageActivity.this, "Images " + i, Toast.LENGTH_SHORT).show();
            }
        });


        //todo: must include delete in firebase storage
        btnDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               downloadedURLs.remove(imageIndex);
               //put the arraylist back to the ImageFolder object
                propertyImageFolder.setImages(downloadedURLs);
                DocumentReference propertyImageDocRef = database.collection("imageFolders").document("3621be8b-bb6f-4186-9067-fdb860053c62");
                propertyImageDocRef.set(propertyImageFolder).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(UploadPropertyImageActivity.this, "Successfully deleted image", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadPropertyImageActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    public void display(ImageFolder imageFolder) {
        if(imageFolder != null) {
            downloadedURLs = imageFolder.getImages();

            ArrayList<SlideModel> imageSlides = new ArrayList<>();

            for(String url : downloadedURLs) {
                imageSlides.add(new SlideModel(url, ScaleTypes.CENTER_INSIDE));
            }

            imageSlider.setImageList(imageSlides);
        } else {
            Toast.makeText(UploadPropertyImageActivity.this, "NULL", Toast.LENGTH_SHORT).show();
        }
    }
}