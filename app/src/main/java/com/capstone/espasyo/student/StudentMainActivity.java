package com.capstone.espasyo.student;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.capstone.espasyo.MainActivity;
import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;

public class StudentMainActivity extends AppCompatActivity {

    /* Just a sample student dashboard for testing if the user logged in is student*/

    private Button btnLogout;
    private AuthViewModel viewModel;

    public final String SHARED_PREFS = "sharedPrefs";
    public final String USER_ROLE = "userRole";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_activity_main);

        btnLogout = findViewById(R.id.btnLogoutFromStudent);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        viewModel.getLoggedStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isUserLoggedIn) {
                if(!isUserLoggedIn) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeUserRolePreference();
                viewModel.signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    //remove USER_ROLE in sharedPreferences
    public void removeUserRolePreference() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(USER_ROLE);
        editor.apply();
    }

}