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

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.capstone.espasyo.landlord.SampleLandlordDashboard;
import com.capstone.espasyo.student.SampleStudentDashboard;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


public class LoginFragment extends Fragment {

    private final int ADMIN_CODE = 1;
    private final int LANDLORD_CODE = 2;
    private final int STUDENT_CODE = 3;

    private TextInputLayout textInputEmailLayout, textInputPasswordLayout;
    private TextInputEditText textInputEmail, textInputPassword;
    private Button btnLogin;
    private TextView gotoSignUp;
    private ProgressBar loginProgressBar;

    private AuthViewModel viewModel;
    private NavController navController;

    private FirebaseFirestore database;
    private DocumentReference userReference;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        database = FirebaseFirestore.getInstance();

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AuthViewModel.class);

        //check each time if there is a logged user
        viewModel.getUserData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if(firebaseUser != null) {
                    if(firebaseUser.isEmailVerified()) {

                        //Get currentUser's UID
                        String UID = firebaseUser.getUid();

                        userReference = database.collection("users").document(UID);
                        userReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                                DocumentSnapshot user = value;
                                int userRole = user.getLong("userRole").intValue();
                                //Navigate to different modules depending on the user's role
                                if(userRole == ADMIN_CODE) {

                                } else if(userRole == LANDLORD_CODE){
                                    Intent intent = new Intent(getActivity(), SampleLandlordDashboard.class);
                                    startActivity(intent);
                                    getActivity().finish();
                                } else if(userRole == STUDENT_CODE) {
                                    Intent intent = new Intent(getActivity(), SampleStudentDashboard.class);
                                    startActivity(intent);
                                    getActivity().finish();
                                }
                            }
                        });

                    } else {
                        navController.navigate(R.id.action_loginFragment_to_emailVerificationFragment);
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

        // TextInputLayouts
        textInputEmailLayout = view.findViewById(R.id.text_input_email_layout_login);
        textInputPasswordLayout = view.findViewById(R.id.text_input_password_layout_login);

        //TextInputEditTexts
        textInputEmail = view.findViewById(R.id.text_input_email_login);
        textInputPassword = view.findViewById(R.id.text_input_password_login);

        gotoSignUp = view.findViewById(R.id.gotoSignUp);
        btnLogin = view.findViewById(R.id.btnLogin);
        navController = Navigation.findNavController(view);
        loginProgressBar = view.findViewById(R.id.loginProgressBar);

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

                    loginProgressBar.setVisibility(View.VISIBLE);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loginProgressBar.setVisibility(View.INVISIBLE);
                            viewModel.signIn(txtEmail, txtPassword);
                        }
                    }, 4000);

                } else {
                    Toast.makeText(getActivity(), "Please fill out everything", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // --------------------------------------------  FUNCTIONS  -------------------------------------------------------//
    // ------ input validations -------------------------------
    public final String TAG = "TESTING";

    private boolean isEmailEmpty(String email) {
        if(email.isEmpty()) {
            textInputEmailLayout.setError("Email address field cannot be empty");
            Log.d(TAG, "EMAIL: EMPTY");
            return false;
        } else {
            textInputEmailLayout.setError(null);
            Log.d(TAG, "EMAIL: NOT EMPTY");
            return true;
        }
    }

    private boolean validatePassword(String password) {
        if(password.isEmpty()) {
            textInputPasswordLayout.setError("Password field cannot be empty");
            Log.d(TAG, "PASSWORD: EMPTY");
            return false;
        } else {
            textInputPasswordLayout.setError(null);
            Log.d(TAG, "PASSWORD: NOT EMPTY");
            return true;
        }
    }

    public boolean confirmInput(String email, String password) {

        boolean isValid = false;

        boolean emailResult = isEmailEmpty(email);
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