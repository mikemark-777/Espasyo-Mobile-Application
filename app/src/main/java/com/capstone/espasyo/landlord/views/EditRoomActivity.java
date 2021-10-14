package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.LandlordMainActivity;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Room;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditRoomActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;

    private TextInputLayout textEditRoomNameLayout,
                            textEditRoomPriceLayout;

    private TextInputEditText textEditRoomName,
                              textEditRoomPrice;

    private SwitchCompat editRoomAvailabilitySwitch,
                         editBathroomSwitch,
                         editKitchenSwitch;

    private TextView textEditNumberOfPersons;
    private Button incrementEdit, decrementEdit;
    private int numberOfPersons = 1;
    private Button btnEditRoom,
                   btnCancelEditRoom,
                   btnDeleteRoom;
    private ImageView imageViewDeleteRoom,
                      imageButtonBackToViewRoomsToEdit;

    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_edit_room);

        //initialize firebaseConnection and firebaseFirestore
        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();

        initializeUI();
        displayRoomData();

        incrementEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(numberOfPersons < 9) {
                    numberOfPersons++;
                    textEditNumberOfPersons.setText(String.valueOf(numberOfPersons));
                }
            }
        });

        decrementEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(numberOfPersons > 1) {
                    numberOfPersons--;
                    textEditNumberOfPersons.setText(String.valueOf(numberOfPersons));
                }
            }
        });

        btnEditRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String editedRoomName = textEditRoomName.getText().toString().trim();
                String editedRoomPrice = textEditRoomPrice.getText().toString().trim();
                int editedNumberOfPersons =Integer.parseInt(textEditNumberOfPersons.getText().toString().trim());
                boolean editedRoomAvailability = editRoomAvailabilitySwitch.isChecked();
                boolean editedHasBathroom = editBathroomSwitch.isChecked();
                boolean editedHasKitchen = editKitchenSwitch.isChecked();

                if(areInputsValid(editedRoomName, editedRoomPrice)) {

                    room.setRoomName(editedRoomName);
                    room.setPrice(Integer.parseInt(editedRoomPrice));
                    room.setNumberOfPersons(editedNumberOfPersons);
                    room.setIsAvailable(editedRoomAvailability);
                    room.setHasBathRoom(editedHasBathroom);
                    room.setHasKitchen(editedHasKitchen);

                    saveChangesToRoom(room);
                }
            }
        });

        imageViewDeleteRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDeleteRoom();
            }
        });

        imageButtonBackToViewRoomsToEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /*----------------------------------------------------------- functions ---------------------------------------------------------------*/

    /*----------- input validations ----------*/

    public final String TAG = "[EDIT ROOM TESTING]";

    public boolean isRoomNameValid(String roomName) {
        if(!roomName.isEmpty()) {
            textEditRoomNameLayout.setError(null);
            Log.d(TAG, "ROOM NAME: NOT EMPTY");
            return true;
        } else {
            textEditRoomNameLayout.setError("Room Name Required");
            Log.d(TAG, "ROOM NAME: EMPTY");
            return false;
        }
    }

    public boolean isRoomPriceValid(String roomPrice) {
        if(!roomPrice.isEmpty()) {
            textEditRoomPriceLayout.setError(null);
            Log.d(TAG, "ROOM PRICE: NOT EMPTY");
            return true;
        } else {
            textEditRoomPriceLayout.setError("Room Price Required");
            Log.d(TAG, "ROOM PRICE: EMPTY");
            return false;
        }
    }

    public boolean areInputsValid(String roomName, String roomPrice) {

        boolean roomNameResult = isRoomNameValid(roomName);
        boolean roomPriceResult = isRoomPriceValid(roomPrice);

        if(roomNameResult && roomPriceResult) {
            Log.d(TAG, "CAN PROCEED: TRUE");
            return true;
        } else {
            Log.d(TAG, "CAN PROCEED: FALSE");
            return false;
        }
    }

    /*----------- other functions ----------*/

    public void initializeUI() {
        //initialize textInputLayouts, textInputEditTexts, textViews (numberOfRooms input), switches and buttons
        textEditRoomNameLayout = findViewById(R.id.text_edit_room_name_layout);
        textEditRoomPriceLayout = findViewById(R.id.text_edit_room_price_layout);
        textEditRoomName = findViewById(R.id.text_edit_room_name);
        textEditRoomPrice = findViewById(R.id.text_edit_room_price);
        textEditNumberOfPersons = findViewById(R.id.text_edit_numberOfPersons);
        editRoomAvailabilitySwitch = findViewById(R.id.editRoomAvailabilitySwitch);
        editBathroomSwitch = findViewById(R.id.editBathroomSwitch);
        editKitchenSwitch = findViewById(R.id.editKitchenSwitch);
        incrementEdit = findViewById(R.id.increment_Edit);
        decrementEdit = findViewById(R.id.decrement_Edit);
        btnEditRoom = findViewById(R.id.btnEditRoom);
        btnCancelEditRoom = findViewById(R.id.btnCancelEditRoom);
        btnDeleteRoom = findViewById(R.id.btnDeleteRoom);
        imageViewDeleteRoom = findViewById(R.id.imageViewDeleteRoom);
        imageButtonBackToViewRoomsToEdit = findViewById(R.id.imageButtonBackToViewRoomsToEdit);
    }

    public void displayRoomData() {
        Intent intent = getIntent();
        room = intent.getParcelableExtra("room");

        //get data from room object
        String roomName = room.getRoomName();
        int roomPrice = room.getPrice();
        numberOfPersons = room.getNumberOfPersons(); //TODO: see this and refactor
        boolean isRoomAvailable = room.getIsAvailable();
        boolean hasBathroom = room.getHasBathRoom();
        boolean hasKitchen = room.getHasKitchen();

        textEditRoomName.setText(roomName);
        textEditRoomPrice.setText(String.valueOf(roomPrice));
        textEditNumberOfPersons.setText(String.valueOf(numberOfPersons));
        editRoomAvailabilitySwitch.setChecked(isRoomAvailable);
        editBathroomSwitch.setChecked(hasBathroom);
        editKitchenSwitch.setChecked(hasKitchen);
    }

    public void saveChangesToRoom(Room editedRoom) {
        String propertyID = editedRoom.getPropertyID();
        String roomID = editedRoom.getRoomID();

        DocumentReference roomDocumentReference = database.collection("properties").document(propertyID)
                .collection("rooms").document(roomID);

        roomDocumentReference.set(editedRoom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(EditRoomActivity.this, "Room Successfully Edited!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditRoomActivity.this, "Error saving edited Room: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void showConfirmationDeleteRoom() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.landlord_delete_room_confirmation_dialog, null);

        Button btnConfirmDelete = view.findViewById(R.id.btnConfirmDeleteRoom);
        Button btnCancelDelete = view.findViewById(R.id.btnCancelDeleteRoom);

        AlertDialog confirmationDialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnConfirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRoom(room);
                confirmationDialog.cancel();
                finish();
            }
        });

        btnCancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationDialog.cancel();
            }
        });

        confirmationDialog.show();
    }

    public void deleteRoom(Room roomToDelete) {
        String propertyID = roomToDelete.getPropertyID();
        String roomID = roomToDelete.getRoomID();
        database.collection("properties").document(propertyID)
                .collection( "rooms").document(roomID)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(EditRoomActivity.this, "Room Successfully Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}