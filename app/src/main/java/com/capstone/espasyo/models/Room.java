package com.capstone.espasyo.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Room implements Parcelable {
    //TODO: Must include Room ID
    private String roomID;
    private String roomName;
    private int price;
    private int numberOfPersons;
    private boolean isAvailable;
    private boolean hasBathRoom;
    private boolean hasKitchen;


    public Room() {
        //empty room constructor
    }

    public Room(String roomID, String roomName, int price, int numberOfPersons, boolean isAvailable,  boolean hasBathroom, boolean hasKitchen) {
        this.roomID = roomID;
        this.roomName = roomName;
        this.price = price;
        this.numberOfPersons = numberOfPersons;
        this.isAvailable = isAvailable;
        this.hasBathRoom = hasBathroom;
        this.hasKitchen = hasKitchen;
    }

    protected Room(Parcel in) {
        roomID = in.readString();
        roomName = in.readString();
        price = in.readInt();
        numberOfPersons = in.readInt();
        isAvailable = in.readByte() != 0;
        hasBathRoom = in.readByte() != 0;
        hasKitchen = in.readByte() != 0;
    }

    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNumberOfPersons() {
        return numberOfPersons;
    }

    public void setNumberOfPersons(int numberOfPersons) {
        this.numberOfPersons = numberOfPersons;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isHasBathRoom() {
        return hasBathRoom;
    }

    public void setHasBathRoom(boolean hasBathRoom) {
        this.hasBathRoom = hasBathRoom;
    }

    public boolean isHasKitchen() {
        return hasKitchen;
    }

    public void setHasKitchen(boolean hasKitchen) {
        this.hasKitchen = hasKitchen;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(roomID);
        dest.writeString(roomName);
        dest.writeInt(price);
        dest.writeInt(numberOfPersons);
        dest.writeByte((byte) (isAvailable ? 1 : 0));
        dest.writeByte((byte) (hasBathRoom ? 1 : 0));
        dest.writeByte((byte) (hasKitchen ? 1 : 0));
    }
}
