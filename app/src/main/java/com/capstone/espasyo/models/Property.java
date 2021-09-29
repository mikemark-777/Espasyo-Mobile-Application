package com.capstone.espasyo.models;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Property {

    private String propertyID;
    private String owner;
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

    public Property(String propertyID, String owner, boolean isVerified, String propertyType, String name, String address, String landlordName, String landlordPhoneNumber, int minimumPrice, int maximumPrice) {
        this.propertyID = propertyID;
        this.owner = owner;
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

    public void setPropertyID(String propertyID) {
        this.propertyID = propertyID;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLandlordName(String landlordName) {
        this.landlordName = landlordName;
    }

    public void setLandlordPhoneNumber(String landlordPhoneNumber) {
        this.landlordPhoneNumber = landlordPhoneNumber;
    }

    public void setPriceRange(Map<String, Integer> priceRange) {
        this.priceRange = priceRange;
    }

    public String getPropertyID() { return propertyID; }

    public String getOwner() {
        return owner;
    }

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
