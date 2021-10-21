package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.adapters.EditRoomAdapter;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.models.Room;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ViewRoomsToEditActivity extends AppCompatActivity implements EditRoomAdapter.OnRoomListener{

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth fAuth;
    private FirebaseFirestore database;

    private RecyclerView editRoomRecyclerView;
    private EditRoomAdapter editRoomAdapter;
    private ArrayList<Room> propertyRooms;

    private String propertyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_view_rooms_to_edit);

        //Initialize FirebaseConnection, FirebaseAuth and FirebaseFirestore
        firebaseConnection = FirebaseConnection.getInstance();
        fAuth = firebaseConnection.getFirebaseAuthInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        propertyRooms = new ArrayList<>();

        getDataFromIntent();
        initEditRoomRecyclerView();
        fetchPropertyRooms();
    }

    public void getDataFromIntent() {
        //See if what is better, just propertyID or whole property Object
        Intent intent = getIntent();
        propertyID = intent.getStringExtra("propertyID");
    }

    public void initEditRoomRecyclerView()  {
        editRoomRecyclerView = findViewById(R.id.editRoomRecyclerView);
        editRoomRecyclerView.setHasFixedSize(true);
        LinearLayoutManager editRoomLayoutManager = new LinearLayoutManager(ViewRoomsToEditActivity.this, LinearLayoutManager.VERTICAL, false);
        editRoomRecyclerView.setLayoutManager(editRoomLayoutManager);
        editRoomAdapter = new EditRoomAdapter(ViewRoomsToEditActivity.this, propertyRooms, this);
        editRoomRecyclerView.setAdapter(editRoomAdapter);
    }

    public void fetchPropertyRooms() {
        String ownerPropertyID = propertyID;
        CollectionReference roomsCollection = database.collection("properties").document(ownerPropertyID)
                .collection("rooms");
        roomsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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