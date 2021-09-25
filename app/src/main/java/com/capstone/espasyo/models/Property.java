package com.capstone.espasyo.models;

import java.util.List;

public class Property {

    private String propertyID;
    private boolean isVerified;
    private String propertyType;
    private String name;
    private String address;
    private String landlordName;
    private String landlordPhoneNumber;
   // private List<Room> rooms;

    public Property() {
        //empty property constructor
    }

    public Property(String propertyID, boolean isVerified, String propertyType, String name, String address, String landlordName, String landlordPhoneNumber) {
        this.propertyID = propertyID;
        this.isVerified = isVerified;
        this.propertyType = propertyType;
        this.name = name;
        this.address = address;
        this.landlordName = landlordName;
        this.landlordPhoneNumber = landlordPhoneNumber;
    }

    public String getPropertyID() { return propertyID; }

    public boolean getIsVerified() {
        return isVerified;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getLandlordName() {
        return landlordName;
    }

    public String getLandlordPhoneNumber() {
        return landlordPhoneNumber;
    }

}
