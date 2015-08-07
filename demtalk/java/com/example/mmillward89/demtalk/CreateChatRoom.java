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

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.packet.DataForm;

import java.util.List;
import java.util.Random;


public class CreateChatRoom extends AppCompatActivity implements View.OnClickListener{
    private Button create_chat_button;
    private EditText subject_textbox, message_textbox;
    private UserLocalStore userLocalStore;
    private User user;

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
        String subject = subject_textbox.getText().toString();
        String message = message_textbox.getText().toString();
        String[] details = {subject, message};
        createChat(user, details);

    }

    private void createChat(User user, String details[]) {
        new CreateChatAsyncTask(user, new PassMessageCallBack() {
            @Override
            public void done(String message) {
                if(message.equals("Could not create chat, please try again")) {
                    showMessage(message);
                } else {
                    goToMainActivity();
                }
            }
        }, details).execute();
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

    //Creates a connection, creates chat room and posts details based
    //on user input, confirms success or failure
    private class CreateChatAsyncTask extends AsyncTask<Void, Void, String> {
        private PassMessageCallBack callBack;
        private String returnMessage, username, password, subject, message;
        private XMPPTCPConnection connection;

         private CreateChatAsyncTask(User user, PassMessageCallBack callBack, String[] details) {
            this.callBack = callBack;
            subject= details[0]; message = details[1];
            username = user.getUsername(); password = user.getPassword();
            returnMessage = "Chat created";
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
                connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(10000);
                connection.connect();
                connection.login(username, password);

                MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
                List<HostedRoom> list = manager.getHostedRooms("conference.marks-macbook-pro.local");
                int i = (list.size()) + 1;
                MultiUserChat muc =
                        manager.getMultiUserChat("room" + i + "@conference.marks-macbook-pro.local");

                muc.create(username);
                muc.sendConfigurationForm(new Form(DataForm.Type.submit));
                muc.changeSubject(subject);
                muc.sendMessage(message);

                connection.disconnect();

            } catch (Exception e) {
                //Try again as it's possible but very unlikely creating chat rooms will overlap
                //and connection has already been established to log in
                returnMessage = "Could not create chat, please try again";
            }

            return returnMessage;
        }

        @Override
        protected void onPostExecute(String returnMessage) {
            super.onPostExecute(returnMessage);
            callBack.done(returnMessage);
        }
    }
}
