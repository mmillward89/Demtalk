package com.example.mmillward89.demtalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class test extends AppCompatActivity implements View.OnClickListener, MessageListener{
    private EditText add_reply_textbox;
    private Button add_reply_button;
    private String[] Info;
    private UserLocalStore userLocalStore;
    private User user;
    private MultiUserChat multiUserChat;
    private LinearLayout layout;
    private String messageBody;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        layout = (LinearLayout) findViewById(R.id.test_layout);
        add_reply_textbox = (EditText) findViewById(R.id.add_reply_textbox);
        add_reply_button = (Button) findViewById(R.id.add_reply_button);
        add_reply_button.setOnClickListener(this);
        userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();
        Info = new String[2];

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Info[0] = null;
            } else {
                Info[0] = extras.getString("JID");
            }
        } else {
            Info[0] = (String) savedInstanceState.getSerializable("JID");
        }

        if(Info[0] != null) {
            joinChat();
        } else{
            showMessage("Room information not passed");
        }

    }

    private void joinChat() {
        new DisplayChatAsyncTask(user, new PassMessageCallBack() {
            @Override
            public void done(String message) {
                if(message.equals("Could not find chat, please try again")) {
                    showMessage(message);
                }
            }
        },Info).execute();
    }

    @Override
    public void onClick(View v) {
        Info[1] = add_reply_textbox.getText().toString();
        try {
            multiUserChat.sendMessage(Info[1]);
        } catch (Exception e) {
            showMessage("Could not send message");
        }
    }

    @Override
    public void processMessage(Message message) {
        messageBody = message.getBody();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    TextView textView = new TextView(test.this);
                    textView.setLayoutParams(new LinearLayout.LayoutParams
                            (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    textView.setText(messageBody);
                    layout.addView(textView);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


    }

    private void addChat(MultiUserChat multiUserChat) {
        this.multiUserChat = multiUserChat;
        this.multiUserChat.addMessageListener(this);
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
        private PassMessageCallBack callBack;
        private String returnMessage, username, password;
        private String[] info;
        private XMPPTCPConnection connection;

        private DisplayChatAsyncTask(User user, PassMessageCallBack callBack, String[] info) {
            this.callBack = callBack;
            this.info = info;
            returnMessage = "Chat found";
            username = user.getUsername();
            password = user.getPassword();
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
                multiUserChat = manager.getMultiUserChat(info[0]);

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
            addChat(multiUserChat);
            callBack.done(returnMessage);
        }
    }
}
