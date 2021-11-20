package com.capstone.espasyo.landlord.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.adapters.VerificationRequestAdapter;
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.landlord.widgets.VerificationRequestRecyclerView;
import com.capstone.espasyo.models.VerificationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class VerificationFragment extends Fragment implements VerificationRequestAdapter.OnVerificationRequestListener {

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth fAuth;
    private FirebaseFirestore database;

    private VerificationRequestRecyclerView verificationRequestsRecyclerView;
    private View verificationRequestsRecyclerViewEmptyState;
    private VerificationRequestAdapter verificationRequestAdapter;
    private ArrayList<VerificationRequest> ownedPropertyVerifications;

    private ExtendedFloatingActionButton composeVerificationRequestFAB;
    private SwipeRefreshLayout verificationRequestRVSwipeRefresh;
    private CustomProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize
        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        fAuth = firebaseConnection.getFirebaseAuthInstance();
        ownedPropertyVerifications = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.landlord_fragment_verification, container, false);
        initVerificationRequestRecyclerView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog.showProgressDialog("Loading Verification Requests...", false);
        fetchSentVerificationRequest();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressDialog.isShowing()) {
                    progressDialog.dismissProgressDialog();
                }
            }
        }, 500);

        composeVerificationRequestFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ChoosePropertyToVerifyActivity.class));
            }
        });

        verificationRequestRVSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchSentVerificationRequest();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        verificationRequestRVSwipeRefresh.setRefreshing(false);
                    }
                }, 2000);

            }
        });
    }
    /*----------------------------------------------------------- functions ---------------------------------------------------------------*/

    public void initVerificationRequestRecyclerView(View view) {
        // initialize verificationRequestRecyclerView, layoutManager and verificationRequestAdapter
        verificationRequestsRecyclerViewEmptyState = view.findViewById(R.id.empty_verification_request_state_verifFragment);
        verificationRequestsRecyclerView = (VerificationRequestRecyclerView) view.findViewById(R.id.verificationRequestRecyclerView);
        verificationRequestsRecyclerView.showIfEmpty(verificationRequestsRecyclerViewEmptyState);
        verificationRequestsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager verificationRequestLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        verificationRequestsRecyclerView.setLayoutManager(verificationRequestLayoutManager);
        verificationRequestAdapter = new VerificationRequestAdapter(getActivity(), ownedPropertyVerifications, this);
        verificationRequestsRecyclerView.setAdapter(verificationRequestAdapter);
        verificationRequestRVSwipeRefresh = view.findViewById(R.id.verificationRequestSwipeRefresh);

        //initialize custom progress dialog
        composeVerificationRequestFAB = view.findViewById(R.id.composeVerificationRequestFAB);
        progressDialog = new CustomProgressDialog(getActivity());
    }


    public void fetchSentVerificationRequest() {
        //get the current user and compare it to the requestee who issued a verification request. If match, get the verificationrequest
        String currentUser = fAuth.getCurrentUser().getUid().toString();

        CollectionReference verificationRequestCollection = database.collection("verificationRequests");
        verificationRequestCollection.whereEqualTo("requesteeID", currentUser)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ownedPropertyVerifications.clear();
                        for (QueryDocumentSnapshot verification : queryDocumentSnapshots) {
                            VerificationRequest verificationRequestObject = verification.toObject(VerificationRequest.class);
                            ownedPropertyVerifications.add(verificationRequestObject);
                        }
                        verificationRequestAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onVerificationRequestClick(int position) {
        // get the position of the clicked verification request
        Intent intent = new Intent(getActivity(), VerificationRequestDetailsActivity.class);
        intent.putExtra("chosenVerificationRequest", ownedPropertyVerifications.get(position));
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // TODO: Handle Activity Life Cycle

    @Override
    public void onResume() {
        super.onResume();
        fetchSentVerificationRequest();
    }

}