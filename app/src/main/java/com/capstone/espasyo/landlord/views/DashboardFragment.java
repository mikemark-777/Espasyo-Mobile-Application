package com.capstone.espasyo.landlord.views;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.capstone.espasyo.models.Property;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class DashboardFragment extends Fragment implements PropertyAdapter.OnPropertyListener {

    private FirebaseAuth fAuth;
    private FirebaseFirestore database;

    private RecyclerView propertyRecyclerView;
    private PropertyAdapter propertyAdapter;
    private ArrayList<Property> ownedPropertyList;

    private ExtendedFloatingActionButton addPropertyFAB;
    private NavController landlord_navigation;
    private TextView noPropertyAddedYetText;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize
        database = FirebaseFirestore.getInstance();
        fAuth    = FirebaseAuth.getInstance();
        ownedPropertyList = new ArrayList<>();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.landlord_fragment_dashboard, container, false);
        initPropertyRecyclerView(view);
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


        addPropertyFAB = view.findViewById(R.id.addPropertyFAB);
        addPropertyFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // goto add property activity
                startActivity(new Intent(getActivity(), AddPropertyActivity.class));
                getActivity().finish();
               /* Snackbar.make(view, "Add a property", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        //shrink and extend the FAB
        propertyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    addPropertyFAB.shrink();
                } else if (dy < 0) {
                    addPropertyFAB.extend();
                }
            }
        });
    }

    public void initPropertyRecyclerView(View view) {
        // initialize propertyRecyclerView, layoutManager and propertyAdapter
        propertyRecyclerView = view.findViewById(R.id.propertyRecyclerView);
        propertyRecyclerView.setHasFixedSize(true);
        LinearLayoutManager propertyLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        propertyRecyclerView.setLayoutManager(propertyLayoutManager);
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
        Intent intent = new Intent(getActivity(), PropertyDetailsActivity.class);
        intent.putExtra("property", ownedPropertyList.get(position));
        startActivity(intent);
    }

    // TODO: Handle Fragment Life Cycle
}
