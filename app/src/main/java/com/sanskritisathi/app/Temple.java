package com.sanskritisathi.app;

public class Temple {

    private String name;
    private String location;
    private String description;
    private int imageResId;

    public Temple(
            String name,
            String location,
            String description,
            int imageResId) {

        this.name = name;
        this.location = location;
        this.description = description;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }
}
