package com.example.mmillward89.demtalk;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Mmillward89 on 17/07/2015.
 */
public class ServerRequests {

    public ServerRequests(Context context) {

    }

    public void registerUserInBackground(User user, GetUserCallBack callBack) {
        new RegisterUserAsyncTask(user, callBack).execute();
    }

    public void loginUserInBackground(User user, GetUserCallBack callBack) {
        new LoginUserAsyncTask(user, callBack).execute();
    }

    public void createChat(User user, PassMessageCallBack callBack, String[] details) {
        new CreateChatAsyncTask(user, callBack, details).execute();
    }

    //Connect to server
    public class CreateChatAsyncTask extends AsyncTask<Void, Void, String> {
        private PassMessageCallBack callBack;
        private String returnMessage, username, password;
        private String[] details;
        private XMPPTCPConnection connection;

        public CreateChatAsyncTask(User user, PassMessageCallBack callBack, String[] details) {
            this.callBack = callBack;
            this.details = details;
            returnMessage = "Chat created";
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
                        manager.getMultiUserChat("room" + i + "@conference.marks-macbook-pro.local");

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

    //Register
    public class RegisterUserAsyncTask extends AsyncTask<Void, Void, String> {
        private User user;
        private GetUserCallBack userCallBack;
        private String returnMessage, username, password;

        public RegisterUserAsyncTask(User user, GetUserCallBack userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
            returnMessage = null;
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
                XMPPTCPConnection connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(10000);
                connection.connect();

                if(connection.isConnected()) {
                    AccountManager manager = AccountManager.getInstance(connection);
                    manager.createAccount(username, password);
                    returnMessage = "Account created";
                }
            } catch(Exception e) {
                returnMessage = "Could not register details, please ensure details are correct";
            }

            return returnMessage;
        }

        @Override
        protected void onPostExecute(String returnMessage) {
            userCallBack.done(returnMessage, user);
            super.onPostExecute(returnMessage);
        }
    }

    //Login
    public class LoginUserAsyncTask extends AsyncTask<Void, Void, User> {
        private User user;
        private GetUserCallBack userCallBack;
        private String returnMessage, username, password;

        public LoginUserAsyncTask(User user, GetUserCallBack userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
            returnMessage = null;
            username = user.getUsername();
            password = user.getPassword();
        }

        @Override
        protected User doInBackground(Void... params) {

            User returnedUser = null;

            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setServiceName("marks-macbook-pro.local")
                    .setHost("10.0.2.2")
                    .setPort(5222).setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .build();

            try {
                XMPPTCPConnection connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(10000);
                connection.connect();
                connection.login(username, password);

                if(connection.isConnected() && connection.isAuthenticated()) {
                    returnedUser = new User(username, password);
                } else {
                    //Shouldn't get here but just in case
                    returnMessage = "Could not login, please ensure details are correct";
                }
            } catch(Exception e) {
                returnMessage = "Could not login, please ensure details are correct";
            }

            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser) {
            userCallBack.done(returnMessage, returnedUser);
            super.onPostExecute(returnedUser);
        }
    }
}
