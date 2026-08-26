package com.sanskritisathi.app;

public class Raja {

    private String name;
    private String dynasty;
    private String period;
    private String description;
    private String kingdom;
    private String capital;

    public Raja(
            String name,
            String dynasty,
            String period,
            String description,
            String kingdom,
            String capital) {

        this.name = name;
        this.dynasty = dynasty;
        this.period = period;
        this.description = description;
        this.kingdom = kingdom;
        this.capital = capital;
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

    public String getDescription() {
        return description;
    }

    public String getKingdom() {
        return kingdom;
    }

    public String getCapital() {
        return capital;
    }
}
