package com.sanskritisathi.app;

import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReelActivity extends AppCompatActivity {

    private RecyclerView reelRecyclerView;
    private ReelAdapter reelAdapter;

    private final List<Reel> reelList = new ArrayList<>();

    private PagerSnapHelper snapHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(android.graphics.Color.BLACK);
        window.setNavigationBarColor(android.graphics.Color.BLACK);

        setContentView(R.layout.activity_reel);

        reelRecyclerView = findViewById(R.id.reelRecyclerView);

        setupRecyclerView();
    }

    private void setupRecyclerView() {

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        false
                );

        reelRecyclerView.setLayoutManager(layoutManager);

        reelRecyclerView.setHasFixedSize(false);
        reelRecyclerView.setItemViewCacheSize(2);
        reelRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        reelRecyclerView.setClipToPadding(false);

        reelAdapter =
                new ReelAdapter(
                        this,
                        reelList
                );

        reelRecyclerView.setAdapter(reelAdapter);

        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(
                reelRecyclerView
        );

        reelRecyclerView.addOnScrollListener(
                new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(
                            RecyclerView recyclerView,
                            int newState) {

                        super.onScrollStateChanged(
                                recyclerView,
                                newState
                        );

                        if (newState ==
                                RecyclerView.SCROLL_STATE_IDLE) {

                            playCurrentReel();
                        }
                    }
                }
        );
    }

    private void playCurrentReel() {

        if (reelAdapter != null) {
            reelAdapter.resumeCurrentVideo();
        }
    }

    public RecyclerView getReelRecyclerView() {
        return reelRecyclerView;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (reelAdapter != null) {
            reelAdapter.resumeCurrentVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (reelAdapter != null) {
            reelAdapter.pauseAllVideos();
        }
    }

    @Override
    protected void onDestroy() {

        if (reelAdapter != null) {
            reelAdapter.releaseAllVideos();
        }

        super.onDestroy();
    }
}
