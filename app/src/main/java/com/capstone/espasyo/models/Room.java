package com.capstone.espasyo.models;

import java.util.List;

public class Room {
    //TODO: Must include Room ID
    private String roomID;
    private String roomName;
    private int price;
    private boolean isAvailable;
    private List<String> rentInclusions;
    private boolean hasBathroom;
    private boolean hasKitchen;
    private boolean hasBed;
    private boolean hasTable;
    private boolean hasChair;
    private boolean hasCabinet;


    public Room() {
        //empty room constructor
    }

    public Room(String roomName, int price, boolean isAvailable, List<String> rentInclusions, boolean hasBathroom, boolean hasKitchen, boolean hasBed, boolean hasTable, boolean hasChair, boolean hasCabinet) {
        this.roomName = roomName;
        this.price = price;
        this.isAvailable = isAvailable;
        this.rentInclusions = rentInclusions;
        this.hasBathroom = hasBathroom;
        this.hasKitchen = hasKitchen;
        this.hasBed = hasBed;
        this.hasTable = hasTable;
        this.hasChair = hasChair;
        this.hasCabinet = hasCabinet;
    }
}
