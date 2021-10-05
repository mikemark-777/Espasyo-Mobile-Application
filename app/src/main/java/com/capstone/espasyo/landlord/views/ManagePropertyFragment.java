package com.capstone.espasyo.landlord.views;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.adapters.PropertyAdapter;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Property;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ManagePropertyFragment extends Fragment implements PropertyAdapter.OnPropertyListener {

    private FirebaseConnection firebaseConnection;

    private FirebaseAuth fAuth;
    private FirebaseFirestore database;

    private RecyclerView propertyRecyclerView;
    private PropertyAdapter propertyAdapter;
    private ArrayList<Property> ownedPropertyList;

    private FloatingActionButton addPropertyFAB;
    private NavController landlord_navigation;
    private TextView noPropertyAddedYetText;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize
        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        fAuth    = firebaseConnection.getFirebaseAuthInstance();
        ownedPropertyList = new ArrayList<>();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.landlord_fragment_manage_property, container, false);
        initRecyclerView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        progressDialog = new ProgressDialog(this.getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading Properties");
        progressDialog.show();
        getUserProperties();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if(progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }, 2000);
    }

    public void initRecyclerView(View view) {
        // initialize propertyRecyclerView, layoutManager and propertyAdapter
        propertyRecyclerView = view.findViewById(R.id.propertyRecyclerView_manage);
        propertyRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        propertyRecyclerView.setLayoutManager(layoutManager);
        propertyAdapter = new PropertyAdapter(getActivity(), ownedPropertyList, this);
        propertyRecyclerView.setAdapter(propertyAdapter);
    }

    //get all the ownedProperty of the currentUser
    public  void  getUserProperties() {
        //will be used to retrieve owned properties in the Properties Collection
        String currentUserID = fAuth.getCurrentUser().getUid().toString();
        CollectionReference propertiesCollection = database.collection("properties");

        propertiesCollection.whereEqualTo("owner", currentUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ownedPropertyList.clear();
                        for(QueryDocumentSnapshot document: queryDocumentSnapshots) {
                            Property property = document.toObject(Property.class);
                            ownedPropertyList.add(property);
                        }
                        propertyAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onPropertyClick(int position) {
        Toast.makeText(getActivity(), "Item Clicked at position " + position, Toast.LENGTH_SHORT).show();
    }
}
