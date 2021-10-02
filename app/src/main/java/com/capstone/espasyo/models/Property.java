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

public class Property implements Parcelable{

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

    public Property(){
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

    protected Property(Parcel in) {
        propertyID = in.readString();
        owner = in.readString();
        isVerified = in.readByte() != 0;
        propertyType = in.readString();
        name = in.readString();
        address = in.readString();
        landlordName = in.readString();
        landlordPhoneNumber = in.readString();
        minimumPrice = in.readInt();
        maximumPrice = in.readInt();
        isElectricityIncluded = in.readByte() != 0;
        isWaterIncluded = in.readByte() != 0;
        isInternetIncluded = in.readByte() != 0;
        isGarbageCollectionIncluded = in.readByte() != 0;
    }


    public static final Creator<Property> CREATOR = new Creator<Property>() {
        @Override
        public Property createFromParcel(Parcel in) {
            return new Property(in);
        }

        @Override
        public Property[] newArray(int size) {
            return new Property[size];
        }
    };

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

    public boolean getIsElectricityIncluded() {
        return isElectricityIncluded;
    }

    public boolean getIsWaterIncluded() {
        return isWaterIncluded;
    }

    public boolean getIsInternetIncluded() {
        return isInternetIncluded;
    }

    public boolean getIsGarbageCollectionIncluded() {
        return isGarbageCollectionIncluded;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(propertyID);
        dest.writeString(owner);
        dest.writeByte((byte) (isVerified ? 1 : 0));
        dest.writeString(propertyType);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(landlordName);
        dest.writeString(landlordPhoneNumber);
        dest.writeInt(minimumPrice);
        dest.writeInt(maximumPrice);
        dest.writeByte((byte) (isElectricityIncluded ? 1 : 0));
        dest.writeByte((byte) (isWaterIncluded ? 1 : 0));
        dest.writeByte((byte) (isInternetIncluded ? 1 : 0));
        dest.writeByte((byte) (isGarbageCollectionIncluded ? 1 : 0));
    }
}
