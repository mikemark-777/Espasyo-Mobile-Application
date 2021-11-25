package com.capstone.espasyo.student.views;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.MainActivity;
import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.landlord.views.LandlordAccountActivity;
import com.capstone.espasyo.models.Student;
import com.capstone.espasyo.student.StudentMainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentAccountActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;

    //student object
    private Student student;

    private BottomNavigationView bottomNavigationView;

    private TextView displayStudentName, displayStudentEmail;
    private CardView btnChangeName, btnChangePassword, btnLogout, btnDeleteAccount;
    private CustomProgressDialog progressDialog;

    private AuthViewModel viewModel;

    public final String SHARED_PREFS = "sharedPrefs";
    public final String USER_ROLE = "userRole";

    private ActivityResultLauncher<Intent> ChangeNameActivityResultLauncher;
    private ActivityResultLauncher<Intent> ChangePasswordActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_activity_account);

        //initialize firebase
        firebaseConnection = FirebaseConnection.getInstance();
        firebaseAuth = firebaseConnection.getFirebaseAuthInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initializeViews();
        getStudentAccountData();

        bottomNavigationView.setSelectedItemId(R.id.Account);
        bottomNavigationView.setOnItemSelectedListener(navListener);

        btnChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentAccountActivity.this, StudentChangeNameActivity.class);
                intent.putExtra("student", student);
                ChangeNameActivityResultLauncher.launch(intent);
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentAccountActivity.this, StudentChangePasswordActivity.class);
                intent.putExtra("student", student);
                ChangePasswordActivityResultLauncher.launch(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.showProgressDialog("Logging out...", false);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog.isShowing()) {
                            removeUserRolePreference();
                            viewModel.signOut();
                            Intent intent = new Intent(StudentAccountActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }, 3000);
            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDeleteDialog();
            }
        });

        //will handle the result if the student has reset his name or not
        ChangeNameActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            getStudentAccountData();
                        }
                    }
                });

        //will handle the result if the student has reset his password or not
        ChangePasswordActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            getStudentAccountData();
                        }
                    }
                });

    }

    //interface for on item selected because setOnNavigationItemSelectedListener is depracated
    private BottomNavigationView.OnItemSelectedListener navListener = new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.List:
                            startActivity(new Intent(getApplicationContext(), StudentMainActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                            return true;
                        case R.id.Map:
                            startActivity(new Intent(getApplicationContext(), StudentMapActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                            return true;
                        case R.id.Account:
                            startActivity(new Intent(getApplicationContext(), StudentAccountActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                            return true;
                    }
                    return false;
                }
            };

    public void initializeViews() {
        //bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        //textviews
        displayStudentName = findViewById(R.id.displayStudentName_studentAccount);
        displayStudentEmail = findViewById(R.id.displayStudentEmail_studentAccount);

        //cardviews
        btnChangeName = findViewById(R.id.btnChangeName_studentAccount);
        btnChangePassword = findViewById(R.id.btnChangePassword_studentAccount);
        btnLogout = findViewById(R.id.btnLogout_studentAccount);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount_studentAccount);

        //progress bars
        progressDialog = new CustomProgressDialog(StudentAccountActivity.this);
    }

    public void getStudentAccountData() {

        String studentID = firebaseAuth.getCurrentUser().getUid();
        DocumentReference studentDocRef = database.collection("students").document(studentID);

        studentDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                student = documentSnapshot.toObject(Student.class);
                displayStudentData(student);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(StudentAccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayStudentData(Student student) {
        String studentName = student.getFirstName() + " " + student.getLastName();
        String studentEmail = student.getEmail();

        displayStudentName.setText(studentName);
        displayStudentEmail.setText(studentEmail);
    }

    public void deleteAccount() {
        String studentID = student.getStudentID();
        DocumentReference studentDocRef = database.collection("students").document(studentID);
        DocumentReference userDocRef = database.collection("users").document(studentID);
        studentDocRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                userDocRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        deleteAccountOnFirebase();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StudentAccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(StudentAccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteAccountOnFirebase() {
        progressDialog.showProgressDialog("Deleting Account...", false);
        String studentEmail = student.getEmail();
        String studentPassword = student.getPassword();

        FirebaseUser landlordUser = firebaseAuth.getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(studentEmail, studentPassword);
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
                                                    Toast.makeText(StudentAccountActivity.this, "Account Successfully Deleted", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(StudentAccountActivity.this, MainActivity.class);
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
                                Toast.makeText(StudentAccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

    public void enterPasswordToConfirmDeleteAccount() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StudentAccountActivity.this);
        alertDialog.setTitle("Delete Account");
        alertDialog.setMessage("Enter Password to delete");

        final EditText paswordInput = new EditText(StudentAccountActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(20, LinearLayout.LayoutParams.MATCH_PARENT);
        paswordInput.setLayoutParams(lp);
        alertDialog.setView(paswordInput);

        alertDialog.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (paswordInput.getText().toString().equals(student.getPassword())) {
                            deleteAccount();
                        } else {
                            Toast.makeText(StudentAccountActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
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
        View view = inflater.inflate(R.layout.student_delete_account_confirmation_dialog, null);

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

    //remove USER_ROLE in sharedPreferences
    public void removeUserRolePreference() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(USER_ROLE);
        editor.apply();
    }

}