package com.capstone.espasyo.landlord.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Property;

import java.util.ArrayList;

public class ManagePropertyViewModel extends ViewModel {

    private static final String TAG = "ManagePropertyViewModel";

    private FirebaseConnection mRepo;
    private MutableLiveData<ArrayList<Property>> mOwnedProperty;
    public ManagePropertyViewModel() {
        //empty constructor **
    }

    public void init() {
        if(mOwnedProperty != null) {
            Log.d(TAG, "mOwnedProperty: not null");
        }
        Log.d(TAG, "mOwnedProperty: null");
        mRepo =  FirebaseConnection.getInstance();
        mOwnedProperty = mRepo.getOwnedProperty();
    }

    public LiveData<ArrayList<Property>> getOwnedProperty() {
        Log.d(TAG, "YOU ARE HERE: getOwnedPropertY");
        return mOwnedProperty;
    }

}