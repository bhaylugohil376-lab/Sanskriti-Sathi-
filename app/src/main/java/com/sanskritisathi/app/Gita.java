package com.sanskritisathi.app;

public class Gita {

    private String chapter;
    private String title;
    private String description;

    public Gita(String chapter, String title, String description) {
        this.chapter = chapter;
        this.title = title;
        this.description = description;
    }

    public String getChapter() {
        return chapter;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
