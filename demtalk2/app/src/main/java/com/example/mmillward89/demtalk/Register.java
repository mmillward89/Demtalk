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

    /**
     * Initializes buttons, layout, textboxes
     * @param savedInstanceState
     */
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

    /**
     * Initializes buttons to register if details have been provided
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_button:

                String username = username_textbox_register.getText().toString();
                String password = password_textbox_register.getText().toString();
                if(username.equals("") || password.equals("")){
                    showMessage(getString(R.string.blank_textboxes));
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

    /**
     * Called if details exist, calls thread to register details with server
     * @param user
     */
    private void registerUser(User user) {
        if(user == null) {
            showMessage(getString(R.string.user_details_not_provided));
        } else {
            new RegisterUserAsyncTask(user, new GetUserCallBack() {
                @Override
                public void done(String message, User returnedUser) {
                    showMessage(message);
                }
            }).execute();
        }
    }

    /**
     * Shows alert dialog to user with defined message
     * @param s
     */
    private void showMessage(String s) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Register.this);
        if(s == null) {
            dialogBuilder.setMessage(getString(R.string.message_not_provided));
        } else {
            dialogBuilder.setMessage(s);
            dialogBuilder.setPositiveButton("OK", null);
            dialogBuilder.show();
        }
    }

    private class RegisterUserAsyncTask extends AsyncTask<Void, Void, String> {
        private User user;
        private GetUserCallBack userCallBack;
        private String returnMessage, username, password;

        private RegisterUserAsyncTask(User user, GetUserCallBack userCallBack) {
            if (user == null || userCallBack == null) {
                showMessage(getString(R.string.no_chat_details));
            } else {
                this.user = user;
                this.userCallBack = userCallBack;
                returnMessage = null;
                username = user.getUsername();
                password = user.getPassword();
            }
        }

        /**
         * Registers user details with server
         * @param params
         * @return
         */
        @Override
        protected String doInBackground(Void... params) {
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

                AccountManager manager = AccountManager.getInstance(connection);
                manager.createAccount(username, password);
                returnMessage = getString(R.string.account_created);

                connection.disconnect();

            } catch(Exception e) {
                returnMessage = getString(R.string.couldnt_register_message);
            }

            return returnMessage;
        }

        /**
         * Passes message back to activity determining success or failure
         * @param returnMessage
         */
        @Override
        protected void onPostExecute(String returnMessage) {
            userCallBack.done(returnMessage, user);
            super.onPostExecute(returnMessage);
        }
    }
}
