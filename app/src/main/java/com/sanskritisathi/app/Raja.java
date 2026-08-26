package com.sanskritisathi.app;

public class Raja {

    private String name;
    private String period;
    private String dynasty;
    private String description;

    public Raja(String name, String period, String dynasty, String description) {
        this.name = name;
        this.period = period;
        this.dynasty = dynasty;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getPeriod() {
        return period;
    }

    public String getDynasty() {
        return dynasty;
    }

    public String getDescription() {
        return description;
    }
}
