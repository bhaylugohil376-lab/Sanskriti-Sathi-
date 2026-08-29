package com.sanskritisathi.app;

public class Raja {

    private String name;
    private String period;
    private String history;
    private String wars;
    private int imageResId;

    public Raja(
            String name,
            String period,
            String history,
            String wars,
            int imageResId
    ) {
        this.name = name;
        this.period = period;
        this.history = history;
        this.wars = wars;
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

    public String getWars() {
        return wars;
    }

    public int getImageResId() {
        return imageResId;
    }
}
