package com.sanskritisathi.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
        setupMoreSections();
    }

    private void initializeViews() {
        exploreSearchInput = findViewById(R.id.exploreSearchInput);
    }

    // BACK
    private void setupBackButton() {
        View backButton = findViewById(R.id.backButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    // SEARCH
    private void setupSearch() {

        if (exploreSearchInput == null) return;

        exploreSearchInput.setSingleLine(true);
        exploreSearchInput.setImeOptions(
                EditorInfo.IME_ACTION_SEARCH
        );

        exploreSearchInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                        String query = exploreSearchInput
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

                            hideKeyboard();
                            openSearch(query);
                        }

                        return true;
                    }

                    return false;
                }
        );
    }

    private void openSearch(String query) {

        Intent intent = new Intent(
                this,
                SearchActivity.class
        );

        intent.putExtra(
                "search_query",
                query
        );

        startActivity(intent);
    }

    // MAIN CATEGORIES
    private void setupCategories() {

        setupCategory(
                R.id.categoryCulture,
                TempleActivity.class
        );

        setupCategory(
                R.id.categoryHistory,
                RajaActivity.class
        );

        setupCategory(
                R.id.categoryKnowledge,
                GitaActivity.class
        );

        setupCategory(
                R.id.categoryFestivals,
                DeviDevtaActivity.class
        );
    }

    private void setupCategory(
            int viewId,
            Class<?> targetActivity
    ) {

        View view = findViewById(viewId);

        if (view != null) {

            view.setClickable(true);

            view.setOnClickListener(
                    v -> openActivity(targetActivity)
            );
        }
    }

    // MORE SANSKRITI
    private void setupMoreSections() {

        setupOptionalClick(
                R.id.moreTemples,
                TempleActivity.class
        );

        setupOptionalClick(
                R.id.moreKings,
                RajaActivity.class
        );

        setupOptionalClick(
                R.id.moreGita,
                GitaActivity.class
        );
    }

    private void setupOptionalClick(
            int viewId,
            Class<?> targetActivity
    ) {

        View view = findViewById(viewId);

        if (view != null) {

            view.setClickable(true);

            view.setFocusable(true);

            view.setOnClickListener(
                    v -> openActivity(targetActivity)
            );
        }
    }

    // OPEN SECTION
    private void openActivity(
            Class<?> activityClass
    ) {

        try {

            startActivity(
                    new Intent(
                            this,
                            activityClass
                    )
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Section open nahi ho saka",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // KEYBOARD
    private void hideKeyboard() {

        if (exploreSearchInput == null) return;

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

    @Override
    protected void onPause() {
        hideKeyboard();
        super.onPause();
    }
}
