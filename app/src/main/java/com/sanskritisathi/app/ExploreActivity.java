package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ExploreActivity extends AppCompatActivity {

    private EditText exploreSearchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_explore);

        initializeViews();
        setupBackButton();
        setupSearch();
        setupCategories();
    }

    private void initializeViews() {

        exploreSearchInput =
                findViewById(R.id.exploreSearchInput);
    }

    // ---------------------------------------------------------
    // BACK
    // ---------------------------------------------------------

    private void setupBackButton() {

        View backButton =
                findViewById(R.id.backButton);

        if (backButton != null) {

            backButton.setOnClickListener(v -> finish());
        }
    }

    // ---------------------------------------------------------
    // SEARCH
    // ---------------------------------------------------------

    private void setupSearch() {

        if (exploreSearchInput == null) {
            return;
        }

        exploreSearchInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    String query =
                            exploreSearchInput
                                    .getText()
                                    .toString()
                                    .trim();

                    if (query.isEmpty()) {

                        Toast.makeText(
                                this,
                                "Search something",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        openSearch(query);
                    }

                    return true;
                }
        );
    }

    private void openSearch(String query) {

        Intent intent =
                new Intent(
                        this,
                        SearchActivity.class
                );

        intent.putExtra(
                "search_query",
                query
        );

        startActivity(intent);
    }

    // ---------------------------------------------------------
    // CATEGORIES
    // ---------------------------------------------------------

    private void setupCategories() {

        View culture =
                findViewById(R.id.categoryCulture);

        if (culture != null) {

            culture.setOnClickListener(v ->
                    openActivity(
                            TempleActivity.class
                    )
            );
        }


        View history =
                findViewById(R.id.categoryHistory);

        if (history != null) {

            history.setOnClickListener(v ->
                    openActivity(
                            RajaActivity.class
                    )
            );
        }


        View knowledge =
                findViewById(R.id.categoryKnowledge);

        if (knowledge != null) {

            knowledge.setOnClickListener(v ->
                    openActivity(
                            GitaActivity.class
                    )
            );
        }


        View festivals =
                findViewById(R.id.categoryFestivals);

        if (festivals != null) {

            festivals.setOnClickListener(v ->
                    openActivity(
                            DeviDevtaActivity.class
                    )
            );
        }
    }

    // ---------------------------------------------------------
    // OPEN ACTIVITY
    // ---------------------------------------------------------

    private void openActivity(
            Class<?> activityClass
    ) {

        try {

            Intent intent =
                    new Intent(
                            this,
                            activityClass
                    );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Section open nahi ho saka",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ---------------------------------------------------------
    // KEYBOARD
    // ---------------------------------------------------------

    private void hideKeyboard() {

        if (exploreSearchInput == null) {
            return;
        }

        InputMethodManager imm =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (imm != null) {

            imm.hideSoftInputFromWindow(
                    exploreSearchInput.getWindowToken(),
                    0
            );
        }
    }

    // ---------------------------------------------------------
    // LIFECYCLE
    // ---------------------------------------------------------

    @Override
    protected void onPause() {

        hideKeyboard();

        super.onPause();
    }
}
