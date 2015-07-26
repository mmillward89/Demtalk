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
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Mmillward89 on 17/07/2015.
 */
public class ServerRequests {

    public static final int CONNECTION_TIME = 1000 * 15;
    public static final String SERVER_ADDRESS ="http://10.0.2.2/demtalk/";

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
        private String errorMessage;

        public StoreUserDataAsyncTask(User user, GetUserCallBack userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
            this.errorMessage = null;
        }

        @Override
        protected String doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("username", user.username));
            dataToSend.add(new BasicNameValuePair("password", user.password));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "register.php");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();

                String result = EntityUtils.toString(entity);
                JSONObject jObject = new JSONObject(result);
                errorMessage = jObject.getString("message");

            } catch (Exception e) {
                e.printStackTrace();
            }

            return errorMessage;
        }

        @Override
        protected void onPostExecute(String errorMessage) {
            userCallBack.done(errorMessage, null);
            super.onPostExecute(errorMessage);
        }
    }

    //Login
    public class FetchUserDataAsyncTask extends AsyncTask<Void, Void, User> {
        private User user;
        private GetUserCallBack userCallBack;
        private String returnMessage;

        public FetchUserDataAsyncTask(User user, GetUserCallBack userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
            this.returnMessage = null;
        }

        @Override
        protected User doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("username", user.username));
            dataToSend.add(new BasicNameValuePair("password", user.password));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "login.php");

            User returnedUser = null;
            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                HttpResponse httpResponse = client.execute(post);

                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                JSONObject jObject = new JSONObject(result);

                returnMessage = jObject.getString("message");

                returnedUser = jObject.getBoolean("success") ?
                        new User(user.username, user.password) :
                        null;

            } catch (Exception e) {
                e.printStackTrace();
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
