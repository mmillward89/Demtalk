package com.example.mmillward89.demtalk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.UserManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.packet.DataForm;

import java.util.Collection;
import java.util.List;


public class Login extends AppCompatActivity implements View.OnClickListener {

    private Button login_button, startregister_button;
    private EditText username_textbox, password_textbox;
    private TextView clear_text;
    private UserLocalStore userLocalStore;

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
        userLocalStore.clearUserData();
        userLocalStore.setUserLoggedIn(false);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.login_button:
                String username = username_textbox.getText().toString();
                String password = password_textbox.getText().toString();
                if(username.equals("") || password.equals("")){
                    showMessage(getString(R.string.blank_textboxes));
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
        new LoginUserAsyncTask(user, new GetUserCallBack() {
            @Override
            public void done(String errorMessage, User returnedUser) {
                if (returnedUser == null) {
                    showMessage(errorMessage);
                } else {
                    logUserIn(returnedUser);
                }
            }
        }).execute();
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

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private class LoginUserAsyncTask extends AsyncTask<Void, Void, User> {
        private User user;
        private GetUserCallBack userCallBack;
        private String returnMessage, username, password;

        private LoginUserAsyncTask(User user, GetUserCallBack userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
            returnMessage = null;
            username = user.getUsername();
            password = user.getPassword();
        }

        @Override
        protected User doInBackground(Void... params) {

            User returnedUser = null;

            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setServiceName(getString(R.string.service_name))
                    .setHost(getString(R.string.host_name))
                    .setPort(5222).setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .build();

            try {
                XMPPTCPConnection connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(10000);
                connection.connect();
                connection.login(username, password);

                returnedUser = new User(username, password);

                connection.disconnect();

            } catch(Exception e) {
                returnMessage = getString(R.string.couldnt_login_message);
            }

            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser) {
            userCallBack.done(returnMessage, returnedUser);
            super.onPostExecute(returnedUser);
        }
    }
}
