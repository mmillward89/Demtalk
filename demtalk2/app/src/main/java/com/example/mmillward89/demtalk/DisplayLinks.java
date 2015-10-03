package com.example.mmillward89.demtalk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DisplayLinks extends AppCompatActivity implements View.OnClickListener {

    private Button add_link_button;
    private TextView clear_link_button;
    private EditText add_link_edittext, add_name_edittext;
    private LinkManager linkManager;
    private LinearLayout scroll_layout_links;
    private List<Link> links;
    private HashSet<String> names;
    private String storedLink, removeLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_links);

        scroll_layout_links = (LinearLayout) findViewById(R.id.scroll_layout_links);
        add_link_edittext = (EditText)findViewById(R.id.add_link_edittext);
        add_name_edittext = (EditText)findViewById(R.id.add_name_edittext);
        add_link_button = (Button) findViewById(R.id.add_link_button);
        clear_link_button = (TextView) findViewById(R.id.clear_link_button);

        add_link_button.setOnClickListener(this);
        clear_link_button.setOnClickListener(this);

        links = new ArrayList<>();
        names = new HashSet<>();
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

            case R.id.clear_link_button:
                removeLinkDialog();
                break;
        }
    }

    /**
     * Creates an HTML link reference to add to the scroll view
     * @param userLink - hyperlink
     * @param name - name for link
     * @return - HTML link format
     */
    private String createStoredLink(String userLink, String name) {
        String storedlink = "<a href='" + userLink + "'>" + name + "</a>";
        return storedlink;
    }

    /**
     * Takes user input, creates an HTML link, adds it to database and passes it to display
     */
    private void addLink() {

        String userLink = add_link_edittext.getText().toString();

        String http = userLink.substring(0, 7);
        if(http.equals("http://")) {
            String name = add_name_edittext.getText().toString();

            if(userLink.equals("") || name.equals("")) {
                showMessage(getString(R.string.blank_link_message));
            }

            //Check for already entered name

                boolean b = names.add(name);

                if(b == false) {
                    showMessage(getString(R.string.link_name_taken));
                } else {
                    storedLink = createStoredLink(userLink, name);
                    linkManager.createLink(storedLink, name);
                    displayNewLink();
                }
        } else {
            showMessage(getString(R.string.incorrect_link_format));
        }



    }

    /**
     * Creates textview of new link and adds it to scrollview
     */
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
    }

    /**
     * Called as activity is opened, displays all links in database
     */
    private void displayLinks() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                links = linkManager.getAllLinks();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(15, 15, 15, 15);

                 for (Link l : links) {
                     //Ensures duplicates can't be added
                     String name = l.getName();
                     names.add(name);

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

    /**
     * Creates a dialog accepting user input of link to remove
     */
    private void removeLinkDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getString(R.string.clear_link_button));
        dialogBuilder.setMessage(getString(R.string.remove_link_confirmation));
        final EditText editText = new EditText(this);
        dialogBuilder.setView(editText);

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int it) {
                Editable e = editText.getText();
                removeLink = e.toString();

                if(removeLink.equals("")) {
                    showMessage(getString(R.string.please_enter_link));
                } else {
                    removeLink();
                }
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int it) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    /**
     * Finds link that matches user input, matches it to appropriate textview, and removes
     * it from scrollview
     */
    private void removeLink() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String storedLink = "";
                Link l = null;
                boolean foundLink = false;

                //Look through links, when correct one is found that matches user input,
                //delete it and create a 'stored link' i.e. it what it's displayed as
                //on views
                List<Link> links = linkManager.getAllLinks();
                for (Link link : links) {
                    String name = link.getName();
                    if (name.equals(removeLink)) {
                        storedLink = name;
                        l = link;
                    }
                }

                //Find the view with the matching link and remove it
                //(might not work due to fromHTML method)
                int childCount = scroll_layout_links.getChildCount();

                if (childCount == 0) {
                    showMessage(getString(R.string.link_not_found));
                }

                for (int i = 0; i < childCount; i++) {
                    View view = scroll_layout_links.getChildAt(i);
                    TextView textView = (TextView) view;
                    String viewString = textView.getText().toString().trim();

                    if (viewString.equals(storedLink)) {
                        foundLink = true;
                        view.setVisibility(View.GONE);
                        scroll_layout_links.removeView(view);
                        linkManager.deleteLink(l);
                    }
                    if (foundLink == false) {
                        showMessage(getString(R.string.link_not_found));
                    }
                }
            }
        });

        //Called so the user can re-add the link without the set thinking
        //they already have it
        removeLinkFromSet();
    }


    /**
     * Removes name from set so that user can re-add if they want
     */
    private void removeLinkFromSet() {
        try {
            for (String name : names) {
                if (name.equals(removeLink)) {
                    names.remove(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

}
