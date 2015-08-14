package com.example.mmillward89.demtalk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
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

public class DisplayChat extends AppCompatActivity implements MessageListener, View.OnClickListener{
    private EditText add_message_textbox;
    private Button add_message_button;
    private String[] Info;
    private UserLocalStore userLocalStore;
    private User user;
    private MultiUserChat chatRoom;
    private LinearLayout scrolllayout;
    private String messageBody;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_chat);

        scrolllayout = (LinearLayout) findViewById(R.id.scroll_layout);
        add_message_textbox = (EditText) findViewById(R.id.add_message_textbox);

        add_message_button = (Button) findViewById(R.id.add_message_button);
        add_message_button.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.progress_title_display));
        progressDialog.setMessage(getString(R.string.progress_message));

        userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();

        Info = new String[2];
        retrieveIntent(savedInstanceState);

        if(Info[0] != null) {
            joinChat();
        } else{
            showMessage(getString(R.string.no_room_message));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_display_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.main_menu_icon:
                startActivity(new Intent(this, MainActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void retrieveIntent(Bundle savedInstanceState) {
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
    }

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
        }
    }

    private void addChat(MultiUserChat chatRoom) {
        this.chatRoom = chatRoom;
        this.chatRoom.addMessageListener(this);
    }

    private void addHistory() {

        try {
            for (int i = 0; i < 5; i++) {
                Message message = chatRoom.nextMessage();

                if(message != null) {
                    processMessage(message);
                } else {
                    i = 5;
                }
            }
        } catch (Exception e) {
            showMessage(getString(R.string.couldnt_display));
        }

    }

    private void showNoMessages() {
        TextView textView = new TextView(this);
        textView.setText(getString(R.string.no_messages));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(Color.parseColor("#FFFFFF"));
        scrolllayout.addView(textView);
    }

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
                textView.setText(getString(R.string.no_messages_added));
                if (messageBody == null) {
                    textView.setText(getString(R.string.no_messages_added));
                } else {
                    textView.setText(messageBody);
                    textView.setBackgroundResource(R.drawable.speech_bubble_reverse);
                }
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

    private void showToast() {
        Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.message_received),
                Toast.LENGTH_SHORT);
        toast.show();
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
            returnMessage = getString(R.string.chat_found);
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
                    .setServiceName(getString(R.string.service_name))
                    .setHost(getString(R.string.host_name))
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
