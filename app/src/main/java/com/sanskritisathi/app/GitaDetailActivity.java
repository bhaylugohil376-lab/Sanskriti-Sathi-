package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GitaDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CHAPTER = "chapter";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DESCRIPTION = "description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gita_detail);

        TextView chapter = findViewById(R.id.detailChapter);
        TextView title = findViewById(R.id.detailTitle);
        TextView description = findViewById(R.id.detailDescription);

        String chapterText = getIntent().getStringExtra(EXTRA_CHAPTER);
        String titleText = getIntent().getStringExtra(EXTRA_TITLE);
        String descriptionText =
                getIntent().getStringExtra(EXTRA_DESCRIPTION);

        chapter.setText(chapterText);
        title.setText(titleText);
        description.setText(descriptionText);
    }
}
