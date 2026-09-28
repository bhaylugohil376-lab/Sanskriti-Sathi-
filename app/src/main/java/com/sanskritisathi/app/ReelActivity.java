package com.sanskritisathi.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

        setContentView(R.layout.activity_reel);

        reelRecyclerView = findViewById(
                R.id.reelRecyclerView
        );

        setupRecyclerView();

        loadReels();
    }

    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        LinearLayoutManager layoutManager =
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

        reelRecyclerView.setVerticalScrollBarEnabled(
                false
        );

        // One complete Reel per swipe
        snapHelper = new PagerSnapHelper();

        snapHelper.attachToRecyclerView(
                reelRecyclerView
        );

        reelAdapter = new ReelAdapter(
                this,
                reelList
        );

        reelRecyclerView.setAdapter(
                reelAdapter
        );

        // -----------------------------------------------------
        // Scroll listener
        // -----------------------------------------------------

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

                        if (newState
                                == RecyclerView.SCROLL_STATE_IDLE) {

                            playCurrentReel();
                        }
                    }
                }
        );
    }

    // =========================================================
    // LOAD REELS
    // =========================================================

    private void loadReels() {

        /*
         * IMPORTANT:
         *
         * Yahan tumhare existing Supabase helper
         * ka reel loading method use karo.
         *
         * Agar tumhare ReelSupabaseHelper me method
         * ka naam different hai, wahi existing method
         * use karna hai.
         */

        ReelSupabaseHelper.loadReels(
                this,
                new ReelSupabaseHelper.ReelsCallback() {

                    @Override
                    public void onSuccess(
                            List<Reel> reels) {

                        runOnUiThread(() -> {

                            reelList.clear();

                            if (reels != null) {

                                reelList.addAll(
                                        reels
                                );
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
                             * First Reel ko automatically
                             * play karo.
                             */
                            reelRecyclerView.post(
                                    () -> playCurrentReel()
                            );
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() -> {

                            Toast.makeText(
                                    ReelActivity.this,
                                    message == null
                                            ? "Reels load nahi hui."
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    // =========================================================
    // CURRENT REEL PLAY
    // =========================================================

    private void playCurrentReel() {

        if (reelRecyclerView == null
                || reelAdapter == null) {
            return;
        }

        /*
         * Pehle sab videos pause.
         */
        reelAdapter.pauseAllVideos();

        View snapView =
                snapHelper.findSnapView(
                        reelRecyclerView.getLayoutManager()
                );

        if (snapView == null) {
            return;
        }

        int position =
                reelRecyclerView
                        .getLayoutManager()
                        .getPosition(
                                snapView
                        );

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        /*
         * Adapter ke current player ko
         * resume karne do.
         */
        reelAdapter.resumeCurrentVideo();
    }

    // =========================================================
    // PAUSE WHEN ACTIVITY GOES BACKGROUND
    // =========================================================

    @Override
    protected void onPause() {

        if (reelAdapter != null) {

            reelAdapter.pauseAllVideos();
        }

        super.onPause();
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (reelRecyclerView != null
                && reelAdapter != null) {

            reelRecyclerView.post(
                    () -> playCurrentReel()
            );
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        if (reelAdapter != null) {

            reelAdapter.releaseAllVideos();
        }

        super.onDestroy();
    }

    // =========================================================
    // GET RECYCLER VIEW
    // ReelAdapter is using this method
    // =========================================================

    public RecyclerView getReelRecyclerView() {

        return reelRecyclerView;
    }
}
