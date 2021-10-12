package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.Room;

public class EditRoomActivity extends AppCompatActivity {

    private TextView roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_edit_room);

        roomName = findViewById(R.id.roomName);

        Intent intent = getIntent();
        Room room = intent.getParcelableExtra("room");        Toast.makeText(EditRoomActivity.this, "Room to edit: " + room.getRoomName(), Toast.LENGTH_SHORT).show();
        roomName.setText(room.getRoomName());
    }
}