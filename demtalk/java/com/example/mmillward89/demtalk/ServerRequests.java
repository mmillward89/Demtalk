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
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Mmillward89 on 17/07/2015.
 */
public class ServerRequests {

    public ServerRequests(Context context) {

    }

    public void storeUserDataInBackground(User user, GetUserCallBack callBack) {
        new StoreUserDataAsyncTask(user, callBack).execute();
    }

    public void fetchUserDataInBackground(User user, GetUserCallBack callBack) {
        new FetchUserDataAsyncTask(user, callBack).execute();
    }

    //Register
    public class StoreUserDataAsyncTask extends AsyncTask<Void, Void, String> {
        private User user;
        private GetUserCallBack userCallBack;
        private String returnMessage, username, password;

        public StoreUserDataAsyncTask(User user, GetUserCallBack userCallBack) {
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
    public class FetchUserDataAsyncTask extends AsyncTask<Void, Void, User> {
        private User user;
        private GetUserCallBack userCallBack;
        private String returnMessage, username, password;

        public FetchUserDataAsyncTask(User user, GetUserCallBack userCallBack) {
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
                    .setUsernameAndPassword("admin", "pridepark47")
                    .setServiceName("marks-macbook-pro.local")
                    .setHost("10.0.2.2")
                    .setPort(5222).setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .build();

            try {
                XMPPTCPConnection connection = new XMPPTCPConnection(config);
                connection.connect();
                connection.login(username, password);

                if(connection.isConnected() && connection.isAuthenticated()) {
                    returnedUser = new User(username, password);
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
