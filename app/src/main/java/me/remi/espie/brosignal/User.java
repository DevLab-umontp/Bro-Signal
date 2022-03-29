package me.remi.espie.brosignal;

import com.google.gson.Gson;

public class User {
    private String contactID = null;
    private String contactName = null;
    private String contactThumbnails= null;
    private String contactNumber = null;

    public User(String contactID, String contactName, String contactThumbnails, String contactNumber) {
        this.contactID = contactID;
        this.contactName = contactName;
        this.contactThumbnails = contactThumbnails;
        this.contactNumber = contactNumber;
    }

    public void setContactID(String contactID) {
        this.contactID = contactID;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setContactThumbnails(String contactThumbnails) {
        this.contactThumbnails = contactThumbnails;
    }

    public void setContactNumber(String contactNumber) {
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

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
