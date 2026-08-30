package com.sanskritisathi.app;

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

    private static final String RSS_FEED_URL =
            "https://www.pib.gov.in/RssMain.aspx?ModId=6&Lang=1&Regid=1";

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
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                connection.setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0"
                );

                connection.connect();

                int responseCode =
                        connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    try (InputStream inputStream =
                                 connection.getInputStream()) {

                        items = RssParser.parse(inputStream);
                    }

                } else {

                    throw new Exception(
                            "HTTP Error: " + responseCode
                    );
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

                    rssList.add(
                            new Rss(
                                    item.getTitle(),
                                    item.getDescription(),
                                    item.getLink(),
                                    item.getPubDate(),
                                    "Press Information Bureau (PIB)"
                            )
                    );
                }

                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        executor.shutdownNow();
    }
}
