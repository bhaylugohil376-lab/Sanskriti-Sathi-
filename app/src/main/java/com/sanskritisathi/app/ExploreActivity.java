package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ExploreActivity extends AppCompatActivity {

    private EditText searchInput;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_explore);

        backButton = findViewById(R.id.backButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        searchInput = findViewById(R.id.searchInput);

        setupCategories();
        setupSearch();
    }

    private void setupCategories() {

        /*
         * Culture
         */
        setCategoryClick(
                R.id.categoryCulture,
                TempleActivity.class
        );

        /*
         * History
         */
        setCategoryClick(
                R.id.categoryHistory,
                RajaActivity.class
        );

        /*
         * Knowledge
         */
        setCategoryClick(
                R.id.categoryKnowledge,
                GitaActivity.class
        );

        /*
         * Festivals / Devi Devta
         */
        setCategoryClick(
                R.id.categoryFestivals,
                DeviDevtaActivity.class
        );
    }

    private void setCategoryClick(
            int viewId,
            Class<?> target
    ) {

        if (findViewById(viewId) != null) {

            findViewById(viewId)
                    .setOnClickListener(v -> {

                        startActivity(
                                new Intent(
                                        ExploreActivity.this,
                                        target
                                )
                        );
                    });
        }
    }

    private void setupSearch() {

        if (searchInput == null) {
            return;
        }

        searchInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    String query =
                            searchInput
                                    .getText()
                                    .toString()
                                    .trim();

                    if (query.isEmpty()) {

                        Toast.makeText(
                                this,
                                "Search something",
                                Toast.LENGTH_SHORT
                        ).show();

                        return true;
                    }

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

                    return true;
                }
        );
    }
}
