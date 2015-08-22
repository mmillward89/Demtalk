package com.example.mmillward89.demtalk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mmillward89 on 15/08/2015.
 */
public class LinkManager {

    private LinkDatabase linkDatabase;
    private SQLiteDatabase database;
    private String[] allColumns = {LinkDatabase.COLUMN_ID, LinkDatabase.COLUMN_LINK};

    public LinkManager(Context context) {
        linkDatabase = new LinkDatabase(context);
    }

    public void open() throws SQLException {
        database = linkDatabase.getWritableDatabase();
    }

    public void close() {
        linkDatabase.close();
    }

    /**
     * Creates a link object and adds it to the database
     * @param link
     * @return
     */
    public Link createLink(String link) {
        ContentValues values = new ContentValues();
        values.put(LinkDatabase.COLUMN_LINK, link);
        long insertId = database.insert(LinkDatabase.TABLE_LINKS, null, values);

        Cursor cursor = database.query(LinkDatabase.TABLE_LINKS, allColumns,
                LinkDatabase.COLUMN_ID + " = " + insertId, null, null, null, null);

        cursor.moveToFirst();
        Link link1 = cursorToLink(cursor);
        cursor.close();

        return link1;
    }

    /**
     * Removes a link from the database
     * @param link
     */
    public void deleteLink(Link link) {
        long id = link.getId();
        //prints out 'comment deleted with id: (here)'
        database.delete(LinkDatabase.TABLE_LINKS, LinkDatabase.COLUMN_ID + " = " + id, null);
    }

    /**
     * Retrieves all link information from the database and returns them
     * @return
     */
    public List<Link> getAllLinks() {
        List<Link> links = new ArrayList<Link>();
        Cursor cursor = database.query(LinkDatabase.TABLE_LINKS, allColumns,
                null, null, null, null, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Link link = cursorToLink(cursor);
            links.add(link);
            cursor.moveToNext();
        }
        cursor.close();

        return links;
    }

    /**
     * Used to take a cursor object and create a link object
     * @param cursor
     * @return
     */
    private Link cursorToLink(Cursor cursor) {

        Link link = new Link();
        link.setId(cursor.getLong(0));
        link.setLink(cursor.getString(1));

        return link;
    }

}
