package com.capstone.espasyo.models;

import java.util.List;

public class Room {
    private String roomName;
    private int price;
    private boolean isAvailable;
    private List<String> rentInclusions;
    private boolean hasBathroom;
    private boolean hasKitchen;


    public Room() {
        //empty room constructor
    }

    public Room(String roomName, int price, boolean isAvailable, List<String> rentInclusions, boolean hasBathroom, boolean hasKitchen) {
        this.roomName = roomName;
        this.price = price;
        this.isAvailable = isAvailable;
        this.rentInclusions = rentInclusions;
        this.hasBathroom = hasBathroom;
        this.hasKitchen = hasKitchen;
    }


}
