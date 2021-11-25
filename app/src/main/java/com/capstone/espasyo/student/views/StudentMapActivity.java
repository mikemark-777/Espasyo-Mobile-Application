package com.capstone.espasyo.student.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.capstone.espasyo.R;
import com.capstone.espasyo.student.StudentMainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class StudentMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_activity_map);

        //Initialize And Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Set List Selected
        bottomNavigationView.setSelectedItemId(R.id.Map);

        //Perform ItemSelectedListener
        bottomNavigationView.setOnItemSelectedListener(navListener);
    }

    //interface for on item selected because setOnNavigationItemSelectedListener is depracated
    private BottomNavigationView.OnItemSelectedListener navListener =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.List:
                            startActivity(new Intent(getApplicationContext() ,StudentMainActivity.class));
                            overridePendingTransition(0,0);
                            finish();
                            return true;
                        case R.id.Map:
                            startActivity(new Intent(getApplicationContext(), StudentMapActivity.class));
                            overridePendingTransition(0,0);
                            finish();
                            return true;
                        case R.id.Account:
                            startActivity(new Intent(getApplicationContext(), StudentAccountActivity.class));
                            overridePendingTransition(0,0);
                            finish();
                            return true;
                    }
                    return false;
                }
            };




}