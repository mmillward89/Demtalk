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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.security.cert.TrustAnchor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DisplayLinks extends AppCompatActivity implements View.OnClickListener {

    private Button add_link_button;
    private TextView clear_link_button, show_links_button;
    private EditText add_link_edittext, add_name_edittext;
    private LinkManager linkManager;
    private List<Link> links;
    private HashSet<String> names;
    private String storedLink, removeLink;
    private ListView listView;
    private String[] linksForListview;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> linkArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_links);

        //Initialize layout widgets
        listView = (ListView) findViewById(R.id.listView);
        add_link_edittext = (EditText)findViewById(R.id.add_link_edittext);
        add_name_edittext = (EditText)findViewById(R.id.add_name_edittext);
        add_link_button = (Button) findViewById(R.id.add_link_button);
        clear_link_button = (TextView) findViewById(R.id.clear_link_button);
        show_links_button = (TextView) findViewById(R.id.show_links_button);

        //Set event listeners
        add_link_button.setOnClickListener(this);
        clear_link_button.setOnClickListener(this);
        show_links_button.setOnClickListener(this);

        //Initialize list for storing links and link names, and SQLite database manager
        links = new ArrayList<>();
        names = new HashSet<>();
        linkManager = new LinkManager(this);

        //Open connection to database
        try {
            linkManager.open();
        } catch (Exception e) {
            showMessage(getString(R.string.couldnt_open));
        }

        //Gets all links and adds them to array, then array list
        //Array list allows for further links to be added to the adapter
        addLinksToListView();
        linkArrayList = new ArrayList<>();

        for(int i=0; i<linksForListview.length; i++) {
            linkArrayList.add(linksForListview[i]);
        }

        adapter = new ArrayAdapter<String>(this, R.layout.simplerow, linkArrayList);
        listView.setAdapter(adapter);
        }

    @Override
    protected void onResume() {
        try {
            linkManager.open();
        } catch(Exception e) {
            showMessage(getString(R.string.couldnt_open));
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        //Close the connection to the database when the user accesses another activity
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

            //Start the corresponding activity when the user selects an icon on the activity bar
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

            //Event listener, begin process for adding or removing a link
            case R.id.add_link_button:
                addLink();
                break;

            case R.id.clear_link_button:
                removeLinkDialog();
                break;

            case R.id.show_links_button:
                makeLinksVisible();
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
        //Creates an html reference to allow the layout to render a clickable link for users
        String storedlink = "<a href='" + userLink + "'>" + name + "</a>";
        return storedlink;
    }

    /**
     * Takes user input, creates an HTML link, adds it to database and passes it to display
     */
    private void addLink() {

        //Get user input and check that they have added an http reference (won't connect if not)
        String userLink = add_link_edittext.getText().toString();
        String http = userLink.substring(0, 7);

        if(http.equals("http://")) {
            //If user has provided link, get name from edittext
            String name = add_name_edittext.getText().toString();

            //If both are blank return message
            if(userLink.equals("") || name.equals("")) {
                showMessage(getString(R.string.blank_link_message));
            } else {
                //Check the user hasn't entered an already existing name
                boolean b = names.add(name);

                if(b == false) {
                    showMessage(getString(R.string.link_name_taken));
                } else {
                    //Create an html reference, add the link to the database, and call the method
                    //to display the link
                    storedLink = createStoredLink(userLink, name);
                    linkManager.createLink(storedLink, name);
                    displayNewLink(storedLink);
                }
            }


        } else {
            showMessage(getString(R.string.incorrect_link_format));
        }



    }

    /**
     * Creates textview of new link and adds it to scrollview
     */
    private void displayNewLink(String userLink) {
       //add link and notify listview adapter of new link
        linkArrayList.add(userLink);
        adapter.notifyDataSetChanged();

        //Get the most recent link and make it clickable for users
        TextView wantedView = (TextView) listView.getChildAt(listView.getCount() - 1);
        String s = wantedView.getText().toString();
        wantedView.setText(Html.fromHtml(s));
        wantedView.setClickable(true);
        wantedView.setMovementMethod(LinkMovementMethod.getInstance());
        wantedView.setVisibility(View.VISIBLE);

        //Make user input boxes blank for ease of reuse.
        add_link_edittext.setText("");
        add_name_edittext.setText("");
    }

    /**
     * Called as activity is opened, displays all links in database
     */
    private void addLinksToListView() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Retrieve all links, initialize array for storing links to match the size of links in the database
                links = linkManager.getAllLinks();
                linksForListview = new String[links.size()];

                //Add the link name to the HashSet to ensure it can't be duplicated
                //Then add the link to the array, which will be passed to the listview adapter (see onCreate)
                int i = 0;
                for (Link l : links) {
                    String name = l.getName();
                    names.add(name);
                    String link = l.getLink();
                    linksForListview[i] = link;
                    i++;
                }
            }
        });
    }

    /**
     * Loops through all links in the listview and turns them into hyperlinks
     * Need to add a button user clicks to see them
     */
    public void makeLinksVisible() {

        int i = 0;
        TextView wantedView = (TextView) listView.getChildAt(i);

        while(wantedView != null) {
            //get text, set textview to html
            String s = wantedView.getText().toString();
            wantedView.setText(Html.fromHtml(s));
            wantedView.setClickable(true);
            wantedView.setMovementMethod(LinkMovementMethod.getInstance());
            wantedView.setVisibility(View.VISIBLE);

            i++;
            wantedView = (TextView) listView.getChildAt(i);
        }


    }

    /**
     * Creates a dialog accepting user input of link to remove
     */
    private void removeLinkDialog() {

        //Builds the alert dialog, sets the message and adds an edittext for user input
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getString(R.string.clear_link_button));
        dialogBuilder.setMessage(getString(R.string.remove_link_confirmation));
        final EditText editText = new EditText(this);
        dialogBuilder.setView(editText);

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int it) {

                //Takes user input (link name) and stores it for
                //other method to find in database
                Editable e = editText.getText();
                removeLink = e.toString();

                if(removeLink.equals("")) {
                    showMessage(getString(R.string.please_enter_link));
                } else {
                    //If user input isn't blank, remove the link
                    removeLink();
                }
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int it) {
                //Dialog is removed with no change to app
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

        //Creates a link reference to store and remove the desired link, and a boolean
        //to determine the link has been found
        String storedLink = "";
        Link l = null;
        boolean foundLink = false;

        //Look through links in database, when correct one is found that matches user input,
        //store it and create a 'stored link' i.e. it what it's displayed as
        //on views
        List<Link> links = linkManager.getAllLinks();
        for (Link link : links) {
            String name = link.getName();
            if (name.equals(removeLink)) {
                storedLink = name;
                l = link;
            }
        }
        //Need to find element based on name, remove it from adapter, then delete from database
        int i = 0;
        TextView wantedView = (TextView) listView.getChildAt(i);

        while(wantedView != null) {

            String name = wantedView.getText().toString();
            if(storedLink.equals(name)) {
                linkArrayList.remove(i);
                adapter.notifyDataSetChanged();
            }

            i++;
            wantedView = (TextView) listView.getChildAt(i);
        }

        linkManager.deleteLink(l);

        //Used to confirm the link was found
        if (foundLink == false) {
            showMessage(getString(R.string.link_not_found));
        }

        //Called so the user can re-add the link without the set thinking
        //that it is still there
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
            showMessage("Link name not found");
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
