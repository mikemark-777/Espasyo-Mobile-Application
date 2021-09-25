package com.capstone.espasyo.models;

import java.util.List;

public class Property {

    private boolean isVerified;
    private String name;
    private String address;
    private String proprietorName;
    private String landlordName;
    private String landlordPhoneNumber;
    private List<Room> rooms;

    public Property() {
        //empty property constructor
    }


    public Property(boolean isVerified, String name, String address, String proprietorName, String landlordName, String landlordPhoneNumber) {
        this.isVerified = isVerified;
        this.name = name;
        this.address = address;
        this.proprietorName = proprietorName;
        this.landlordName = landlordName;
        this.landlordPhoneNumber = landlordPhoneNumber;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getProprietorName() {
        return proprietorName;
    }

    public String getLandlordName() {
        return landlordName;
    }

    public String getLandlordPhoneNumber() {
        return landlordPhoneNumber;
    }
}
