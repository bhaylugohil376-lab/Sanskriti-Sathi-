package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class RajaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView title = new TextView(this);
        title.setText("👑 भारत के राजा और योद्धा");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 24);

        layout.addView(title);

        List<Raja> rajaList = RajaData.getAllRajas();

        for (Raja raja : rajaList) {

            TextView item = new TextView(this);

            item.setText(
                    "👑 " + raja.getName()
                    + "\n🏰 राज्य: " + raja.getKingdom()
                    + "\n📍 राजधानी: " + raja.getCapital()
                    + "\n\n" + raja.getDescription()
            );

            item.setTextSize(17);
            item.setPadding(20, 20, 20, 20);

            layout.addView(item);
        }

        setContentView(layout);
    }
}
