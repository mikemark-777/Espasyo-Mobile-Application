package com.capstone.espasyo.auth.viewmodels;

import android.app.Activity;
import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.capstone.espasyo.auth.repository.AuthenticationRepository;
import com.capstone.espasyo.models.Landlord;
import com.capstone.espasyo.models.Student;
import com.capstone.espasyo.models.User;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends AndroidViewModel {

    public AuthenticationRepository repository;
    private MutableLiveData<FirebaseUser> userData;
    private MutableLiveData<Boolean> loggedStatus;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthenticationRepository(application);
        userData = repository.getFirebaseUserMutableLiveData();
        loggedStatus = repository.getUserLoggedMutableLiveData();
    }

    /* ---------------- For Authenticating Users in the System -----------------------------*/

    public MutableLiveData<FirebaseUser> getUserData() {
        return userData;
    }

    public MutableLiveData<Boolean> getLoggedStatus() {
        return loggedStatus;
    }

    public void registerLandlord(Landlord newLandlord) {
        repository.registerLandlord(newLandlord);
    }

    public void registerStudent(Student newStudent) {
        repository.registerStudent(newStudent);
    }

    public void login(String email, String password) {
        repository.login(email, password);
    }

    public void loginNewlySetPassword(String email, String password) {
        repository.loginNewlySetPassword(email, password);
    }

    public void signOut() {
        repository.logout();
    }

    public void updateEmailAddress(FirebaseUser currentUser,String currentEmail, String newEmail, String password) {
        repository.updateEmailAddress(currentUser, currentEmail, newEmail, password);
    }

    public void sendResetPasswordLink(String email) {
        repository.sendResetPasswordLink(email);
    }

}
