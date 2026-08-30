package com.sanskritisathi.app;

public class Temple {

    private String name;
    private String location;
    private String description;
    private String history;
    private String religiousImportance;
    private String mainDeity;
    private String festivals;
    private String traditions;
    private String architecture;
    private String stories;
    private int imageResId;

    public Temple(
            String name,
            String location,
            String description,
            String history,
            String religiousImportance,
            String mainDeity,
            String festivals,
            String traditions,
            String architecture,
            String stories,
            int imageResId) {

        this.name = name;
        this.location = location;
        this.description = description;
        this.history = history;
        this.religiousImportance = religiousImportance;
        this.mainDeity = mainDeity;
        this.festivals = festivals;
        this.traditions = traditions;
        this.architecture = architecture;
        this.stories = stories;
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

    public String getHistory() {
        return history;
    }

    public String getReligiousImportance() {
        return religiousImportance;
    }

    public String getMainDeity() {
        return mainDeity;
    }

    public String getFestivals() {
        return festivals;
    }

    public String getTraditions() {
        return traditions;
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getStories() {
        return stories;
    }

    public int getImageResId() {
        return imageResId;
    }
}
