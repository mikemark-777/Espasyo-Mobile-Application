package com.capstone.espasyo.auth.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.auth.viewmodels.AuthViewModel;
import com.capstone.espasyo.models.Landlord;
import com.capstone.espasyo.models.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

public class SignUpFragment extends Fragment {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private AuthViewModel viewModel;
    private NavController navController;

    //UI Elements
    private TextInputLayout textInputEmailLayout, textInputFirstNameLayout, textInputLastNameLayout, textInputPasswordLayout, textInputConfirmPasswordLayout, textInputRoleLayout, textInputLandlordPhoneNumberlayout;
    private TextInputEditText textInputEmail, textInputFirstName, textInputLastName, textInputPassword, textInputConfirmPassword, textInputLandlordPhoneNumber;
    private AutoCompleteTextView roleChosen;

    String[] roles = {"Student", "Landlord or Landlady"};
    ArrayAdapter<String> rolesAdapter;

    private CheckBox agreeToTermsAndConditions;

    //navigate to Login using text
    private TextView gotoLogin;
    private TextView btnTermsAndConditions;
    private ProgressBar signUpProgressBar;
    private Button btnSignUp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AuthViewModel.class);

        //check each time if there is a logged user
        viewModel.getUserData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser != null) {

                    if (firebaseUser.isEmailVerified()) {
                        //TODO:must include logout user for them to try login in their verified credentials
                        viewModel.signOut();
                        navController.navigate(R.id.action_emailVerificationFragment_to_loginFragment);
                    } else {
                        navController.navigate(R.id.action_signUpFragment_to_emailVerificationFragment);
                    }
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.auth_fragment_sign_up, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);

        rolesAdapter = new ArrayAdapter<String>(getActivity(), R.layout.auth_role_list_item, roles);
        roleChosen.setAdapter(rolesAdapter);

        btnSignUp.setEnabled(false);

        roleChosen.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    textInputLandlordPhoneNumberlayout.setVisibility(View.GONE);
                } else if(position == 1) {
                    textInputLandlordPhoneNumberlayout.setVisibility(View.VISIBLE);
                }
            }
        });

        //Navigate to Login Fragment
        gotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_signUpFragment_to_loginFragment);
            }
        });

        agreeToTermsAndConditions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    btnSignUp.setEnabled(false);
                } else {
                    btnSignUp.setEnabled(true);
                }
            }
        });

        btnTermsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTermsAndConditions();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = textInputFirstName.getText().toString().trim();
                String lastName = textInputLastName.getText().toString().trim();
                String email = textInputEmail.getText().toString().trim();
                String password = textInputPassword.getText().toString().trim();
                String confirmPassword = textInputConfirmPassword.getText().toString().trim();
                String userRole = roleChosen.getText().toString().trim();
                String landlordPhoneNumber = textInputLandlordPhoneNumber.getText().toString().trim();

                if (areInputsValid(firstName, lastName, email, password, confirmPassword, userRole)) {

                    //check if email already exists
                    firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().getSignInMethods().size() == 0) {

                                    /*Check if what role and change it into user-role-code
                                     * User-Role-Code:
                                     * 2 - Landlord/Landlady
                                     * 3 - Student
                                     */
                                    int uRole = userRole.equals("Student") ? 3 : 2;

                                    if (uRole == 2) {
                                        if(isLandlordPhoneNumberValid(landlordPhoneNumber)) {
                                            Landlord newLandlord = new Landlord("", firstName, lastName, email, password, uRole, landlordPhoneNumber);
                                            signUpProgressBar.setVisibility(View.VISIBLE);
                                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    signUpProgressBar.setVisibility(View.INVISIBLE);
                                                    viewModel.registerLandlord(newLandlord);
                                                }
                                            }, 4000);
                                        }
                                    } else if (uRole == 3) {
                                        Student newStudent = new Student("", firstName, lastName, email, password, uRole);
                                        signUpProgressBar.setVisibility(View.VISIBLE);
                                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                signUpProgressBar.setVisibility(View.INVISIBLE);
                                                viewModel.registerStudent(newStudent);
                                            }
                                        }, 4000);
                                    }
                                } else {
                                    textInputEmailLayout.setError("Email already exists");
                                }
                            } else {
                                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
            }
        });

    }

    // Functions

    public void initializeViews(View view) {

        //TextInputLayouts
        textInputEmailLayout = view.findViewById(R.id.text_input_email_layout_signup);
        textInputFirstNameLayout = view.findViewById(R.id.text_input_firstname_layout_signup);
        textInputLastNameLayout = view.findViewById(R.id.text_input_lastname_layout_signup);
        textInputPasswordLayout = view.findViewById(R.id.text_input_password_layout_signup);
        textInputConfirmPasswordLayout = view.findViewById(R.id.text_input_confirmpassword_layout_signup);
        textInputRoleLayout = view.findViewById(R.id.text_input_role_layout_signup);
        textInputLandlordPhoneNumberlayout = view.findViewById(R.id.text_input_landlord_phone_number_layout_signup);

        //TextInputEditText
        textInputFirstName = view.findViewById(R.id.text_input_firstname_signup);
        textInputLastName = view.findViewById(R.id.text_input_lastname_signup);
        textInputEmail = view.findViewById(R.id.text_input_email_signup);
        textInputPassword = view.findViewById(R.id.text_input_password_signup);
        textInputConfirmPassword = view.findViewById(R.id.text_input_confirmpassword_signup);
        roleChosen = view.findViewById(R.id.text_input_role_signup);
        textInputLandlordPhoneNumber = view.findViewById(R.id.text_input_landlord_phone_number_signup);

        //Checkbox
        agreeToTermsAndConditions = view.findViewById(R.id.agreeToTermsAndConditions);

        //progress bar
        signUpProgressBar = view.findViewById(R.id.signUpProgressBar);

        //buttons and others
        btnSignUp = view.findViewById(R.id.btnSignUp);
        gotoLogin = view.findViewById(R.id.gotoLogin);
        btnTermsAndConditions = view.findViewById(R.id.btnTermsAndConditions);
        navController = Navigation.findNavController(view);
    }

    // ------ input validations -------------------------------
    public final String TAG = "TESTING";

    //TODO: Check logic errors on isEmpty validations

    /* Check if firstName is empty */
    private Boolean isFirstNameValid(String firstName) {
        if (!firstName.isEmpty()) {
            textInputFirstNameLayout.setError(null);
            Log.d(TAG, "FIRSTNAME: NOT EMPTY");
            return true;
        } else {
            textInputFirstNameLayout.setError("First Name required");
            Log.d(TAG, "FIRSTNAME: EMPTY");
            return false;
        }
    }

    /* Check if lastName is empty */
    private Boolean isLastNameValid(String lastName) {
        if (!lastName.isEmpty()) {
            textInputLastNameLayout.setError(null);
            Log.d(TAG, "LASTNAME: NOT EMPTY");
            return true;
        } else {
            textInputLastNameLayout.setError("Last Name required");
            Log.d(TAG, "LASTNAME: EMPTY");
            return false;
        }
    }

    /* Check if email is empty */
    private Boolean isEmailValid(String email) {
        //TODO: Must include validations if email exist in firebase auth and database
        if (!email.isEmpty()) {
            textInputEmailLayout.setError(null);
            Log.d(TAG, "EMAIL: NOT EMPTY");
            return true;
        } else {
            textInputEmailLayout.setError("Email required");
            Log.d(TAG, "EMAIL: EMPTY");
            return false;
        }
    }


    /* Check if password and confirmPassword is empty */
    private Boolean arePasswordsEmpty(String password, String confirmPassword) {

        boolean isPasswordEmpty = false;
        boolean isConfirmPasswordEmpty = false;

        //check if password is empty
        if (!password.isEmpty()) {
            textInputPasswordLayout.setError(null);
            Log.d(TAG, "PASSWORD: NOT EMPTY");
            isPasswordEmpty = true;
        } else {
            textInputPasswordLayout.setError("Password required");
            Log.d(TAG, "PASSWORD: EMPTY");
            isPasswordEmpty = false;
        }
        //check if confirmPassword is empty
        if (!confirmPassword.isEmpty()) {
            textInputConfirmPasswordLayout.setError(null);
            Log.d(TAG, "CONFIRM PASSWORD: NOT EMPTY");
            isConfirmPasswordEmpty = true;
        } else {
            textInputConfirmPasswordLayout.setError("Confirm Password required");
            Log.d(TAG, "CONFIRM PASSWORD: EMPTY");
            isConfirmPasswordEmpty = false;
        }

        if (isPasswordEmpty && isConfirmPasswordEmpty) {
            return true;
        } else {
            return false;
        }
    }

    /* Check if userRole is empty */
    private Boolean isUserRoleValid(String userRole) {
        if (!userRole.isEmpty()) {
            textInputRoleLayout.setError(null);
            Log.d(TAG, "ROLE: NOT EMPTY");
            return true;
        } else {
            textInputRoleLayout.setError("Role required");
            Log.d(TAG, "ROLE: EMPTY");
            return false;
        }
    }

    /* Check if password1 and password2 are not empty and match*/
    private Boolean arePasswordsValid(String password1, String password2) {

        if (arePasswordsEmpty(password1, password2)) {
            if (password1.length() > 5 && password2.length() > 5) {

                Log.d(TAG, "PASSWORD COUNT: GREATER THAN 5");
                if (matchPassword(password1, password2)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                textInputPasswordLayout.setError("Password must be 6-15 characters");
                textInputConfirmPasswordLayout.setError("Password must be 6-15 characters");
                Log.d(TAG, "PASSWORD COUNT: LESS THAN 6");
                return false;
            }

        } else {
            return false;
        }
    }

    /* Check if password1 and password2 are match*/
    private Boolean matchPassword(String password, String confirmPassword) {

        if (password.equals(confirmPassword)) {
            Log.d(TAG, "PASSWORD MATCHING: MATCH");
            return true;
        } else {
            textInputPasswordLayout.setError("Password Not Match");
            textInputConfirmPasswordLayout.setError("Password Not Match");
            Log.d(TAG, "PASSWORD MATCHING: NOT MATCH");
            return false;
        }
    }

    //Check if the phone number is valid
    private boolean isLandlordPhoneNumberValid(String landlordPhoneNumbers) {
        if (!landlordPhoneNumbers.isEmpty()) {
            if (landlordPhoneNumbers.length() == 10) {
                textInputLandlordPhoneNumberlayout.setError(null);
                Log.d(TAG, "LANDLORD PHONE NUMBER: NOT EMPTY");
                return true;
            } else {
                textInputLandlordPhoneNumberlayout.setError("Phone number must be 12 digit");
                Log.d(TAG, "LANDLORD PHONE NUMBER: NOT EMPTY");
                return false;
            }
        } else {
            textInputLandlordPhoneNumberlayout.setError("Landlord Phone Number Required");
            Log.d(TAG, "PHONE NUMBER: EMPTY");
            return false;
        }
    }

    private Boolean areInputsValid(String firstName, String lastName, String email, String password, String confirmPassword, String userRole) {

        boolean firstNameResult = isFirstNameValid(firstName);
        boolean lastNameResult = isLastNameValid(lastName);
        boolean emailResult = isEmailValid(email);
        boolean passwordResult = arePasswordsValid(password, confirmPassword);
        boolean userRoleResult = isUserRoleValid(userRole);
        //boolean phoneNumberResult = isPhoneNumberValid(phoneNumber);

        if (firstNameResult && lastNameResult && emailResult && passwordResult && userRoleResult) {
            Log.d(TAG, "CAN PROCEED: TRUE");
            return true;
        } else {
            Log.d(TAG, "CAN PROCEED: FALSE");
            return false;
        }
    }

    public void showTermsAndConditions() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.terms_and_conditions, null);

        Button btnContinue = view.findViewById(R.id.btnContinue_termsAndConditions);

        AlertDialog termsAndConditionsDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                termsAndConditionsDialog.dismiss();
            }
        });
        termsAndConditionsDialog.show();
    }

}