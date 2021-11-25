package com.capstone.espasyo.student.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.MainActivity;
import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Student;
import com.capstone.espasyo.student.StudentMainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
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

        bottomNavigationView.setSelectedItemId(R.id.Profile);
        bottomNavigationView.setOnItemSelectedListener(navListener);
        btnChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StudentAccountActivity.this, "Change Name", Toast.LENGTH_SHORT).show();
               /* Intent intent = new Intent(StudentAccountActivity.this, ChangeNameActivity.class);
                intent.putExtra("student", student);
                startActivity(intent);*/
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StudentAccountActivity.this, "Change Password", Toast.LENGTH_SHORT).show();
               /* Intent intent = new Intent(StudentAccountActivity.this, ChangePasswordActivity.class);
                intent.putExtra("student", student);
                startActivity(intent);*/
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
                Toast.makeText(StudentAccountActivity.this, "Delete Account", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //interface for on item selected because setOnNavigationItemSelectedListener is depracated
    private BottomNavigationView.OnItemSelectedListener navListener =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.List:
                            startActivity(new Intent(getApplicationContext(), StudentMainActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                            return true;
                        case R.id.Map:
                            startActivity(new Intent(getApplicationContext(), MapActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                            return true;
                        case R.id.Profile:
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

    //remove USER_ROLE in sharedPreferences
    public void removeUserRolePreference() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(USER_ROLE);
        editor.apply();
    }
}