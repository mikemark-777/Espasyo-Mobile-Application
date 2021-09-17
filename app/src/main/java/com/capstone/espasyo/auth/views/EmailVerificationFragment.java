package com.capstone.espasyo.auth.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class EmailVerificationFragment extends Fragment {

    private AuthViewModel viewModel;

    private Button btnVerifyEmail;
    private Button btnProceed;
    private NavController navController;
    private TextView gotoUpdateEmailFragment;
    private TextView txtEmail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AuthViewModel.class);

        viewModel.getUserData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if(firebaseUser != null) {
                    String email = firebaseUser.getEmail();
                    txtEmail.setText(email);
                }
            }
        });



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_email_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        btnVerifyEmail = view.findViewById(R.id.btnVerifyEmail);
        btnProceed = view.findViewById(R.id.btnProceed);
        gotoUpdateEmailFragment = view.findViewById(R.id.gotoUpdateEmailFragment);
        txtEmail = view.findViewById(R.id.txtEmail);

        btnProceed.setEnabled(false);

        btnVerifyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            btnVerifyEmail.setEnabled(false);
            btnProceed.setEnabled(true);
            Toast.makeText(getActivity(), "Verification Sent to your email.", Toast.LENGTH_SHORT).show();
             /*   //send verification to newUser's email
                viewModel.getUserData().getValue().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                    }
                });
                //navController.navigate(R.id.gotoLoginFragment_From_EmailVerificationFragment);*/
            }
        });

        gotoUpdateEmailFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_emailVerificationFragment_to_updateEmailFragment);
            }
        });
    }
}