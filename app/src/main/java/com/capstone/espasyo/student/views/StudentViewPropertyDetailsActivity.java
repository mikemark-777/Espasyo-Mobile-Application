package com.capstone.espasyo.student.views;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.capstone.espasyo.R;
import com.capstone.espasyo.landlord.adapters.RoomAdapter;
import com.capstone.espasyo.landlord.customdialogs.CustomProgressDialog;
import com.capstone.espasyo.landlord.repository.FirebaseConnection;
import com.capstone.espasyo.landlord.views.AddRoomActivity;
import com.capstone.espasyo.landlord.views.ManagePropertyImageActivity;
import com.capstone.espasyo.landlord.views.RoomDetailsActivity;
import com.capstone.espasyo.landlord.views.ShowAllRoomsActivity;
import com.capstone.espasyo.landlord.views.ViewPropertyOnMapActivity;
import com.capstone.espasyo.landlord.widgets.RoomRecyclerView;
import com.capstone.espasyo.models.ImageFolder;
import com.capstone.espasyo.models.Landlord;
import com.capstone.espasyo.models.Property;
import com.capstone.espasyo.models.Room;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemChangeListener;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
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

import java.util.ArrayList;

public class StudentViewPropertyDetailsActivity extends AppCompatActivity implements RoomAdapter.OnRoomListener {

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


    //for property image
    private ImageFolder propertyImageFolder;
    private ImageSlider propertyImageSlider;
    private CustomProgressDialog progressDialog;
    private ArrayList<String> downloadedURLs = new ArrayList<>();
    private ImageView btnZoomImage;
    private int imageIndex = 0;
    private ImageView emptyImagesDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_activity_view_property_details);

        //Initialize FirebaseConnection, FirebaseAuth and FirebaseFirestore
        firebaseConnection = FirebaseConnection.getInstance();
        fAuth = firebaseConnection.getFirebaseAuthInstance();
        database = firebaseConnection.getFirebaseFirestoreInstance();
        propertyRooms = new ArrayList<>();

        initRoomRecyclerView();
        loadPropertyData();
        fetchPropertyRooms();

        showAllRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentViewPropertyDetailsActivity.this, StudentShowAllRoomsActivity.class);
                intent.putExtra("propertyID", propertyID);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        imageButtonViewPropertyOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentViewPropertyDetailsActivity.this, ViewPropertyOnMapActivity.class);
                intent.putExtra("chosenProperty", property);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        propertyImageSlider.setItemChangeListener(new ItemChangeListener() {
            @Override
            public void onItemChanged(int i) {
                imageIndex = i;
            }
        });

        btnZoomImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downloadedURLs.size() > 0) {
                    Intent intent = new Intent(StudentViewPropertyDetailsActivity.this, StudentPreviewImageActivity.class);
                    intent.putExtra("previewImage", downloadedURLs.get(imageIndex));
                    startActivity(intent);
                }
            }
        });
    }

    // Functions -----

    //Load Property Details
    public void loadPropertyData() {
        //get data from intent
        Intent intent = getIntent();
        property = intent.getParcelableExtra("property");
        getImageFolderOf(property);

        propertyID = property.getPropertyID();
        String landlordID = property.getOwner();

        //get landlord data
        getLandlord(landlordID);

        String name = property.getName();
        String propertyType = property.getPropertyType();
        String address = property.getAddress();
        int minimumPrice = property.getMinimumPrice();
        int maximumPrice = property.getMaximumPrice();
        boolean isElectricityIncluded = property.isElectricityIncluded();
        boolean isWaterIncluded = property.isWaterIncluded();
        boolean isInternetIncluded = property.isInternetIncluded();
        boolean isGarbageCollectionIncluded = property.isGarbageCollectionIncluded();

        TextView propName = findViewById(R.id.propertyNameDisplay);
        TextView propType = findViewById(R.id.propertyTypeDisplay);
        TextView propAddress = findViewById(R.id.propertyAddressDisplay);
        TextView propMinimumPrice = findViewById(R.id.propertyMinimumPriceDisplay);
        TextView propMaximumPrice = findViewById(R.id.propertyMaximumPriceDisplay);

        ImageView electricityImageView = findViewById(R.id.icon_electricity);
        ImageView waterImageView = findViewById(R.id.icon_water);
        ImageView internetImageView = findViewById(R.id.icon_internet);
        ImageView garbageCollectionImageView = findViewById(R.id.icon_garbage);

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

    }

    //initialize roomRecyclerView, layoutManager, and roomAdapter
    public void initRoomRecyclerView() {
        roomRecyclerView = (RoomRecyclerView) findViewById(R.id.roomsRecyclerView);
        roomRecylerViewEmptyState = findViewById(R.id.empty_room_state_propertyDetailsActivity_PDA);
        showAllRooms = findViewById(R.id.showAllRooms_propertyDetails);
        roomRecyclerView.showIfEmpty(roomRecylerViewEmptyState);
        roomRecyclerView.showIfRoomsAreGreaterThanSeven(showAllRooms);
        roomRecyclerView.setHasFixedSize(true);
        LinearLayoutManager roomLayoutManager = new LinearLayoutManager(StudentViewPropertyDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        roomRecyclerView.setLayoutManager(roomLayoutManager);
        roomAdapter = new RoomAdapter(StudentViewPropertyDetailsActivity.this, propertyRooms, this);
        roomRecyclerView.setAdapter(roomAdapter);

        //initialize views aside from recyclerview
        imageButtonViewPropertyOnMap = findViewById(R.id.imageButtonViewPropertyOnMap);
        btnZoomImage = findViewById(R.id.btnZoomImage_student);

        //for property images
        propertyImageSlider = findViewById(R.id.image_slider_propertyDetails);
        progressDialog = new CustomProgressDialog(this);
        emptyImagesDisplay = findViewById(R.id.emptyImagesDisplay);
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
                Landlord landlord = documentSnapshot.toObject(Landlord.class);
                displayLandlordDetails(landlord);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(StudentViewPropertyDetailsActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayLandlordDetails(Landlord landlord) {
        TextView propLandlordName = findViewById(R.id.propertyLandlordNameDisplay);
        TextView propLandlordPhoneNumber = findViewById(R.id.propertyLandlordPhoneNumberDisplay);

        String landlordName = landlord.getFirstName() + " " + landlord.getLastName();
        String landlordPhoneNumber = landlord.getPhoneNumber();

        propLandlordName.setText(landlordName);
        propLandlordPhoneNumber.setText("+63" + landlordPhoneNumber);
    }

    //fetch the imageFolder from the property
    public void getImageFolderOf(Property property) {
        String imageFolderID = property.getImageFolder();
        //will check if the property has imageFolder, if not create an imageFolder
        if (imageFolderID != null) {
            DocumentReference imageFolderDocRef = database.collection("imageFolders").document(imageFolderID);
            progressDialog.showProgressDialog("Loading images...", false);
            imageFolderDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    propertyImageFolder = documentSnapshot.toObject(ImageFolder.class);
                    displayImagesFrom(propertyImageFolder);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(StudentViewPropertyDetailsActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(StudentViewPropertyDetailsActivity.this, "imageFolder of property is null", Toast.LENGTH_SHORT).show();
        }
    }

    //will display images from imageFolder of the property
    public void displayImagesFrom(ImageFolder imageFolder) {
        ArrayList<SlideModel> imageSlides = new ArrayList<>();
        propertyImageSlider.setImageList(imageSlides);
        if (imageFolder != null) {
            downloadedURLs = imageFolder.getImages();

            if (!downloadedURLs.isEmpty()) {
                //emptyImagesDisplay.setVisibility(View.GONE);
                for (String url : downloadedURLs) {
                    imageSlides.add(new SlideModel(url, ScaleTypes.CENTER_INSIDE));
                }
                propertyImageSlider.setImageList(imageSlides);
                progressDialog.dismissProgressDialog();
            } else {
                //emptyImagesDisplay.setVisibility(View.VISIBLE);
                progressDialog.dismissProgressDialog();
            }
        } else {
            Toast.makeText(StudentViewPropertyDetailsActivity.this, "NULL", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRoomClick(int position) {
        Intent intent = new Intent(StudentViewPropertyDetailsActivity.this, RoomDetailsActivity.class);
        intent.putExtra("chosenRoom", propertyRooms.get(position));
        startActivity(intent);
    }

    // TODO: Handle Activity Life Cycle
    //propertyDetailActivity Lifecycle -------------------------------------------------------------
    @Override
    protected void onRestart() {
        super.onRestart();
        fetchPropertyRooms();
        getImageFolderOf(property);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}