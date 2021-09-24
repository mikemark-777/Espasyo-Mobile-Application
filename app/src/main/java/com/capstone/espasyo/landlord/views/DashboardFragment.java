package com.capstone.espasyo.landlord.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.capstone.espasyo.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class DashboardFragment extends Fragment {

    private FloatingActionButton addPropertyFAB;
    private NavController landlord_navigation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.landlord_fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
    }
}
