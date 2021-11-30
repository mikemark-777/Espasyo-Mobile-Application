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
import android.widget.ImageView;
import android.widget.TextView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Property;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    private ImageView imageButtonBackToManageProperty;
    private TextView displayPropertyName;
    private CardView editPropertyCardView;
    private CardView editRoomsCardView;

    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_choose_edit);

        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();

        initializeViews();

        Intent intent = getIntent();
        selectedPropertyID = intent.getStringExtra("propertyID");

        getSelectedProperty(selectedPropertyID);

        imageButtonBackToManageProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editPropertyCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(selectedProperty != null) {
                    Intent intent = new Intent(ChooseEditActivity.this, EditPropertyActivity.class);
                    intent.putExtra("property", selectedProperty);
                    startActivity(intent);
                   overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
               }
            }
        });

        editRoomsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(ChooseEditActivity.this, "editRoomsCardView clicked!", Toast.LENGTH_SHORT).show();
                if(selectedProperty != null) {
                    Intent intent = new Intent(ChooseEditActivity.this, ViewRoomsToEditActivity.class);
                    intent.putExtra("propertyID", selectedProperty.getPropertyID());
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

    }

    /*----------------------------------------------------------- functions ---------------------------------------------------------------*/

    private void initializeViews() {
        imageButtonBackToManageProperty = findViewById(R.id.imageButtonBackToManageProperty);
        editPropertyCardView = findViewById(R.id.editPropertyCardView);
        editRoomsCardView = findViewById(R.id.editRoomsCardView);
        displayPropertyName = findViewById(R.id.displayPropertyName);
        progressDialog = new CustomProgressDialog(this);
    }

    private void getSelectedProperty(String selectedPropertyID) {
        progressDialog.showProgressDialog("Loading...", false);
        DocumentReference selectedPropertyDocumentReference = database.collection("properties").document(selectedPropertyID);
        selectedPropertyDocumentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    selectedProperty = task.getResult().toObject(Property.class);
                    displayPropertyName(selectedProperty);
                }
            }
        });
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if(progressDialog.isShowing()) {
                    progressDialog.dismissProgressDialog();
                }
            }
        }, 1000);
    }

    public void displayPropertyName(Property property) {
        String propertyName = property.getName();
        displayPropertyName.setText(propertyName);
    }

    // TODO: Handle Activity Life Cycle

    @Override
    protected void onRestart() {
        super.onRestart();
        getSelectedProperty(selectedPropertyID);
    }
}