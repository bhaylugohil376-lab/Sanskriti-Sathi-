package com.sanskritisathi.app;

public class Raja {

    private String name;
    private String kingdom;
    private String capital;
    private String description;

    public Raja(String name, String kingdom, String capital, String description) {
        this.name = name;
        this.kingdom = kingdom;
        this.capital = capital;
        this.description = description;
    }

    public String getName() {
        return name;
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
