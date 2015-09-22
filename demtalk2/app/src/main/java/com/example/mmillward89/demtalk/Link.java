package com.example.mmillward89.demtalk;

/**
 * Created by Mmillward89 on 15/08/2015.
 */
public class Link {

    private long id;
    private String hyperlink;
    private String name;

    public long getId() {
        return id;
    }

    public void setId (long id) {
        this.id = id;
    }

    public String getLink() {
        return hyperlink;
    }

    public void setLink(String hyperlink) {
        this.hyperlink = hyperlink;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
