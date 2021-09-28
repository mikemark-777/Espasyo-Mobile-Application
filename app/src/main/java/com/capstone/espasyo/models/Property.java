package com.capstone.espasyo.models;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Property {

    private String propertyID;
    private boolean isVerified;
    private String propertyType;
    private String name;
    private String address;
    private String landlordName;
    private String landlordPhoneNumber;
    Map<String, Integer> priceRange;

    public Property() {
        //empty property constructor **required
    }

    public Property(String propertyID, boolean isVerified, String propertyType, String name, String address, String landlordName, String landlordPhoneNumber, int minimumPrice, int maximumPrice) {
        this.propertyID = propertyID;
        this.isVerified = isVerified;
        this.propertyType = propertyType;
        this.name = name;
        this.address = address;
        this.landlordName = landlordName;
        this.landlordPhoneNumber = landlordPhoneNumber;


        priceRange = new HashMap<>();
        priceRange.put("minimumPrice", minimumPrice);
        priceRange.put("maximumPrice", maximumPrice);
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

    public Map<String, Integer> getPriceRange() {
        return priceRange;
    }
}
