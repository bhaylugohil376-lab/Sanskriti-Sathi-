package com.sanskritisathi.app;

import android.content.Intent;
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

    private PagerSnapHelper snapHelper;

    private final List<Reel> reelList =
            new ArrayList<>();

    private boolean loadingReels = false;

    private String lastViewedReelId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_reel
        );

        bindViews();
        setupRecyclerView();
        setupSnapHelper();
        loadReels();
    }

    private void bindViews() {

        reelRecyclerView =
                findViewById(
                        R.id.reelRecyclerView
                );
    }

    private void setupRecyclerView() {

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        layoutManager.setOrientation(
                RecyclerView.VERTICAL
        );

        reelRecyclerView.setLayoutManager(
                layoutManager
        );

        reelRecyclerView.setHasFixedSize(
                true
        );

        reelRecyclerView.setItemViewCacheSize(
                2
        );

        reelRecyclerView.setOverScrollMode(
                View.OVER_SCROLL_NEVER
        );

        reelAdapter =
                new ReelAdapter(
                        this,
                        reelList
                );

        reelRecyclerView.setAdapter(
                reelAdapter
        );

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

                            playCurrentReel();
                        }
                    }
                }
        );
    }

    private void setupSnapHelper() {

        snapHelper =
                new PagerSnapHelper();

        snapHelper.attachToRecyclerView(
                reelRecyclerView
        );
    }

    private void loadReels() {

        if (loadingReels) {
            return;
        }

        if (!SupabaseAuthManager.isLoggedIn(this)) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        loadingReels = true;

        ReelSupabaseHelper.getActiveReels(
                this,
                new ReelSupabaseHelper.ReelsCallback() {

                    @Override
                    public void onSuccess(
                            List<Reel> reels) {

                        runOnUiThread(() -> {

                            loadingReels = false;

                            reelList.clear();

                            if (reels != null) {
                                reelList.addAll(
                                        reels
                                );
                            }

                            reelAdapter.notifyDataSetChanged();

                            lastViewedReelId = "";

                            if (reelList.isEmpty()) {

                                Toast.makeText(
                                        ReelActivity.this,
                                        "Abhi koi Reel available nahi hai",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            reelRecyclerView.post(
                                    () -> playCurrentReel()
                            );
                        });
                    }

                    @Override
                    public void onError(
                            String error) {

                        runOnUiThread(() -> {

                            loadingReels = false;

                            Toast.makeText(
                                    ReelActivity.this,
                                    error == null
                                            ? "Reels load nahi hui"
                                            : error,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    private void playCurrentReel() {

        if (reelRecyclerView == null ||
                reelAdapter == null ||
                snapHelper == null) {
            return;
        }

        View snapView =
                snapHelper.findSnapView(
                        reelRecyclerView.getLayoutManager()
                );

        if (snapView == null) {
            return;
        }

        RecyclerView.ViewHolder holder =
                reelRecyclerView.getChildViewHolder(
                        snapView
                );

        if (!(holder instanceof ReelAdapter.ReelViewHolder)) {
            return;
        }

        ReelAdapter.ReelViewHolder reelHolder =
                (ReelAdapter.ReelViewHolder) holder;

        // Pehle attached videos pause.
        reelAdapter.pauseAllVideos();

        // Sirf snapped Reel play.
        reelHolder.resumePlayer();

        int position =
                holder.getBindingAdapterPosition();

        if (position == RecyclerView.NO_POSITION ||
                position >= reelList.size()) {
            return;
        }

        Reel reel =
                reelList.get(position);

        String reelId =
                reel.getId();

        if (reelId == null ||
                reelId.trim().isEmpty()) {
            return;
        }

        /*
         * Same Reel ko repeatedly count nahi karna
         * jab tak user doosri Reel par nahi jaata.
         */
        if (reelId.equals(lastViewedReelId)) {
            return;
        }

        lastViewedReelId =
                reelId;

        ReelSupabaseHelper.addReelView(
                this,
                reelId,
                new ReelSupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        runOnUiThread(() -> {

                            int currentPosition =
                                    findReelPosition(
                                            reelId
                                    );

                            if (currentPosition ==
                                    RecyclerView.NO_POSITION) {
                                return;
                            }

                            Reel currentReel =
                                    reelList.get(
                                            currentPosition
                                    );

                            currentReel.setViews(
                                    Math.max(
                                            0,
                                            currentReel.getViews()
                                                    + 1
                                    )
                            );

                            RecyclerView.ViewHolder currentHolder =
                                    reelRecyclerView
                                            .findViewHolderForAdapterPosition(
                                                    currentPosition
                                            );

                            if (currentHolder instanceof ReelAdapter.ReelViewHolder) {

                                /*
                                 * notifyItemChanged se player
                                 * unnecessarily recreate na ho,
                                 * isliye adapter ko full rebind
                                 * nahi kar rahe.
                                 *
                                 * View count database me update ho
                                 * chuka hai.
                                 */
                            }
                        });
                    }

                    @Override
                    public void onError(
                            String error) {

                        // View count failure se playback
                        // interrupt nahi karna.
                    }
                }
        );
    }

    private int findReelPosition(
            String reelId) {

        if (reelId == null) {
            return RecyclerView.NO_POSITION;
        }

        for (int i = 0;
             i < reelList.size();
             i++) {

            Reel reel =
                    reelList.get(i);

            if (reel != null &&
                    reelId.equals(
                            reel.getId()
                    )) {

                return i;
            }
        }

        return RecyclerView.NO_POSITION;
    }

    public RecyclerView getReelRecyclerView() {
        return reelRecyclerView;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (reelAdapter != null &&
                reelRecyclerView != null) {

            reelRecyclerView.post(
                    () -> {

                        reelAdapter.pauseAllVideos();

                        playCurrentReel();
                    }
            );
        }
    }

    @Override
    protected void onPause() {

        if (reelAdapter != null) {
            reelAdapter.pauseAllVideos();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (reelAdapter != null) {
            reelAdapter.releaseAllVideos();
        }

        super.onDestroy();
    }
}

