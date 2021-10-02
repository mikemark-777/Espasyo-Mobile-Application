package com.capstone.espasyo.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class Property{

    private String propertyID;
    private String owner;
    private boolean isVerified;
    private String propertyType;
    private String name;
    private String address;
    private String landlordName;
    private String landlordPhoneNumber;
    private int minimumPrice;
    private int maximumPrice;
    private boolean isElectricityIncluded;
    private boolean isWaterIncluded;
    private boolean isInternetIncluded;
    private boolean isGarbageCollectionIncluded;



    public Property() {
        //empty property constructor **required
    }

    public Property(String propertyID, String owner, boolean isVerified, String propertyType, String name, String address, String landlordName, String landlordPhoneNumber, int minimumPrice, int maximumPrice, boolean isElectricityIncluded, boolean isWaterIncluded, boolean isInternetIncluded, boolean isGarbageCollectionIncluded) {
        this.propertyID = propertyID;
        this.owner = owner;
        this.isVerified = isVerified;
        this.propertyType = propertyType;
        this.name = name;
        this.address = address;
        this.landlordName = landlordName;
        this.landlordPhoneNumber = landlordPhoneNumber;
        this.minimumPrice = minimumPrice;
        this.maximumPrice = maximumPrice;
        this.isElectricityIncluded = isElectricityIncluded;
        this.isWaterIncluded = isWaterIncluded;
        this.isInternetIncluded = isInternetIncluded;
        this.isGarbageCollectionIncluded = isGarbageCollectionIncluded;
    }


    //setters

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

    public void setMinimumPrice(int minimumPrice) {
        this.minimumPrice = minimumPrice;
    }

    public void setMaximumPrice(int maximumPrice) {
        this.maximumPrice = maximumPrice;
    }

    public void setElectricityIncluded(boolean electricityIncluded) {
        isElectricityIncluded = electricityIncluded;
    }

    public void setWaterIncluded(boolean waterIncluded) {
        isWaterIncluded = waterIncluded;
    }

    public void setInternetIncluded(boolean internetIncluded) {
        isInternetIncluded = internetIncluded;
    }

    public void setGarbageCollectionIncluded(boolean garbageCollectionIncluded) {
        isGarbageCollectionIncluded = garbageCollectionIncluded;
    }

    //getters

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

    public int getMinimumPrice() {
        return minimumPrice;
    }

    public int getMaximumPrice() {
        return maximumPrice;
    }

    public boolean isElectricityIncluded() {
        return isElectricityIncluded;
    }

    public boolean isWaterIncluded() {
        return isWaterIncluded;
    }

    public boolean isInternetIncluded() {
        return isInternetIncluded;
    }

    public boolean isGarbageCollectionIncluded() {
        return isGarbageCollectionIncluded;
    }
}
