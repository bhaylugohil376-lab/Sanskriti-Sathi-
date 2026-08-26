package com.sanskritisathi.app;

public class Raja {

    private String name;
    private String dynasty;
    private String period;
    private String kingdom;
    private String capital;
    private String description;

    public Raja(
            String name,
            String dynasty,
            String period,
            String kingdom,
            String capital,
            String description) {

        this.name = name;
        this.dynasty = dynasty;
        this.period = period;
        this.kingdom = kingdom;
        this.capital = capital;
        this.description = description;
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

    public String getDescription() {
        return description;
    }
}
