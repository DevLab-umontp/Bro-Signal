package me.remi.espie.brosignal;

import java.util.ArrayList;
import java.util.List;

public class UserGroup {

    private String name;
    private String description;
    private String customMessage;
    private int color;
    private List<User> userList = new ArrayList<>();
    private transient List<UserGroup> parentList = new ArrayList<>();

    public UserGroup() {
    }

    public UserGroup(String name, String description, String customMessage, int color, List<UserGroup> parentList) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.customMessage = customMessage;
        this.parentList = parentList;
    }

    public UserGroup(String name, String description, String customMessage, int color) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.customMessage = customMessage;
    }

    public List<UserGroup> getParentList() {
        return parentList;
    }

    public void setParentList(List<UserGroup> parentList) {
        this.parentList = parentList;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
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

    public boolean deleteSelf(){return parentList.remove(this);}

}
