package com.example.mmillward89.demtalk;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class Test extends AppCompatActivity implements View.OnClickListener{
    private Button view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        view = (Button) findViewById(R.id.view);
        view.setOnClickListener(this);

        String[] items = { "<a href='http://www.facebook.com'>facebook</a>",
                "<a href='http://www.google.com'>google</a>", "<a href='http://www.reddit.com'>reddit</a>",
                "<a href='http://www.twitter.com'>twitter</a>" };



    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.view:



                break;
        }

    }
}
