package com.capstone.espasyo.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Student implements Parcelable {

    private String studentID;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private int userRole;

    public Student() {
        //empty student constructor **
    }

    public Student(String studID, String firstName, String lastName, String email, String password, int userRole) {
        this.studentID = studID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }

    protected Student(Parcel in) {
        studentID = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        email = in.readString();
        password = in.readString();
        userRole = in.readInt();
    }

    public static final Creator<Student> CREATOR = new Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel in) {
            return new Student(in);
        }

        @Override
        public Student[] newArray(int size) {
            return new Student[size];
        }
    };

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUserRole() {
        return userRole;
    }

    public void setUserRole(int userRole) {
        this.userRole = userRole;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(studentID);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeInt(userRole);
    }
}
