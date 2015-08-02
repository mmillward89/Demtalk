package com.example.mmillward89.demtalk;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button logout_button, chat_button;
    private UserLocalStore userLocalStore;
    private User user;
    private HashMap<String, String> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logout_button = (Button) findViewById(R.id.logout_button);
        chat_button = (Button) findViewById(R.id.chat_button);
        logout_button.setOnClickListener(this);
        chat_button.setOnClickListener(this);

        userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();
    }


    protected void onStart() {
        super.onStart();
        if(authenticate()){
            getChatRoomButtons();
        } else {
            startActivity(new Intent(this, Login.class));
        }

    }

    private boolean authenticate() {
        return userLocalStore.getUserLoggedIn();
    }

    private void getChatRoomButtons() {
        new GetChatRoomData(user, new GetSubjectsCallBack() {
            @Override
            public void done(String returnMessage, HashMap<String, String> map) {
                if(returnMessage.equals("No chat links to display")) {
                    showMessage(returnMessage);
                } else {
                    addMap(map);
                    addButtons();
                }
            }
        }, this);
    }

    private void addMap(HashMap map) {
        this.map = map;
    }

    private void addButtons() {
        for(String subject: map.keySet()) {
            LinearLayout l = new LinearLayout(this);
            Button b = new Button(getApplicationContext());
            b.setText(subject);
            b.setOnClickListener(this);
            l.addView(b);
        }
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.logout_button:
                userLocalStore.clearUserData();
                userLocalStore.setUserLoggedIn(false);
                startActivity(new Intent(this, Login.class));
                break;

            case R.id.chat_button:
                startActivity(new Intent(this, CreateChatRoom.class));
                break;

            default:
                Button b = (Button) v;
                String subject = b.getText().toString();
                String jid = map.get(subject);
                Intent intent = new Intent(this, DisplayChat.class);
                intent.putExtra("JID", jid);
                startActivity(intent);
                break;
        }
    }

    private void showMessage(String s) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(s);
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.show();
    }

    //Gets all room subject and JID data and adds them to hash map,
    //to allow display buttons with subject text and easy JID reference to
    //pass to DisplayChat when user clicks
    private class GetChatRoomData extends AsyncTask<Void, Void, HashMap> {
        private Context context;
        private String username, password, returnMessage;
        private XMPPTCPConnection connection;
        private HashMap<String, String> map;
        private GetSubjectsCallBack callBack;

        private GetChatRoomData(User user, GetSubjectsCallBack callBack, Context context) {
            this.context = context;
            this.callBack = callBack;
            map = new HashMap<String, String>();
            username = user.getUsername(); password = user.getPassword();
            returnMessage = "Subjects found";
        }

        @Override
        protected HashMap doInBackground(Void... params) {
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
                for(int i = 0; i<list.size(); i++) {
                    HostedRoom room = list.get(i);
                    String jid = room.getJid();
                    MultiUserChat tempMuc =
                            manager.getMultiUserChat(jid);
                    String subject = tempMuc.getSubject();
                    map.put(subject, jid);
                }

            } catch (Exception e) {
                returnMessage = "No chat links to display";
            }

            return map;
        }

        @Override
        protected void onPostExecute(HashMap map) {
            callBack.done(returnMessage, map);
            super.onPostExecute(map);
        }
    }
}
