package com.capstone.espasyo.auth.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AuthenticationRepository {

    private Application application;
    private MutableLiveData<FirebaseUser> firebaseUserMutableLiveData;
    private MutableLiveData<Boolean> userLoggedMutableLiveData;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;
    private DocumentReference dbUsers;
    private DocumentReference dbStudents;
    private DocumentReference dbLandlords;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String USER_ROLE = "userRole";
    public static final String RESET_PASSWORD = "resetPassword";

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

    public void loginNewlySetPassword(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    updateNewlySetPassword(email, password);
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

    public void sendResetPasswordLink(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(application, "Link has been sent to your email", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, e.toString() , Toast.LENGTH_SHORT).show();
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

    public void updateNewlySetPassword(String email, String newPassword) {
        CollectionReference usersCollectionRef = database.collection("users");

        usersCollectionRef.whereEqualTo("email", email)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList<User> users = new ArrayList<>();
                for(DocumentSnapshot userSnapshot : queryDocumentSnapshots) {
                    User user = userSnapshot.toObject(User.class);
                    users.add(user);
                }
                User user = users.get(0);
                user.setPassword(newPassword);
                updateUserPasswordInUsersCollection(user);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateUserPasswordInUsersCollection(User updatedUser) {
        String userID = updatedUser.getUID();
        int userRole = updatedUser.getUserRole();

        DocumentReference userDocRef = database.collection("users").document(userID);
        userDocRef.set(updatedUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if(userRole == 2) {
                    updateUserPasswordInLandlordCollection(updatedUser);
                } else if(userRole == 3) {
                    updateUserPasswordInStudentCollection(updatedUser);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateUserPasswordInLandlordCollection(User updatedUser) {
        String landlordID = updatedUser.getUID();
        String newPassword = updatedUser.getPassword();
        DocumentReference landlordDocRef = database.collection("landlords").document(landlordID);
        landlordDocRef.update("password", newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                removeResetPasswordPreference();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateUserPasswordInStudentCollection(User updatedUser) {
        String studentID = updatedUser.getUID();
        String newPassword = updatedUser.getPassword();
        DocumentReference studentDocRef = database.collection("students").document(studentID);
        studentDocRef.update("password", newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                removeResetPasswordPreference();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(application, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //remove RESET_PASSWORD in sharedPreferences
    public void removeResetPasswordPreference() {
        SharedPreferences sharedPreferences = application.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(RESET_PASSWORD);
        editor.apply();
    }

}
