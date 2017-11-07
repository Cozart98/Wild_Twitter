package com.example.apprenti.twitter;

/**
 * Created by apprenti on 06/11/17.
 */

public class User {


    private String user_name;
    private String user_password;
    private String user_quest;
    private String user_createdquestID;
    private String user_createdquestName;
    private String user_indice;
    private String user_challenge;
    private int score;
    private String state;

    public User() {
        // Needed for firebase
    }

    public User(String user_name, String user_password) {
        this.user_name = user_name;
        this.user_password = user_password;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_password() {
        return user_password;
    }

    public void setUser_password(String user_password) {
        this.user_password = user_password;
    }
}
