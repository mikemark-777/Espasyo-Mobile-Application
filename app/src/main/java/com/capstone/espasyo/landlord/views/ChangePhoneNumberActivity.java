package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Landlord;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePhoneNumberActivity extends AppCompatActivity {


    private FirebaseConnection firebaseConnection;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;

    //landlord object
    private Landlord landlord;

    private TextInputLayout textInputPhoneNumberLayout;
    private TextInputEditText textInputPhoneNumber;
    private Button btnChangePhoneNumber, btnCancelChangePhoneNumber;
    private ProgressBar changePhoneNumberProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_change_phone_number);

        firebaseConnection = FirebaseConnection.getInstance();
        firebaseAuth = firebaseConnection.getFirebaseAuthInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);

        btnChangePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = textInputPhoneNumber.getText().toString();

                if (isLandlordPhoneNumberValid(phoneNumber)) {
                    updateLandlordPhoneNumber(landlord, phoneNumber);
                    Toast.makeText(ChangePhoneNumberActivity.this, "Goods", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void initializeViews() {
        //textInputLayouts
        textInputPhoneNumberLayout = findViewById(R.id.text_input_landlord_phone_number_layout_change);

        //textInputEditText
        textInputPhoneNumber = findViewById(R.id.text_input_landlord_phone_number_change);

        //button
        btnChangePhoneNumber = findViewById(R.id.btnChangePhoneNumber_landlord);
        btnCancelChangePhoneNumber = findViewById(R.id.btnCancelChangePhoneNumber_landlord);

        //progress bar
        changePhoneNumberProgressBar = findViewById(R.id.changePhoneNumberProgressBar_landlord);
    }

    public void getDataFromIntent(Intent intent) {
        landlord = intent.getParcelableExtra("landlord");
        String landlordPhoneNumber = landlord.getPhoneNumber();
        textInputPhoneNumber.setText(landlordPhoneNumber);
    }

    // INPUT VALIDATIONS
    //Check if the phone number is valid
    private boolean isLandlordPhoneNumberValid(String phoneNumber) {
        if (!phoneNumber.isEmpty()) {
            if (phoneNumber.length() == 10) {
                textInputPhoneNumberLayout.setError(null);
                return true;
            } else {
                textInputPhoneNumberLayout.setError("Phone number must be 12 digit");
                return false;
            }
        } else {
            textInputPhoneNumberLayout.setError("Landlord Phone Number Required");
            return false;
        }
    }

    public void updateLandlordPhoneNumber(Landlord landlord, String updatedPhoneNumber) {
        changePhoneNumberProgressBar.setVisibility(View.VISIBLE);
        String landlordID = landlord.getLandlordID();
        landlord.setPhoneNumber(updatedPhoneNumber);
        DocumentReference landlordDocRef = database.collection("landlords").document(landlordID);

        landlordDocRef.set(landlord).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        changePhoneNumberProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(ChangePhoneNumberActivity.this, "Phone Number Successfully Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, 3000);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChangePhoneNumberActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}