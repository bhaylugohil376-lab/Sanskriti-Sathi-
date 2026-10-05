package com.sanskritisathi.app;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private TextView searchResultText;
    private TextView searchEmptyText;
    private RecyclerView searchRecyclerView;

    private SearchUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        initializeViews();
        setupRecyclerView();
        setupBackButton();
        setupSearch();

        String query =
                getIntent().getStringExtra("search_query");

        if (query != null && !query.trim().isEmpty()) {

            searchInput.setText(query);
            searchInput.setSelection(
                    searchInput.length()
            );

            performSearch();

        } else {

            searchInput.requestFocus();
        }
    }

    private void initializeViews() {

        searchInput =
                findViewById(R.id.searchInput);

        searchResultText =
                findViewById(R.id.searchResultText);

        searchEmptyText =
                findViewById(R.id.searchEmptyText);

        searchRecyclerView =
                findViewById(R.id.searchRecyclerView);
    }

    private void setupRecyclerView() {

        if (searchRecyclerView == null) {
            return;
        }

        adapter =
                new SearchUserAdapter(this);

        searchRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        searchRecyclerView.setHasFixedSize(false);

        searchRecyclerView.setAdapter(adapter);
    }

    private void setupBackButton() {

        View button =
                findViewById(R.id.searchBackButton);

        if (button != null) {
            button.setOnClickListener(v -> finish());
        }
    }

    private void setupSearch() {

        if (searchInput == null) {
            return;
        }

        searchInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    boolean searchAction =
                            actionId ==
                                    EditorInfo.IME_ACTION_SEARCH;

                    boolean enterAction =
                            event != null
                                    && event.getKeyCode()
                                    == KeyEvent.KEYCODE_ENTER
                                    && event.getAction()
                                    == KeyEvent.ACTION_DOWN;

                    if (searchAction || enterAction) {

                        performSearch();

                        return true;
                    }

                    return false;
                }
        );
    }

    private void performSearch() {

        String query =
                searchInput
                        .getText()
                        .toString()
                        .trim();

        hideKeyboard();

        if (query.isEmpty()) {

            clearResults();

            searchResultText.setText(
                    "Search users"
            );

            searchEmptyText.setText(
                    "Name ya username search karein."
            );

            searchEmptyText.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        searchResultText.setText(
                "Search results for \"" + query + "\""
        );

        /*
         * Temporary local filtering.
         *
         * Supabase users ko yahan connect kiya jayega.
         */
        List<SearchUser> results =
                searchLocalUsers(query);

        adapter.setUsers(results);

        if (results.isEmpty()) {

            searchEmptyText.setText(
                    "Is naam ya username ke liye koi result nahi mila."
            );

            searchEmptyText.setVisibility(
                    View.VISIBLE
            );

        } else {

            searchEmptyText.setVisibility(
                    View.GONE
            );
        }
    }

    private List<SearchUser> searchLocalUsers(
            String query
    ) {

        List<SearchUser> allUsers =
                new ArrayList<>();

        /*
         * Demo data.
         *
         * Is list ko next step mein
         * Supabase profile table se replace karenge.
         */

        allUsers.add(
                new SearchUser(
                        "demo_1",
                        "sanskriti_sathi",
                        "Sanskriti Sathi",
                        ""
                )
        );

        allUsers.add(
                new SearchUser(
                        "demo_2",
                        "bharat_culture",
                        "Bharat Culture",
                        ""
                )
        );

        allUsers.add(
                new SearchUser(
                        "demo_3",
                        "indian_history",
                        "Indian History",
                        ""
                )
        );

        String lowerQuery =
                query.toLowerCase();

        List<SearchUser> filtered =
                new ArrayList<>();

        for (SearchUser user : allUsers) {

            String username =
                    user.getUsername() == null
                            ? ""
                            : user.getUsername()
                            .toLowerCase();

            String name =
                    user.getDisplayName() == null
                            ? ""
                            : user.getDisplayName()
                            .toLowerCase();

            if (username.contains(lowerQuery)
                    || name.contains(lowerQuery)) {

                filtered.add(user);
            }
        }

        return filtered;
    }

    private void clearResults() {

        if (adapter != null) {
            adapter.setUsers(
                    new ArrayList<>()
            );
        }
    }

    private void hideKeyboard() {

        if (searchInput == null) {
            return;
        }

        InputMethodManager manager =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (manager != null) {

            manager.hideSoftInputFromWindow(
                    searchInput.getWindowToken(),
                    0
            );
        }

        searchInput.clearFocus();
    }

    @Override
    protected void onPause() {

        hideKeyboard();

        super.onPause();
    }
}
