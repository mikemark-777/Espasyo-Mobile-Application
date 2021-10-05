package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;

public class AddRoomActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;

    private TextView numberOfPersonsInput;
    private Button increment, decrement;
    private int numberOfPersons = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_add_room);

        numberOfPersonsInput = findViewById(R.id.numberOfPersonsInput);
        increment = findViewById(R.id.increment);
        decrement = findViewById(R.id.decrement);

        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(numberOfPersons < 9) {
                   numberOfPersons++;
                   numberOfPersonsInput.setText(String.valueOf(numberOfPersons));
               }
            }
        });

        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(numberOfPersons > 1) {
                   numberOfPersons--;
                   numberOfPersonsInput.setText(String.valueOf(numberOfPersons));
               }
            }
        });

    }
}