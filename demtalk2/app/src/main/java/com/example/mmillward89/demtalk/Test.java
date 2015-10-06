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
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Test extends AppCompatActivity implements View.OnClickListener{
    private Button view;
    private String[] items;
    private ArrayList<String> list;
    private ListView listView1;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        listView1 = (ListView) findViewById(R.id.listView1);
        view = (Button) findViewById(R.id.view);
        view.setOnClickListener(this);

        items = new String[]{ "<a href='http://www.facebook.com'>facebook</a>",
                "<a href='http://www.google.com'>google</a>", "<a href='http://www.reddit.com'>reddit</a>",
                "<a href='http://www.twitter.com'>twitter</a>" };

        list = new ArrayList<>();

        for(int i=0; i<items.length; i++) {
            list.add(items[i]);
        }

        adapter = new ArrayAdapter<String>(Test.this, R.layout.simplerow, list);
        listView1.setAdapter(adapter);

    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.view:



                break;
        }

    }
}
