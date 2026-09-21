package com.sanskritisathi.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.List;

public class ReelAdapter
        extends RecyclerView.Adapter<ReelAdapter.ReelViewHolder> {

    private final Context context;
    private final List<Reel> reels;

    public ReelAdapter(
            Context context,
            List<Reel> reels) {

        this.context = context;
        this.reels =
                reels == null
                        ? new ArrayList<>()
                        : reels;
    }

    public void setReels(List<Reel> newReels) {

        reels.clear();

        if (newReels != null) {
            reels.addAll(newReels);
        }

        notifyDataSetChanged();
    }

    public void addReels(List<Reel> newReels) {

        if (newReels == null
                || newReels.isEmpty()) {
            return;
        }

        int start =
                reels.size();

        reels.addAll(newReels);

        notifyItemRangeInserted(
                start,
                newReels.size()
        );
    }

    public List<Reel> getReels() {
        return reels;
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(context)
                        .inflate(
                                R.layout.item_reel,
                                parent,
                                false
                        );

        return new ReelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ReelViewHolder holder,
            int position) {

        Reel reel =
                reels.get(position);

        holder.bind(reel);
    }

    @Override
    public int getItemCount() {
        return reels.size();
    }

    @Override
    public void onViewRecycled(
            @NonNull ReelViewHolder holder) {

        holder.releasePlayer();

        super.onViewRecycled(holder);
    }

    // ============================================================
    // VIEW HOLDER
    // ============================================================

    class ReelViewHolder
            extends RecyclerView.ViewHolder {

        PlayerView playerView;

        TextView usernameText;
        TextView captionText;
        TextView likesText;
        TextView commentsText;
        TextView viewsText;

        ImageButton likeButton;
        ImageButton commentButton;
        ImageButton shareButton;
        ImageButton deleteButton;

        ExoPlayer player;

        Reel currentReel;

        ReelViewHolder(
                @NonNull View itemView) {

            super(itemView);

            playerView =
                    itemView.findViewById(
                            R.id.reelPlayerView
                    );

            usernameText =
                    itemView.findViewById(
                            R.id.reelUsername
                    );

            captionText =
                    itemView.findViewById(
                            R.id.reelCaption
                    );

            likesText =
                    itemView.findViewById(
                            R.id.reelLikes
                    );

            commentsText =
                    itemView.findViewById(
                            R.id.reelComments
                    );

            viewsText =
                    itemView.findViewById(
                            R.id.reelViews
                    );

            likeButton =
                    itemView.findViewById(
                            R.id.reelLikeButton
                    );

            commentButton =
                    itemView.findViewById(
                            R.id.reelCommentButton
                    );

            shareButton =
                    itemView.findViewById(
                            R.id.reelShareButton
                    );

            deleteButton =
                    itemView.findViewById(
                            R.id.reelDeleteButton
                    );
        }

        void bind(Reel reel) {

            currentReel = reel;

            // ----------------------------------------------------
            // TEXT
            // ----------------------------------------------------

            String username =
                    reel.getUsername();

            if (TextUtils.isEmpty(username)) {
                username = "Sanskriti User";
            }

            usernameText.setText(
                    "@" + username
            );

            String caption =
                    reel.getCaption();

            if (TextUtils.isEmpty(caption)) {

                captionText.setVisibility(
                        View.GONE
                );

            } else {

                captionText.setVisibility(
                        View.VISIBLE
                );

                captionText.setText(
                        caption
                );
            }

            likesText.setText(
                    formatCount(
                            reel.getLikes()
                    )
            );

            commentsText.setText(
                    formatCount(
                            reel.getComments()
                    )
            );

            viewsText.setText(
                    formatCount(
                            reel.getViews()
                    )
            );

            // ----------------------------------------------------
            // DELETE BUTTON
            // ----------------------------------------------------

            if (reel.isOwnReel()) {

                deleteButton.setVisibility(
                        View.VISIBLE
                );

            } else {

                deleteButton.setVisibility(
                        View.GONE
                );
            }

            // ----------------------------------------------------
            // PLAYER
            // ----------------------------------------------------

            setupPlayer(reel);

            // ----------------------------------------------------
            // LIKE STATUS
            // ----------------------------------------------------

            updateLikeUI(
                    reel.isLiked()
            );

            checkLikeStatus(reel);

            // ----------------------------------------------------
            // LISTENERS
            // ----------------------------------------------------

            likeButton.setOnClickListener(
                    v -> toggleLike(reel)
            );

            commentButton.setOnClickListener(
                    v -> openComments(reel)
            );

            shareButton.setOnClickListener(
                    v -> shareReel(reel)
            );

            deleteButton.setOnClickListener(
                    v -> confirmDelete(reel)
            );

            // ----------------------------------------------------
            // VIEW COUNT
            // ----------------------------------------------------

            addView(reel);
        }

        // ========================================================
        // PLAYER
        // ========================================================

        private void setupPlayer(
                Reel reel) {

            releasePlayer();

            String videoUrl =
                    reel.getVideoUrl();

            if (TextUtils.isEmpty(videoUrl)) {
                return;
            }

            try {

                player =
                        new ExoPlayer.Builder(context)
                                .build();

                playerView.setPlayer(
                        player
                );

                MediaItem mediaItem =
                        MediaItem.fromUri(
                                Uri.parse(videoUrl)
                        );

                player.setMediaItem(
                        mediaItem
                );

                player.setRepeatMode(
                        ExoPlayer.REPEAT_MODE_ONE
                );

                player.prepare();

                player.setPlayWhenReady(
                        true
                );

            } catch (Exception e) {

                Toast.makeText(
                        context,
                        "Video load nahi ho paaya.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        void releasePlayer() {

            if (player != null) {

                player.release();

                player = null;
            }

            if (playerView != null) {
                playerView.setPlayer(null);
            }
        }

        // ========================================================
        // VIEW
        // ========================================================

        private void addView(
                Reel reel) {

            if (reel == null
                    || TextUtils.isEmpty(
                    reel.getId())) {
                return;
            }

            ReelSupabaseHelper.addReelView(
                    context,
                    reel.getId(),
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            /*
                             * Local count bhi update kar do,
                             * taaki UI immediately refresh ho.
                             */
                            int oldViews =
                                    reel.getViews();

                            reel.setViews(
                                    oldViews + 1
                            );

                            viewsText.setText(
                                    formatCount(
                                            reel.getViews()
                                    )
                            );
                        }

                        @Override
                        public void onError(
                                String message) {

                            // View count failure ko
                            // user ko disturb nahi karna.
                        }
                    }
            );
        }

        // ========================================================
        // LIKE CHECK
        // ========================================================

        private void checkLikeStatus(
                Reel reel) {

            if (reel == null
                    || TextUtils.isEmpty(
                    reel.getId())) {
                return;
            }

            ReelSupabaseHelper.checkReelLike(
                    context,
                    reel.getId(),
                    new ReelSupabaseHelper.LikeCheckCallback() {

                        @Override
                        public void onResult(
                                boolean liked) {

                            reel.setLiked(
                                    liked
                            );

                            updateLikeUI(
                                    liked
                            );
                        }

                        @Override
                        public void onError(
                                String message) {

                            /*
                             * Login/session issue par
                             * existing state hi rakho.
                             */
                        }
                    }
            );
        }

        // ========================================================
        // LIKE TOGGLE
        // ========================================================

        private void toggleLike(
                Reel reel) {

            if (reel == null) {
                return;
            }

            boolean oldLiked =
                    reel.isLiked();

            /*
             * Optimistic UI
             */
            boolean newLiked =
                    !oldLiked;

            reel.setLiked(
                    newLiked
            );

            int oldLikes =
                    reel.getLikes();

            if (newLiked) {

                reel.setLikes(
                        oldLikes + 1
                );

            } else {

                reel.setLikes(
                        Math.max(
                                0,
                                oldLikes - 1
                        )
                );
            }

            updateLikeUI(
                    newLiked
            );

            likesText.setText(
                    formatCount(
                            reel.getLikes()
                    )
            );

            likeButton.setEnabled(
                    false
            );

            ReelSupabaseHelper.toggleReelLike(
                    context,
                    reel.getId(),
                    oldLiked,
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            likeButton.setEnabled(
                                    true
                            );
                        }

                        @Override
                        public void onError(
                                String message) {

                            /*
                             * Server failed:
                             * rollback local UI.
                             */
                            reel.setLiked(
                                    oldLiked
                            );

                            reel.setLikes(
                                    oldLikes
                            );

                            updateLikeUI(
                                    oldLiked
                            );

                            likesText.setText(
                                    formatCount(
                                            oldLikes
                                    )
                            );

                            likeButton.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    context,
                                    message,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        }

        private void updateLikeUI(
                boolean liked) {

            if (liked) {

                likeButton.setImageResource(
                        android.R.drawable.btn_star_big_on
                );

            } else {

                likeButton.setImageResource(
                        android.R.drawable.btn_star_big_off
                );
            }
        }

        // ========================================================
        // COMMENTS
        // ========================================================

        private void openComments(
                Reel reel) {

            if (reel == null
                    || TextUtils.isEmpty(
                    reel.getId())) {
                return;
            }

            Intent intent =
                    new Intent(
                            context,
                            ReelCommentsActivity.class
                    );

            intent.putExtra(
                    "reel_id",
                    reel.getId()
            );

            context.startActivity(
                    intent
            );
        }

        // ========================================================
        // SHARE
        // ========================================================

        private void shareReel(
                Reel reel) {

            if (reel == null) {
                return;
            }

            String videoUrl =
                    reel.getVideoUrl();

            if (TextUtils.isEmpty(videoUrl)) {
                Toast.makeText(
                        context,
                        "Video link available nahi hai.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String caption =
                    reel.getCaption();

            StringBuilder shareText =
                    new StringBuilder();

            if (!TextUtils.isEmpty(caption)) {

                shareText.append(
                        caption
                );

                shareText.append(
                        "\n\n"
                );
            }

            shareText.append(
                    videoUrl
            );

            Intent shareIntent =
                    new Intent(
                            Intent.ACTION_SEND
                    );

            shareIntent.setType(
                    "text/plain"
            );

            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    shareText.toString()
            );

            context.startActivity(
                    Intent.createChooser(
                            shareIntent,
                            "Share Reel"
                    )
            );
        }

        // ========================================================
        // DELETE
        // ========================================================

        private void confirmDelete(
                Reel reel) {

            if (reel == null) {
                return;
            }

            new AlertDialog.Builder(context)
                    .setTitle(
                            "Delete Reel?"
                    )
                    .setMessage(
                            "Kya aap is Reel ko delete karna chahte ho?"
                    )
                    .setNegativeButton(
                            "Cancel",
                            null
                    )
                    .setPositiveButton(
                            "Delete",
                            (dialog, which) ->
                                    deleteReel(reel)
                    )
                    .show();
        }

        private void deleteReel(
                Reel reel) {

            deleteButton.setEnabled(
                    false
            );

            ReelSupabaseHelper.deleteReel(
                    context,
                    reel.getId(),
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            int position =
                                    getBindingAdapterPosition();

                            if (position !=
                                    RecyclerView.NO_POSITION) {

                                reels.remove(
                                        position
                                );

                                notifyItemRemoved(
                                        position
                                );
                            }

                            Toast.makeText(
                                    context,
                                    "Reel delete ho gayi.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onError(
                                String message) {

                            deleteButton.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    context,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        }
    }

    // ============================================================
    // COUNT FORMAT
    // ============================================================

    private String formatCount(
            int count) {

        if (count < 1000) {
            return String.valueOf(count);
        }

        if (count < 1000000) {

            return String.format(
                    java.util.Locale.US,
                    "%.1fK",
                    count / 1000.0
            );
        }

        return String.format(
                java.util.Locale.US,
                "%.1fM",
                count / 1000000.0
        );
    }
}
