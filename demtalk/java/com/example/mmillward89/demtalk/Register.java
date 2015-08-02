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


public class Register extends AppCompatActivity implements View.OnClickListener{

    Button register_button;
    EditText username_textbox_register, password_textbox_register;
    TextView clear_text_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username_textbox_register = (EditText) findViewById(R.id.username_textbox_register);
        password_textbox_register = (EditText) findViewById(R.id.password_textbox_register);
        register_button = (Button) findViewById(R.id.register_button);
        clear_text_register = (TextView) findViewById(R.id.clear_text_register);

        register_button.setOnClickListener(this);
        clear_text_register.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_button:

                String username = username_textbox_register.getText().toString();
                String password = password_textbox_register.getText().toString();
                if(username.equals("") || password.equals("")){
                    showMessage("Please add a username and password.");
                } else {
                    User user = new User(username, password);
                    registerUser(user);
                }
                break;

            case R.id.clear_text_register:
                username_textbox_register.setText("");
                password_textbox_register.setText("");
                break;
        }
    }

    private void registerUser(User user) {
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.registerUserInBackground(user, new GetUserCallBack() {
            @Override
            public void done(String message, User returnedUser) {
                showMessage(message);
                startActivity(new Intent(Register.this, Login.class));
            }
        });
    }

    private void showMessage(String s) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Register.this);
        dialogBuilder.setMessage(s);
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.show();
    }
}
