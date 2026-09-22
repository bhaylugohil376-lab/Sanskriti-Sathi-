package com.sanskritisathi.app;

import android.os.Bundle;
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

    private final List<Reel> reelList =
            new ArrayList<>();

    private PagerSnapHelper snapHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reel);

        bindViews();
        setupRecyclerView();
        loadReels();
    }

    // ============================================================
    // BIND
    // ============================================================

    private void bindViews() {

        reelRecyclerView =
                findViewById(R.id.reelRecyclerView);
    }

    // ============================================================
    // RECYCLER VIEW
    // ============================================================

    private void setupRecyclerView() {

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        layoutManager.setOrientation(
                LinearLayoutManager.VERTICAL
        );

        reelRecyclerView.setLayoutManager(
                layoutManager
        );

        reelRecyclerView.setHasFixedSize(false);

        reelAdapter =
                new ReelAdapter(
                        this,
                        reelList
                );

        reelRecyclerView.setAdapter(
                reelAdapter
        );

        /*
         * Instagram-style:
         * ek swipe = ek Reel.
         */
        snapHelper = new PagerSnapHelper();

        snapHelper.attachToRecyclerView(
                reelRecyclerView
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

    // ============================================================
    // LOAD REELS
    // ============================================================

    private void loadReels() {

        ReelSupabaseHelper.getActiveReels(
                this,
                new ReelSupabaseHelper.ReelsCallback() {

                    @Override
                    public void onSuccess(
                            List<Reel> reels) {

                        runOnUiThread(() -> {

                            reelList.clear();

                            if (reels != null) {
                                reelList.addAll(reels);
                            }

                            reelAdapter.setReels(
                                    reelList
                            );

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
                             * play karne ke liye.
                             */
                            reelRecyclerView.post(
                                    () -> playCurrentReel()
                            );
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() ->
                                Toast.makeText(
                                        ReelActivity.this,
                                        message == null
                                                ? "Reels load nahi hui."
                                                : message,
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }
                }
        );
    }

    // ============================================================
    // CURRENT REEL PLAY
    // ============================================================

    private void playCurrentReel() {

        if (reelRecyclerView == null
                || reelAdapter == null) {
            return;
        }

        if (snapHelper == null) {
            return;
        }

        ViewHolderWrapper wrapper =
                findSnappedViewHolder();

        if (wrapper != null) {
            wrapper.play();
        }
    }

    private ViewHolderWrapper findSnappedViewHolder() {

        android.view.View snapView =
                snapHelper.findSnapView(
                        reelRecyclerView.getLayoutManager()
                );

        if (snapView == null) {
            return null;
        }

        RecyclerView.ViewHolder holder =
                reelRecyclerView.getChildViewHolder(
                        snapView
                );

        if (holder instanceof ReelAdapter.ReelViewHolder) {

            return new ViewHolderWrapper(
                    (ReelAdapter.ReelViewHolder) holder
            );
        }

        return null;
    }

    // ============================================================
    // SMALL PLAYER WRAPPER
    // ============================================================

    private static class ViewHolderWrapper {

        private final ReelAdapter.ReelViewHolder holder;

        ViewHolderWrapper(
                ReelAdapter.ReelViewHolder holder) {

            this.holder = holder;
        }

        void play() {

            holder.resumePlayer();
        }
    }

    // ============================================================
    // RECYCLER VIEW ACCESS
    // ============================================================

    public RecyclerView getReelRecyclerView() {

        return reelRecyclerView;
    }

    // ============================================================
    // RESUME
    // ============================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (reelAdapter != null) {

            /*
             * Feed refresh:
             * upload/delete ke baad latest data.
             */
            loadReels();
        }
    }

    // ============================================================
    // PAUSE
    // ============================================================

    @Override
    protected void onPause() {

        if (reelAdapter != null) {

            reelAdapter.pauseAllVideos();
        }

        super.onPause();
    }

    // ============================================================
    // DESTROY
    // ============================================================

    @Override
    protected void onDestroy() {

        if (reelAdapter != null) {

            reelAdapter.releaseAllVideos();
        }

        super.onDestroy();
    }
}
