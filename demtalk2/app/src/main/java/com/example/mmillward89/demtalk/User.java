package com.example.mmillward89.demtalk;

/**
 * Created by Mmillward89 on 17/07/2015.
 */
public class User {
    String username, password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }
}
