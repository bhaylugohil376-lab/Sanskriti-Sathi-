package com.sanskritisathi.app;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        searchInput = findViewById(R.id.searchInput);

        if (searchInput == null) {
            return;
        }

        // Query received from ExploreActivity
        String query =
                getIntent().getStringExtra("search_query");

        if (query != null && !query.trim().isEmpty()) {
            searchInput.setText(query);
            searchInput.setSelection(
                    searchInput.getText().length()
            );
        }

        searchInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                        performSearch();

                        return true;
                    }

                    return false;
                }
        );

        searchInput.requestFocus();
    }

    private void performSearch() {

        String query =
                searchInput.getText()
                        .toString()
                        .trim();

        if (query.isEmpty()) {

            Toast.makeText(
                    this,
                    "Search something",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Next step:
         * Connect this query to Supabase and show
         * users, posts, reels and categories.
         */
        Toast.makeText(
                this,
                "Searching: " + query,
                Toast.LENGTH_SHORT
        ).show();
    }
}
