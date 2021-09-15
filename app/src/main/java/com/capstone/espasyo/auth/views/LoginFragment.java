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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.capstone.espasyo.landlord.SampleLandlordDashboard;
import com.capstone.espasyo.student.SampleStudentDashboard;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;


public class LoginFragment extends Fragment {

    private TextInputLayout sampleLayout;
    private TextInputEditText textInputEmail, textInputPassword;
    private Button btnLogin;
    private TextView gotoSignUp;

    private AuthViewModel viewModel;
    private NavController navController;

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
                    //String UID = viewModel.getUserData().getValue().getUid();
                   // navController.navigate(R.id.action_loginFragment_to_sampleFragment);


                    //TODO: GET USER DATA AND CHECK IF IT IS A STUDENT OR LANDLADY
                    //String id = firebaseUser.getUid().toString();

                    int user = 2;

                    if(user == 1) {
                       Intent intent = new Intent(getActivity(), SampleStudentDashboard.class);
                       startActivity(intent);
                    } else if(user == 2){
                        Intent intent = new Intent(getActivity(), SampleLandlordDashboard.class);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sampleLayout = view.findViewById(R.id.sampleLayout);

        textInputEmail = view.findViewById(R.id.text_input_email);
        textInputPassword = view.findViewById(R.id.text_input_password);
        gotoSignUp = view.findViewById(R.id.gotoSignUp);
        btnLogin = view.findViewById(R.id.btnLogin);
        navController = Navigation.findNavController(view);

        //Navigate to Signup Fragment
        gotoSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_loginFragment_to_signUpFragment);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtEmail = textInputEmail.getText().toString().trim();
                String txtPassword = textInputPassword.getText().toString().trim();


                if(confirmInput(txtEmail, txtPassword)) {
                    viewModel.signIn(txtEmail, txtPassword);
                } else {
                    Toast.makeText(getActivity(), "Please fill out everything", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Functions
    // ------ input validations -------------------------------
    public final String TAG = "TESTING";

    private boolean validateEmail(String email) {
        if(email.isEmpty()) {
            textInputEmail.setError("Email address field cannot be empty");
            Log.d(TAG, "EMAIL: EMPTY");
            return false;
        } else {
            textInputEmail.setError(null);
            Log.d(TAG, "EMAIL: NOT EMPTY");
            return true;
        }
    }

    private boolean validatePassword(String password) {
        if(password.isEmpty()) {
            textInputPassword.setError("Password field cannot be empty");
            Log.d(TAG, "PASSWORD: EMPTY");
            return false;
        } else {
            textInputPassword.setError(null);
            Log.d(TAG, "PASSWORD: NOT EMPTY");
            return true;
        }
    }

    public boolean confirmInput(String email, String password) {

        boolean isValid = false;

        boolean emailResult = validateEmail(email);
        boolean passwordResult = validatePassword(password);

        if(emailResult == true && passwordResult == true) {
            Log.d(TAG, "CAN PROCEED: TRUE");
            isValid = true;
        } else {
            Log.d(TAG, "CAN PROCEED: FALSE");
            isValid = false;
        }

        return isValid;
    }
}