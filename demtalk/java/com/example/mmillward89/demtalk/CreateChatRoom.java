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

import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.packet.DataForm;

import java.util.Random;


public class CreateChatRoom extends AppCompatActivity implements View.OnClickListener{
    Button create_chat_button;
    EditText subject_textbox, message_textbox;
    UserLocalStore userLocalStore;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat_room);

        subject_textbox = (EditText) findViewById(R.id.subject_textbox);
        message_textbox = (EditText) findViewById(R.id.message_textbox);
        create_chat_button = (Button) findViewById(R.id.create_chat_button);

        userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();

        create_chat_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.create_chat_button:
                String subject = subject_textbox.getText().toString();
                String message = message_textbox.getText().toString();
                String[] details = {subject, message};
                createChat(user, details);
                break;
        }
    }

    private void createChat(User user, String details[]) {
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.createChat(user, new PassMessageCallBack() {
            @Override
            public void done(String message) {
                showMessage(message);
                if(!(message.equals("Could not create chat, please try again"))) {
                    goToMainActivity();
                }
            }
        }, details);
    }


    private void showMessage(String s) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(s);
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.show();
    }

    private void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
