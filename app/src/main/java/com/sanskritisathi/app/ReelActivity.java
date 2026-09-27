package com.sanskritisathi.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReelActivity extends AppCompatActivity {

    private RecyclerView reelRecyclerView;
    private ReelAdapter reelAdapter;

    private final List<Reel> reelList = new ArrayList<>();

    private LinearLayoutManager layoutManager;
    private LinearSnapHelper snapHelper;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reel);

        bindViews();
        setupRecyclerView();
        setupScrollListener();

        loadReels();
    }

    // =========================================================
    // BIND
    // =========================================================

    private void bindViews() {

        reelRecyclerView =
                findViewById(R.id.reelRecyclerView);
    }

    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        layoutManager =
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        false
                );

        reelRecyclerView.setLayoutManager(
                layoutManager
        );

        reelRecyclerView.setHasFixedSize(false);

        reelRecyclerView.setItemViewCacheSize(2);

        reelRecyclerView.setOverScrollMode(
                View.OVER_SCROLL_NEVER
        );

        reelRecyclerView.setClipToPadding(false);

        /*
         * Instagram/TikTok style:
         * ek time par ek reel snap hogi.
         */
        snapHelper =
                new LinearSnapHelper();

        snapHelper.attachToRecyclerView(
                reelRecyclerView
        );

        reelAdapter =
                new ReelAdapter(
                        this,
                        reelList
                );

        reelRecyclerView.setAdapter(
                reelAdapter
        );
    }

    // =========================================================
    // LOAD REELS
    // =========================================================

    private void loadReels() {

        if (loading) {
            return;
        }

        loading = true;

        ReelSupabaseHelper.getActiveReels(
                this,
                new ReelSupabaseHelper.ReelsCallback() {

                    @Override
                    public void onSuccess(
                            List<Reel> reels) {

                        runOnUiThread(() -> {

                            loading = false;

                            reelList.clear();

                            if (reels != null) {
                                reelList.addAll(reels);
                            }

                            reelAdapter.notifyDataSetChanged();

                            if (reelList.isEmpty()) {

                                Toast.makeText(
                                        ReelActivity.this,
                                        "Abhi koi Reel available nahi hai.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            /*
                             * First reel ko thoda delay ke baad
                             * play karenge.
                             */
                            handler.postDelayed(
                                    () -> playCurrentReel(),
                                    250
                            );
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() -> {

                            loading = false;

                            Toast.makeText(
                                    ReelActivity.this,
                                    message == null
                                            ? "Reels load failed."
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    // =========================================================
    // SCROLL LISTENER
    // =========================================================

    private void setupScrollListener() {

        reelRecyclerView.addOnScrollListener(
                new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(
                            @NonNull RecyclerView recyclerView,
                            int newState) {

                        super.onScrollStateChanged(
                                recyclerView,
                                newState
                        );

                        if (newState ==
                                RecyclerView.SCROLL_STATE_IDLE) {

                            /*
                             * Snap complete hone ke baad
                             * current reel play hogi.
                             */
                            handler.postDelayed(
                                    () -> playCurrentReel(),
                                    100
                            );
                        }
                    }
                }
        );
    }

    // =========================================================
    // PLAY CURRENT REEL
    // =========================================================

    private void playCurrentReel() {

        if (reelAdapter == null) {
            return;
        }

        reelAdapter.pauseAllVideos();

        reelAdapter.resumeCurrentVideo();

        addViewToCurrentReel();
    }

    // =========================================================
    // ADD VIEW
    // =========================================================

    private void addViewToCurrentReel() {

        int position =
                layoutManager
                        .findFirstCompletelyVisibleItemPosition();

        if (position == RecyclerView.NO_POSITION) {

            position =
                    layoutManager
                            .findFirstVisibleItemPosition();
        }

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        if (position < 0
                || position >= reelList.size()) {
            return;
        }

        Reel reel =
                reelList.get(position);

        if (reel == null
                || reel.getId() == null) {
            return;
        }

        /*
         * Local count immediately update karte hain
         * taaki UI responsive rahe.
         */
        int currentViews =
                Math.max(
                        0,
                        reel.getViews()
                );

        reel.setViews(
                currentViews + 1
        );

        reelAdapter.notifyItemChanged(
                position,
                "views"
        );

        ReelSupabaseHelper.addReelView(
                this,
                reel.getId(),
                new ReelSupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {
                        // View successfully saved.
                    }

                    @Override
                    public void onError(
                            String message) {

                        /*
                         * Server fail hone par
                         * UI ko disturb nahi karenge.
                         */
                    }
                }
        );
    }

    // =========================================================
    // LIFECYCLE
    // =========================================================

    @Override
    protected void onPause() {

        super.onPause();

        if (reelAdapter != null) {
            reelAdapter.pauseAllVideos();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (reelAdapter != null) {

            handler.postDelayed(
                    () -> playCurrentReel(),
                    200
            );
        }
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacksAndMessages(null);

        if (reelAdapter != null) {
            reelAdapter.releaseAllVideos();
        }

        super.onDestroy();
    }

    // =========================================================
    // PUBLIC ACCESS
    // =========================================================

    public RecyclerView getReelRecyclerView() {
        return reelRecyclerView;
    }
}
