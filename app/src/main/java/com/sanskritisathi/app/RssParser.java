package com.sanskritisathi.app;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;

public class RssParser {

    public static ArrayList<RssItem> parse(
            InputStream inputStream) throws Exception {

        ArrayList<RssItem> items = new ArrayList<>();

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, null);

        String title = "";
        String description = "";
        String link = "";
        String pubDate = "";

        boolean insideItem = false;

        try {

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {

                    String tagName = parser.getName();

                    if (tagName.equalsIgnoreCase("item")) {

                        insideItem = true;

                        title = "";
                        description = "";
                        link = "";
                        pubDate = "";
                    }

                    else if (insideItem &&
                            tagName.equalsIgnoreCase("title")) {

                        title = parser.nextText();
                    }

                    else if (insideItem &&
                            tagName.equalsIgnoreCase("description")) {

                        description = parser.nextText();
                    }

                    else if (insideItem &&
                            tagName.equalsIgnoreCase("link")) {

                        link = parser.nextText();
                    }

                    else if (insideItem &&
                            tagName.equalsIgnoreCase("pubDate")) {

                        pubDate = parser.nextText();
                    }
                }

                else if (eventType == XmlPullParser.END_TAG &&
                        parser.getName().equalsIgnoreCase("item")) {

                    if (!title.trim().isEmpty()) {

                        items.add(
                                new RssItem(
                                        title.trim(),
                                        description.trim(),
                                        link.trim(),
                                        pubDate.trim()
                                )
                        );
                    }

                    insideItem = false;
                }

                eventType = parser.next();
            }

        } finally {
            inputStream.close();
        }

        return items;
    }
}
