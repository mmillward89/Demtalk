package com.example.mmillward89.demtalk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import java.util.Set;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button logout_button, chat_button, display_button;
    private UserLocalStore userLocalStore;
    private User user;
    private HashMap<String, String> map;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chat_button = (Button) findViewById(R.id.chat_button);
        display_button = (Button) findViewById(R.id.display_button);
        logout_button = (Button) findViewById(R.id.logout_button);

        chat_button.setOnClickListener(this);
        display_button.setOnClickListener(this);
        logout_button.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait");

        userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();

    }

    @Override
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
                if(returnMessage.equals("Could not connect") ||
                        returnMessage.equals("No topics created yet")) {
                    showMessage(returnMessage);
                } else {
                    addMap(map);
                    addButtons();
                }
            }
        }, this).execute();
    }

    private void addMap(HashMap<String, String> map) {
        this.map = map;
    }

    private void addButtons() {
        LinearLayout l = (LinearLayout) findViewById(R.id.main_activity_layout);
        for(String subject: map.keySet()) {
            Button b = new Button(this);
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

            case R.id.display_button:
                startActivity(new Intent(this, test.class));
                break;

            default:
                Button b = (Button) v;
                String subject = b.getText().toString();
                String jid = map.get(subject);
                Intent intent = new Intent(this, Login.class);
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
    private class GetChatRoomData extends AsyncTask<Void, Void, HashMap<String, String>> {
        private Context context;
        private String username, password, returnMessage;
        private XMPPTCPConnection connection;
        private GetSubjectsCallBack callBack;

        private GetChatRoomData(User user, GetSubjectsCallBack callBack, Context context) {
            this.context = context;
            this.callBack = callBack;
            username = user.getUsername(); password = user.getPassword();
            returnMessage = "Subjects found";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected HashMap<String, String> doInBackground(Void... params) {
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setServiceName("marks-macbook-pro.local")
                    .setHost("10.0.2.2")
                    .setPort(5222).setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .build();

            HashMap<String, String> returnmap = new HashMap<String, String>();

            try {
                connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(10000);
                connection.connect();
                connection.login(username, password);

                MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
                List<HostedRoom> list = manager.getHostedRooms("conference.marks-macbook-pro.local");

                if(list.size() != 0) {
                    for (HostedRoom room : list) {
                        String jid = room.getJid();
                        MultiUserChat tempMuc =
                                manager.getMultiUserChat(jid);
                        if (!(tempMuc.isJoined())) {
                            tempMuc.join(username);
                        }
                        String subject = tempMuc.getSubject();
                        returnmap.put(subject, jid);
                    }
                } else {
                    returnMessage = "No topics created yet";
                }

                connection.disconnect();

            } catch (Exception e) {
                returnMessage = "Could not connect";
            }

                return returnmap;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> map) {
            progressDialog.dismiss();
            callBack.done(returnMessage, map);
            super.onPostExecute(map);
        }
    }
}
