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
import android.widget.Toast;

import com.capstone.espasyo.MainActivity;
import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.capstone.espasyo.landlord.views.DashboardFragment;
import com.capstone.espasyo.landlord.views.ManagePropertyFragment;
import com.capstone.espasyo.landlord.views.SettingsActivity;
import com.capstone.espasyo.landlord.views.VerificationFragment;
import com.google.android.material.navigation.NavigationView;

public class LandlordMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;

    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_main);

        drawer = findViewById(R.id.landlord_drawer_layout);
        navigationView = findViewById(R.id.landlordNavigationView);
        toolbar = findViewById(R.id.landlord_toolbar);

        setSupportActionBar(toolbar);
        toolbar.setTitle("Dashboard");

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.landlord_menuOpen, R.string.landlord_menuClose);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.landlord_fragmentsContainer, new DashboardFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(this);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        viewModel.getLoggedStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_dashboard:
                //go to landlord dashboard
                Log.i("MENU_DRAWER_TAG", "GOTO DASHBOARD");
                toolbar.setTitle("Dashboard");
                getSupportFragmentManager().beginTransaction().replace(R.id.landlord_fragmentsContainer, new DashboardFragment()).commit();
                drawer.closeDrawer(GravityCompat.START);
                break;

            case R.id.nav_manage:
                //go to landlord dashboard
                Log.i("MENU_DRAWER_TAG", "GOTO MANAGE");
                toolbar.setTitle("Manage Property");
                getSupportFragmentManager().beginTransaction().replace(R.id.landlord_fragmentsContainer, new ManagePropertyFragment()).commit();
                drawer.closeDrawer(GravityCompat.START);
                break;

            case R.id.nav_verification:
                //go to landlord dashboard
                Log.i("MENU_DRAWER_TAG", "GOTO VERIFICATION");
                toolbar.setTitle("Verification");
                getSupportFragmentManager().beginTransaction().replace(R.id.landlord_fragmentsContainer, new VerificationFragment()).commit();
                drawer.closeDrawer(GravityCompat.START);
                break;

            case R.id.nav_settings:
                //go to landlord dashboard
                Log.i("MENU_DRAWER_TAG", "GOTO SETTINGS");
                //Toast.makeText(this, "Go to Settings", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LandlordMainActivity.this, SettingsActivity.class));
                drawer.closeDrawer(GravityCompat.START);
                break;

            case R.id.nav_logout:
                //go to landlord dashboard
                Log.i("MENU_DRAWER_TAG", "LOGOUT, GOTO LOGIN");
                viewModel.signOut();
                finish();
                drawer.closeDrawer(GravityCompat.START);
                break;
        }
        //add here to close the drawer navigation
        return true;
    }

    @Override
    public void onBackPressed() {

        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


}