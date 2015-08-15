package com.example.mmillward89.demtalk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.sasl.SASLError;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.DefaultUserStatusListener;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.UserStatusListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button logout_button;
    private UserLocalStore userLocalStore;
    private User user;
    private ProgressDialog progressDialog;
    private List<HostedRoom> list;
    private HashMap<String, String> map;
    private String[] Subjects;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = (LinearLayout) findViewById(R.id.main_scroll_view);

        logout_button = (Button) findViewById(R.id.logout_button);
        logout_button.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.progress_title));
        progressDialog.setMessage(getString(R.string.progress_message));

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.main_menu_icon:
                startActivity(new Intent(this, MainActivity.class));
                return true;

            case R.id.create_chat_icon:
                getAllSubjects();
                Intent intent = new Intent(this, CreateChatRoom.class);
                intent.putExtra("Subjects", Subjects);
                startActivity(intent);
                return true;

            case R.id.log_out_icon:
                startActivity(new Intent(this, Login.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
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

            default:
                Button b = (Button) v;
                String subject = b.getText().toString();
                String jid = map.get(subject);

                Intent intent1 = new Intent(this, DisplayChat.class);
                intent1.putExtra("JID", jid);
                startActivity(intent1);
                break;
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

                if(returnMessage.equals(getString(R.string.could_not_connect))) {
                    showMessage(returnMessage);
                    startActivity(new Intent(MainActivity.this, Login.class));
                }

            }
        }, this).execute();
    }

    private void addMap(HashMap<String, String> map) {
        this.map = map;
    }

    private void addButtons(HashMap<String, String> map) {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(15, 15, 15, 15);

        if(map.keySet().size() == 0) {
            TextView textView = new TextView(this);
            textView.setLayoutParams(params);
            textView.setText(R.string.no_chat_rooms);
            textView.setTextColor(Color.parseColor("#FFFFFF"));
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextSize(15);
            textView.setAllCaps(false);
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            linearLayout.addView(textView);
        }
        else {
            for (String subject : map.keySet()) {
                Button b = new Button(this);
                b.setLayoutParams(params);
                b.setText(subject);
                b.setOnClickListener(this);
                b.setBackgroundResource(R.drawable.buttonshape);
                b.setTextColor(Color.parseColor("#FFFFFF"));
                b.setAllCaps(false);
                linearLayout.addView(b);
            }
        }

    }

    private void getAllSubjects() {

        int size = map.keySet().size();
        Subjects = new String[size];

        int i = 0;
        for(String subject : map.keySet()) {
            Subjects[i] = subject;
            i++;
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
            returnMessage = getString(R.string.subjects_found);
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
                    .setServiceName(getString(R.string.service_name))
                    .setHost(getString(R.string.host_name))
                    .setPort(5222).setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .build();

            HashMap<String, String> map = new HashMap<String, String>();

            try {
                connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(10000);
                connection.connect();
                connection.login(username, password);

                MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
                list = manager.getHostedRooms("conference." + getString(R.string.service_name));


                for (HostedRoom room : list) {
                    String jid = room.getJid();
                    MultiUserChat tempMuc =
                            manager.getMultiUserChat(jid);

                    if (!(tempMuc.isJoined())) {
                        tempMuc.join(user.getUsername());
                        Thread.sleep(2000);
                    }

                    String subject = tempMuc.getSubject();
                    map.put(subject, jid);
                }

                connection.disconnect();

            } catch (Exception e) {
                String s = e.getMessage();

                //checks if user account was removed
                if(s.equals("SASLError using DIGEST-MD5: not-authorized")) {
                    startActivity(new Intent(getApplicationContext(), Login.class));
                }
                returnMessage = getString(R.string.could_not_connect);
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
