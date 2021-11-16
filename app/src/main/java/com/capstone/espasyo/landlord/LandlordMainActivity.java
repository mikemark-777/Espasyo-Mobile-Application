package com.capstone.espasyo.landlord;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.capstone.espasyo.MainActivity;
import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.landlord.views.DashboardFragment;
import com.capstone.espasyo.landlord.views.ManagePropertyFragment;
import com.capstone.espasyo.landlord.views.SettingsActivity;
import com.capstone.espasyo.landlord.views.VerificationFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class LandlordMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private FirebaseAuth fAuth;
    private AuthViewModel viewModel;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    public final String SHARED_PREFS = "sharedPrefs";
    public final String USER_ROLE = "userRole";
    private TextView landlordEmailTextView;
    private String landlordEmail;

    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_main);

        fAuth = FirebaseAuth.getInstance();
        drawer = findViewById(R.id.landlord_drawer_layout);
        navigationView = findViewById(R.id.landlordNavigationView);
        toolbar = findViewById(R.id.landlord_toolbar);
        progressDialog = new CustomProgressDialog(LandlordMainActivity.this);


        setSupportActionBar(toolbar);
        toolbar.setTitle("Dashboard");

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.landlord_menuOpen, R.string.landlord_menuClose) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                landlordEmailTextView = drawerView.findViewById(R.id.landlordEmail_drawer);

                //get current user's email
                landlordEmail= fAuth.getCurrentUser().getEmail();
                landlordEmailTextView.setText(landlordEmail);
            }
        };
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
                progressDialog.showProgressDialog("Logging out..." , false);
                drawer.closeDrawer(GravityCompat.START);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(progressDialog.isShowing()) {
                            viewModel.signOut();
                            removeUserRolePreference();
                            finish();
                            progressDialog.dismissProgressDialog();
                        }
                    }
                }, 3000);
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

    //remove USER_ROLE in sharedPreferences
    public void removeUserRolePreference() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(USER_ROLE);
        editor.apply();
    }

}