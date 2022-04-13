package me.remi.espie.brosignal;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class UserGroup {

    private String name;
    private String description;
    private int color;
    private List<User> userList = new ArrayList<>();

    public UserGroup() {
    }

    public UserGroup(String name, String description, int color) {
        this.name = name;
        this.description = description;
        this.color = color;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public void addUser(User user){
        if (!userList.contains(user)) userList.add(user);
    }

    public boolean removeUser(User user){
        return userList.remove(user);
    }

}
