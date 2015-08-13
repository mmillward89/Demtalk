package com.example.mmillward89.demtalk;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;


public class Register extends AppCompatActivity implements View.OnClickListener{

    Button register_button, back_to_login_button;
    EditText username_textbox_register, password_textbox_register;
    TextView clear_text_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username_textbox_register = (EditText) findViewById(R.id.username_textbox_register);
        password_textbox_register = (EditText) findViewById(R.id.password_textbox_register);
        register_button = (Button) findViewById(R.id.register_button);
        back_to_login_button = (Button) findViewById(R.id.back_to_login_button);
        clear_text_register = (TextView) findViewById(R.id.clear_text_register);

        register_button.setOnClickListener(this);
        clear_text_register.setOnClickListener(this);
        back_to_login_button.setOnClickListener(this);

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

            case R.id.back_to_login_button:
                startActivity(new Intent(this, Login.class));
                break;

            case R.id.clear_text_register:
                username_textbox_register.setText("");
                password_textbox_register.setText("");
                break;
        }
    }

    private void registerUser(User user) {
        new RegisterUserAsyncTask(user, new GetUserCallBack() {
            @Override
            public void done(String message, User returnedUser) {
                showMessage(message);
                startActivity(new Intent(Register.this, Login.class));
            }
        }).execute();
    }

    private void showMessage(String s) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Register.this);
        dialogBuilder.setMessage(s);
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.show();
    }

    private class RegisterUserAsyncTask extends AsyncTask<Void, Void, String> {
        private User user;
        private GetUserCallBack userCallBack;
        private String returnMessage, username, password;

        private RegisterUserAsyncTask(User user, GetUserCallBack userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
            returnMessage = null;
            username = user.getUsername();
            password = user.getPassword();
        }

        @Override
        protected String doInBackground(Void... params) {
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setServiceName("marks-macbook-pro.local")
                    .setHost("10.0.2.2")
                    .setPort(5222).setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .build();
            try {
                XMPPTCPConnection connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(10000);
                connection.connect();

                AccountManager manager = AccountManager.getInstance(connection);
                manager.createAccount(username, password);
                returnMessage = "Account created";

                connection.disconnect();

            } catch(Exception e) {
                returnMessage = "Could not register details, please ensure details are correct";
            }

            return returnMessage;
        }

        @Override
        protected void onPostExecute(String returnMessage) {
            userCallBack.done(returnMessage, user);
            super.onPostExecute(returnMessage);
        }
    }
}
