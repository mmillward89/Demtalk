package com.example.mmillward89.demtalk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mmillward89.demtalk.GetChatCallBack;
import com.example.mmillward89.demtalk.R;
import com.example.mmillward89.demtalk.User;
import com.example.mmillward89.demtalk.UserLocalStore;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

public class DisplayChat extends AppCompatActivity implements MessageListener, View.OnClickListener{
    private EditText add_message_textbox;
    private Button add_message_button;
    private String JID;
    private UserLocalStore userLocalStore;
    private User user;
    private MultiUserChat chatRoom;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_chat);

        add_message_textbox = (EditText) findViewById(R.id.add_message_textbox);
        add_message_button = (Button) findViewById(R.id.add_message_button);
        userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait");

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                JID = null;
            } else {
                JID = extras.getString("JID");
            }
        } else {
            JID = (String) savedInstanceState.getSerializable("JID");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        joinChat();
    }

    private void joinChat() {
        new DisplayChatAsyncTask(user, new GetChatCallBack() {
            @Override
            public void done(String message, MultiUserChat multiUserChat) {
                if(!(message.equals("Could not find chat, please try again"))) {
                    chatRoom = multiUserChat;
                } else {
                    showMessage(message);
                }
            }
        }, JID).execute();

        chatRoom.addMessageListener(this);
    }

    @Override
    public void processMessage(Message message) {
        LinearLayout l = new LinearLayout(this);
        TextView text = new TextView(getApplicationContext());
        text.setText(message.getBody());
        l.addView(text);
    }

    @Override
    public void onClick(View v) {
        String message = add_message_textbox.getText().toString();
        try {
            chatRoom.sendMessage(message);
        } catch (Exception e) {
            showMessage("Message not sent");
        }
    }

    private void showMessage(String s) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(s);
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.show();
    }

    //Finds the appropriate chat based on subject, joins it
    //and returns a reference to the object
    private class DisplayChatAsyncTask extends AsyncTask<Void, Void, MultiUserChat> {
        private GetChatCallBack callBack;
        private String returnMessage, username, password, JID;
        private XMPPTCPConnection connection;

        private DisplayChatAsyncTask(User user, GetChatCallBack callBack, String JID) {
            this.callBack = callBack;
            this.JID = JID;
            returnMessage = "Chat found";
            username = user.getUsername();
            password = user.getPassword();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected MultiUserChat doInBackground(Void... params) {
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setServiceName("marks-macbook-pro.local")
                    .setHost("10.0.2.2")
                    .setPort(5222).setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .build();

            MultiUserChat multiUserChat = null;
            try {
                connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(10000);
                connection.connect();
                connection.login(username, password);

                MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
                multiUserChat = manager.getMultiUserChat(JID);
                DiscussionHistory history = new DiscussionHistory();
                //need to get all history here
                history.setMaxStanzas(5);
                multiUserChat.join(username, "", history, connection.getPacketReplyTimeout());

            } catch (Exception e) {
                returnMessage = "Could not find chat, please try again";
            }

            return multiUserChat;
        }

        @Override
        protected void onPostExecute(MultiUserChat multiUserChat) {
            super.onPostExecute(multiUserChat);
            progressDialog.dismiss();
            callBack.done(returnMessage, multiUserChat);
        }
    }
}
