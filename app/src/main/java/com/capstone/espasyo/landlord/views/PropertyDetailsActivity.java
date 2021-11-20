package com.capstone.espasyo.landlord.views;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.adapters.EditRoomAdapter;
import com.capstone.espasyo.landlord.adapters.RoomAdapter;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.landlord.widgets.RoomRecyclerView;
import com.capstone.espasyo.models.Landlord;
import com.capstone.espasyo.models.Property;
import com.capstone.espasyo.models.Room;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;

import java.util.ArrayList;
import java.util.List;

public class PropertyDetailsActivity extends AppCompatActivity implements RoomAdapter.OnRoomListener {

    private FirebaseConnection firebaseConnection;
    private FirebaseAuth fAuth;
    private FirebaseFirestore database;

    private RoomRecyclerView roomRecyclerView;
    private View roomRecylerViewEmptyState;
    private RoomAdapter roomAdapter;
    private ArrayList<Room> propertyRooms;

    //property object
    private Property property;
    //landlord object
    private Landlord landlord;

    private ImageButton imageButtonViewPropertyOnMap;
    private View showAllRooms;
    private String propertyID;

    //for verification information
    private ImageView verificationInfoIcon;
    private TextView verificationInfoMessage;
    private Button btnAddRoom;
    private final String UNVERIFIED_MESSAGE = "This property is not verified";
    private final String VERIFIED_MESSAGE = "Verified Property";
    private final String LOCKED_MESSAGE = "This property is locked by Admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_property_details);

        //Initialize FirebaseConnection, FirebaseAuth and FirebaseFirestore
        firebaseConnection = FirebaseConnection.getInstance();
        fAuth = firebaseConnection.getFirebaseAuthInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        propertyRooms = new ArrayList<>();

        initRoomRecyclerView();
        loadPropertyData();
        fetchPropertyRooms();

        btnAddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Must pass propertyID to addRoomActivity
                Intent intent = new Intent(PropertyDetailsActivity.this, AddRoomActivity.class);
                intent.putExtra("propertyID", propertyID);
                startActivity(intent);
            }
        });

        showAllRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PropertyDetailsActivity.this, ShowAllRoomsActivity.class);
                intent.putExtra("propertyID", propertyID);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        imageButtonViewPropertyOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PropertyDetailsActivity.this, ViewPropertyOnMapActivity.class);
                intent.putExtra("chosenProperty", property);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    // Functions -----

    //Load Property Details
    public void loadPropertyData() {
        //get data from intent
        Intent intent = getIntent();
        property = intent.getParcelableExtra("property");

        propertyID = property.getPropertyID();
        String landlordID = property.getOwner();
        boolean isVerified = property.getIsVerified();
        boolean isLocked = property.getIsLocked();

        //get landlord data
        getLandlord(landlordID);


        String name = property.getName();
        String propertyType = property.getPropertyType();
        String address = property.getAddress();
        int minimumPrice = property.getMinimumPrice();
        int maximumPrice = property.getMaximumPrice();
        boolean isElectricityIncluded = property.getIsElectricityIncluded();
        boolean isWaterIncluded = property.getIsWaterIncluded();
        boolean isInternetIncluded = property.getIsInternetIncluded();
        boolean isGarbageCollectionIncluded = property.getIsGarbageCollectionIncluded();

        TextView propName = findViewById(R.id.propertyNameDisplay);
        TextView propType = findViewById(R.id.propertyTypeDisplay);
        TextView propAddress = findViewById(R.id.propertyAddressDisplay);
        TextView propMinimumPrice = findViewById(R.id.propertyMinimumPriceDisplay);
        TextView propMaximumPrice = findViewById(R.id.propertyMaximumPriceDisplay);

        ImageView electricityImageView = findViewById(R.id.icon_electricity);
        ImageView waterImageView = findViewById(R.id.icon_water);
        ImageView internetImageView = findViewById(R.id.icon_internet);
        ImageView garbageCollectionImageView = findViewById(R.id.icon_garbage);
        LinearLayout verificationWarning = findViewById(R.id.verificationWarning);

        if (!isElectricityIncluded) {
            electricityImageView.setImageResource(R.drawable.icon_no_electricity);
        }
        if (!isWaterIncluded) {
            waterImageView.setImageResource(R.drawable.icon_no_water);
        }
        if (!isInternetIncluded) {
            internetImageView.setImageResource(R.drawable.icon_no_internet);
        }
        if (!isGarbageCollectionIncluded) {
            garbageCollectionImageView.setImageResource(R.drawable.icon_no_garbage);
        }

        propName.setText(name);
        propType.setText(propertyType);
        propAddress.setText(address);
        propMinimumPrice.setText(Integer.toString(minimumPrice));
        propMaximumPrice.setText(Integer.toString(maximumPrice));

        //set the visibility of the information about the verification of the property
        if (isVerified != true) {
            verificationWarning.setVisibility(View.VISIBLE);
            verificationWarning.setBackgroundColor(getResources().getColor(R.color.espasyo_red_200));
            verificationInfoIcon.setVisibility(View.VISIBLE);
            verificationInfoMessage.setText(UNVERIFIED_MESSAGE);
        } else {
            if (isLocked == true) {
                verificationWarning.setVisibility(View.VISIBLE);
                verificationWarning.setBackgroundColor(getResources().getColor(R.color.espasyo_red_200));
                verificationInfoIcon.setVisibility(View.VISIBLE);
                verificationInfoMessage.setText(LOCKED_MESSAGE);
            } else {
                verificationWarning.setVisibility(View.VISIBLE);
                verificationWarning.setBackgroundColor(getResources().getColor(R.color.espasyo_green_200));
                verificationInfoIcon.setVisibility(View.GONE);
                verificationInfoMessage.setText(VERIFIED_MESSAGE);
            }
        }
    }

    //initialize roomRecyclerView, layoutManager, and roomAdapter
    public void initRoomRecyclerView() {
        roomRecyclerView = (RoomRecyclerView) findViewById(R.id.roomsRecyclerView);
        roomRecylerViewEmptyState = findViewById(R.id.empty_room_state_propertyDetailsActivity_PDA);
        showAllRooms = findViewById(R.id.showAllRooms_propertyDetails);
        roomRecyclerView.showIfEmpty(roomRecylerViewEmptyState);
        roomRecyclerView.showIfRoomsAreGreaterThanSeven(showAllRooms);
        roomRecyclerView.setHasFixedSize(true);
        LinearLayoutManager roomLayoutManager = new LinearLayoutManager(PropertyDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        roomRecyclerView.setLayoutManager(roomLayoutManager);
        roomAdapter = new RoomAdapter(PropertyDetailsActivity.this, propertyRooms, this);
        roomRecyclerView.setAdapter(roomAdapter);

        //initialize views aside from recyclerview
        imageButtonViewPropertyOnMap = findViewById(R.id.imageButtonViewPropertyOnMap);
        verificationInfoIcon = findViewById(R.id.verificationInfoIcon);
        verificationInfoMessage = findViewById(R.id.verificationInfoMessage);
        btnAddRoom = findViewById(R.id.addRoomButton);
    }

    public void fetchPropertyRooms() {
        String ownerPropertyID = propertyID;
        CollectionReference roomsCollection = database.collection("properties").document(ownerPropertyID)
                .collection("rooms");
        roomsCollection
                .orderBy("roomName", Query.Direction.ASCENDING)
                .limit(7)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        propertyRooms.clear();
                        for (QueryDocumentSnapshot room : queryDocumentSnapshots) {
                            Room roomObj = room.toObject(Room.class);
                            propertyRooms.add(roomObj);
                        }
                        roomAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void getLandlord(String landlordID) {
        DocumentReference landlordDocRef = database.collection("landlords").document(landlordID);
        landlordDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                landlord = documentSnapshot.toObject(Landlord.class);
                TextView propLandlordName = findViewById(R.id.propertyLandlordNameDisplay);
                TextView propLandlordPhoneNumber = findViewById(R.id.propertyLandlordPhoneNumberDisplay);
                String landlordName = landlord.getFirstName() + " " + landlord.getLastName();
                String landlordPhoneNumber = landlord.getPhoneNumber();
                propLandlordName.setText(landlordName);
                propLandlordPhoneNumber.setText("+63" + landlordPhoneNumber);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PropertyDetailsActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // TODO: Handle Activity Life Cycle
    //propertyDetailActivity Lifecycle -------------------------------------------------------------
    @Override
    protected void onRestart() {
        super.onRestart();
        fetchPropertyRooms();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onRoomClick(int position) {
        Intent intent = new Intent(PropertyDetailsActivity.this, RoomDetailsActivity.class);
        intent.putExtra("chosenRoom", propertyRooms.get(position));
        startActivity(intent);
    }
}