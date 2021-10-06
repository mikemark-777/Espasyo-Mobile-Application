package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Room;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AddRoomActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth fAuth;
    private FirebaseFirestore database;
    private DocumentReference roomsDocumentReference;

    private TextInputLayout textInputRoomNameLayout,
                            textInputRoomPriceLayout;

    private TextInputEditText textInputRoomName,
                              textInputRoomPrice;

    private SwitchCompat roomAvailabilitySwitch,
                         bathroomSwitch,
                         kitchenSwitch;

    private TextView textInputNumberOfPersons;
    private Button increment, decrement;
    private int numberOfPersons = 1;

    private Button btnAddRoom,
            btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_add_room);

        Intent intent = getIntent();
        String propertyID = intent.getStringExtra("propertyID");

        //initialize firebaseConnection, firebase auth and firestore
        firebaseConnection = FirebaseConnection.getInstance();
        fAuth = firebaseConnection.getFirebaseAuthInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();

        //initialize textInputLayouts, textInputEditTexts, textView (numberOfRooms input), switches and buttons
        textInputRoomNameLayout = findViewById(R.id.text_input_room_name_layout);
        textInputRoomPriceLayout = findViewById(R.id.text_input_room_price_layout);
        textInputRoomName = findViewById(R.id.text_input_room_name);
        textInputRoomPrice= findViewById(R.id.text_input_room_price);
        textInputNumberOfPersons = findViewById(R.id.text_input_numberOfPersons);
        roomAvailabilitySwitch = findViewById(R.id.roomAvailabilitySwitch);
        bathroomSwitch = findViewById(R.id.bathroomSwitch);
        kitchenSwitch = findViewById(R.id.kitchenSwitch);
        roomAvailabilitySwitch = findViewById(R.id.roomAvailabilitySwitch);
        textInputNumberOfPersons = findViewById(R.id.text_input_numberOfPersons);
        increment = findViewById(R.id.increment);
        decrement = findViewById(R.id.decrement);
        btnAddRoom = findViewById(R.id.btnAddRoom);
        btnCancel = findViewById(R.id.btnCancelAddRoom);

        // add room
        btnAddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomName = textInputRoomName.getText().toString().trim();
                int roomPrice = Integer.parseInt(textInputRoomPrice.getText().toString().trim());
                int numberOfPersonInRoom =Integer.parseInt(textInputNumberOfPersons.getText().toString().trim());
                boolean isRoomAvailable = roomAvailabilitySwitch.isChecked();
                boolean hasBathroom = bathroomSwitch.isChecked();
                boolean hasKitchen = kitchenSwitch.isChecked();

                //TODO: Must add input validations here

                //New Room Object
                String newRoomID = UUID.randomUUID().toString();
                //Get Property ID where this room belongs (get through Intent)
                Room newRoom = new Room(
                        propertyID,
                        newRoomID,
                        roomName,
                        roomPrice,
                        numberOfPersonInRoom,
                        isRoomAvailable,
                        hasBathroom,
                        hasKitchen
                );

                // TESTING PURPOSES - Add newRoom to properties/{propertyID}/rooms/{newRoomID}
                roomsDocumentReference = database.collection("properties").document(propertyID)
                                                      .collection("rooms").document(newRoomID);
                roomsDocumentReference.set(newRoom).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(AddRoomActivity.this, "New Room Successfully Added", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ADD ROOM", "Adding room error: " + e.toString());
                    }
                });

            }
        });


        // Increment and Decrement number of persons per room --------------------
        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(numberOfPersons < 9) {
                   numberOfPersons++;
                   textInputNumberOfPersons.setText(String.valueOf(numberOfPersons));
               }
            }
        });

        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(numberOfPersons > 1) {
                   numberOfPersons--;
                   textInputNumberOfPersons.setText(String.valueOf(numberOfPersons));
               }
            }
        });
    }
}