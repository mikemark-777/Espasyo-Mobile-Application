package com.capstone.espasyo.landlord.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.adapters.VerificationRequestAdapter;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.landlord.widgets.VerificationRequestRecyclerView;
import com.capstone.espasyo.models.VerificationRequest;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class VerificationFragment extends Fragment implements VerificationRequestAdapter.OnVerificationRequestListener{

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth fAuth;
    private FirebaseFirestore database;

    private VerificationRequestRecyclerView verificationRequestsRecyclerView;
    private View mEmptyView;
    private VerificationRequestAdapter verificationRequestAdapter;
    private ArrayList<VerificationRequest> ownedPropertyVerifications;

    private ExtendedFloatingActionButton composeVerificationRequestFAB;
    private ProgressDialog progressDialog;

    private ActivityResultLauncher<Intent> LocationPickerActivityResultLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize
        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        fAuth    = firebaseConnection.getFirebaseAuthInstance();
        ownedPropertyVerifications = new ArrayList<>();

        //will handle all the data from the LocationPickerActivity
        LocationPickerActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                        } else if(result.getResultCode() == Activity.RESULT_CANCELED) {
                            Toast.makeText(getActivity(), "Cancelled: Back in Verification Fragment", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view =  inflater.inflate(R.layout.landlord_fragment_verification, container, false);
       initPropertyRecyclerView(view);
       return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //get verification request issued by the current user

        composeVerificationRequestFAB = view.findViewById(R.id.composeVerificationRequestFAB);
        composeVerificationRequestFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // goto add compose  activity
                //startActivity(new Intent(getActivity(), ChoosePropertyToVerifyActivity.class));
                LocationPickerActivityResultLauncher.launch(new Intent(getActivity(), ChoosePropertyToVerifyActivity.class));
            }
        });
    }

    /*----------------------------------------------------------- functions ---------------------------------------------------------------*/

    public void initPropertyRecyclerView(View view) {
        // initialize propertyRecyclerView, layoutManager and propertyAdapter
        mEmptyView = view.findViewById(R.id.empty_verification_request_state_verifFragment);
        verificationRequestsRecyclerView = (VerificationRequestRecyclerView) view.findViewById(R.id.verificationRequestRecyclerView);
        verificationRequestsRecyclerView.showIfEmpty(mEmptyView);
        verificationRequestsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager verificationRequestLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        verificationRequestsRecyclerView.setLayoutManager(verificationRequestLayoutManager);
        verificationRequestAdapter = new VerificationRequestAdapter(getActivity(), ownedPropertyVerifications, this);
        verificationRequestsRecyclerView.setAdapter(verificationRequestAdapter);
    }

    @Override
    public void onVerificationRequestClick(int position) {
        // get the position of the clicked verification request
    }

    // TODO: Handle Activity Life Cycle
}
