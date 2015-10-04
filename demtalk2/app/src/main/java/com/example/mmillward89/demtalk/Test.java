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
    private ListView listView1;
    private Button view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        view = (Button) findViewById(R.id.view);
        view.setOnClickListener(this);

        listView1 = (ListView) findViewById(R.id.listView1);

        String[] items = { "<a href='http://www.facebook.com'>facebook</a>",
                "<a href='http://www.google.com'>google</a>", "<a href='http://www.reddit.com'>reddit</a>",
                "<a href='http://www.twitter.com'>twitter</a>" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.simplerow, items);

        listView1.setAdapter(adapter);

    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.view:
                int i = 0;
                TextView wantedView = (TextView) listView1.getChildAt(i);
                String s = wantedView.getText().toString();
                while(!(s.equals(""))) {

                    if (wantedView != null) {
                        wantedView.setText(Html.fromHtml(s));
                        wantedView.setClickable(true);
                        wantedView.setMovementMethod(LinkMovementMethod.getInstance());
                        wantedView.setVisibility(View.VISIBLE);

                        i++;
                        wantedView = (TextView) listView1.getChildAt(i);
                        if (wantedView == null) {
                            s = "";
                        } else {
                            s = wantedView.getText().toString();
                        }

                    } else {

                    }

                }
                break;
        }

    }
}
