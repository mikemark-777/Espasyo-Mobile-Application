package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.LandlordMainActivity;
import com.capstone.espasyo.models.Property;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AddPropertyActivity extends AppCompatActivity {

    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private DocumentReference dbProperties;

    private Button btnAddProperty,
                   btnCancel;
    private TextInputEditText phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_add_property);
        btnAddProperty = findViewById(R.id.btnAddProperty);
        btnCancel = findViewById(R.id.btnCancel);
        phoneNumber = findViewById(R.id.text_input_landlord_phoneNumber);



        btnAddProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // CREATE SAMPLE PROPERTY OBJECT
                Property newProperty = new Property(
                        UUID.randomUUID().toString(),
                        false,
                        "Apartment",
                        "Mike's Apartment",
                        "District VI, Bayombong, Nueva Vizcaya",
                        "Mike Marcos",
                        "+9368530700"
                );

                // TESTING PURPOSES
                dbProperties = database.collection("properties").document("mm1");

                dbProperties.set(newProperty).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(AddPropertyActivity.this, "Property Successfully Added", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddPropertyActivity.this, LandlordMainActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddPropertyActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddPropertyActivity.this, LandlordMainActivity.class));
                finish();
            }
        });

    }

    //TODO: Add input validations

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Toast.makeText(this, "Pressing back button", Toast.LENGTH_SHORT).show();
    }
}