package com.capstone.espasyo.landlord.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.adapters.PropertyAdapter;
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.landlord.widgets.PropertyRecyclerView;
import com.capstone.espasyo.models.Property;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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

    private PropertyRecyclerView propertyRecyclerView;
    private View propertyRecyclerViewEmptyState;
    private PropertyAdapter propertyAdapter;
    private ArrayList<Property> ownedPropertyList;

    private ExtendedFloatingActionButton addPropertyFAB;
    private SwipeRefreshLayout managePropertyRVSwipeRefresh;
    private CustomProgressDialog progressDialog;

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
        initPropertyRecyclerView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog.showProgressDialog("Loading Properties..." , false);
        fetchUserProperties();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if(progressDialog.isShowing()) {
                    progressDialog.dismissProgressDialog();
                }
            }
        }, 500);

        addPropertyFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // goto add property activity
                startActivity(new Intent(getActivity(), AddPropertyActivity.class));
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

        managePropertyRVSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchUserProperties();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        managePropertyRVSwipeRefresh.setRefreshing(false);
                    }
                }, 2000);
            }
        });
    }

    public void initPropertyRecyclerView(View view) {
        // initialize propertyRecyclerView, layoutManager and propertyAdapter
        propertyRecyclerViewEmptyState = view.findViewById(R.id.empty_property_state_managePropertyFragment);
        propertyRecyclerView = (PropertyRecyclerView) view.findViewById(R.id.propertyRecyclerView_manage);
        propertyRecyclerView.showIfEmpty(propertyRecyclerViewEmptyState);
        propertyRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        propertyRecyclerView.setLayoutManager(layoutManager);
        propertyAdapter = new PropertyAdapter(getActivity(), ownedPropertyList, this);
        propertyRecyclerView.setAdapter(propertyAdapter);

        //initialize other UI that is not related to Recyclerview
        addPropertyFAB = view.findViewById(R.id.addPropertyFAB);
        progressDialog = new CustomProgressDialog(getActivity());
        managePropertyRVSwipeRefresh = view.findViewById(R.id.managePropertyRVSwipeRefresh);
    }

    //get all the ownedProperty of the currentUser
    public  void fetchUserProperties() {
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
        //pass selected property's propertyId to ChooseEditActivity
        Intent intent = new Intent(getActivity(), ChooseEditActivity.class);
        intent.putExtra("propertyID", ownedPropertyList.get(position).getPropertyID());
        startActivity(intent);
    }

    // TODO: Handle Activity Life Cycle
}
