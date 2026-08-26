package com.sanskritisathi.app;

public class Raja {

    private String name;
    private String dynasty;
    private String period;
    private String description;

    public Raja(String name, String dynasty, String period, String description) {
        this.name = name;
        this.dynasty = dynasty;
        this.period = period;
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

    public String getDescription() {
        return description;
    }
}
