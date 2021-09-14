package com.capstone.espasyo.database.repository;

import android.app.Application;

import com.capstone.espasyo.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreRepository {
    private Application application;
    private FirebaseFirestore database;

    public FirestoreRepository(Application application) {
        this.application = application;
        database = FirebaseFirestore.getInstance();
    }
    
    // Save user data on Users Collections
    public void saveNewUserData(User newUser) {

    }
}