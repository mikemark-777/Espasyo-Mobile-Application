package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.LandlordMainActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class AddPropertyActivity extends AppCompatActivity {

    private Button btnAddProperty,
                   btnCancel;
    private TextInputEditText phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_add_property);
        btnAddProperty = findViewById(R.id.btnAddProperty);
        btnCancel = findViewById(R.id.btnCancel);
        phoneNumber = findViewById(R.id.text_input_landlord_phoneNumber);



        btnAddProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Phone Number: " + phoneNumber.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddPropertyActivity.this, LandlordMainActivity.class));
                finish();
            }
        });

    }

    //TODO: Add input validations

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Toast.makeText(this, "Pressing back button", Toast.LENGTH_SHORT).show();
    }
}