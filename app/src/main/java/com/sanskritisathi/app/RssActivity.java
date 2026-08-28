package com.sanskritisathi.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RssActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RssAdapter adapter;
    private ArrayList<Rss> rssList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss);

        recyclerView = findViewById(R.id.rssRecyclerView);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rssList = new ArrayList<>();

        // फिलहाल sample entries.
        // Baad mein real RSS feed se automatically news load karenge.

        rssList.add(new Rss(
                "Indian Culture & Heritage",
                "Indian culture, traditions and heritage se judi latest information.",
                ""
        ));

        rssList.add(new Rss(
                "Temple & Pilgrimage News",
                "Temples aur pilgrimage places se judi updates aur information.",
                ""
        ));

        rssList.add(new Rss(
                "Sanatan Sanskriti",
                "Indian traditions, festivals aur cultural activities ke baare mein information.",
                ""
        ));

        rssList.add(new Rss(
                "Heritage & History",
                "Bharat ke historical places, monuments aur heritage se judi information.",
                ""
        ));

        adapter = new RssAdapter(this, rssList);
        recyclerView.setAdapter(adapter);
    }
}
