package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Room;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
            btnCancelAddRoom; //TODO: add cancel functionality

    String propertyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_add_room);

        Intent intent = getIntent();
        propertyID = intent.getStringExtra("propertyID");

        //initialize firebaseConnection, firebaseAuth and firebaseFirestore
        firebaseConnection = FirebaseConnection.getInstance();
        fAuth = firebaseConnection.getFirebaseAuthInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();

        initializeViews();

        // add room
        btnAddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomName = textInputRoomName.getText().toString().trim();
                String roomPriceInString = textInputRoomPrice.getText().toString().trim();
                int numberOfPersonInRoom = Integer.parseInt(textInputNumberOfPersons.getText().toString().trim());
                boolean isRoomAvailable = roomAvailabilitySwitch.isChecked();
                boolean hasBathroom = bathroomSwitch.isChecked();
                boolean hasKitchen = kitchenSwitch.isChecked();

                if (areInputsValid(roomName, roomPriceInString)) {
                    //TODO: Must add input validations here
                    int roomPrice = Integer.parseInt(roomPriceInString);
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

                    addNewRoom(propertyID, newRoomID, newRoom);

                }
            }
        });

        btnCancelAddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Increment and Decrement number of persons per room --------------------
        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOfPersons < 9) {
                    numberOfPersons++;
                    textInputNumberOfPersons.setText(String.valueOf(numberOfPersons));
                }
            }
        });

        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOfPersons > 1) {
                    numberOfPersons--;
                    textInputNumberOfPersons.setText(String.valueOf(numberOfPersons));
                }
            }
        });
    }

    /*----------------------------------------------------------- functions ---------------------------------------------------------------*/

    /*----------- input validations ----------*/
    public final String TAG = "[ADD ROOM TESTING]";

    private boolean isRoomNameValid(String roomName) {
        if (!roomName.isEmpty()) {
            textInputRoomNameLayout.setError(null);
            Log.d(TAG, "ROOM NAME: NOT EMPTY");
            return true;
        } else {
            textInputRoomNameLayout.setError("Room Name Required");
            Log.d(TAG, "ROOM NAME: EMPTY");
            return false;
        }
    }

    private boolean isRoomPriceEmpty(String roomPrice) {
        if (!roomPrice.isEmpty()) {
            textInputRoomPriceLayout.setError(null);
            Log.d(TAG, "ROOM PRICE: NOT EMPTY");
            return true;
        } else {
            textInputRoomPriceLayout.setError("Room Price Required");
            Log.d(TAG, "ROOM PRICE: EMPTY");
            return false;
        }
    }

    private boolean areInputsValid(String roomName, String roomPrice) {
        boolean roomNameResult = isRoomNameValid(roomName);
        boolean roomPriceResult = isRoomPriceEmpty(roomPrice);

        if (roomNameResult && roomPriceResult) {
            Log.d(TAG, "CAN PROCEED: TRUE");
            return true;
        } else {
            Log.d(TAG, "CAN PROCEED: FALSE");
            return false;
        }
    }

    /*----------- other functions ----------*/

    //initialize textInputLayouts, textInputEditTexts, textView (numberOfRooms input), switches and buttons
    public void initializeViews() {
        textInputRoomNameLayout = findViewById(R.id.text_input_room_name_layout);
        textInputRoomPriceLayout = findViewById(R.id.text_input_room_price_layout);
        textInputRoomName = findViewById(R.id.text_input_room_name);
        textInputRoomPrice = findViewById(R.id.text_input_room_price);
        textInputNumberOfPersons = findViewById(R.id.text_input_numberOfPersons);
        roomAvailabilitySwitch = findViewById(R.id.roomAvailabilitySwitch);
        bathroomSwitch = findViewById(R.id.bathroomSwitch);
        kitchenSwitch = findViewById(R.id.kitchenSwitch);
        roomAvailabilitySwitch = findViewById(R.id.roomAvailabilitySwitch);
        textInputNumberOfPersons = findViewById(R.id.text_input_numberOfPersons);
        increment = findViewById(R.id.increment);
        decrement = findViewById(R.id.decrement);
        btnAddRoom = findViewById(R.id.btnAddRoom);
        btnCancelAddRoom = findViewById(R.id.btnCancelAddRoom);
    }

    public void addNewRoom(String propertyID, String newRoomID, Room newRoom) {

        // TESTING PURPOSES - Add newRoom to properties/{propertyID}/rooms/{newRoomID}
        roomsDocumentReference = database.collection("properties").document(propertyID)
                .collection("rooms").document(newRoomID);
        roomsDocumentReference.set(newRoom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
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

    // TODO: Handle Activity Life Cycle
} 