package com.capstone.espasyo.student.views;

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
import com.capstone.espasyo.landlord.views.LandlordChangeNameActivity;
import com.capstone.espasyo.models.Landlord;
import com.capstone.espasyo.models.Student;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentChangeNameActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;

    //student object
    private Student student;
    //student current firstName and lastName
    private String firstName;
    private String lastName;

    private TextInputLayout textInputFirstNameLayout, textInputLastNameLayout;
    private TextInputEditText textInputFirstName, textInputLastName;
    private Button btnChangeName, btnCancelChangeName;
    private ProgressBar studentChangeNameProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_activity_student_change_name);

        firebaseConnection = FirebaseConnection.getInstance();
        firebaseAuth = firebaseConnection.getFirebaseAuthInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);

        btnChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fName = textInputFirstName.getText().toString();
                String lName = textInputLastName.getText().toString();

                if (areInputsValid(fName, lName)) {
                    //check if has the same input as before
                    if (isNameChanged(firstName, lastName, fName, lName)) {
                        //updated name in landlord object
                        student.setFirstName(fName);
                        student.setLastName(lName);
                        updateStudentName(student);
                    } else {
                        Toast.makeText(StudentChangeNameActivity.this, "Nothing to change", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(StudentChangeNameActivity.this, "Inputs Invalid", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancelChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void initializeViews() {
        //textInputLayouts
        textInputFirstNameLayout = findViewById(R.id.text_input_firstname_layout_studentChangeName);
        textInputLastNameLayout = findViewById(R.id.text_input_lastname_layout_studentChangeName);

        //textInputEditText
        textInputFirstName = findViewById(R.id.text_input_firstname_studentChangeName);
        textInputLastName = findViewById(R.id.text_input_lastname_studentChangeName);

        //button
        btnChangeName = findViewById(R.id.btnChangeStudentName);
        btnCancelChangeName = findViewById(R.id.btnCancelChangeStudentName);

        //progress bar
        studentChangeNameProgressBar = findViewById(R.id.studentChangeNameProgressBar);
    }

    public void getDataFromIntent(Intent intent) {
        student = intent.getParcelableExtra("student");

        firstName = student.getFirstName();
        lastName = student.getLastName();

        //display firstname and lastname to textEdit
        textInputFirstName.setText(firstName);
        textInputLastName.setText(lastName);
    }

    // Functions
    // ------ input validations -------------------------------
    public final String TAG = "TESTING";

    /* Check if firstName is empty */
    private Boolean isFirstNameValid(String firstName) {
        if (!firstName.isEmpty()) {
            textInputFirstNameLayout.setError(null);
            Log.d(TAG, "FIRSTNAME: NOT EMPTY");
            return true;
        } else {
            textInputFirstNameLayout.setError("First Name required");
            Log.d(TAG, "FIRSTNAME: EMPTY");
            return false;
        }
    }

    /* Check if lastName is empty */
    private Boolean isLastNameValid(String lastName) {
        if (!lastName.isEmpty()) {
            textInputLastNameLayout.setError(null);
            Log.d(TAG, "LASTNAME: NOT EMPTY");
            return true;
        } else {
            textInputLastNameLayout.setError("Last Name required");
            Log.d(TAG, "LASTNAME: EMPTY");
            return false;
        }
    }

    private Boolean areInputsValid(String firstName, String lastName) {

        boolean firstNameResult = isFirstNameValid(firstName);
        boolean lastNameResult = isLastNameValid(lastName);

        if (firstNameResult && lastNameResult) {
            Log.d(TAG, "CAN PROCEED: TRUE");
            return true;
        } else {
            Log.d(TAG, "CAN PROCEED: FALSE");
            return false;
        }
    }

    private Boolean isNameChanged(String currentFirstName, String currentLastName, String newFirstName, String newLastName) {
        return !currentFirstName.equals(newFirstName) || !currentLastName.equals(newLastName);
    }

    public void updateStudentName(Student updatedStudent) {
        studentChangeNameProgressBar.setVisibility(View.VISIBLE);
        String studentID = updatedStudent.getStudentID();
        DocumentReference studentDocRef = database.collection("students").document(studentID);

        //update name in  students collection
        studentDocRef.set(updatedStudent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        studentChangeNameProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(StudentChangeNameActivity.this, "Name Successfully Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, 3000);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                studentChangeNameProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(StudentChangeNameActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}