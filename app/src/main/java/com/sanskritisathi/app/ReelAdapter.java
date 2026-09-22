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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

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
        this.reels = reels == null
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

    public List<Reel> getReels() {
        return reels;
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
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

        holder.bind(reels.get(position));
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

        private final PlayerView playerView;

        private final TextView usernameText;
        private final TextView captionText;
        private final TextView likesText;
        private final TextView commentsText;
        private final TextView viewsText;

        private final ImageButton likeButton;
        private final ImageButton commentButton;
        private final ImageButton shareButton;
        private final ImageButton deleteButton;

        private ExoPlayer player;
        private Reel currentReel;

        ReelViewHolder(
                @NonNull View itemView) {

            super(itemView);

            playerView = itemView.findViewById(
                    R.id.reelPlayerView
            );

            /*
             * IMPORTANT:
             * Ye IDs actual item_reel.xml ke hain.
             */
            usernameText = itemView.findViewById(
                    R.id.reelUsernameText
            );

            captionText = itemView.findViewById(
                    R.id.reelCaptionText
            );

            likesText = itemView.findViewById(
                    R.id.reelLikesText
            );

            commentsText = itemView.findViewById(
                    R.id.reelCommentsText
            );

            viewsText = itemView.findViewById(
                    R.id.reelViewsText
            );

            likeButton = itemView.findViewById(
                    R.id.reelLikeButton
            );

            commentButton = itemView.findViewById(
                    R.id.reelCommentButton
            );

            shareButton = itemView.findViewById(
                    R.id.reelShareButton
            );

            deleteButton = itemView.findViewById(
                    R.id.reelDeleteButton
            );
        }

        // ========================================================
        // BIND
        // ========================================================

        void bind(Reel reel) {

            currentReel = reel;

            bindText(reel);
            bindDeleteButton(reel);
            setupPlayer(reel);

            /*
             * Existing liked state pehle show karo.
             */
            updateLikeUI(reel.isLiked());

            /*
             * Server se actual like status check.
             */
            checkLikeStatus(reel);

            /*
             * Click listeners.
             */
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

            /*
             * View count.
             */
            addView(reel);
        }

        // ========================================================
        // TEXT
        // ========================================================

        private void bindText(Reel reel) {

            String username = reel.getUsername();

            if (TextUtils.isEmpty(username)) {
                username = "Sanskriti User";
            }

            usernameText.setText(
                    "@" + username
            );

            String caption = reel.getCaption();

            if (TextUtils.isEmpty(caption)) {

                captionText.setVisibility(
                        View.GONE
                );

            } else {

                captionText.setVisibility(
                        View.VISIBLE
                );

                captionText.setText(caption);
            }

            likesText.setText(
                    formatCount(reel.getLikes())
            );

            commentsText.setText(
                    formatCount(reel.getComments())
            );

            viewsText.setText(
                    formatCount(reel.getViews())
            );
        }

        private String formatCount(int count) {

            if (count < 0) {
                count = 0;
            }

            if (count >= 1_000_000) {

                return String.format(
                        java.util.Locale.US,
                        "%.1fM",
                        count / 1_000_000f
                );
            }

            if (count >= 1_000) {

                return String.format(
                        java.util.Locale.US,
                        "%.1fK",
                        count / 1_000f
                );
            }

            return String.valueOf(count);
        }

        // ========================================================
        // DELETE VISIBILITY
        // ========================================================

        private void bindDeleteButton(Reel reel) {

            if (reel.isOwnReel()) {

                deleteButton.setVisibility(
                        View.VISIBLE
                );

            } else {

                deleteButton.setVisibility(
                        View.GONE
                );
            }
        }

        // ========================================================
        // MEDIA3 PLAYER
        // ========================================================

        private void setupPlayer(Reel reel) {

            releasePlayer();

            String videoUrl = reel.getVideoUrl();

            if (TextUtils.isEmpty(videoUrl)) {

                return;
            }

            try {

                player = new ExoPlayer.Builder(context)
                        .build();

                playerView.setPlayer(player);

                /*
                 * Instagram-style:
                 * video repeat hota rahe.
                 */
                player.setRepeatMode(
                        Player.REPEAT_MODE_ONE
                );

                /*
                 * Video ready hote hi play.
                 */
                player.setPlayWhenReady(true);

                MediaItem mediaItem =
                        MediaItem.fromUri(
                                Uri.parse(videoUrl)
                        );

                player.setMediaItem(mediaItem);

                player.prepare();

            } catch (Exception e) {

                Toast.makeText(
                        context,
                        "Video load nahi ho paaya.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        void pausePlayer() {

            if (player != null) {
                player.pause();
            }
        }

        void resumePlayer() {

            if (player != null) {
                player.play();
            }
        }

        void releasePlayer() {

            if (player != null) {

                player.stop();
                player.release();
                player = null;
            }

            if (playerView != null) {
                playerView.setPlayer(null);
            }
        }

        // ========================================================
        // VIEW COUNT
        // ========================================================

        private void addView(Reel reel) {

            if (reel == null
                    || TextUtils.isEmpty(reel.getId())) {
                return;
            }

            ReelSupabaseHelper.addReelView(
                    context,
                    reel.getId(),
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            /*
                             * Activity destroy/recycle hone par
                             * stale holder update avoid karo.
                             */
                            if (currentReel != reel) {
                                return;
                            }

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
                            // View count failure silently ignore.
                        }
                    }
            );
        }

        // ========================================================
        // LIKE STATUS
        // ========================================================

        private void checkLikeStatus(Reel reel) {

            if (reel == null
                    || TextUtils.isEmpty(reel.getId())) {
                return;
            }

            ReelSupabaseHelper.checkReelLike(
                    context,
                    reel.getId(),
                    new ReelSupabaseHelper.LikeCheckCallback() {

                        @Override
                        public void onResult(
                                boolean liked) {

                            if (currentReel != reel) {
                                return;
                            }

                            reel.setLiked(liked);

                            updateLikeUI(liked);
                        }

                        @Override
                        public void onError(
                                String message) {
                            // Existing local state keep karo.
                        }
                    }
            );
        }

        // ========================================================
        // LIKE
        // ========================================================

        private void toggleLike(Reel reel) {

            if (reel == null
                    || TextUtils.isEmpty(reel.getId())) {
                return;
            }

            boolean oldLiked =
                    reel.isLiked();

            int oldLikes =
                    reel.getLikes();

            boolean newLiked =
                    !oldLiked;

            /*
             * Optimistic UI.
             */
            reel.setLiked(newLiked);

            reel.setLikes(
                    newLiked
                            ? oldLikes + 1
                            : Math.max(0, oldLikes - 1)
            );

            updateLikeUI(newLiked);

            likesText.setText(
                    formatCount(reel.getLikes())
            );

            likeButton.setEnabled(false);

            ReelSupabaseHelper.toggleReelLike(
                    context,
                    reel.getId(),
                    oldLiked,
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            if (currentReel == reel) {
                                likeButton.setEnabled(true);
                            }
                        }

                        @Override
                        public void onError(
                                String message) {

                            /*
                             * Rollback.
                             */
                            reel.setLiked(oldLiked);
                            reel.setLikes(oldLikes);

                            updateLikeUI(oldLiked);

                            likesText.setText(
                                    formatCount(oldLikes)
                            );

                            likeButton.setEnabled(true);

                            Toast.makeText(
                                    context,
                                    message,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        }

        private void updateLikeUI(boolean liked) {

            /*
             * Existing drawable resources use kar rahe hain
             * taaki extra icon dependency na aaye.
             */
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

        private void openComments(Reel reel) {

            if (reel == null
                    || TextUtils.isEmpty(reel.getId())) {
                return;
            }

            Intent intent = new Intent(
                    context,
                    ReelCommentsActivity.class
            );

            intent.putExtra(
                    "reel_id",
                    reel.getId()
            );

            context.startActivity(intent);
        }

        // ========================================================
        // SHARE
        // ========================================================

        private void shareReel(Reel reel) {

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

            StringBuilder text =
                    new StringBuilder();

            if (!TextUtils.isEmpty(caption)) {

                text.append(caption);
                text.append("\n\n");
            }

            text.append(videoUrl);

            Intent shareIntent =
                    new Intent(Intent.ACTION_SEND);

            shareIntent.setType("text/plain");

            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    text.toString()
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

        private void confirmDelete(Reel reel) {

            if (reel == null) {
                return;
            }

            new AlertDialog.Builder(context)
                    .setTitle("Delete Reel?")
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

        private void deleteReel(Reel reel) {

            deleteButton.setEnabled(false);

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

                                releasePlayer();

                                reels.remove(position);

                                notifyItemRemoved(position);
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

                            deleteButton.setEnabled(true);

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
    // ACTIVITY / FEED PLAYER CONTROL
    // ============================================================

    public void pauseAllVideos() {

        for (int i = 0; i < getItemCount(); i++) {

            RecyclerView recyclerView =
                    findRecyclerView();

            if (recyclerView == null) {
                return;
            }

            RecyclerView.ViewHolder holder =
                    recyclerView.findViewHolderForAdapterPosition(i);

            if (holder instanceof ReelViewHolder) {

                ((ReelViewHolder) holder).pausePlayer();
            }
        }
    }

    public void releaseAllVideos() {

        RecyclerView recyclerView =
                findRecyclerView();

        if (recyclerView == null) {
            return;
        }

        for (int i = 0; i < getItemCount(); i++) {

            RecyclerView.ViewHolder holder =
                    recyclerView.findViewHolderForAdapterPosition(i);

            if (holder instanceof ReelViewHolder) {

                ((ReelViewHolder) holder).releasePlayer();
            }
        }
    }

    private RecyclerView findRecyclerView() {

        if (context instanceof ReelActivity) {

            return ((ReelActivity) context)
                    .getReelRecyclerView();
        }

        return null;
    }
}
