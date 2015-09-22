package com.example.mmillward89.demtalk;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    private TextView clear_text;
    private EditText subject_textbox, message_textbox;
    private UserLocalStore userLocalStore;
    private User user;
    String[] Subjects;

    /**
     * Initializes interface and userlocalstore, retrieves intent information
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat_room);

        subject_textbox = (EditText) findViewById(R.id.subject_textbox);
        message_textbox = (EditText) findViewById(R.id.message_textbox);
        clear_text = (TextView) findViewById(R.id.clear_text_chat);
        clear_text.setOnClickListener(this);

        create_chat_button = (Button) findViewById(R.id.create_chat_button);
        create_chat_button.setOnClickListener(this);

        userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();

        retrieveIntent(savedInstanceState);

    }

    /**
     * Confirms user is logged in, takes them to login if not
     */
    @Override
    protected void onStart() {
        super.onStart();

        if(!authenticate()){
            startActivity(new Intent(this, Login.class));
        }
    }

    /**
     * Adds action bar and icons
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_chat_room, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Defines icon functionality, takes user to corresponding activity
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.main_menu_icon:
                startActivity(new Intent(this, MainActivity.class));
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
     * Retrieves subject data from intent and adds it to object as appropriately
     * @param savedInstanceState
     */
    private void retrieveIntent(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Subjects = null;
            } else {
                Subjects = extras.getStringArray("Subjects");
            }
        } else {
            Subjects = (String[]) savedInstanceState.getSerializable("Subjects");
        }
    }

    /**
     * Defines create and clear chat functionality, checks that subject does
     * not already exist
     * @param v
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.create_chat_button:
                String subject = subject_textbox.getText().toString();

                if(checkSubjects(subject)) {
                    String message = message_textbox.getText().toString();
                    String[] details = {subject, message};
                    createChat(user, details);
                }
                break;

            case R.id.clear_text_chat:
                subject_textbox.setText("");
                message_textbox.setText("");
                break;
        }


    }

    /**
     * Determines user pressing the back key takes them to the appropriate activity
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Returns true if user is logged in
     * @return
     */
    private boolean authenticate() {
        return userLocalStore.getUserLoggedIn();
    }

    /**
     * Called by button click, checks that the subject hasn't already been used
     * @param subject
     * @return
     */
    private boolean checkSubjects(String subject) {

        if(subject == null || subject.equals("")) {
            showMessage(getString(R.string.no_subjects));
            return false;

        } else if (Subjects != null) {

            for(int i = 0; i<Subjects.length; i++) {
                if(subject.equals(Subjects[i])) {
                    showMessage(getString(R.string.subject_exists));
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Calls thread that adds chat room to server
     * @param user
     * @param details
     */
    private void createChat(User user, String details[]) {

        if(user == null || details == null) {
            showMessage(getString(R.string.no_chat_details));
        } else {
            new CreateChatAsyncTask(user, new PassMessageCallBack() {
                @Override
                public void done(String message) {
                    if (message.equals(getString(R.string.no_chat_room))) {
                        showMessage(message);
                    } else {
                        goToMainActivity();
                    }
                }
            }, details).execute();
        }
    }

    /**
     * Shows alert dialog based on user parameters
     * @param s
     */
    private void showMessage(String s) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        if(s == null){
            dialogBuilder.setMessage("");
        } else {
            dialogBuilder.setMessage(s);
            dialogBuilder.setPositiveButton("OK", null);
            dialogBuilder.show();
        }
    }

    /**
     * Takes user to home page
     */
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
            if(user == null || callBack == null || details == null) {
                showMessage(getString(R.string.no_chat_details));
            } else {
                this.callBack = callBack;
                subject = details[0];
                message = details[1];
                username = user.getUsername();
                password = user.getPassword();
                returnMessage = getString(R.string.chat_created);
            }
        }

        /**
         * Adds user chat room to server, ensuring it has a unique id
         * @param params
         * @return
         */
        @Override
        protected String doInBackground(Void... params) {
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setServiceName(getString(R.string.service_name))
                    .setHost(getString(R.string.host_name))
                    .setPort(5222).setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .build();

            try {

                connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(10000);
                connection.connect();
                connection.login(username, password);

                MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
                List<HostedRoom> list = manager.getHostedRooms("conference." +
                        getString(R.string.service_name));
                int i = (list.size()) + 1;
                MultiUserChat muc =
                        manager.getMultiUserChat("room" + i + "@conference." +
                                getString(R.string.service_name));

                muc.create(username);
                muc.sendConfigurationForm(new Form(DataForm.Type.submit));
                muc.changeSubject(subject);
                if(!(message.equals(""))) {
                    muc.sendMessage(message);
                }

                connection.disconnect();

            } catch (Exception e) {
                String s = e.getMessage();

                //checks if user account was removed
                if(s.equals("SASLError using DIGEST-MD5: not-authorized")) {
                    startActivity(new Intent(getApplicationContext(), Login.class));
                }

                //Try again as it's possible but very unlikely creating chat rooms will overlap
                //and connection has already been established to log in
                returnMessage = getString(R.string.couldnt_create_chat);
            }

            return returnMessage;
        }

        //Passes success/failure return message to main class
        @Override
        protected void onPostExecute(String returnMessage) {
            super.onPostExecute(returnMessage);
            callBack.done(returnMessage);
        }
    }
}
