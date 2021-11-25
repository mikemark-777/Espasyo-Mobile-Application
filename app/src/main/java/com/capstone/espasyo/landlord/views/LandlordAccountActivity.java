package com.capstone.espasyo.landlord.views;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.ImageFolder;
import com.capstone.espasyo.models.Landlord;
import com.capstone.espasyo.models.Property;
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

import java.util.ArrayList;

public class LandlordAccountActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;
    private FirebaseStorage storage;

    //landlord object
    private Landlord landlord;

    private ImageView exitLandlordAccountPage;
    private TextView displayLandlordName, displayLandlordEmail;
    private CardView btnChangeName, btnChangePassword, btnChangePhoneNumber, btnDeleteAccount;
    private CustomProgressDialog progressDialog;

    public final String SHARED_PREFS = "sharedPrefs";
    public final String USER_ROLE = "userRole";

    private ActivityResultLauncher<Intent> ChangeNameActivityResultLauncher;
    private ActivityResultLauncher<Intent> ChangePasswordActivityResultLauncher;
    private ActivityResultLauncher<Intent> ChangePhoneNumberActivityResultLauncher;

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
                Intent intent = new Intent(LandlordAccountActivity.this, LandlordChangeNameActivity.class);
                intent.putExtra("landlord", landlord);
                ChangeNameActivityResultLauncher.launch(intent);
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandlordAccountActivity.this, LandlordChangePasswordActivity.class);
                intent.putExtra("landlord", landlord);
                ChangePasswordActivityResultLauncher.launch(intent);
            }
        });

        btnChangePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandlordAccountActivity.this, LandlordChangePhoneNumberActivity.class);
                intent.putExtra("landlord", landlord);
                ChangePhoneNumberActivityResultLauncher.launch(intent);
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

        //will handle the result if the admin has reset his name or not
        ChangeNameActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            getLandlordAccountData();
                        }
                    }
                });

        //will handle the result if the admin has reset his password or not
        ChangePasswordActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            getLandlordAccountData();
                        }
                    }
                });

        //will handle the result if the admin has reset his password or not
        ChangePhoneNumberActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            getLandlordAccountData();
                        }
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
        progressDialog = new CustomProgressDialog(LandlordAccountActivity.this);
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
                Toast.makeText(LandlordAccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
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
                enterPasswordToConfirmDeleteAccount();
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

    public void enterPasswordToConfirmDeleteAccount() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LandlordAccountActivity.this);
        alertDialog.setTitle("Delete Account");
        alertDialog.setMessage("Enter Password to delete");

        final EditText paswordInput = new EditText(LandlordAccountActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(20, LinearLayout.LayoutParams.MATCH_PARENT);
        paswordInput.setLayoutParams(lp);
        alertDialog.setView(paswordInput);

        alertDialog.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (paswordInput.getText().toString().equals(landlord.getPassword())) {
                            deleteAccount();
                        } else {
                            Toast.makeText(LandlordAccountActivity.this, "INCORRECT", Toast.LENGTH_SHORT).show();
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

    public void deleteAccount() {

        String landlordID = landlord.getLandlordID();

        //first delete the rooms of landlord's properties
        CollectionReference propertyCollectionRef = database.collection("properties");
        propertyCollectionRef.whereEqualTo("owner", landlordID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().size() >= 1) {
                            for (QueryDocumentSnapshot property : task.getResult()) {
                                database.collection("properties/" + property.getId() + "/rooms")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                for (QueryDocumentSnapshot room : task.getResult()) {
                                                    database.collection("properties/" + property.getId() + "/rooms")
                                                            .document(room.getId())
                                                            .delete();
                                                }
                                                deleteVerificationRequest(landlordID);
                                            }
                                        });
                            }
                        } else {
                            deleteVerificationRequest(landlordID);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LandlordAccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(LandlordAccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
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
                            deleteImageFolder(landlordID);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LandlordAccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteImageFolder(String landlordID) {

        //first is to get the property and get the imageFolder
        CollectionReference propertyCollectionRef = database.collection("properties");
        propertyCollectionRef.whereEqualTo("owner", landlordID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot propertySnapshot : queryDocumentSnapshots) {
                            Property propertyObj = propertySnapshot.toObject(Property.class);
                            String imageFolderID = propertyObj.getImageFolder();
                            DocumentReference imageFolderDocRef = database.collection("imageFolders").document(imageFolderID);
                            imageFolderDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    ImageFolder imageFolder = documentSnapshot.toObject(ImageFolder.class);
                                    ArrayList<String> imageURLs = imageFolder.getImages();
                                    for(String url : imageURLs) {
                                        StorageReference imageStorageRef = storage.getReferenceFromUrl(url);
                                        imageStorageRef.delete();
                                    }
                                    imageFolderDocRef.delete();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(LandlordAccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LandlordAccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        //next is to delete the imageFolder itself
        //delete the properties
        deleteProperties(landlordID);
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
                Toast.makeText(LandlordAccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
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
                                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressDialog.dismissProgressDialog();
                                                    removeUserRolePreference();
                                                    Toast.makeText(LandlordAccountActivity.this, "Account Successfully Deleted", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(LandlordAccountActivity.this, MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }, 10000);
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LandlordAccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
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