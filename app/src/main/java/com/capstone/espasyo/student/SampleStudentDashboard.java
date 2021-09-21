package com.capstone.espasyo.student;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.capstone.espasyo.MainActivity;
import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;

public class SampleStudentDashboard extends AppCompatActivity {

    /* Just a sample student dashboard for testing if the user logged in is student*/

    private Button btnLogout;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_student_dashboard);

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
                viewModel.signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}