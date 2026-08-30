package com.sanskritisathi.app;

public class Gita {

    private String chapter;
    private String title;
    private String introduction;
    private String teachings;
    private String explanation;

    public Gita(
            String chapter,
            String title,
            String introduction,
            String teachings,
            String explanation) {

        this.chapter = chapter;
        this.title = title;
        this.introduction = introduction;
        this.teachings = teachings;
        this.explanation = explanation;
    }

    public String getChapter() {
        return chapter;
    }

    public String getTitle() {
        return title;
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getTeachings() {
        return teachings;
    }

    public String getExplanation() {
        return explanation;
    }
}
