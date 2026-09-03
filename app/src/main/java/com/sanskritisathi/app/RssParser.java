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

        parser.setFeature(
                XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                false
        );

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

                    if (tagName != null &&
                            tagName.equalsIgnoreCase("item")) {

                        insideItem = true;

                        title = "";
                        description = "";
                        link = "";
                        pubDate = "";
                    }

                    else if (insideItem &&
                            tagName != null &&
                            tagName.equalsIgnoreCase("title")) {

                        title = readText(parser);
                    }

                    else if (insideItem &&
                            tagName != null &&
                            tagName.equalsIgnoreCase("description")) {

                        description = readText(parser);
                    }

                    else if (insideItem &&
                            tagName != null &&
                            tagName.equalsIgnoreCase("link")) {

                        link = readText(parser);
                    }

                    else if (insideItem &&
                            tagName != null &&
                            tagName.equalsIgnoreCase("pubDate")) {

                        pubDate = readText(parser);
                    }
                }

                else if (eventType == XmlPullParser.END_TAG) {

                    String tagName = parser.getName();

                    if (tagName != null &&
                            tagName.equalsIgnoreCase("item")) {

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
                }

                eventType = parser.next();
            }

        } finally {

            try {
                inputStream.close();
            } catch (Exception ignored) {
            }
        }

        return items;
    }

    private static String readText(
            XmlPullParser parser) throws Exception {

        String text = parser.nextText();

        if (text == null) {
            return "";
        }

        return text.trim();
    }
}
