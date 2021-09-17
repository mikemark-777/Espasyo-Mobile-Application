package com.capstone.espasyo.auth.views;

import android.content.Intent;
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
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.capstone.espasyo.landlord.SampleLandlordDashboard;
import com.capstone.espasyo.student.SampleStudentDashboard;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;


public class UpdateEmailFragment extends Fragment {

    private FirebaseUser currentUser;

    private TextInputLayout sampleLayout;
    private TextInputEditText textInputCurrentEmail, textInputNewEmail, textInputPassword;
    private Button btnChangeEmail;

    private AuthViewModel viewModel;
    private NavController navController;

    private Boolean isEmailChanged = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AuthViewModel.class);

        //check each time if there is a logged user
        viewModel.getUserData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if(firebaseUser != null) {

                    Toast.makeText(getActivity(), "Here again", Toast.LENGTH_SHORT).show();
                   currentUser = firebaseUser;
                    if(isEmailChanged) {
                        isEmailChanged = false;
                        navController.navigate(R.id.gotoEmailVerificationFragment_From_UpdateEmailFragment);
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        textInputCurrentEmail = view.findViewById(R.id.text_input_CurrentEmail_update);
        textInputNewEmail = view.findViewById(R.id.text_input_NewEmail_update);
        textInputPassword = view.findViewById(R.id.text_input_Password_update);
        btnChangeEmail = view.findViewById(R.id.btnChangeEmail);

        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String currentEmail = textInputCurrentEmail.getText().toString();
                String newEmail = textInputNewEmail.getText().toString();
                String password = textInputPassword.getText().toString();

                viewModel.updateEmailAddress(currentUser, currentEmail ,newEmail ,password);
                isEmailChanged = true;
                textInputCurrentEmail.setText("");
                textInputNewEmail.setText("");
                textInputPassword.setText("");
            }
        });

    }
}