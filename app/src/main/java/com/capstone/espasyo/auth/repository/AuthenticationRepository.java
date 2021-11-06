package com.capstone.espasyo.auth.repository;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.capstone.espasyo.models.Landlord;
import com.capstone.espasyo.models.Student;
import com.capstone.espasyo.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class AuthenticationRepository {

    private Application application;
    private MutableLiveData<FirebaseUser> firebaseUserMutableLiveData;
    private MutableLiveData<Boolean> userLoggedMutableLiveData;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;
    private DocumentReference dbUsers;
    private DocumentReference dbStudents;
    private DocumentReference dbLandlords;

    public AuthenticationRepository(Application application) {
        this.application = application;
        firebaseUserMutableLiveData = new MutableLiveData<>();
        userLoggedMutableLiveData = new MutableLiveData<>();
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());
        }
    }

    /* ---------------- For Authenticating Users in the System -----------------------------*/

    public MutableLiveData<FirebaseUser> getFirebaseUserMutableLiveData() {
        return firebaseUserMutableLiveData;
    }

    public MutableLiveData<Boolean> getUserLoggedMutableLiveData() {
        return userLoggedMutableLiveData;
    }

    public void registerLandlord(Landlord newLandlord) {
        /* extract email, password and userRole */
        String email = newLandlord.getEmail();
        String password = newLandlord.getPassword();
        int userRole = newLandlord.getUserRole();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());
                    String UID = firebaseAuth.getCurrentUser().getUid(); //get currentUser's UID
                    User newUser = new User(UID, email, password, userRole);
                    newLandlord.setLandlordID(UID);
                    saveToUsersCollection(newUser); //save user data to users collection
                    saveToLandlordsCollection(newLandlord); //save user data to landlord collection
                    sendEmailVerification();

                } else {
                    Toast.makeText(application, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void registerStudent(Student newStudent) {
        /* extract email, password and userRole */
        String email = newStudent.getEmail();
        String password = newStudent.getPassword();
        int userRole = newStudent.getUserRole();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());
                    String UID = firebaseAuth.getCurrentUser().getUid(); //get currentUser's UID
                    User newUser = new User(UID, email, password, userRole);
                    newStudent.setStudentID(UID);
                    saveToUsersCollection(newUser); //save user data to users collection
                    saveToStudentsCollection(newStudent); //save user data to student collection
                    sendEmailVerification();

                } else {
                    Toast.makeText(application, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void login(String email, String password) {

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());
                } else {
                    Toast.makeText(application, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void logout() {
        firebaseAuth.signOut();
        userLoggedMutableLiveData.postValue(true);
    }

    /*Re-authenticate email | Update email address*/
    public void updateEmailAddress(FirebaseUser currentUser, String currentEmail, String newEmail, String password) {

        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);// Get current auth credentials from the user for re-authentication
        currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                currentUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            String userID = currentUser.getUid();
                            //Update email in database
                            DocumentReference currentUserDocumentRef = database.collection("users").document(userID);
                            currentUserDocumentRef.update("email", newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        sendEmailVerification();
                                        firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());
                                        Toast.makeText(application, "Email Address successfully updated", Toast.LENGTH_SHORT).show();

                                        currentUserDocumentRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                                DocumentSnapshot user = value;
                                                int userRole = user.getLong("userRole").intValue();
                                                if(userRole == 2) {
                                                    updateLandlordEmail(userID, newEmail);
                                                }
                                                else if(userRole == 3) {
                                                    updateStudentEmail(userID, newEmail);
                                                }
                                            }
                                        });

                                        //TODO: Must update the email in the Firestore database as well
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(application, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }


    public void sendEmailVerification() {
        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(application, "Email Verification Successfully sent", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /*----------------------- FIRESTORE DATABASE OPERATIONS --------------------------------*/

    public void saveToUsersCollection(User newUser) {

        String UID = newUser.getUID();
        //Set the path where the data will be saved, Set the UID of the data that will be saved
        dbUsers = database.collection("users").document(UID);

        dbUsers.set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(application, "Account successfully created", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, "Failed to create account", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void saveToStudentsCollection(Student newStudent) {

        String studentID = newStudent.getStudentID();

        dbLandlords = database.collection("students").document(studentID);
        dbLandlords.set(newStudent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, "Failed to save landlord data to landlords collection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void saveToLandlordsCollection(Landlord newLandlord) {

        String landlordID = newLandlord.getLandlordID();

        dbLandlords = database.collection("landlords").document(landlordID);
        dbLandlords.set(newLandlord).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, "Failed to save landlord data to landlords collection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateStudentEmail(String studentID, String newEmail) {
        //todo: update student's document if the student changes email
        DocumentReference studentDocRef = database.collection("students").document(studentID);
        studentDocRef.update("email", newEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //will not give notification because the updateEmailAddress function will do it
            }
        });
    }

    public void updateLandlordEmail(String landlordID, String newEmail) {
        //todo: update student's document if the student changes email
        DocumentReference landlordDocRef = database.collection("landlords").document(landlordID);
        landlordDocRef.update("email", newEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //will not give notification because the updateEmailAddress function will do it
            }
        });
    }

}
