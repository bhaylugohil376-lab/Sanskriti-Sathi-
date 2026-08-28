package com.sanskritisathi.app;

public class RssItem {

    private final String title;
    private final String description;
    private final String link;

    public RssItem(String title, String description, String link) {
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
