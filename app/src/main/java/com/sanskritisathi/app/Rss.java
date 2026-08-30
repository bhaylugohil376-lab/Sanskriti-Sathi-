package com.sanskritisathi.app;

public class Rss {

    private final String title;
    private final String description;
    private final String link;
    private final String pubDate;
    private final String source;

    public Rss(
            String title,
            String description,
            String link,
            String pubDate,
            String source) {

        this.title = title;
        this.description = description;
        this.link = link;
        this.pubDate = pubDate;
        this.source = source;
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

    public String getPubDate() {
        return pubDate;
    }

    public String getSource() {
        return source;
    }
}
