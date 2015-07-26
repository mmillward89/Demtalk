package com.example.mmillward89.demtalk;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Mmillward89 on 17/07/2015.
 */
public class UserLocalStore {

    public static final String SP_NAME = "userDetails";
    SharedPreferences userLocalDatabase;

    public UserLocalStore(Context context) {
        userLocalDatabase = context.getSharedPreferences(SP_NAME, 0);
    }

    //Could make the user immutable
    public void storeUserData(User user) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("username", user.username);
        spEditor.putString("password", user.password);
        spEditor.commit();
    }

    public User getLoggedInUser() {
        String username = userLocalDatabase.getString("username", "");
        String password = userLocalDatabase.getString("password", "");

        User storedUser = new User(username, password);
        return storedUser;
    }

    public void setUserLoggedIn(boolean loggedIn) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("loggedIn", loggedIn);
        spEditor.commit();
    }

    public void clearUserData() {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.clear();
        spEditor.commit();
    }

    public boolean getUserLoggedIn() {
        if (userLocalDatabase.getBoolean("loggedIn", false) == true) {
            return true;
        } else {
            return false;
        }
    }
}
