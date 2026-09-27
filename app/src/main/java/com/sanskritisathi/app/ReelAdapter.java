package com.sanskritisathi.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReelAdapter
        extends RecyclerView.Adapter<ReelAdapter.ReelViewHolder> {

    private final Context context;
    private final List<Reel> reelList;

    public ReelAdapter(
            Context context,
            List<Reel> reelList) {

        this.context = context;
        this.reelList = reelList;

        setHasStableIds(true);
    }

    // =========================================================
    // STABLE ID
    // =========================================================

    @Override
    public long getItemId(int position) {

        if (position < 0
                || position >= reelList.size()) {

            return RecyclerView.NO_ID;
        }

        Reel reel = reelList.get(position);

        if (reel == null
                || reel.getId() == null) {

            return position;
        }

        return reel.getId().hashCode();
    }

    // =========================================================
    // CREATE VIEW HOLDER
    // =========================================================

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_reel,
                                parent,
                                false
                        );

        return new ReelViewHolder(view);
    }

    // =========================================================
    // BIND
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull ReelViewHolder holder,
            int position) {

        if (position < 0
                || position >= reelList.size()) {

            return;
        }

        Reel reel = reelList.get(position);

        if (reel == null) {
            return;
        }

        holder.bind(reel);
    }

    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {
        return reelList == null
                ? 0
                : reelList.size();
    }

    // =========================================================
    // RECYCLE
    // =========================================================

    @Override
    public void onViewRecycled(
            @NonNull ReelViewHolder holder) {

        holder.releasePlayer();

        super.onViewRecycled(holder);
    }

    // =========================================================
    // PAUSE ALL
    // =========================================================

    public void pauseAllVideos() {

        if (reelList == null) {
            return;
        }

        RecyclerView recyclerView =
                findRecyclerView();

        if (recyclerView == null) {
            return;
        }

        for (int i = 0;
             i < recyclerView.getChildCount();
             i++) {

            View child =
                    recyclerView.getChildAt(i);

            RecyclerView.ViewHolder viewHolder =
                    recyclerView.getChildViewHolder(child);

            if (viewHolder instanceof ReelViewHolder) {

                ReelViewHolder holder =
                        (ReelViewHolder) viewHolder;

                holder.pauseVideo();
            }
        }
    }

    // =========================================================
    // RESUME CURRENT
    // =========================================================

    public void resumeCurrentVideo() {

        RecyclerView recyclerView =
                findRecyclerView();

        if (recyclerView == null) {
            return;
        }

        View completelyVisible =
                null;

        View firstVisible =
                null;

        for (int i = 0;
             i < recyclerView.getChildCount();
             i++) {

            View child =
                    recyclerView.getChildAt(i);

            if (firstVisible == null) {
                firstVisible = child;
            }

            int top =
                    child.getTop();

            int bottom =
                    child.getBottom();

            int recyclerTop =
                    recyclerView.getPaddingTop();

            int recyclerBottom =
                    recyclerView.getHeight()
                            - recyclerView.getPaddingBottom();

            if (top >= recyclerTop
                    && bottom <= recyclerBottom) {

                completelyVisible = child;
                break;
            }
        }

        View target =
                completelyVisible != null
                        ? completelyVisible
                        : firstVisible;

        if (target == null) {
            return;
        }

        RecyclerView.ViewHolder viewHolder =
                recyclerView.getChildViewHolder(target);

        if (viewHolder instanceof ReelViewHolder) {

            ReelViewHolder holder =
                    (ReelViewHolder) viewHolder;

            holder.playVideo();
        }
    }

    // =========================================================
    // RELEASE ALL
    // =========================================================

    public void releaseAllVideos() {

        RecyclerView recyclerView =
                findRecyclerView();

        if (recyclerView == null) {
            return;
        }

        for (int i = 0;
             i < recyclerView.getChildCount();
             i++) {

            View child =
                    recyclerView.getChildAt(i);

            RecyclerView.ViewHolder viewHolder =
                    recyclerView.getChildViewHolder(child);

            if (viewHolder instanceof ReelViewHolder) {

                ReelViewHolder holder =
                        (ReelViewHolder) viewHolder;

                holder.releasePlayer();
            }
        }
    }

    // =========================================================
    // FIND RECYCLER VIEW
    // =========================================================

    private RecyclerView findRecyclerView() {

        if (context instanceof ReelActivity) {

            return ((ReelActivity) context)
                    .getReelRecyclerView();
        }

        return null;
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public class ReelViewHolder
            extends RecyclerView.ViewHolder {

        private final PlayerView reelPlayerView;
        private final TextView reelErrorText;

        private final ImageView reelProfileImage;

        private final TextView reelUsernameText;
        private final TextView reelBottomUsername;
        private final TextView reelCaptionText;

        private final ImageButton reelLikeButton;
        private final ImageButton reelCommentButton;
        private final ImageButton reelShareButton;
        private final ImageButton reelDeleteButton;
        private final ImageButton reelEditButton;

        private final TextView reelLikesText;
        private final TextView reelCommentsText;
        private final TextView reelViewsText;

        private ExoPlayer player;

        private Reel boundReel;

        public ReelViewHolder(
                @NonNull View itemView) {

            super(itemView);

            reelPlayerView =
                    itemView.findViewById(
                            R.id.reelPlayerView
                    );

            reelErrorText =
                    itemView.findViewById(
                            R.id.reelErrorText
                    );

            reelProfileImage =
                    itemView.findViewById(
                            R.id.reelProfileImage
                    );

            reelUsernameText =
                    itemView.findViewById(
                            R.id.reelUsernameText
                    );

            reelBottomUsername =
                    itemView.findViewById(
                            R.id.reelBottomUsername
                    );

            reelCaptionText =
                    itemView.findViewById(
                            R.id.reelCaptionText
                    );

            reelLikeButton =
                    itemView.findViewById(
                            R.id.reelLikeButton
                    );

            reelCommentButton =
                    itemView.findViewById(
                            R.id.reelCommentButton
                    );

            reelShareButton =
                    itemView.findViewById(
                            R.id.reelShareButton
                    );

            reelDeleteButton =
                    itemView.findViewById(
                            R.id.reelDeleteButton
                    );

            reelEditButton =
                    itemView.findViewById(
                            R.id.reelEditButton
                    );

            reelLikesText =
                    itemView.findViewById(
                            R.id.reelLikesText
                    );

            reelCommentsText =
                    itemView.findViewById(
                            R.id.reelCommentsText
                    );

            reelViewsText =
                    itemView.findViewById(
                            R.id.reelViewsText
                    );
        }

        // =====================================================
        // BIND REEL
        // =====================================================

        public void bind(Reel reel) {

            boundReel = reel;

            releasePlayer();

            reelErrorText.setVisibility(
                    View.GONE
            );

            // -------------------------------------------------
            // USERNAME
            // -------------------------------------------------

            String username =
                    reel.getUsername();

            if (username == null
                    || username.trim().isEmpty()) {

                username = "Sanskriti User";
            }

            reelUsernameText.setText(username);

            reelBottomUsername.setText(username);

            // -------------------------------------------------
            // CAPTION
            // -------------------------------------------------

            String caption =
                    reel.getCaption();

            if (caption == null) {
                caption = "";
            }

            reelCaptionText.setText(caption);

            // -------------------------------------------------
            // COUNTS
            // -------------------------------------------------

            int likes =
                    Math.max(
                            0,
                            reel.getLikes()
                    );

            int comments =
                    Math.max(
                            0,
                            reel.getComments()
                    );

            int views =
                    Math.max(
                            0,
                            reel.getViews()
                    );

            reelLikesText.setText(
                    formatCount(likes)
            );

            reelCommentsText.setText(
                    formatCount(comments)
            );

            reelViewsText.setText(
                    formatCount(views)
            );

            // -------------------------------------------------
            // PROFILE
            // -------------------------------------------------

            reelProfileImage.setImageResource(
                    R.drawable.icon_foreground
            );

            // -------------------------------------------------
            // LIKE ICON
            // -------------------------------------------------

            updateLikeUI();

            // -------------------------------------------------
            // DELETE
            // -------------------------------------------------

            if (reel.isOwnReel()) {

                reelDeleteButton.setVisibility(
                        View.VISIBLE
                );

            } else {

                reelDeleteButton.setVisibility(
                        View.GONE
                );
            }

            // -------------------------------------------------
            // EDIT
            // -------------------------------------------------

            if (reelEditButton != null) {

                reelEditButton.setVisibility(
                        View.GONE
                );
            }

            // -------------------------------------------------
            // VIDEO
            // -------------------------------------------------

            setupPlayer(
                    reel.getVideoUrl()
            );

            // -------------------------------------------------
            // ACTIONS
            // -------------------------------------------------

            setupActions();
        }

        // =====================================================
        // PLAYER
        // =====================================================

        private void setupPlayer(
                String videoUrl) {

            if (videoUrl == null
                    || videoUrl.trim().isEmpty()) {

                reelErrorText.setText(
                        "Video unavailable"
                );

                reelErrorText.setVisibility(
                        View.VISIBLE
                );

                return;
            }

            player =
                    new ExoPlayer.Builder(context)
                            .build();

            reelPlayerView.setPlayer(
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
                    Player.REPEAT_MODE_ONE
            );

            player.setPlayWhenReady(
                    false
            );

            player.addListener(
                    new Player.Listener() {

                        @Override
                        public void onPlaybackStateChanged(
                                int playbackState) {

                            if (playbackState
                                    == Player.STATE_READY) {

                                reelErrorText
                                        .setVisibility(
                                                View.GONE
                                        );
                            }
                        }

                        @Override
                        public void onPlayerError(
                                @NonNull PlaybackException error) {

                            reelErrorText.setText(
                                    "Video unavailable"
                            );

                            reelErrorText
                                    .setVisibility(
                                            View.VISIBLE
                                    );
                        }
                    }
            );

            player.prepare();
        }

        // =====================================================
        // PLAY
        // =====================================================

        public void playVideo() {

            if (player == null) {
                return;
            }

            player.setPlayWhenReady(
                    true
            );

            player.play();
        }

        // =====================================================
        // PAUSE
        // =====================================================

        public void pauseVideo() {

            if (player == null) {
                return;
            }

            player.pause();

            player.setPlayWhenReady(
                    false
            );
        }

        // =====================================================
        // RELEASE
        // =====================================================

        public void releasePlayer() {

            if (player != null) {

                player.stop();

                player.release();

                player = null;
            }

            if (reelPlayerView != null) {

                reelPlayerView.setPlayer(
                        null
                );
            }
        }

        // =====================================================
        // ACTIONS
        // =====================================================

        private void setupActions() {

            // -------------------------------------------------
            // LIKE
            // -------------------------------------------------

            reelLikeButton.setOnClickListener(
                    v -> toggleLike()
            );

            // -------------------------------------------------
            // COMMENTS
            // -------------------------------------------------

            reelCommentButton.setOnClickListener(
                    v -> openComments()
            );

            // -------------------------------------------------
            // SHARE
            // -------------------------------------------------

            reelShareButton.setOnClickListener(
                    v -> shareReel()
            );

            // -------------------------------------------------
            // DELETE
            // -------------------------------------------------

            reelDeleteButton.setOnClickListener(
                    v -> confirmDelete()
            );
        }

        // =====================================================
        // LIKE
        // =====================================================

        private void toggleLike() {

            if (boundReel == null) {
                return;
            }

            if (!SupabaseAuthManager
                    .isLoggedIn(context)) {

                Toast.makeText(
                        context,
                        "Please login first.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            reelLikeButton.setEnabled(
                    false
            );

            boolean currentlyLiked =
                    boundReel.isLiked();

            ReelSupabaseHelper
                    .toggleReelLike(
                            context,
                            boundReel.getId(),
                            currentlyLiked,
                            new ReelSupabaseHelper.ActionCallback() {

                                @Override
                                public void onSuccess() {

                                    boundReel.setLiked(
                                            !currentlyLiked
                                    );

                                    int oldLikes =
                                            Math.max(
                                                    0,
                                                    boundReel.getLikes()
                                            );

                                    int newLikes;

                                    if (currentlyLiked) {

                                        newLikes =
                                                Math.max(
                                                        0,
                                                        oldLikes - 1
                                                );

                                    } else {

                                        newLikes =
                                                oldLikes + 1;
                                    }

                                    boundReel.setLikes(
                                            newLikes
                                    );

                                    reelLikesText.setText(
                                            formatCount(
                                                    newLikes
                                            )
                                    );

                                    updateLikeUI();

                                    reelLikeButton
                                            .setEnabled(
                                                    true
                                            );
                                }

                                @Override
                                public void onError(
                                        String message) {

                                    reelLikeButton
                                            .setEnabled(
                                                    true
                                            );

                                    Toast.makeText(
                                            context,
                                            message == null
                                                    ? "Like update failed."
                                                    : message,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                    );
        }

        // =====================================================
        // LIKE UI
        // =====================================================

        private void updateLikeUI() {

            if (boundReel == null) {
                return;
            }

            if (boundReel.isLiked()) {

                reelLikeButton.setImageResource(
                        android.R.drawable.btn_star_big_on
                );

                reelLikeButton.setContentDescription(
                        "Unlike"
                );

            } else {

                reelLikeButton.setImageResource(
                        android.R.drawable.btn_star_big_off
                );

                reelLikeButton.setContentDescription(
                        "Like"
                );
            }
        }

        // =====================================================
        // COMMENTS
        // =====================================================

        private void openComments() {

            if (boundReel == null
                    || boundReel.getId() == null) {

                return;
            }

            Intent intent =
                    new Intent(
                            context,
                            ReelCommentsActivity.class
                    );

            intent.putExtra(
                    "reel_id",
                    boundReel.getId()
            );

            context.startActivity(
                    intent
            );
        }

        // =====================================================
        // SHARE
        // =====================================================

        private void shareReel() {

            if (boundReel == null) {
                return;
            }

            String videoUrl =
                    boundReel.getVideoUrl();

            if (videoUrl == null
                    || videoUrl.trim().isEmpty()) {

                Toast.makeText(
                        context,
                        "Video link available nahi hai.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String caption =
                    boundReel.getCaption();

            if (caption == null) {
                caption = "";
            }

            String shareText =
                    caption.trim()
                            + "\n\n"
                            + videoUrl;

            Intent shareIntent =
                    new Intent(
                            Intent.ACTION_SEND
                    );

            shareIntent.setType(
                    "text/plain"
            );

            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    shareText
            );

            context.startActivity(
                    Intent.createChooser(
                            shareIntent,
                            "Share Reel"
                    )
            );
        }

        // =====================================================
        // DELETE CONFIRM
        // =====================================================

        private void confirmDelete() {

            if (boundReel == null) {
                return;
            }

            new androidx.appcompat.app.AlertDialog.Builder(
                    context
            )
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
                                    deleteReel()
                    )
                    .show();
        }

        // =====================================================
        // DELETE
        // =====================================================

        private void deleteReel() {

            if (boundReel == null) {
                return;
            }

            reelDeleteButton.setEnabled(
                    false
            );

            ReelSupabaseHelper
                    .deleteReel(
                            context,
                            boundReel.getId(),
                            new ReelSupabaseHelper.ActionCallback() {

                                @Override
                                public void onSuccess() {

                                    int position =
                                            getBindingAdapterPosition();

                                    if (position != RecyclerView.NO_POSITION
                                            && position < reelList.size()) {

                                        reelList.remove(
                                                position
                                        );

                                        notifyItemRemoved(
                                                position
                                        );
                                    }

                                    Toast.makeText(
                                            context,
                                            "Reel deleted",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                                @Override
                                public void onError(
                                        String message) {

                                    reelDeleteButton
                                            .setEnabled(
                                                    true
                                            );

                                    Toast.makeText(
                                            context,
                                            message == null
                                                    ? "Reel delete failed."
                                                    : message,
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }
                    );
        }
    }

    // =========================================================
    // COUNT FORMAT
    // =========================================================

    private String formatCount(int count) {

        if (count < 1000) {
            return String.valueOf(count);
        }

        if (count < 1000000) {

            double value =
                    count / 1000.0;

            if (value >= 100) {
                return String.format(
                        java.util.Locale.US,
                        "%.0fK",
                        value
                );
            }

            return String.format(
                    java.util.Locale.US,
                    "%.1fK",
                    value
            );
        }

        double value =
                count / 1000000.0;

        if (value >= 100) {
            return String.format(
                    java.util.Locale.US,
                    "%.0fM",
                    value
            );
        }

        return String.format(
                java.util.Locale.US,
                "%.1fM",
                value
        );
    }
}
