package com.capstone.espasyo.models;

import java.util.ArrayList;
import java.util.Map;

public class ImageFolder {

    private String folderID;
    private ArrayList<String> images;

    public ImageFolder() {
        //empty constructor
    }

    public void setFolderID(String folderID) {
        this.folderID = folderID;
    }


    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public String getFolderID() {
        return folderID;
    }

    public ArrayList<String> getImages() {
        return images;
    }
}
