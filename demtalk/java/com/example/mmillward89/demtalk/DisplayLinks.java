package com.example.mmillward89.demtalk;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DisplayLinks extends AppCompatActivity implements View.OnClickListener {

    private Button add_link_button;
    private EditText add_link_edittext, add_name_edittext;
    private LinkManager linkManager;
    private LinearLayout scroll_layout_links;
    private List<Link> links;
    private String storedLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_links);

        scroll_layout_links = (LinearLayout) findViewById(R.id.scroll_layout_links);
        add_link_edittext = (EditText)findViewById(R.id.add_link_edittext);
        add_name_edittext = (EditText)findViewById(R.id.add_name_edittext);
        add_link_button = (Button) findViewById(R.id.add_link_button);
        add_link_button.setOnClickListener(this);

        links = new ArrayList<Link>();
        linkManager = new LinkManager(this);

        try {
            linkManager.open();
        } catch (Exception e) {
            showMessage(getString(R.string.couldnt_open));
        }

        displayLinks();
    }

    @Override
    protected void onResume() {
        try {
            linkManager.open();
        } catch (Exception e) {
            showMessage(getString(R.string.couldnt_open));
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        linkManager.close();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_links, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.main_menu_icon:
                startActivity(new Intent(this, MainActivity.class));
                return true;

            case R.id.create_chat_icon:
                startActivity(new Intent(this, CreateChatRoom.class));
                return true;

            case R.id.log_out_icon:
                startActivity(new Intent(this, Login.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.add_link_button:
                addLink();
                break;

        }
    }

    private void addLink() {

        String userLink = add_link_edittext.getText().toString();
        String name = add_name_edittext.getText().toString();

        if(userLink.equals("") || name.equals("")) {
            showMessage("Please add a valid address and name");
        } else{
            storedLink = "<a href='" + userLink + "'> " + name + " </a>";
            linkManager.createLink(storedLink);
            displayNewLink();
        }
    }

    private void displayNewLink() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView t = new TextView(DisplayLinks.this);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(15, 15, 15, 15);
                t.setLayoutParams(params);
                t.setText(Html.fromHtml(storedLink));

                t.setTextAppearance(DisplayLinks.this, R.style.MessageFont);
                t.setTextColor(Color.parseColor("#FFFFFF"));
                t.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                t.setClickable(true);
                t.setMovementMethod(LinkMovementMethod.getInstance());

                try {
                    scroll_layout_links.addView(t);
                } catch (Exception e) {
                    showMessage(getString(R.string.couldnt_display));
                }
            }
        });
        ;
    }

    private void displayLinks() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                links = linkManager.getAllLinks();

                for(Link l : links) {
                    linkManager.deleteLink(l);
                }

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(15, 15, 15, 15);

                for (Link l : links) {

                    String link = l.getLink();
                    TextView t = new TextView(DisplayLinks.this);
                    t.setLayoutParams(params);
                    t.setText(Html.fromHtml(link));

                    t.setTextAppearance(DisplayLinks.this, R.style.MessageFont);
                    t.setTextColor(Color.parseColor("#FFFFFF"));
                    t.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    t.setClickable(true);
                    t.setMovementMethod(LinkMovementMethod.getInstance());

                    try {
                        scroll_layout_links.addView(t);
                    } catch (Exception e) {
                        showMessage(getString(R.string.couldnt_display));
                    }

                }
            }
        });
    }

    private void showMessage(String s) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(s);
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.show();
    }

}
