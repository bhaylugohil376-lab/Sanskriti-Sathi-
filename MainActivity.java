package com.sanskritisathi.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView infoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoText = findViewById(R.id.infoText);

        Button btnKrishna = findViewById(R.id.btnKrishna);
        Button btnShiva = findViewById(R.id.btnShiva);
        Button btnRam = findViewById(R.id.btnRam);
        Button btnTemple = findViewById(R.id.btnTemple);
        Button btnRss = findViewById(R.id.btnRss);

        btnKrishna.setOnClickListener(v -> showDeity(
                "भगवान श्री कृष्ण",
                "श्री कृष्ण भगवान विष्णु के अवतार माने जाते हैं। " +
                "भगवद्गीता में उन्होंने अर्जुन को कर्म, धर्म और जीवन के बारे में उपदेश दिया।"
        ));

        btnShiva.setOnClickListener(v -> showDeity(
                "भगवान शिव",
                "भगवान शिव को महादेव के नाम से जाना जाता है। " +
                "उनसे जुड़े अनेक तीर्थस्थल और धार्मिक परंपराएँ भारत में प्रसिद्ध हैं।"
        ));

        btnRam.setOnClickListener(v -> showDeity(
                "भगवान श्री राम",
                "श्री राम को मर्यादा पुरुषोत्तम कहा जाता है। " +
                "रामायण में उनके जीवन, आदर्श और धर्म का वर्णन मिलता है।"
        ));

        btnTemple.setOnClickListener(v -> openTempleLocation());
        btnRss.setOnClickListener(v -> showRssInfo());
    }

    private void showDeity(String title, String description) {
        infoText.setText(title + "\n\n" + description);
    }

    private void openTempleLocation() {
        String query = "Hindu temples near me";
        Uri uri = Uri.parse(
                "https://www.google.com/maps/search/?api=1&query="
                        + Uri.encode(query)
        );

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Location खोलने में समस्या हुई",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void showRssInfo() {
        infoText.setText(
                "📰 SanskritiSathi RSS\n\n" +
                "यह सेक्शन आगे मंदिर, संस्कृति और धार्मिक समाचार " +
                "के RSS feeds दिखाने के लिए इस्तेमाल किया जा सकता है।\n\n" +
                "इस basic version में paid API key की जरूरत नहीं है।"
        );
    }
}
