package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private TextView searchResultText;
    private TextView searchEmptyText;
    private RecyclerView searchRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        initializeViews();
        setupBackButton();
        setupSearch();

        String query =
                getIntent().getStringExtra("search_query");

        if (query != null && !query.trim().isEmpty()) {

            searchInput.setText(query);
            searchInput.setSelection(
                    searchInput.getText().length()
            );

            performSearch();
        } else {

            searchInput.requestFocus();
        }
    }

    // ---------------------------------------------------------
    // INITIALIZE
    // ---------------------------------------------------------

    private void initializeViews() {

        searchInput =
                findViewById(R.id.searchInput);

        searchResultText =
                findViewById(R.id.searchResultText);

        searchEmptyText =
                findViewById(R.id.searchEmptyText);

        searchRecyclerView =
                findViewById(R.id.searchRecyclerView);

        if (searchRecyclerView != null) {

            searchRecyclerView.setLayoutManager(
                    new LinearLayoutManager(this)
            );

            searchRecyclerView.setHasFixedSize(false);
        }
    }

    // ---------------------------------------------------------
    // BACK BUTTON
    // ---------------------------------------------------------

    private void setupBackButton() {

        View backButton =
                findViewById(R.id.searchBackButton);

        if (backButton != null) {

            backButton.setOnClickListener(v -> finish());
        }
    }

    // ---------------------------------------------------------
    // SEARCH
    // ---------------------------------------------------------

    private void setupSearch() {

        if (searchInput == null) {
            return;
        }

        searchInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    boolean searchPressed =
                            actionId == EditorInfo.IME_ACTION_SEARCH;

                    boolean enterPressed =
                            event != null
                                    && event.getKeyCode()
                                    == KeyEvent.KEYCODE_ENTER
                                    && event.getAction()
                                    == KeyEvent.ACTION_DOWN;

                    if (searchPressed || enterPressed) {

                        performSearch();

                        return true;
                    }

                    return false;
                }
        );
    }

    // ---------------------------------------------------------
    // PERFORM SEARCH
    // ---------------------------------------------------------

    private void performSearch() {

        String query =
                searchInput
                        .getText()
                        .toString()
                        .trim();

        if (query.isEmpty()) {

            if (searchResultText != null) {
                searchResultText.setText(
                        "Search users"
                );
            }

            if (searchEmptyText != null) {

                searchEmptyText.setText(
                        "Name ya username search karein."
                );

                searchEmptyText.setVisibility(
                        View.VISIBLE
                );
            }

            hideKeyboard();

            Toast.makeText(
                    this,
                    "Search something",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        hideKeyboard();

        if (searchResultText != null) {

            searchResultText.setText(
                    "Search results for \"" + query + "\""
            );
        }

        /*
         * Abhi empty state dikhegi jab tak
         * Supabase user-search query connect nahi hoti.
         *
         * Is point par future mein:
         * users
         * posts
         * reels
         * categories
         * search history
         * Supabase search
         * connect kiya ja sakta hai.
         */

        if (searchRecyclerView != null) {

            searchRecyclerView.setVisibility(
                    View.VISIBLE
            );
        }

        if (searchEmptyText != null) {

            searchEmptyText.setText(
                    "Is naam ya username ke liye abhi koi result nahi mila."
            );

            searchEmptyText.setVisibility(
                    View.VISIBLE
            );
        }
    }

    // ---------------------------------------------------------
    // KEYBOARD
    // ---------------------------------------------------------

    private void hideKeyboard() {

        if (searchInput == null) {
            return;
        }

        InputMethodManager imm =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (imm != null) {

            imm.hideSoftInputFromWindow(
                    searchInput.getWindowToken(),
                    0
            );
        }

        searchInput.clearFocus();
    }
}
