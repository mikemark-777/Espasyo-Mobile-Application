package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Landlord;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;

    private Landlord landlord;

    private ImageView exitLandlordAccountPage;
    private TextView displayLandlordName, displayLandlordEmail;
    private CardView btnChangeName, btnChangePassword, btnChangePhoneNumber, btnDeleteAccount;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_account);
        //initialize firebase
        firebaseConnection = FirebaseConnection.getInstance();
        firebaseAuth = firebaseConnection.getFirebaseAuthInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();

        initializeViews();
        getLandlordAccountData();

        btnChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(AccountActivity.this, ChangeNameActivity.class);
               intent.putExtra("landlord", landlord);
               startActivity(intent);
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AccountActivity.this, "Change Password", Toast.LENGTH_SHORT).show();
            }
        });

        btnChangePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AccountActivity.this, "Change Phone Number", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AccountActivity.this, "Change Delete Account", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void initializeViews() {
        //imageViews
        exitLandlordAccountPage = findViewById(R.id.exitLandlordAccountPage);
        //textviews
        displayLandlordName = findViewById(R.id.displayLandlordName_landlordAccount);
        displayLandlordEmail = findViewById(R.id.displayLandlordEmail_landlordAccount);

        //cardviews
        btnChangeName = findViewById(R.id.btnChangeName_landlordAccount);
        btnChangePassword = findViewById(R.id.btnChangePassword_landlordAccount);
        btnChangePhoneNumber = findViewById(R.id.btnChangePhoneNumber_landlordAccount);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount_landlordAccount);

        //progress bars
        progressDialog = new CustomProgressDialog(AccountActivity.this);
    }


    public void getLandlordAccountData() {

        String landlordID = firebaseAuth.getCurrentUser().getUid();
        DocumentReference landlordDocRef = database.collection("landlords").document(landlordID);
        landlordDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                landlord = documentSnapshot.toObject(Landlord.class);
                displayLandlordData(landlord);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayLandlordData(Landlord landlord) {
        String landlordName = landlord.getFirstName() + " " + landlord.getLastName();
        String landlordEmail = landlord.getEmail();

        displayLandlordName.setText(landlordName);
        displayLandlordEmail.setText(landlordEmail);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getLandlordAccountData();
    }
}