package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.Room;

public class RoomDetailsActivity extends AppCompatActivity {

    private TextView roomName,
                     roomPrice,
                     roomNumberOfPerson,
                     roomIsAvailable;
    
    private ImageView hasBathroomImageView,
                      hasKitchenImageView;

    private Room chosenRoom;
    final String AVAILABLE = "Available";
    final String UNAVAILABLE = "Unavailable";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_room_details);

        initializeViews();
        
        Intent intent = getIntent();
        getDataFromIntent(intent);

    }

    public void initializeViews() {
        roomName = findViewById(R.id.roomName_display);
        roomPrice = findViewById(R.id.roomPrice_display);
        roomNumberOfPerson = findViewById(R.id.roomNumberOfPerson_display);
        roomIsAvailable = findViewById(R.id.roomAvailability_display);

        hasBathroomImageView = findViewById(R.id.icon_hasBathroom);
        hasKitchenImageView = findViewById(R.id.icon_hasKitchen);
    }


    public void getDataFromIntent(Intent intent) {
        chosenRoom = intent.getParcelableExtra("chosenRoom");

        String name = chosenRoom.getRoomName();
        int price = chosenRoom.getPrice();
        int numberOfPerson = chosenRoom.getNumberOfPersons();
        boolean isAvailable = chosenRoom.getIsAvailable();
        boolean hasBathroom = chosenRoom.getHasBathRoom();
        boolean hasKitchen = chosenRoom.getHasKitchen();

        roomName.setText(name);
        roomPrice.setText(String.valueOf(price));
        roomNumberOfPerson.setText(String.valueOf(numberOfPerson));

        if(!isAvailable) {
            roomIsAvailable.setText(UNAVAILABLE);
            roomIsAvailable.setTextColor(this.getResources().getColor(R.color.espasyo_red_200));
        } else {
            roomIsAvailable.setText(AVAILABLE);
            roomIsAvailable.setTextColor(this.getResources().getColor(R.color.espasyo_green_200));
        }

        if(!hasBathroom) {
            hasBathroomImageView.setImageResource(R.drawable.icon_no_bathroom);
        }

        if(!hasKitchen) {
            hasKitchenImageView.setImageResource(R.drawable.icon_no_kitchen);
        }

    }
}