package com.sanskritisathi.app;

public class Raja {

    private String name;
    private String dynasty;
    private String period;
    private String kingdom;
    private String capital;
    private String history;
    private String wars;
    private int imageResId;

    public Raja(
            String name,
            String dynasty,
            String period,
            String kingdom,
            String capital,
            String history,
            String wars,
            int imageResId
    ) {
        this.name = name;
        this.dynasty = dynasty;
        this.period = period;
        this.kingdom = kingdom;
        this.capital = capital;
        this.history = history;
        this.wars = wars;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getDynasty() {
        return dynasty;
    }

    public String getPeriod() {
        return period;
    }

    public String getKingdom() {
        return kingdom;
    }

    public String getCapital() {
        return capital;
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
