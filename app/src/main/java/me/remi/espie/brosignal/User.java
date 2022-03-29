package me.remi.espie.brosignal;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

public class User {
    private final String contactID;
    private final String contactName;
    private final String contactThumbnails;
    private final String contactNumber;

    public User(String contactID, String contactName, String contactThumbnails, String contactNumber) {
        this.contactID = contactID;
        this.contactName = contactName;
        this.contactThumbnails = contactThumbnails;
        this.contactNumber = contactNumber;
    }

    public String getContactID() {
        return contactID;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactThumbnails() {
        return contactThumbnails;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
