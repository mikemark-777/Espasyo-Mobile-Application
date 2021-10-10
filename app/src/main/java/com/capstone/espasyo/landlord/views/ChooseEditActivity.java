package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Property;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChooseEditActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;

    private Property selectedProperty;
    private String selectedPropertyID;

    private CardView editPropertyCardView;
    private CardView editRoomsCardView;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_choose_edit);

        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();

        initializeCardViews();

        Intent intent = getIntent();
        selectedPropertyID = intent.getStringExtra("propertyID");

        getSelectedProperty(selectedPropertyID);

        editPropertyCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(selectedProperty != null) {
                    Intent intent = new Intent(ChooseEditActivity.this, EditPropertyActivity.class);
                    intent.putExtra("property", selectedProperty);
                    startActivity(intent);
               }
            }
        });

        editRoomsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChooseEditActivity.this, "editRoomsCardView clicked!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initializeCardViews() {
        editPropertyCardView = findViewById(R.id.editPropertyCardView);
        editRoomsCardView = findViewById(R.id.editRoomsCardView);
    }

    private void getSelectedProperty(String selectedPropertyID) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading");
        progressDialog.show();
        DocumentReference selectedPropertyDocumentReference = database.collection("properties").document(selectedPropertyID);
        selectedPropertyDocumentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    selectedProperty = task.getResult().toObject(Property.class);
                }
            }
        });
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if(progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }, 2000);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getSelectedProperty(selectedPropertyID);
    }
}