package com.capstone.espasyo.student.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.landlord.views.LandlordChangePasswordActivity;
import com.capstone.espasyo.models.Landlord;
import com.capstone.espasyo.models.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentChangePasswordActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;

    //Student object
    private Student student;

    private TextInputLayout textInputCurrentPasswordLayout, textInputNewPasswordLayout, textInputConfirmPasswordLayout;
    private TextInputEditText textInputCurrentPassword, textInputNewPassword, textInputConfirmPassword;
    private Button btnChangePassword, btnCancelChangePassword;
    private ProgressBar changePasswordProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_activity_student_change_password);

        firebaseConnection = FirebaseConnection.getInstance();
        firebaseAuth = firebaseConnection.getFirebaseAuthInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();

        initializeViews();
        Intent intent = getIntent();
        getDataFromIntent(intent);

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get email from current student
                String email = student.getEmail();
                String currentPassword = textInputCurrentPassword.getText().toString();
                String newPassword = textInputNewPassword.getText().toString();
                String confirmPassword = textInputConfirmPassword.getText().toString();

                if (arePasswordsValid(currentPassword, newPassword, confirmPassword)) {
                    if (!arePasswordsTheSame(currentPassword, newPassword)) {
                        showConfirmationDialog(email, currentPassword, newPassword);
                    }
                }
            }
        });

        btnCancelChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

    }

    public void initializeViews() {
        //textInputLayouts
        textInputCurrentPasswordLayout = findViewById(R.id.text_input_currentPassword_layout_studentChangePassword);
        textInputNewPasswordLayout = findViewById(R.id.text_input_newPassword_layout_studentChangePassword);
        textInputConfirmPasswordLayout = findViewById(R.id.text_input_confirmPassword_layout_studentChangePassword);

        //textInputEditText
        textInputCurrentPassword = findViewById(R.id.text_input_currentPassword_studentChangePassword);
        textInputNewPassword = findViewById(R.id.text_input_newPassword_studentChangePassword);
        textInputConfirmPassword = findViewById(R.id.text_input_confirmPassword_studentChangePassword);

        //button
        btnChangePassword = findViewById(R.id.btnChangePassword_student);
        btnCancelChangePassword = findViewById(R.id.btnCancelChangePassword_student);

        //progress bar
        changePasswordProgressBar = findViewById(R.id.studentChangePasswordProgressBar);
    }

    public void getDataFromIntent(Intent intent) {
        student = intent.getParcelableExtra("student");
    }

    private boolean isCurrentPasswordValid(String currentPassword) {
        if (currentPassword.isEmpty()) {
            textInputCurrentPasswordLayout.setError("Current Password field cannot be empty");
            return false;
        } else {
            textInputCurrentPasswordLayout.setError(null);
            return true;
        }
    }

    private boolean isNewPasswordValid(String newPassword) {
        if (newPassword.isEmpty()) {
            textInputNewPasswordLayout.setError("New Password field cannot be empty");
            return false;
        } else {
            textInputNewPasswordLayout.setError(null);
            return true;
        }
    }

    /* Check if password1 and password2 are not empty and match*/
    private boolean arePasswordsValid(String currentPassword, String newPassword, String confirmPassword) {

        if (areInputsValid(currentPassword, newPassword, confirmPassword)) {
            if (newPassword.length() > 5 && confirmPassword.length() > 5) {

                if (matchPassword(newPassword, confirmPassword)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                textInputNewPasswordLayout.setError("Password must be 6-15 characters");
                textInputConfirmPasswordLayout.setError("Password must be 6-15 characters");
                return false;
            }

        } else {
            return false;
        }
    }

    /* Check if password1 and password2 are match*/
    private boolean matchPassword(String password, String confirmPassword) {

        if (password.equals(confirmPassword)) {
            return true;
        } else {
            textInputNewPasswordLayout.setError("Password Not Match");
            textInputConfirmPasswordLayout.setError("Password Not Match");
            return false;
        }
    }


    private boolean isConfirmPasswordValid(String confirmPassword) {
        if (confirmPassword.isEmpty()) {
            textInputConfirmPasswordLayout.setError("Confirm Password field cannot be empty");
            return false;
        } else {
            textInputConfirmPasswordLayout.setError(null);
            return true;
        }
    }

    private boolean areInputsValid(String currentPassword, String newPassword, String confirmPassword) {
        boolean currentPasswordResult = isCurrentPasswordValid(currentPassword);
        boolean newPasswordResult = isNewPasswordValid(newPassword);
        boolean confirmPasswordResult = isConfirmPasswordValid(confirmPassword);

        if (currentPasswordResult == true && newPasswordResult == true && confirmPasswordResult == true) {
            return true;
        } else {
            return false;
        }
    }

    public boolean arePasswordsTheSame(String currentPassword, String newPassword) {
        if (currentPassword.equals(newPassword)) {
            textInputCurrentPasswordLayout.setError("Passwords the same");
            textInputNewPasswordLayout.setError("Passwords the same");
            return true;
        } else {
            return false;
        }
    }

    public void showConfirmationDialog(String email, String currentPassword, String newPassword) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Change Password")
                .setMessage("Do your want to continue changing your password?")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updatePassword(email, currentPassword, newPassword);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    public void updatePassword(String email, String currentPassword, String newPassword) {
        changePasswordProgressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //set new password to admin Object
                                    student.setPassword(newPassword);
                                    updatePasswordOnDatabase(student);
                                }
                            });
                        } else {
                            textInputCurrentPasswordLayout.setError("Invalid Password");
                            changePasswordProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    public void updatePasswordOnDatabase(Student updatedStudent) {
        String studentID = updatedStudent.getStudentID();
        //update admin password on users and admins collection
        DocumentReference usersDocRef = database.collection("users").document(studentID);
        DocumentReference studentsDocRef = database.collection("students").document(studentID);

        studentsDocRef.set(updatedStudent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                usersDocRef.update("password", updatedStudent.getPassword()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                changePasswordProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(StudentChangePasswordActivity.this, "Password Successfully Updated", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            }
                        }, 3000);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                changePasswordProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(StudentChangePasswordActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}