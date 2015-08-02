package com.example.mmillward89.demtalk;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.packet.DataForm;

import java.util.List;
import java.util.Random;


public class DisplayChat extends ActionBarActivity implements MessageListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_chat);

    }

    public class DisplayChatAsyncTask extends AsyncTask<Void, Void, String> {
        private PassMessageCallBack callBack;
        private String returnMessage, username, password;
        private String[] details;
        private XMPPTCPConnection connection;

        public DisplayChatAsyncTask(User user, PassMessageCallBack callBack, String[] details) {
            this.callBack = callBack;
            this.details = details;
            returnMessage = "Chat created.";
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
                connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(10000);
                connection.connect();
                connection.login(username, password);

                MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
                List<HostedRoom> list = manager.getHostedRooms("conference.marks-macbook-pro.local");
                int i = (list.size()) + 1;
                MultiUserChat muc =
                        manager.getMultiUserChat("@conference.marks-macbook-pro.local");

                muc.create(username);
                muc.sendConfigurationForm(new Form(DataForm.Type.submit));
                muc.changeSubject(details[0]);
                muc.sendMessage(details[1]);

            } catch (Exception e) {
                returnMessage = "Could not create chat, please try again";
            }

            return returnMessage;
        }

        @Override
        protected void onPostExecute(String returnMessage) {
            callBack.done(returnMessage);
            super.onPostExecute(returnMessage);
        }
    }

    @Override
    public void processMessage(Message message) {
        LinearLayout l = new LinearLayout(this);
        TextView text = new TextView(getApplicationContext());
        text.setText(message.getBody());
        text.setBackgroundResource(R.drawable.speech_bubble);
        l.addView(text);
    }
}
