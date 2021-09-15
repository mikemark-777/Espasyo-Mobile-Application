package com.capstone.espasyo.auth.views;

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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.capstone.espasyo.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class SignUpFragment extends Fragment {

    private TextInputEditText textInputEmail,
                              textInputFirstName,
                              textInputLastName,
                              textInputPassword,
                              textInputConfirmPassword;

    String [] roles = {"Student","Landlord or Landlady"};
    AutoCompleteTextView roleChosen;
    ArrayAdapter<String> rolesAdapter;

    //navigate to Login using text
    private TextView gotoLogin;

    private Button btnSignUp;
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


        textInputFirstName = view.findViewById(R.id.firstName);
        textInputLastName = view.findViewById(R.id.lastName);
        textInputEmail = view.findViewById(R.id.emailSignUp);
        textInputPassword = view.findViewById(R.id.password);
        textInputConfirmPassword = view.findViewById(R.id.confirmPassword);

        roleChosen =view.findViewById(R.id.inputRole);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        gotoLogin = view.findViewById(R.id.gotoLogin);
        navController = Navigation.findNavController(view);

        rolesAdapter = new ArrayAdapter<String>(getActivity(), R.layout.role_list_item, roles);
        roleChosen.setAdapter(rolesAdapter);

        //Navigate to Login Fragment
        gotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_signUpFragment_to_loginFragment);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String FName = textInputFirstName.getText().toString().trim();
                String LName = textInputLastName.getText().toString().trim();
                String email = textInputEmail.getText().toString().trim();
                String pass = textInputPassword.getText().toString().trim();
                String confirmPass = textInputConfirmPassword.getText().toString().trim();
                String userRole = roleChosen.getText().toString().trim();

                if(confirmInput(FName, LName, email, pass, confirmPass, userRole)) {

                  /*Check if what role and change it into user-role-code
                  * User-Role-Code:
                  * 1 - Admin
                  * 2 - Landlord/Landlady
                  * 3 - Student
                  */
                  int uRole = userRole.equals("Student") ? 3 : 2;

                  String UID = "";

                    User newUser = new User(
                        UID,
                        FName,
                        LName,
                        email,
                        pass,
                        uRole
                    );

                    viewModel.register(newUser);
                }
            }
        });

    }

    // Functions
    // ------ input validations -------------------------------
    public final String TAG = "TESTING";

    /* Check if firstName is empty */
    public Boolean isFirstNameEmpty(String firstName) {
        if(firstName.isEmpty()) {
            textInputFirstName.setError("First Name field cannot be empty");
            Log.d(TAG, "FIRSTNAME: EMPTY");
            return false;
        } else {
            textInputFirstName.setError(null);
            Log.d(TAG, "FIRSTNAME: NOT EMPTY");
            return true;
        }
    }

    /* Check if lastName is empty */
    public Boolean isLastNameEmpty(String lastName) {
        if(lastName.isEmpty()) {
            textInputLastName.setError("Last Name field cannot be empty");
            Log.d(TAG, "LASTNAME: EMPTY");
            return false;
        } else {
            textInputLastName.setError(null);
            Log.d(TAG, "LASTNAME: NOT EMPTY");
            return true;
        }
    }

    /* Check if email is empty */
    public Boolean isEmailEmpty(String email) {
        if(email.isEmpty()) {
            textInputEmail.setError("Email field cannot be empty");
            Log.d(TAG, "EMAIL: EMPTY");
            return false;
        } else {
            textInputEmail.setError(null);
            Log.d(TAG, "EMAIL: NOT EMPTY");
            return true;
        }
    }

    /* Check if password and confirmPassword is empty */
    public Boolean isPasswordsEmpty(String password, String confirmPassword) {

        boolean isPasswordEmpty = false;
        boolean isConfirmPasswordEmpty = false;

        //check if password is empty
        if(password.isEmpty()) {
            textInputPassword.setError("Password field cannot be empty");
            Log.d(TAG, "PASSWORD: EMPTY");
            isPasswordEmpty = true;
        } else {
            textInputPassword.setError(null);
            Log.d(TAG, "PASSWORD: NOT EMPTY");
            isPasswordEmpty = false;
        }
        //check if confirmPassword is empty
        if(confirmPassword.isEmpty()) {
            textInputConfirmPassword.setError("Confirm Password field cannot be empty");
            Log.d(TAG, "CONFIRM PASSWORD: EMPTY");
            isConfirmPasswordEmpty = true;
        } else {
            textInputConfirmPassword.setError(null);
            Log.d(TAG, "CONFIRM PASSWORD: NOT EMPTY");
            isConfirmPasswordEmpty = false;
        }

          if(isPasswordEmpty == true && isConfirmPasswordEmpty == true) {
              return true;
          } else {
              return false;
          }
    }

    /* Check if userRole is empty */
    public Boolean isUserRoleEmpty(String userRole) {
        if(userRole.isEmpty()) {
            roleChosen.setError("Email field cannot be empty");
            Log.d(TAG, "ROLE: EMPTY");
            return false;
        } else {
            roleChosen.setError(null);
            Log.d(TAG, "ROLE: NOT EMPTY");
            return true;
        }
    }

    /* Check if password1 and password2 are not empty and match*/
    public Boolean validatePassword(String password1, String password2) {

        if(isPasswordsEmpty(password1, password2) == false) {
            if(matchPassword(password1, password2) == true) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /* Check if password1 and password2 are match*/
    public Boolean matchPassword(String password, String confirmPassword) {

        if(password.equals(confirmPassword)) {
            Log.d(TAG, "PASSWORD MATCHING: MATCH");
            return true;
        } else {
            textInputPassword.setError("Password Not Match");
            textInputConfirmPassword.setError("Password Not Match");
            Log.d(TAG, "PASSWORD MATCHING: NOT MATCH");
            return false;
        }
    }


    public Boolean confirmInput(String firstName, String lastName, String email, String password, String confirmPassword, String userRole) {

        boolean firstNameResult = isFirstNameEmpty(firstName);
        boolean lastNameResult = isLastNameEmpty(lastName);
        boolean emailResult = isEmailEmpty(email);
        boolean passwordResult = validatePassword(password, confirmPassword);
        boolean userRoleResult = isUserRoleEmpty(userRole);


        if(firstNameResult == true && lastNameResult == true && emailResult == true && passwordResult == true && userRoleResult == true) {
            Log.d(TAG, "CAN PROCEED: TRUE");
            return true;
        } else {
            Log.d(TAG, "CAN PROCEED: FALSE");
            return false;
        }
    }

}