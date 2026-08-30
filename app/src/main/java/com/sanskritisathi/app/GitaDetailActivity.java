package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GitaDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CHAPTER = "chapter";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_INTRODUCTION = "introduction";
    public static final String EXTRA_TEACHINGS = "teachings";
    public static final String EXTRA_EXPLANATION = "explanation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gita_detail);

        TextView chapter = findViewById(R.id.detailChapter);
        TextView title = findViewById(R.id.detailTitle);
        TextView introduction = findViewById(R.id.detailIntroduction);
        TextView teachings = findViewById(R.id.detailTeachings);
        TextView explanation = findViewById(R.id.detailExplanation);

        String chapterText =
                getIntent().getStringExtra(EXTRA_CHAPTER);

        String titleText =
                getIntent().getStringExtra(EXTRA_TITLE);

        String introductionText =
                getIntent().getStringExtra(EXTRA_INTRODUCTION);

        String teachingsText =
                getIntent().getStringExtra(EXTRA_TEACHINGS);

        String explanationText =
                getIntent().getStringExtra(EXTRA_EXPLANATION);

        chapter.setText(chapterText);
        title.setText(titleText);
        introduction.setText(
                "परिचय\n\n" + introductionText
        );

        teachings.setText(
                "मुख्य शिक्षाएँ\n\n" + teachingsText
        );

        explanation.setText(
                "सरल व्याख्या\n\n" + explanationText
        );
    }
}
