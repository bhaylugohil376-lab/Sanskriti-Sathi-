package com.sanskritisathi.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReelActivity extends AppCompatActivity
        implements ReelAdapter.ReelInteractionListener {

    private static final String TAG = "ReelActivity";

    private RecyclerView recyclerViewReels;
    private ProgressBar progressBar;
    private ReelAdapter reelAdapter;
        private LinearLayoutManager layoutManager;
    private int currentPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reel);

        recyclerViewReels = findViewById(R.id.recyclerViewReels);
        progressBar = findViewById(R.id.progressBar);

        setupRecyclerView();
        fetchActiveReels();
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        );

        recyclerViewReels.setLayoutManager(layoutManager);
        recyclerViewReels.setHasFixedSize(false);
        recyclerViewReels.setItemViewCacheSize(1);
        recyclerViewReels.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerViewReels.setClipToPadding(false);
        recyclerViewReels.setClipChildren(false);

        reelAdapter = new ReelAdapter(this, this);
        recyclerViewReels.setAdapter(reelAdapter);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerViewReels);

        recyclerViewReels.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(
                            @NonNull RecyclerView recyclerView,
                            int newState) {
                        super.onScrollStateChanged(recyclerView, newState);

                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            playCurrentVisibleReel();
                        }
                    }
                }
        );
    }

    private void fetchActiveReels() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ReelSupabaseHelper helper = new ReelSupabaseHelper(this);
        helper.getActiveReels(new ReelSupabaseHelper.GetReelsCallback() {
            @Override
            public void onSuccess(List<Reel> reels) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (reels == null || reels.isEmpty()) {
                    currentPosition = RecyclerView.NO_POSITION;
                    Toast.makeText(ReelActivity.this, "No reels found", Toast.LENGTH_SHORT).show();
                    return;
                }
                reelAdapter.setReels(reels);
                currentPosition = RecyclerView.NO_POSITION;
                recyclerViewReels.post(() -> {
                    recyclerViewReels.scrollToPosition(0);
                    recyclerViewReels.post(ReelActivity.this::playCurrentVisibleReel);
                });
            }

            @Override
            public void onError(Exception e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Reels load failed", e);
                Toast.makeText(ReelActivity.this,
                        e == null || e.getMessage() == null ? "Failed to load reels" : e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void playCurrentVisibleReel() {
        if (recyclerViewReels == null || layoutManager == null) {
            return;
        }

        int position = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (position == RecyclerView.NO_POSITION) {
            position = layoutManager.findFirstVisibleItemPosition();
        }

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        if (position != currentPosition) {
            pauseAllVisiblePlayers();
            currentPosition = position;
        }

        RecyclerView.ViewHolder holder =
                recyclerViewReels.findViewHolderForAdapterPosition(position);

        if (holder instanceof ReelAdapter.ReelViewHolder) {
            ((ReelAdapter.ReelViewHolder) holder).playPlayer();
        } else {
            recyclerViewReels.post(this::playCurrentVisibleReel);
        }
    }

    private void pauseAllVisiblePlayers() {
        if (recyclerViewReels == null) {
            return;
        }

        for (int i = 0; i < recyclerViewReels.getChildCount(); i++) {
            View child = recyclerViewReels.getChildAt(i);
            RecyclerView.ViewHolder holder =
                    recyclerViewReels.getChildViewHolder(child);

            if (holder instanceof ReelAdapter.ReelViewHolder) {
                ((ReelAdapter.ReelViewHolder) holder).pausePlayer();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (recyclerViewReels != null) {
            recyclerViewReels.post(this::playCurrentVisibleReel);
        }
    }

    @Override
    protected void onPause() {
        pauseAllVisiblePlayers();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (recyclerViewReels != null) {
            for (int i = 0; i < recyclerViewReels.getChildCount(); i++) {
                View child = recyclerViewReels.getChildAt(i);
                RecyclerView.ViewHolder holder =
                        recyclerViewReels.getChildViewHolder(child);

                if (holder instanceof ReelAdapter.ReelViewHolder) {
                    ((ReelAdapter.ReelViewHolder) holder).releasePlayer();
                }
            }
        }

        super.onDestroy();
    }

    @Override
    public void onLikeClicked(Reel reel, int position) {
        if (reel == null) return;

        if (!SupabaseAuthManager.isLoggedIn(this)) {
            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        ReelSupabaseHelper.toggleReelLike(
                this,
                reel.getId(),
                reel.isLiked(),
                new ReelSupabaseHelper.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        boolean liked = !reel.isLiked();
                        reel.setLiked(liked);
                        reel.setLikes(
                                liked
                                        ? reel.getLikes() + 1
                                        : Math.max(0, reel.getLikes() - 1)
                        );
                        reelAdapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(
                                ReelActivity.this,
                                message == null
                                        ? "Like update failed"
                                        : message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    @Override
    public void onCommentClicked(Reel reel, int position) {
        if (reel == null || reel.getId() == null) return;

        android.content.Intent intent = new android.content.Intent(
                this,
                ReelCommentsActivity.class
        );
        intent.putExtra("reel_id", reel.getId());
        startActivity(intent);
    }

    @Override
    public void onShareClicked(Reel reel, int position) {
        if (reel == null || reel.getVideoUrl() == null) return;

        android.content.Intent intent = new android.content.Intent(
                android.content.Intent.ACTION_SEND
        );
        intent.setType("text/plain");
        intent.putExtra(
                android.content.Intent.EXTRA_TEXT,
                (reel.getCaption() == null ? "" : reel.getCaption())
                        + "\n\n"
                        + reel.getVideoUrl()
        );

        startActivity(
                android.content.Intent.createChooser(
                        intent,
                        "Share Reel"
                )
        );
    }

    @Override
    public void onDeleteClicked(Reel reel, int position) {
        if (reel == null || reel.getId() == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Reel?")
                .setMessage("Kya aap is Reel ko delete karna chahte ho?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) ->
                        ReelSupabaseHelper.deleteReel(
                                this,
                                reel.getId(),
                                new ReelSupabaseHelper.ActionCallback() {
                                    @Override
                                    public void onSuccess() {
                                        reelAdapter.removeReel(position);
                                        currentPosition = RecyclerView.NO_POSITION;
                                        recyclerViewReels.post(
                                                ReelActivity.this::playCurrentVisibleReel
                                        );
                                    }

                                    @Override
                                    public void onError(String message) {
                                        Toast.makeText(
                                                ReelActivity.this,
                                                message == null
                                                        ? "Delete failed"
                                                        : message,
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                }
                        )
                )
                .show();
    }

    public RecyclerView getReelRecyclerView() {
        return recyclerViewReels;
    }
}
