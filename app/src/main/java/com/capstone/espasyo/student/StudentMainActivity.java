package com.capstone.espasyo.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.capstone.espasyo.MainActivity;
import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.capstone.espasyo.student.adapters.PropertyAdapter;
import com.capstone.espasyo.student.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.student.customdialogs.StudentFilterDialog;
import com.capstone.espasyo.student.views.StudentViewPropertyDetailsActivity;
import com.capstone.espasyo.student.widgets.PropertyRecyclerView;
import com.capstone.espasyo.models.Property;
import com.capstone.espasyo.student.repository.FirebaseConnection;
import com.capstone.espasyo.student.views.StudentMapActivity;
import com.capstone.espasyo.student.views.StudentAccountActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class StudentMainActivity extends AppCompatActivity implements PropertyAdapter.OnPropertyListener, StudentFilterDialog.ConfirmFilterDataListener {

    private AuthViewModel viewModel;
    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;

    private SwipeRefreshLayout listViewSwipeRefresh;
    private PropertyRecyclerView propertyRecyclerView;
    private View mEmptyView;
    private PropertyAdapter propertyAdapter;
    private ArrayList<Property> propertyList;

    private ImageView btnFilter;
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
        progressDialog.showProgressDialog("Loading properties...", false);
        fetchProperties();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if(progressDialog.isShowing()) {
                    progressDialog.dismissProgressDialog();
                }
            }
        }, 1500);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.List);
        bottomNavigationView.setOnItemSelectedListener(navListener);

        listViewSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchProperties();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listViewSwipeRefresh.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
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
        btnFilter = findViewById(R.id.btnFilter);
        listViewSwipeRefresh = findViewById(R.id.listViewSwipeRefresh_student);
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
                            }
                        }
                        propertyAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(StudentMainActivity.this, e.toString() , Toast.LENGTH_SHORT).show();
                progressDialog.dismissProgressDialog();
            }
        });
    }


    //interface for on item selected because setOnNavigationItemSelectedListener is deprecated
    private BottomNavigationView.OnItemSelectedListener navListener = new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.List:
                            startActivity(new Intent(getApplicationContext(), StudentMainActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                            return true;
                        case R.id.Map:
                            startActivity(new Intent(getApplicationContext(), StudentMapActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                            return true;
                        case R.id.Account:
                            startActivity(new Intent(getApplicationContext(), StudentAccountActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                            return true;
                    }
                    return false;
                }
            };

    public void showFilterDialog() {
        //create an instance of the filter dialog dialog
        StudentFilterDialog studentFilterDialog = new StudentFilterDialog();
       // confirmLocationDialog.setArguments(args);
        studentFilterDialog.show(getSupportFragmentManager(), "studentFilterDialog");
    }

    @Override
    public void onPropertyClick(int position) {
        Intent intent = new Intent(StudentMainActivity.this, StudentViewPropertyDetailsActivity.class);
        intent.putExtra("property", propertyList.get(position));
        startActivity(intent);
    }

    @Override
    public void getConfirmedFilterData(String propertyType, int minimumPrice, int maximumPrice, int numberOfPersons) {
        filterProperties(propertyType,minimumPrice, maximumPrice , numberOfPersons);

    }

    @Override
    public void cancelFilter() {
        Toast.makeText(StudentMainActivity.this, "fetch again properties" , Toast.LENGTH_SHORT).show();
        //fetchProperties();
    }

    public void filterProperties(String propertyType, int minPrice, int maxPrice, int numberOfPerson) {
        //will be used to filter properties according to users preferences
        progressDialog.showProgressDialog("Applying filters...", false);
        CollectionReference propertiesCollection = database.collection("properties");
        propertiesCollection.whereEqualTo("propertyType", propertyType)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        propertyList.clear();
                        for (QueryDocumentSnapshot property : queryDocumentSnapshots) {
                            Property propertyObj = property.toObject(Property.class);
                            CollectionReference propertyRoomCollectionRef = database.collection("properties/" + propertyObj.getPropertyID() + "/rooms");
                            propertyRoomCollectionRef.whereEqualTo("numberOfPersons", numberOfPerson)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if(queryDocumentSnapshots.size() > 0) {
                                                if(propertyObj.isVerified() && !propertyObj.isLocked()) {
                                                    if(minPrice != 0 && maxPrice != 0) {
                                                        if(propertyObj.getMinimumPrice() >= minPrice || propertyObj.getMaximumPrice() <= maxPrice) {
                                                            propertyList.add(propertyObj);
                                                        }
                                                    } else if(minPrice != 0 && maxPrice == 0){
                                                        if(propertyObj.getMinimumPrice() >= minPrice) {
                                                            propertyList.add(propertyObj);
                                                        }
                                                    } else if(minPrice == 0 && maxPrice != 0) {
                                                        if(propertyObj.getMaximumPrice() <= maxPrice) {
                                                            propertyList.add(propertyObj);
                                                        }
                                                    } else if(minPrice == 0 && maxPrice == 0){
                                                        propertyList.add(propertyObj);
                                                    }
                                                }
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(StudentMainActivity.this, e.toString() , Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(progressDialog.isShowing()) {
                                    progressDialog.dismissProgressDialog();
                                }
                            }
                        }, 2000);
                        propertyAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(StudentMainActivity.this, e.toString() , Toast.LENGTH_SHORT).show();
                progressDialog.dismissProgressDialog();
            }
        });
    }

}