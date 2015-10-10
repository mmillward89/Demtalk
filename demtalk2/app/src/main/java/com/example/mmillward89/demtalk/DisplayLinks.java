package com.example.mmillward89.demtalk;

import android.app.AlertDialog;
import android.app.ListActivity;
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

public class DisplayLinks extends ListActivity implements View.OnClickListener {

    private Button add_link_button;
    private TextView clear_link_button, show_links_button;
    private EditText add_link_edittext, add_name_edittext;
    private LinkManager linkManager;
    private List<Link> links;
    private HashSet<String> names;
    private String storedLink;
    private ListView listView;
    private String[] linksForListview;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> linkArrayList;
    private boolean newlink;


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
        //And boolean for determing link to display
        links = new ArrayList<>();
        names = new HashSet<>();
        linkManager = new LinkManager(this);
        newlink = false;

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
        setListAdapter(adapter);
        makeViewsHTML();
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

        //Checks that user link submitted isn't too short to be a properly formatted link
        if(userLink.length() < 7) {
            showMessage(getString(R.string.no_link_added_message));
        } else {
            String http = userLink.substring(0, 7);

            if(!(http.equals("http://"))) {
                showMessage(getString(R.string.incorrect_link_format));
            } else {
                //If user has provided link, get name from edittext
                String name = add_name_edittext.getText().toString();

                //If both are blank return message
                if(name.equals("")) {
                    showMessage(getString(R.string.no_link_added_message));
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

                        //add link and notify listview adapter of new link
                        linkArrayList.add(storedLink);
                        adapter.notifyDataSetChanged();

                        //add displaynewlink back if necessary
                    }

                    //Make user input boxes blank for ease of reuse.
                    add_link_edittext.setText("");
                    add_name_edittext.setText("");
                }
            }
        }
    }

    /**
     * Called as activity is opened, displays all links in database
     */
    private void addLinksToListView() {

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

    /**
     * Loops through all links in the listview and turns them into hyperlinks
     * Need to add a button user clicks to see them
     */
    public void makeViewsHTML() {

        try {
            for(int i=0; i<links.size(); i++) {
                TextView wantedView = (TextView) adapter.getView(i, null, null);
                String s = wantedView.getText().toString();
                wantedView.setText(Html.fromHtml(s));
                wantedView.setClickable(true);
                wantedView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                String removeLink = e.toString();

                if(removeLink.equals("")) {
                    showMessage(getString(R.string.please_enter_link));
                } else {
                    //If user input isn't blank, remove the link
                    removeLink(removeLink);
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
    private void removeLink(String removeLink) {

        //Creates a link reference to store and remove the desired link, and a boolean
        //to determine the link has been found
        String storedLink = "";
        Link l = null;


        List<Link> links = linkManager.getAllLinks();

        //Look through links in database, when correct one is found that matches user input,
        //store the html link to compare to views displayed
        for(int i=0; i< links.size(); i++) {
            l = links.get(i);
            storedLink = l.getName();

            if (storedLink.equals(removeLink)) {
                i = links.size() - 1;
            }
        }

        //Start removal process if a link has been found
        if(!(storedLink.equals(""))) {

            int i = 0;
            TextView wantedView = (TextView) listView.getChildAt(i);

            while(wantedView != null) {
                //Get html formatted link displayed in each view
                String name = wantedView.getText().toString();

                //If it matches the user link to delete, remove it from the listview and database
                // and end the loop, else keep looking
                if(storedLink.equals(name)) {
                    linkArrayList.remove(i);
                    adapter.notifyDataSetChanged();
                    linkManager.deleteLink(l);
                    wantedView = null;

                    //Remove link from set used for avoiding duplicates
                    removeLinkFromSet(removeLink);
                } else {
                    i++;
                    wantedView = (TextView) listView.getChildAt(i);
                }
            }

        } else {
            showMessage(getString(R.string.link_not_found));
        }
    }


    /**
     * Removes name from set so that user can re-add if they want
     */
    private void removeLinkFromSet(String removeLink) {
        Iterator<String> iterator = names.iterator();
        while(iterator.hasNext()) {
            String name = iterator.next();
            if (name.equals(removeLink)) {
                iterator.remove();
            }
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
