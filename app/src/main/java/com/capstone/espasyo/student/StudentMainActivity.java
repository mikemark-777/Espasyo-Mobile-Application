package com.capstone.espasyo.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.capstone.espasyo.MainActivity;
import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.capstone.espasyo.student.adapters.PropertyAdapter;
import com.capstone.espasyo.student.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.student.widgets.PropertyRecyclerView;
import com.capstone.espasyo.models.Property;
import com.capstone.espasyo.student.repository.FirebaseConnection;
import com.capstone.espasyo.student.views.MapActivity;
import com.capstone.espasyo.student.views.ProfileActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class StudentMainActivity extends AppCompatActivity implements PropertyAdapter.OnPropertyListener {

    private AuthViewModel viewModel;
    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;

    private PropertyRecyclerView propertyRecyclerView;
    private View mEmptyView;
    private PropertyAdapter propertyAdapter;
    private ArrayList<Property> propertyList;

    private CustomProgressDialog progressDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_activity_main);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        viewModel.getLoggedStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isUserLoggedIn) {
                if (!isUserLoggedIn) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        //Initialize
        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        propertyList = new ArrayList<>();

        initPropertyRecyclerView();
        fetchProperties();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.List);
        bottomNavigationView.setOnItemSelectedListener(navListener);
    }

    public void initPropertyRecyclerView() {
        // initialize propertyRecyclerView, layoutManager and propertyAdapter
        mEmptyView = findViewById(R.id.empty_property_state_listview);
        propertyRecyclerView = (PropertyRecyclerView) findViewById(R.id.propertyListRecyclerView);
        propertyRecyclerView.showIfEmpty(mEmptyView);
        propertyRecyclerView.setHasFixedSize(true);
        LinearLayoutManager propertyLayoutManager = new LinearLayoutManager(StudentMainActivity.this, LinearLayoutManager.VERTICAL, false);
        propertyRecyclerView.setLayoutManager(propertyLayoutManager);
        propertyAdapter = new PropertyAdapter(StudentMainActivity.this, propertyList, this);
        propertyRecyclerView.setAdapter(propertyAdapter);

        //initialize data aside from recyclerView
        progressDialog = new CustomProgressDialog(this);
    }

    public void fetchProperties() {
        //will be used to retrieve all verified properties in the Properties Collection
        CollectionReference propertiesCollection = database.collection("properties");

        propertiesCollection.whereEqualTo("verified", true)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        propertyList.clear();
                        for (QueryDocumentSnapshot property : queryDocumentSnapshots) {
                            Property propertyObj = property.toObject(Property.class);
                            if(!propertyObj.isLocked()) {
                                propertyList.add(propertyObj);
                            } else {
                                Toast.makeText(StudentMainActivity.this, propertyObj.getName() + " is " + propertyObj.isLocked(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        propertyAdapter.notifyDataSetChanged();
                    }
                });
    }

    //interface for on item selected because setOnNavigationItemSelectedListener is depracated
    private BottomNavigationView.OnItemSelectedListener navListener =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.List:
                            startActivity(new Intent(getApplicationContext(), StudentMainActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                            return true;
                        case R.id.Map:
                            startActivity(new Intent(getApplicationContext(), MapActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                            return true;
                        case R.id.Profile:
                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                            return true;
                    }
                    return false;
                }
            };


    @Override
    public void onPropertyClick(int position) {

    }
}