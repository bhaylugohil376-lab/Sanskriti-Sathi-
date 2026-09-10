package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private TextView resultText;
    private TextView emptyText;
    private RecyclerView searchRecyclerView;

    private CulturePostAdapter adapter;
    private final List<CulturePost> allPosts = new ArrayList<>();
    private final List<CulturePost> filteredPosts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchInput = findViewById(R.id.searchInput);
        resultText = findViewById(R.id.searchResultText);
        emptyText = findViewById(R.id.searchEmptyText);
        searchRecyclerView = findViewById(R.id.searchRecyclerView);

        ImageButton backButton = findViewById(R.id.searchBackButton);

        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchRecyclerView.setHasFixedSize(false);

        adapter = new CulturePostAdapter(this, filteredPosts);
        searchRecyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());

        loadPosts();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPosts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadPosts() {
        resultText.setText("Loading posts...");

        CulturePostFirebaseHelper.getPublicPosts(
                posts -> {
                    allPosts.clear();

                    if (posts != null) {
                        allPosts.addAll(posts);
                    }

                    filterPosts(searchInput.getText().toString());
                },
                error -> {
                    allPosts.clear();
                    filterPosts(searchInput.getText().toString());

                    resultText.setText("Unable to load posts");
                    emptyText.setText("Please check your internet connection and try again.");
                    emptyText.setVisibility(View.VISIBLE);
                }
        );
    }

    private void filterPosts(String query) {
        filteredPosts.clear();

        String search = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        if (search.isEmpty()) {
            filteredPosts.addAll(allPosts);
        } else {
            for (CulturePost post : allPosts) {

                if (post == null) {
                    continue;
                }

                String author = safe(post.getAuthor());
                String category = safe(post.getCategory());
                String caption = safe(post.getCaption());

                if (author.contains(search)
                        || category.contains(search)
                        || caption.contains(search)) {

                    filteredPosts.add(post);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateSearchState(search);
    }

    private void updateSearchState(String query) {

        if (filteredPosts.isEmpty()) {
            searchRecyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);

            if (query.isEmpty()) {
                emptyText.setText("No posts available yet.\nCreate the first Sanskriti post!");
                resultText.setText("0 posts");
            } else {
                emptyText.setText("No results found for \"" + query + "\"");
                resultText.setText("0 results");
            }

        } else {
            searchRecyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);

            int count = filteredPosts.size();

            if (query.isEmpty()) {
                resultText.setText(count + (count == 1 ? " post" : " posts"));
            } else {
                resultText.setText(count + (count == 1 ? " result" : " results"));
            }
        }
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase(Locale.ROOT);
    }
}
