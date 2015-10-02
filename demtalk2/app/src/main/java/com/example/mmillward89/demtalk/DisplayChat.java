package com.example.mmillward89.demtalk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.DefaultUserStatusListener;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.util.Iterator;

public class DisplayChat extends AppCompatActivity implements MessageListener, View.OnClickListener{
    private EditText add_message_textbox;
    private Button add_message_button;
    private TextView clear_text;
    private String[] Info;
    private UserLocalStore userLocalStore;
    private User user;
    private MultiUserChat chatRoom;
    private LinearLayout scrolllayout;
    private String messageBody, blankMessageTemplate, roomSubject;
    private ProgressDialog progressDialog;

    /**
     * Sets up buttons and layout, progess dialog, retrieves intent and calls
     * system to join chat room as appropriate
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_chat);

        scrolllayout = (LinearLayout) findViewById(R.id.scroll_layout);
        add_message_textbox = (EditText) findViewById(R.id.add_message_textbox);
        clear_text = (TextView) findViewById(R.id.clear_text_display);
        clear_text.setOnClickListener(this);

        add_message_button = (Button) findViewById(R.id.add_message_button);
        add_message_button.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.progress_title_display));
        progressDialog.setMessage(getString(R.string.progress_message));

        userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();
        //checked if the user has not entered a message when creating the chat
        blankMessageTemplate = user.getUsername() + ": \"" + "" + "\"";

        Info = new String[2];
        retrieveIntent(savedInstanceState);

        if(Info[0] != null) {
            joinChat();
        } else{
            showMessage(getString(R.string.no_room_message));
        }

    }

    /**
     * Adds icons to menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_display_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Icons are set to call appropriate activity
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
     * Retrieves subject and JID data
     * @param savedInstanceState
     */
    private void retrieveIntent(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Info = null;
            } else {
                Info = extras.getStringArray("JIDandSubject");

                //Won't produce nullpointerexception as user cannot create chat room without subject
                //Creates title bar for chat room displaying room subject
                roomSubject = "Room Subject: \"" + Info[1] + "\"";
            }
        } else {
            Info = (String[]) savedInstanceState.getSerializable("JIDandSubject");
        }
    }

    /**
     * Calls thread to join the appropriate chat room
     */
    private void joinChat() {
        new DisplayChatAsyncTask(user, new PassMessageCallBack() {
            @Override
            public void done(String message) {
                if(message.equals(getString(R.string.couldnt_find_chat))) {
                    showMessage(message);
                }
            }
        },Info).execute();
    }

    /**
     * Defines add message function, checking that the message is there and that it sends
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.add_message_button:
                String string = add_message_textbox.getText().toString();

                if(string.equals("")) {
                    showMessage(getString(R.string.please_add_message));
                } else {
                    Info[1] = user.getUsername() + ": \"" +
                            string + "\"";
                    try {
                        chatRoom.sendMessage(Info[1]);
                        add_message_textbox.setText("");
                    } catch (Exception e) {
                        String s = e.getMessage();

                        //checks if user account was removed
                        if(s.equals("SASLError using DIGEST-MD5: not-authorized")
                                || s.equals("Client is not, or no longer, connected")) {
                            startActivity(new Intent(getApplicationContext(), Login.class));
                        }
                        showMessage(getString(R.string.couldnt_send));
                    }
                }

                break;

            case R.id.clear_text_display:
                add_message_textbox.setText("");
                break;
        }
    }

    /**
     * Defines back key press to go to home page
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
     * Adds chat room object to activity
     * @param chatRoom
     */
    private void addChat(MultiUserChat chatRoom) {
        if(chatRoom == null) {
            showMessage(getString(R.string.no_chat_room_provided));
        } else {
            this.chatRoom = chatRoom;
            this.chatRoom.addMessageListener(this);
        }
    }

    /**
     * Adds chat room history messages to layout, calling processMessage to practically
     * add the message string object
     */
    private void addHistory() {

        try {

            boolean moreMessages = true;
            while (moreMessages) {
                Message message = chatRoom.nextMessage();

                if (message != null) {
                    processMessage(message);
                } else {
                    moreMessages = false;
                }
            }

        } catch (Exception e) {
            showMessage(getString(R.string.couldnt_display));
        }

    }

    /**
     * Adds a text view if no current messages exist within the system
     */
    private void showNoMessages() {
        TextView textView = new TextView(this);
        textView.setText(getString(R.string.no_messages));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(Color.parseColor("#FFFFFF"));
        scrolllayout.addView(textView);
    }

    /**
     * Takes the message content and adds a representation to the layout
     * @param message
     */
    @Override
    public void processMessage(final Message message) {

        messageBody = message.getBody();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                TextView textView = new TextView(DisplayChat.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(15, 15, 15, 15);
                textView.setLayoutParams(params);
                if(messageBody == null) {
                    //room subject
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    textView.setText(roomSubject);
                } else if(messageBody.equals(blankMessageTemplate)) {
                    textView.setText(getString(R.string.message_intro));
                }
                else {
                    textView.setText(messageBody);
                    textView.setBackgroundResource(R.drawable.speech_bubble_reverse);
                }
                textView.setTextIsSelectable(true);
                textView.setTextAppearance(DisplayChat.this, R.style.MessageFont);
                textView.setTextColor(Color.parseColor("#FFFFFF"));
                textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

                try {
                    scrolllayout.addView(textView);
                } catch (Exception e) {
                    showMessage(getString(R.string.couldnt_send));
                }

                showToast();

            }
        });

    }

    /**
     * Shows a brief notification to the user, called when messages are added to the
     * chat room
     */
    private void showToast() {
        Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.message_received),
                Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Creates an alert dialog based on the user parameters
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

    //Finds the appropriate chat based on subject, joins it
    //and returns a reference to the object
    private class DisplayChatAsyncTask extends AsyncTask<Void, Void, MultiUserChat> {
        private PassMessageCallBack callBack;
        private String returnMessage, username, password;
        private String[] info;
        private XMPPTCPConnection connection;

        private DisplayChatAsyncTask(User user, PassMessageCallBack callBack, String[] info) {
            if(user == null || callBack == null || info == null) {
                showMessage(getString(R.string.no_chat_details));
            } else {
                this.callBack = callBack;
                this.info = info;
                returnMessage = getString(R.string.chat_found);
                username = user.getUsername();
                password = user.getPassword();
            }
        }

        /**
         * Creates a progress dialog to avoid user input
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        /**
         * Retrieves chat room information and joins it
         * @param params
         * @return
         */
        @Override
        protected MultiUserChat doInBackground(Void... params) {
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setServiceName(getString(R.string.service_name))
                    .setHost(getString(R.string.host_name))
                    .setPort(5222).setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .build();

            MultiUserChat multiUserChat = null;
            try {
                connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(20000);
                connection.connect();
                connection.login(username, password);

                MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
                multiUserChat = manager.getMultiUserChat(info[0]);

                DiscussionHistory history = new DiscussionHistory();
                multiUserChat.join(username, "", history, connection.getPacketReplyTimeout());

                multiUserChat.addUserStatusListener(new DefaultUserStatusListener() {
                    @Override
                    public void banned(String actor, String reason) {
                        super.banned(actor, reason);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                });

            } catch (Exception e) {
                String s = e.getMessage();

                //checks if user account was removed
                if(s.equals("SASLError using DIGEST-MD5: not-authorized")) {
                    startActivity(new Intent(getApplicationContext(), Login.class));
                }

                returnMessage = getString(R.string.couldnt_find_chat);
            }

            return multiUserChat;
        }

        /**
         * Calls methods to add information to layout and dismiss progress dialog
         * @param multiUserChat
         */
        @Override
        protected void onPostExecute(MultiUserChat multiUserChat) {
            super.onPostExecute(multiUserChat);
            addChat(multiUserChat);
            addHistory();
            progressDialog.dismiss();
            callBack.done(returnMessage);
        }
    }
}
