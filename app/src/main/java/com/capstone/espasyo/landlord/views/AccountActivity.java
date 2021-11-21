package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.MainActivity;
import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.LandlordMainActivity;
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Landlord;
import com.capstone.espasyo.models.VerificationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AccountActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;
    private FirebaseStorage storage;

    private Landlord landlord;

    private ImageView exitLandlordAccountPage;
    private TextView displayLandlordName, displayLandlordEmail;
    private CardView btnChangeName, btnChangePassword, btnChangePhoneNumber, btnDeleteAccount;
    private CustomProgressDialog progressDialog;

    public final String SHARED_PREFS = "sharedPrefs";
    public final String USER_ROLE = "userRole";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_account);
        //initialize firebase
        firebaseConnection = FirebaseConnection.getInstance();
        firebaseAuth = firebaseConnection.getFirebaseAuthInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        storage = firebaseConnection.getFirebaseStorageInstance();

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
                Intent intent = new Intent(AccountActivity.this, ChangePasswordActivity.class);
                intent.putExtra("landlord", landlord);
                startActivity(intent);
            }
        });

        btnChangePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, ChangePhoneNumberActivity.class);
                intent.putExtra("landlord", landlord);
                startActivity(intent);
            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDeleteDialog();
            }
        });

        exitLandlordAccountPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

    //=============== DELETE ACCOUNT ========================

    public void deleteAccount() {

        String landlordID = landlord.getLandlordID();

        //first delete the rooms of landlord's properties
        CollectionReference propertyCollectionRef = database.collection("properties");
        propertyCollectionRef.whereEqualTo("owner", landlordID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot property : task.getResult()) {
                            //Log.d("DELETE ACCOUNT", "ROOMS TO DELETE FOR " + property.getId() + ": ");
                            database.collection("properties/" + property.getId() + "/rooms")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            for (QueryDocumentSnapshot room : task.getResult()) {

                                                // Log.d("DELETE ACCOUNT", "ROOM " + room.getId());
                                                database.collection("properties/" + property.getId() + "/rooms")
                                                        .document(room.getId())
                                                        .delete();
                                            }
                                            deleteVerificationRequest(landlordID);
                                        }
                                    });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteVerificationRequest(String landlordID) {
        //first delete the verification images of landlord's properties
        CollectionReference verificationCollectionRef1 = database.collection("verificationRequests");
        verificationCollectionRef1.whereEqualTo("requesteeID", landlordID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                VerificationRequest verificationRequestObj = snapshot.toObject(VerificationRequest.class);
                                String municipalBPUrl = verificationRequestObj.getMunicipalBusinessPermitImageURL();
                                StorageReference municipalBPRef = storage.getReferenceFromUrl(municipalBPUrl);
                                municipalBPRef.delete();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        //second delete the verification request itself
        CollectionReference verificationCollectionRef2 = database.collection("verificationRequests");
        verificationCollectionRef2.whereEqualTo("requesteeID", landlordID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot verificationObj : task.getResult()) {
                                database.collection("verificationRequests")
                                        .document(verificationObj.getId())
                                        .delete();
                            }
                            deleteProperties(landlordID);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteProperties(String landlordID) {
        //delete the properties
        CollectionReference propertiesCollectionRef = database.collection("properties");
        propertiesCollectionRef.whereEqualTo("owner", landlordID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot propertyObj : task.getResult()) {
                                propertiesCollectionRef.document(propertyObj.getId()).delete();
                            }
                            deleteAccountOnLandlordsAndUsersCollection(landlordID);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteAccountOnLandlordsAndUsersCollection(String landlordID) {
        DocumentReference landlordDocRef = database.collection("landlords").document(landlordID);
        DocumentReference userDocRef = database.collection("users").document(landlordID);
        landlordDocRef.delete();
        userDocRef.delete();

        deleteAccountOnFirebase();
    }

    public void deleteAccountOnFirebase() {
        progressDialog.showProgressDialog("Deleting Account...", false);
        String landlordEmail = landlord.getEmail();
        String landlordPassword = landlord.getPassword();

        FirebaseUser landlordUser = firebaseAuth.getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(landlordEmail, landlordPassword);
        landlordUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        landlordUser.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.dismissProgressDialog();
                                            removeUserRolePreference();
                                            Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
    }

    public void enterPasswordToConfirmDeletAccount() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AccountActivity.this);
        alertDialog.setTitle("Enter Password to Confirm");
        alertDialog.setMessage("Enter Password");

        final EditText paswordInput = new EditText(AccountActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(20,
                LinearLayout.LayoutParams.MATCH_PARENT);
        paswordInput.setLayoutParams(lp);
        alertDialog.setView(paswordInput);

        alertDialog.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (paswordInput.getText().toString().equals(landlord.getPassword())) {
                            deleteAccount();
                        } else {
                            Toast.makeText(AccountActivity.this, "INCORRECT", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    public void showConfirmationDeleteDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.landlord_delete_account_confirmation_dialog, null);

        Button btnConfirmDelete = view.findViewById(R.id.btnConfirmDeleteProperty);
        Button btnCancelDelete = view.findViewById(R.id.btnCancelDeleteProperty);

        AlertDialog confirmationDialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnConfirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationDialog.cancel();
                enterPasswordToConfirmDeletAccount();
            }
        });

        btnCancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationDialog.cancel();
            }
        });

        confirmationDialog.show();
    }
    //================================================================================================================

    //remove USER_ROLE in sharedPreferences
    public void removeUserRolePreference() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(USER_ROLE);
        editor.apply();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getLandlordAccountData();
    }
}