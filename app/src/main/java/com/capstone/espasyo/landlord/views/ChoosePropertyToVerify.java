package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.adapters.PropertyAdapter;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.landlord.widgets.PropertyRecyclerView;
import com.capstone.espasyo.models.Property;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ChoosePropertyToVerify extends AppCompatActivity implements PropertyAdapter.OnPropertyListener{

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth fAuth;
    private FirebaseFirestore database;

    private PropertyRecyclerView propertyRecyclerView;
    private View mEmptyView;
    private PropertyAdapter propertyAdapter;
    private ArrayList<Property> ownedPropertyList;

    private ExtendedFloatingActionButton addPropertyFAB;
    private TextView noPropertyAddedYetText;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_choose_property_to_verify);

        //Initialize
        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        fAuth    = firebaseConnection.getFirebaseAuthInstance();
        ownedPropertyList = new ArrayList<>();

        initPropertyRecyclerView();
        fetchUserProperties();
    }

    /*----------------------------------------------------------- functions ---------------------------------------------------------------*/

    public void initPropertyRecyclerView() {
        // initialize propertyRecyclerView, layoutManager and propertyAdapter
        mEmptyView = findViewById(R.id.empty_property_state_dashboardFragment);
        propertyRecyclerView = (PropertyRecyclerView) findViewById(R.id.propertyRecyclerView_verify);
        propertyRecyclerView.showIfEmpty(mEmptyView);
        propertyRecyclerView.setHasFixedSize(true);
        LinearLayoutManager propertyLayoutManager = new LinearLayoutManager(ChoosePropertyToVerify.this, LinearLayoutManager.VERTICAL, false);
        propertyRecyclerView.setLayoutManager(propertyLayoutManager);
        propertyAdapter = new PropertyAdapter(ChoosePropertyToVerify.this, ownedPropertyList, this);
        propertyRecyclerView.setAdapter(propertyAdapter);
    }

    public void fetchUserProperties() {
        //will be used to retrieve owned properties in the Properties Collection
        String currentUserID = fAuth.getCurrentUser().getUid().toString();
        CollectionReference propertiesCollection = database.collection("properties");

        propertiesCollection.whereEqualTo("owner", currentUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ownedPropertyList.clear();
                        for(QueryDocumentSnapshot property: queryDocumentSnapshots) {
                            Property propertyObj = property.toObject(Property.class);
                            ownedPropertyList.add(propertyObj);
                        }
                        propertyAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onPropertyClick(int position) {

    }
}