package com.capstone.espasyo.landlord;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.capstone.espasyo.MainActivity;
import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.google.android.material.navigation.NavigationView;

public class LandlordMainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;

    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_main);

        drawerLayout = findViewById(R.id.landlord_drawer_layout);
        navigationView = findViewById(R.id.landlordNavigationView);
        toolbar = findViewById(R.id.landlord_toolbar);

        setSupportActionBar(toolbar);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.landlord_menuOpen, R.string.landlord_menuClose);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.nav_dashboard:
                        //go to landlord dashboard
                        Log.i("MENU_DRAWER_TAG", "GOTO DASHBOARD");
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.nav_manage:
                        //go to landlord dashboard
                        Log.i("MENU_DRAWER_TAG", "GOTO MANAGE");
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.nav_verification:
                        //go to landlord dashboard
                        Log.i("MENU_DRAWER_TAG", "GOTO VERIFICATION");
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.nav_settings:
                        //go to landlord dashboard
                        Log.i("MENU_DRAWER_TAG", "GOTO SETTINGS");
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.nav_logout:
                        //go to landlord dashboard
                        Log.i("MENU_DRAWER_TAG", "LOGOUT, GOTO LOGIN");
                        viewModel.signOut();
                        finish();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                }
                return false;
            }
        });


        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        viewModel.getLoggedStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}