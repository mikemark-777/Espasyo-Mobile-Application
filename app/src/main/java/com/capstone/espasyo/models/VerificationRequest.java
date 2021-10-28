package com.capstone.espasyo.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class VerificationRequest implements Parcelable {

    private String verificationRequestID;
    private boolean isVerified;
    private String dateSubmitted;
    private String dateVerified;
    private String declinedVerificationDescription;
    private String propertyID;
    private String propertyName;
    private String propertyAddress;
    private String proprietorName;
    private String landlordName;
    private String landlordContactNumber;
    private String barangayBusinessPermitImageURL;
    private String municipalBusinessPermitImageURL;

    public VerificationRequest() {
        //empty verification request constructor **required
    }

    protected VerificationRequest(Parcel in) {
        verificationRequestID = in.readString();
        isVerified = in.readByte() != 0;
        dateSubmitted = in.readString();
        dateVerified = in.readString();
        declinedVerificationDescription = in.readString();
        propertyID = in.readString();
        propertyName = in.readString();
        propertyAddress = in.readString();
        proprietorName = in.readString();
        landlordName = in.readString();
        landlordContactNumber = in.readString();
        barangayBusinessPermitImageURL = in.readString();
        municipalBusinessPermitImageURL = in.readString();
    }

    public static final Creator<VerificationRequest> CREATOR = new Creator<VerificationRequest>() {
        @Override
        public VerificationRequest createFromParcel(Parcel in) {
            return new VerificationRequest(in);
        }

        @Override
        public VerificationRequest[] newArray(int size) {
            return new VerificationRequest[size];
        }
    };

    public void setVerificationRequestID(String verificationRequestID) {
        this.verificationRequestID = verificationRequestID;
    }

    public void setIsVerified(boolean verified) {
        isVerified = verified;
    }

    public void setDateSubmitted(String dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public void setDateVerified(String dateVerified) {
        this.dateVerified = dateVerified;
    }

    public void setDeclinedVerificationDescription(String declinedVerificationDescription) {
        this.declinedVerificationDescription = declinedVerificationDescription;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public void setProprietorName(String proprietorName) {
        this.proprietorName = proprietorName;
    }

    public void setLandlordName(String landlordName) {
        this.landlordName = landlordName;
    }

    public void setLandlordContactNumber(String landlordContactNumber) {
        this.landlordContactNumber = landlordContactNumber;
    }

    public void setPropertyID(String propertyID) {
        this.propertyID = propertyID;
    }

    public void setBarangayBusinessPermitImageURL(String barangayBusinessPermitImageURL) {
        this.barangayBusinessPermitImageURL = barangayBusinessPermitImageURL;
    }

    public void setMunicipalBusinessPermitImageURL(String municipalBusinessPermitImageURL) {
        this.municipalBusinessPermitImageURL = municipalBusinessPermitImageURL;
    }

    public String getVerificationRequestID() {
        return verificationRequestID;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public String getDateSubmitted() {
        return dateSubmitted;
    }

    public String getDateVerified() {
        return dateVerified;
    }

    public String getDeclinedVerificationDescription() {
        return declinedVerificationDescription;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public String getProprietorName() {
        return proprietorName;
    }

    public String getLandlordName() {
        return landlordName;
    }

    public String getLandlordContactNumber() {
        return landlordContactNumber;
    }

    public String getPropertyID() {
        return propertyID;
    }

    public String getBarangayBusinessPermitImageURL() {
        return barangayBusinessPermitImageURL;
    }

    public String getMunicipalBusinessPermitImageURL() {
        return municipalBusinessPermitImageURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(verificationRequestID);
        dest.writeByte((byte) (isVerified ? 1 : 0));
        dest.writeString(dateSubmitted);
        dest.writeString(dateVerified);
        dest.writeString(declinedVerificationDescription);
        dest.writeString(propertyID);
        dest.writeString(propertyName);
        dest.writeString(propertyAddress);
        dest.writeString(proprietorName);
        dest.writeString(landlordName);
        dest.writeString(landlordContactNumber);
        dest.writeString(barangayBusinessPermitImageURL);
        dest.writeString(municipalBusinessPermitImageURL);
    }
}
