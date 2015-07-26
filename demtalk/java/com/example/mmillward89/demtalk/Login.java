package com.example.mmillward89.demtalk;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class Login extends AppCompatActivity implements View.OnClickListener {

    Button login_button, startregister_button;
    EditText username_textbox, password_textbox;
    TextView clear_text;
    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username_textbox = (EditText) findViewById(R.id.username_textbox);
        password_textbox = (EditText) findViewById(R.id.password_textbox);
        login_button = (Button) findViewById(R.id.login_button);
        startregister_button = (Button) findViewById(R.id.startregister_button);
        clear_text = (TextView) findViewById(R.id.clear_text);

        login_button.setOnClickListener(this);
        startregister_button.setOnClickListener(this);
        clear_text.setOnClickListener(this);

        userLocalStore = new UserLocalStore(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.login_button:
                String username = username_textbox.getText().toString();
                String password = password_textbox.getText().toString();
                if(username.equals("") || password.equals("")){
                    showMessage("Please add a username and password.");
                } else {
                    User user = new User(username, password);
                    authenticate(user);
                }

                break;

            case R.id.startregister_button:
                startActivity(new Intent(this, Register.class));
                break;

            case R.id.clear_text:
                username_textbox.setText("");
                password_textbox.setText("");
                break;
        }
    }

    private void authenticate(User user) {
        //ServerRequests takes the callback object as a parameter, stores it
        //then when it's complete, calls that object's method. We create the object
        //below and define its method within the method parameter brackets, rather than
        //creating it above then adding it as a parameter.

        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.fetchUserDataInBackground(user, new GetUserCallBack() {
            @Override
            public void done(String errorMessage, User returnedUser) {
                if(returnedUser == null) {
                    showMessage(errorMessage);
                } else {
                    logUserIn(returnedUser);
                }
            }
        });
    }

    private void showMessage(String s) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Login.this);
        dialogBuilder.setMessage(s);
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.show();
    }

    private void logUserIn(User returnedUser) {
        userLocalStore.storeUserData(returnedUser);
        userLocalStore.setUserLoggedIn(true);

        startActivity(new Intent(this, MainActivity.class));
    }
}
