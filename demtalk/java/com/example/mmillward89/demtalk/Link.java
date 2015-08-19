package com.example.mmillward89.demtalk;

/**
 * Created by Mmillward89 on 15/08/2015.
 */
public class Link {

    private long id;
    private String hyperlink;
    private String description;

    public long getId() {
        return id;
    }

    public void setId (long id) {
        this.id = id;
    }

    public String getLink() {
        return  hyperlink;
    }

    public void setLink(String hyperlink) {
        this.hyperlink = hyperlink;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
