package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.student.repository.FirebaseConnection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UploadPropertyImageActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;

    private TextView displayURLs;
    private Button btnUploadImages, btnDisplayURLs;

    private Map<String, Object> map = new HashMap<>();
    private ArrayList<String> urls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_upload_property_image);

        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();

        displayURLs = findViewById(R.id.diplayImageURLs);
        btnUploadImages = findViewById(R.id.btnUploadImages);
        btnDisplayURLs = findViewById(R.id.btnDisplayURLs);

        btnDisplayURLs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                DocumentReference imgURLsDocRef = database.collection("images").document("abcd-1234-p0p0");
                imgURLsDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            if(task.getResult() != null) {
                                DocumentSnapshot snapshot = task.getResult();
                                if(snapshot.exists()) {
                                    urls = (ArrayList<String>) snapshot.get("imageURLs");
                                    display(urls);
                                }
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadPropertyImageActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnUploadImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> imageURLs = new ArrayList<>();
                imageURLs.add("url1");
                imageURLs.add("url2");
                imageURLs.add("url3");

                // add data to a map
                Map<String, Object> map = new HashMap<>();
                map.put("imageURLs", imageURLs);

                DocumentReference imgURLsDocRef = database.collection("images").document("abcd-1234-p0p0");
                imgURLsDocRef.set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(UploadPropertyImageActivity.this, "Images Successfully uplaoaded", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    public void display(ArrayList<String> urls) {
        String urlss = "";
        for(String url : urls) {
            urlss += url + "\n";
        }
        displayURLs.setText(urlss);
    }
}