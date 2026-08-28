package com.sanskritisathi.app;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;

public class RssParser {

    public static ArrayList<RssItem> parse(InputStream inputStream)
            throws Exception {

        ArrayList<RssItem> items = new ArrayList<>();

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, null);

        String title = "";
        String description = "";
        String link = "";

        boolean insideItem = false;

        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {

            if (eventType == XmlPullParser.START_TAG) {

                String tagName = parser.getName();

                if (tagName.equalsIgnoreCase("item")) {
                    insideItem = true;
                    title = "";
                    description = "";
                    link = "";
                }

                if (insideItem &&
                        tagName.equalsIgnoreCase("title")) {

                    title = parser.nextText();
                }

                if (insideItem &&
                        tagName.equalsIgnoreCase("description")) {

                    description = parser.nextText();
                }

                if (insideItem &&
                        tagName.equalsIgnoreCase("link")) {

                    link = parser.nextText();
                }
            }

            if (eventType == XmlPullParser.END_TAG &&
                    parser.getName().equalsIgnoreCase("item")) {

                if (!title.isEmpty()) {
                    items.add(new RssItem(
                            title.trim(),
                            description.trim(),
                            link.trim()
                    ));
                }

                insideItem = false;
            }

            eventType = parser.next();
        }

        inputStream.close();

        return items;
    }
}
