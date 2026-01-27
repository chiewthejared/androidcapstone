package com.example.test_v2;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class HelperUserAccount {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String fullName;
    private String pin; // Hashed PIN
    private String profileImagePath; // Path to profile image

    // Constructor
    public HelperUserAccount(String fullName, String pin, String profileImagePath) {
        this.fullName = fullName;
        this.pin = pin;
        this.profileImagePath = profileImagePath;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
}