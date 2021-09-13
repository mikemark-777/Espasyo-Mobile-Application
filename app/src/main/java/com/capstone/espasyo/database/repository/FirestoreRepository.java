package com.capstone.espasyo.database.repository;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreRepository {
    private Application application;
    private FirebaseFirestore database;

    public FirestoreRepository(Application application) {
        this.application = application;
        database = FirebaseFirestore.getInstance();
    }
}
