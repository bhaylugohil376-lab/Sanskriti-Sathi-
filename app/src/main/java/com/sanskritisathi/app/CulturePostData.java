package com.sanskritisathi.app;

import java.util.ArrayList;

public class CulturePostData {

    public static ArrayList<CulturePost> getAllPosts() {

        ArrayList<CulturePost> posts = new ArrayList<>();

        // 1️⃣ Bhagavad Gita
        posts.add(new CulturePost(
                "Sanskriti Sathi",
                "📖 Bhagavad Gita",
                "श्रीमद्भगवद्गीता भारतीय दर्शन की महत्वपूर्ण कृति है। "
                        + "इसमें कर्तव्य, कर्म, ज्ञान, भक्ति और जीवन के विभिन्न "
                        + "पहलुओं पर संवाद के माध्यम से विचार प्रस्तुत किए गए हैं।",
                128,
                R.drawable.icon_foreground,
                R.drawable.gita
        ));

        // 2️⃣ Somnath Temple
        posts.add(new CulturePost(
                "Sanskriti Sathi",
                "🛕 Temple • Somnath",
                "सोमनाथ मंदिर गुजरात के प्रभास पाटण में स्थित एक प्रसिद्ध "
                        + "शिव तीर्थ है। यह भारत की धार्मिक और स्थापत्य परंपरा "
                        + "में विशेष स्थान रखता है।",
                96,
                R.drawable.icon_foreground,
                R.drawable.somnath
        ));

        // 3️⃣ Lord Shiva
        posts.add(new CulturePost(
                "Sanskriti Sathi",
                "🕉️ Devi-Devta • Shiva",
                "भगवान शिव भारतीय धार्मिक परंपरा में योग, तप, वैराग्य और "
                        + "आध्यात्मिक चिंतन से जुड़े प्रमुख देवता माने जाते हैं। "
                        + "उनसे संबंधित परंपराएँ भारत के अनेक क्षेत्रों में मिलती हैं।",
                154,
                R.drawable.icon_foreground,
                R.drawable.shiva
        ));

        // 4️⃣ Indian Heritage
        posts.add(new CulturePost(
                "Bharat Heritage",
                "🇮🇳 Indian Culture",
                "भारत की संस्कृति अनेक भाषाओं, परंपराओं, कलाओं, त्योहारों "
                        + "और ऐतिहासिक विरासतों का व्यापक संगम है। "
                        + "हमारी विरासत को समझना और सुरक्षित रखना हमारी "
                        + "सांस्कृतिक जिम्मेदारी का हिस्सा है।",
                211,
                R.drawable.icon_foreground,
                R.drawable.icon_foreground
        ));

        // 5️⃣ Sanskriti News
        posts.add(new CulturePost(
                "Sanskriti News",
                "📰 Culture News",
                "भारत की सांस्कृतिक विरासत, ऐतिहासिक स्थलों, मंदिरों और "
                        + "परंपराओं से जुड़ी महत्वपूर्ण जानकारी एक ही स्थान पर।",
                87,
                R.drawable.icon_foreground,
                R.drawable.rss
        ));

        // 6️⃣ Temple Heritage
        posts.add(new CulturePost(
                "Bharat Darshan",
                "🛕 Heritage • Temples",
                "भारतीय मंदिर केवल पूजा के स्थान नहीं हैं। कई मंदिर "
                        + "स्थापत्य, मूर्तिकला, संगीत, उत्सव और स्थानीय इतिहास "
                        + "की परंपराओं से भी जुड़े हुए हैं।",
                173,
                R.drawable.icon_foreground,
                R.drawable.somnath
        ));

        // 7️⃣ Sanskriti Sathi Community
        posts.add(new CulturePost(
                "Sanskriti Sathi",
                "✨ Community",
                "Sanskriti Sathi का उद्देश्य भारतीय संस्कृति, इतिहास, "
                        + "धार्मिक परंपराओं और ज्ञान को आधुनिक डिजिटल माध्यम "
                        + "के जरिए लोगों तक पहुँचाना है।",
                245,
                R.drawable.icon_foreground,
                R.drawable.icon_foreground
        ));

        return posts;
    }
}
