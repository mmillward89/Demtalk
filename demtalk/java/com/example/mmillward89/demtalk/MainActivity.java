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
    private Button logout_button, chat_button;
    private UserLocalStore userLocalStore;
    private User user;
    private ProgressDialog progressDialog;
    private List<HostedRoom> list;
    private HashMap<String, String> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chat_button = (Button) findViewById(R.id.chat_button);
        logout_button = (Button) findViewById(R.id.logout_button);

        chat_button.setOnClickListener(this);
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

    @Override
    protected void onStop() {
        super.onStop();
        this.map = null;
        //remove map as needs to refresh with newly created chat rooms
        //don't want duplicate key/value pairs
    }

    private boolean authenticate() {
        return userLocalStore.getUserLoggedIn();
    }

    private void getChatRoomButtons() {
        new GetChatRoomData(user, new PassMessageCallBack() {
            @Override
            public void done(String returnMessage) {

                if(returnMessage.equals("Could not connect")) {
                    showMessage(returnMessage);
                }

            }
        }, this).execute();
    }

    private void addMap(HashMap<String, String> map) {
        this.map = map;
    }

    private void addButtons(HashMap<String, String> map) {
        LinearLayout l = (LinearLayout) findViewById(R.id.main_activity_layout);

        if(map.keySet().size() == 0) {
            TextView textView = new TextView(this);
            textView.setText("No chat rooms created yet.");
            l.addView(textView);
        }
        else {
            for (String subject : map.keySet()) {
                Button b = new Button(this);
                b.setText(subject);
                b.setOnClickListener(this);
                l.addView(b);
            }
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
    private class GetChatRoomData extends AsyncTask<Void, Void, HashMap<String, String>> {
        private Context context;
        private String username, password, returnMessage;
        private XMPPTCPConnection connection;
        private PassMessageCallBack callBack;

        private GetChatRoomData(User user, PassMessageCallBack callBack, Context context) {
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

            HashMap<String, String> map = new HashMap<String, String>();

            try {
                connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(10000);
                connection.connect();
                connection.login(username, password);

                MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
                list = manager.getHostedRooms("conference.marks-macbook-pro.local");


                for (HostedRoom room : list) {
                    String jid = room.getJid();
                    MultiUserChat tempMuc =
                            manager.getMultiUserChat(jid);

                    if (!(tempMuc.isJoined())) {
                        tempMuc.join(user.getUsername());
                        Thread.sleep(3000);
                    }

                    String subject = tempMuc.getSubject();
                    map.put(subject, jid);

                }

                connection.disconnect();

            } catch (Exception e) {
                returnMessage = "Could not connect";
            }

            return map;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> map) {
            super.onPostExecute(map);
            addMap(map);
            addButtons(map);
            progressDialog.dismiss();
            callBack.done(returnMessage);
        }
    }
}
