package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.adapters.EditRoomAdapter;
import com.capstone.espasyo.landlord.adapters.RoomAdapter;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.landlord.widgets.RoomRecyclerView;
import com.capstone.espasyo.models.Room;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ShowAllRoomsActivity extends AppCompatActivity {

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth fAuth;
    private FirebaseFirestore database;

    private RoomRecyclerView showAllRoomRecyclerView;
    private View roomRecylerViewEmptyState;
    private RoomAdapter roomAdapter;
    private ArrayList<Room> allRooms;

    private String propertyID;
    private ImageView btnBackToPropertyDetailsActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_show_all_rooms);

        //Initialize FirebaseConnection, FirebaseAuth and FirebaseFirestore
        firebaseConnection = FirebaseConnection.getInstance();
        fAuth = firebaseConnection.getFirebaseAuthInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        allRooms = new ArrayList<>();
    }
}