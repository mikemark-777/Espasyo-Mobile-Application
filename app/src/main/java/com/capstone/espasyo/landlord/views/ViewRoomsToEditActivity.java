package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.adapters.EditRoomAdapter;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.landlord.widgets.RoomRecyclerView;
import com.capstone.espasyo.models.Room;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ViewRoomsToEditActivity extends AppCompatActivity implements EditRoomAdapter.OnRoomListener{

    private FirebaseConnection firebaseConnection;
    private FirebaseFirestore database;

    private RoomRecyclerView editRoomRecyclerView;
    private View roomRecylerViewEmptyState;
    private EditRoomAdapter editRoomAdapter;
    private ArrayList<Room> propertyRooms;

    private String propertyID;
    private ImageView btnBackToChooseEditActivity,
                      btnAddRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_view_rooms_to_edit);

        //Initialize FirebaseConnection, FirebaseAuth and FirebaseFirestore
        firebaseConnection = FirebaseConnection.getInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        propertyRooms = new ArrayList<>();

        getDataFromIntent();
        initEditRoomRecyclerView();
        fetchPropertyRooms();

        btnBackToChooseEditActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewRoomsToEditActivity.this, AddRoomActivity.class);
                intent.putExtra("propertyID", propertyID);
                startActivity(intent);
            }
        });

    }

    public void getDataFromIntent() {
        //See if what is better, just propertyID or whole property Object
        Intent intent = getIntent();
        propertyID = intent.getStringExtra("propertyID");
    }

    public void initEditRoomRecyclerView()  {
        roomRecylerViewEmptyState = findViewById(R.id.empty_room_state_viewRoomsToEdit);
        editRoomRecyclerView = (RoomRecyclerView) findViewById(R.id.editRoomRecyclerView);
        editRoomRecyclerView.showIfEmpty(roomRecylerViewEmptyState);
        editRoomRecyclerView.setHasFixedSize(true);
        LinearLayoutManager editRoomLayoutManager = new LinearLayoutManager(ViewRoomsToEditActivity.this, LinearLayoutManager.VERTICAL, false);
        editRoomRecyclerView.setLayoutManager(editRoomLayoutManager);
        editRoomAdapter = new EditRoomAdapter(ViewRoomsToEditActivity.this, propertyRooms, this);
        editRoomRecyclerView.setAdapter(editRoomAdapter);

        //initialize other views aside from recyclerview
        btnBackToChooseEditActivity = findViewById(R.id.btn_back_to_ChooseEditActivity);
        btnAddRoom = findViewById(R.id.imageButtonAddRoom_viewRoomToEdit);
    }

    public void fetchPropertyRooms() {
        String ownerPropertyID = propertyID;
        CollectionReference roomsCollection = database.collection("properties").document(ownerPropertyID)
                .collection("rooms");

        roomsCollection
                .orderBy("roomName", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                propertyRooms.clear();
                for(QueryDocumentSnapshot room : queryDocumentSnapshots) {
                    Room roomObj = room.toObject(Room.class);
                    propertyRooms.add(roomObj);
                }
                editRoomAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onRoomClick(int position) {
        Intent intent = new Intent(ViewRoomsToEditActivity.this, EditRoomActivity.class );
        intent.putExtra("room", propertyRooms.get(position));
        startActivity(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        fetchPropertyRooms();
    }

    /*----------------------------------------------------------- functions ---------------------------------------------------------------*/

    // TODO: Handle Activity Life Cycle

}