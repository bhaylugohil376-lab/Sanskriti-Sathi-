package com.sanskritisathi.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RssActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RssAdapter adapter;
    private ArrayList<Rss> rssList;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    /*
     * Yahan authorized/public RSS feed URL rakhenge.
     * Final app mein sirf aisa feed use karna hai
     * jiske terms commercial app use ko allow karte hon.
     */
    private static final String RSS_FEED_URL =
            "PASTE_AUTHORIZED_RSS_FEED_URL_HERE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss);

        recyclerView = findViewById(R.id.rssRecyclerView);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rssList = new ArrayList<>();

        adapter = new RssAdapter(this, rssList);
        recyclerView.setAdapter(adapter);

        loadRssFeed();
    }

    private void loadRssFeed() {

        executor.execute(() -> {

            ArrayList<RssItem> items = new ArrayList<>();

            try {

                URL url = new URL(RSS_FEED_URL);

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty(
                        "User-Agent",
                        "SanskritiSathi/1.0"
                );

                connection.connect();

                if (connection.getResponseCode() == 200) {

                    InputStream inputStream =
                            connection.getInputStream();

                    items = RssParser.parse(inputStream);
                }

                connection.disconnect();

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                RssActivity.this,
                                "RSS news load nahi ho saki.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }

            ArrayList<RssItem> finalItems = items;

            runOnUiThread(() -> {

                rssList.clear();

                for (RssItem item : finalItems) {

                    rssList.add(new Rss(
                            item.getTitle(),
                            item.getDescription(),
                            item.getLink()
                    ));
                }

                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
