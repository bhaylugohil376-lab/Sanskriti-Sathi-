package com.sanskritisathi.app;

import java.util.ArrayList;

public class CulturePostData {

    public static ArrayList<CulturePost> getAllPosts() {

        ArrayList<CulturePost> list = new ArrayList<>();

        // Raja
        list.add(new CulturePost(
                "Sanskriti Sathi",
                "👑 भारतीय राजा",
                "भारत के महान राजाओं और उनके इतिहास को जानिए।",
                "0",
                R.drawable.ic_launcher_foreground,
                R.drawable.chandragupta
        ));

        // Temple
        list.add(new CulturePost(
                "Sanskriti Sathi",
                "🛕 भारतीय मंदिर",
                "भारत के प्राचीन मंदिरों की इतिहास, परंपरा और वास्तुकला को जानिए।",
                "0",
                R.drawable.ic_launcher_foreground,
                R.drawable.temple
        ));

        // Devi Devta
        list.add(new CulturePost(
                "Sanskriti Sathi",
                "🕉️ देवी और देवता",
                "भारतीय धार्मिक परंपराओं में देवी-देवताओं के स्वरूप और सांस्कृतिक महत्व को समझिए।",
                "0",
                R.drawable.ic_launcher_foreground,
                R.drawable.shiva
        ));

        // Bhagavad Gita
        list.add(new CulturePost(
                "Sanskriti Sathi",
                "📖 भगवद्गीता",
                "भगवद्गीता के 18 अध्यायों के ज्ञान और प्रमुख शिक्षाओं का अध्ययन करें।",
                "0",
                R.drawable.ic_launcher_foreground,
                R.drawable.gita
        ));

        return list;
    }
}
