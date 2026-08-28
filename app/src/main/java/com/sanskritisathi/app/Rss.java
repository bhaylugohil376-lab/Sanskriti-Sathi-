package com.sanskritisathi.app;

public class Rss {

    private String title;
    private String description;
    private String link;

    public Rss(String title, String description, String link) {
        this.title = title;
        this.description = description;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }
}
