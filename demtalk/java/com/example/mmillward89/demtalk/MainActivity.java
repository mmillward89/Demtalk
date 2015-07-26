package com.example.mmillward89.demtalk;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button logout_button;
    EditText username_textbox_main;
    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username_textbox_main = (EditText) findViewById(R.id.username_textbox_main);
        logout_button = (Button) findViewById(R.id.logout_button);

        logout_button.setOnClickListener(this);

        userLocalStore = new UserLocalStore(this);
    }


    protected void onStart() {
        super.onStart();
        if(authenticate() == true){
            displayUserDetails();
        } else {
            startActivity(new Intent(this, Login.class));
        }

    }

    private boolean authenticate() {
        return userLocalStore.getUserLoggedIn();
    }

    private void displayUserDetails() {
        User user = userLocalStore.getLoggedInUser();
        username_textbox_main.setText(user.username);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.logout_button:
                userLocalStore.clearUserData();
                userLocalStore.setUserLoggedIn(false);
                startActivity(new Intent(this, Login.class));
                break;
        }
    }
}
