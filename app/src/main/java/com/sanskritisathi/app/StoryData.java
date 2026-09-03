package com.sanskritisathi.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StoryData {

    private static final List<Story> stories = new ArrayList<>();

    static {
        long now = System.currentTimeMillis();

        stories.add(new Story(
                "story_1",
                "Sanskriti Sathi",
                "icon_foreground",
                "somnath",
                "Somnath Jyotirlinga — Bharat ki prachin dharohar.",
                "Public",
                now - (2 * 60 * 60 * 1000L),
                128,
                false
        ));

        stories.add(new Story(
                "story_2",
                "Bharat Heritage",
                "icon_foreground",
                "gita",
                "Bhagavad Gita ke amar vichar.",
                "Public",
                now - (5 * 60 * 60 * 1000L),
                86,
                false
        ));

        stories.add(new Story(
                "story_3",
                "Sanskriti Sathi",
                "icon_foreground",
                "shiva",
                "Mahadev ki bhakti aur Bharatiya parampara.",
                "Followers",
                now - (8 * 60 * 60 * 1000L),
                54,
                true
        ));
    }

    public static List<Story> getActiveStories() {

        removeExpiredStories();

        return new ArrayList<>(stories);
    }

    public static void addStory(Story story) {
        stories.add(0, story);
    }

    public static void deleteStory(String storyId) {

        Iterator<Story> iterator = stories.iterator();

        while (iterator.hasNext()) {
            Story story = iterator.next();

            if (story.getId().equals(storyId) && story.isOwnStory()) {
                iterator.remove();
                break;
            }
        }
    }

    private static void removeExpiredStories() {

        Iterator<Story> iterator = stories.iterator();

        while (iterator.hasNext()) {

            Story story = iterator.next();

            if (story.isExpired()) {
                iterator.remove();
            }
        }
    }
}
