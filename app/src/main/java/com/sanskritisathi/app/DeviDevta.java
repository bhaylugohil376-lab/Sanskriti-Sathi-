package com.sanskritisathi.app;

public class DeviDevta {

    private String name;
    private String tradition;
    private String description;
    private String importance;
    private String temples;
    private String festivals;
    private String stories;
    private int imageResId;

    public DeviDevta(
            String name,
            String tradition,
            String description,
            String importance,
            String temples,
            String festivals,
            String stories,
            int imageResId
    ) {
        this.name = name;
        this.tradition = tradition;
        this.description = description;
        this.importance = importance;
        this.temples = temples;
        this.festivals = festivals;
        this.stories = stories;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getTradition() {
        return tradition;
    }

    public String getDescription() {
        return description;
    }

    public String getImportance() {
        return importance;
    }

    public String getTemples() {
        return temples;
    }

    public String getFestivals() {
        return festivals;
    }

    public String getStories() {
        return stories;
    }

    public int getImageResId() {
        return imageResId;
    }
}
