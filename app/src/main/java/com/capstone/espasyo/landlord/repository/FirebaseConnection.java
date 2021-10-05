package com.capstone.espasyo.landlord.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseConnection {

    private  FirebaseAuth fAuth;
    private  FirebaseFirestore database;

    private static FirebaseConnection instance;

    //singleton pattern
    public static FirebaseConnection getInstance() {
        if(instance == null) {
            instance = new FirebaseConnection();
        }
        return instance;
    }

    public FirebaseAuth getFirebaseAuthInstance() {
        if(fAuth == null) {
            fAuth = FirebaseAuth.getInstance();
        }
        return  fAuth;
    }

    public FirebaseFirestore getFirebaseFirestoreInstance() {
        if(database == null) {
            database = FirebaseFirestore.getInstance();
        }
        return  database;
    }

}
