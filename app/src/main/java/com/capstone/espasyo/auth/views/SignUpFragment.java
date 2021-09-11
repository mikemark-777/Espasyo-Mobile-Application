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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class SignUpFragment extends Fragment {

    private TextInputEditText txtEmail, txtFirstName, txtLastName, txtPassword, txtConfirmPassword;

    String [] roles = {"Student","Landlord or Landlady"};
    AutoCompleteTextView roleChosen;
    ArrayAdapter<String> rolesAdapter;

    private TextView gotoLogin;
    private Button signUpButton;
    private AuthViewModel viewModel;
    private NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AuthViewModel.class);

        viewModel.getUserData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if(firebaseUser != null) {
                    navController.navigate(R.id.action_signUpFragment_to_loginFragment);
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtEmail = view.findViewById(R.id.emailSignUp);
        txtFirstName = view.findViewById(R.id.firstName);
        txtLastName = view.findViewById(R.id.lastName);
        txtPassword = view.findViewById(R.id.password);
        txtConfirmPassword = view.findViewById(R.id.confirmPassword);
        roleChosen =view.findViewById(R.id.inputRole);

            rolesAdapter = new ArrayAdapter<String>(getActivity(), R.layout.role_list_item, roles);
            roleChosen.setAdapter(rolesAdapter);




        signUpButton = view.findViewById(R.id.btnSignUp);
        gotoLogin = view.findViewById(R.id.gotoLogin);

        navController = Navigation.findNavController(view);

        gotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_signUpFragment_to_loginFragment);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = txtEmail.getText().toString();
                String FName = txtFirstName.getText().toString();
                String LName = txtLastName.getText().toString();
                String pass = txtPassword.getText().toString();
                String confirmPass = txtConfirmPassword.getText().toString();
                String userRole = roleChosen.getText().toString();


                if(!email.isEmpty() && !FName.isEmpty() && !LName.isEmpty() && !pass.isEmpty() && !confirmPass.isEmpty()) {
                    if(matchPassword(pass, confirmPass)) {
                        //Todo:Edit Authentication viewmodel and repository
                        viewModel.register(email, pass);
                    } else {
                        Toast.makeText(getActivity(),"Password not match" , Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(),"Please Fillout everything." , Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // Functions

    public Boolean matchPassword(String password, String confirmPassword) {

        boolean isMatch = false;
        if(password.equals(confirmPassword)) {
            isMatch = true;
        } else {
            isMatch = false;
        }
        return isMatch;
    }


}