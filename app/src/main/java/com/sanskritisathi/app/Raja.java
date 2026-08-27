package com.sanskritisathi.app;

public class Raja {

    private String name;
    private String period;
    private String history;
    private int imageResId;

    public Raja(String name, String period, String history, int imageResId) {
        this.name = name;
        this.period = period;
        this.history = history;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getPeriod() {
        return period;
    }

    public String getHistory() {
        return history;
    }

    public int getImageResId() {
        return imageResId;
    }
}
