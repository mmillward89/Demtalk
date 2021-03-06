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
    private Button logout_button, test;
    private UserLocalStore userLocalStore;
    private User user;
    private ProgressDialog progressDialog;
    private List<HostedRoom> list;
    private HashMap<String, String> jidMap;
    private String[] Subjects;
    private LinearLayout linearLayout;

    /**
     * Initializes the buttons, layout, user local store and progress dialog.
     * @param savedInstanceState
     */
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

        test = (Button) findViewById(R.id.test);
        test.setOnClickListener(this);
    }

    /**
     * Checks that the user is logged in, and if so, calls method to
     * load chat room buttons, otherwise sends user to login activity
     */
    @Override
    protected void onStart() {
        super.onStart();

        if(authenticate()){
            getChatRoomButtons();
        } else {
            startActivity(new Intent(this, Login.class));
        }
    }

    /**
     * Creates the icon menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Defines the icona ctions, sending user to the appropriate activity
     * @param item
     * @return
     */
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

            case R.id.display_links_icon:
                startActivity(new Intent(this, DisplayLinks.class));
                return true;

            case R.id.log_out_icon:
                startActivity(new Intent(this, Login.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Determines functionality for logout button and chat room buttons
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {

            case R.id.logout_button:
                userLocalStore.clearUserData();
                userLocalStore.setUserLoggedIn(false);
                startActivity(new Intent(this, Login.class));
                break;

            case R.id.test:
                startActivity(new Intent(this, Test.class));
                break;

            default:
                Button b = (Button) v;
                String subject = b.getText().toString();
                String jid = jidMap.get(subject);
                String[] info = {jid, subject};

                Intent intent1 = new Intent(this, DisplayChat.class);
                intent1.putExtra("JIDandSubject", info);
                startActivity(intent1);
                break;
        }
    }

    /**
     * Removes subject data to ensure information is reloaded for each activity refresh
     */
    @Override
    protected void onStop() {
        super.onStop();
        this.jidMap = null;
        //remove map as needs to refresh with newly created chat rooms
        //don't want duplicate key/value pairs
    }

    /**
     * Determines user is logged in
     * @return
     */
    private boolean authenticate() {
        return userLocalStore.getUserLoggedIn();
    }

    /**
     * Calls thread to get chat room data and create buttons
     */
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

    /**
     * Adds Chat Room subject map data to variable object
     * @param map
     */
    private void addMap(HashMap<String, String> map) {
        jidMap = map;
    }

    /**
     * Adds a chat room button for all available chat rooms to the layout,
     * setting the appropriate information
     * @param map
     */
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

    /**
     * Adds all room subject data to an array
     */
    private void getAllSubjects() {

        int size = jidMap.keySet().size();
        Subjects = new String[size];

        int i = 0;
        for(String subject : jidMap.keySet()) {
            Subjects[i] = subject;
            i++;
        }
    }

    /**
     * Creates and shows an alert dialog with the parameter message
     * @param s
     */
    private void showMessage(String s) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        if(s == null) {
            dialogBuilder.setMessage(getString(R.string.message_not_provided));
        } else {
            dialogBuilder.setMessage(s);
            dialogBuilder.setPositiveButton("OK", null);
            dialogBuilder.show();
        }
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
            if (user == null || callBack == null || context == null) {
               showMessage(getString(R.string.no_chat_details));
            } else {
                this.context = context;
                this.callBack = callBack;
                username = user.getUsername();
                password = user.getPassword();
                returnMessage = getString(R.string.subjects_found);
            }
        }

        /**
         * Shows progress dialog to prevent user input
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        /**
         * Creates a connection to the server and returns chat room subject and JID data
         * @param params
         * @return
         */
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
                connection.setPacketReplyTimeout(20000);
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

        /**
         * Calls the methods to add the data and display buttons, removes progress dialog
         * @param map
         */
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
