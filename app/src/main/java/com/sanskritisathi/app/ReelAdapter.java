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
import androidx.media3.common.MediaItem;
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

        if (position >= 0
                && position < reelList.size()) {

            Reel reel = reelList.get(position);

            if (reel != null
                    && !TextUtils.isEmpty(reel.getId())) {

                return reel.getId().hashCode();
            }
        }

        return position;
    }

    // =========================================================
    // CREATE HOLDER
    // =========================================================

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

        Reel reel =
                reelList.get(position);

        holder.bind(reel);
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
    // COUNT
    // =========================================================

    @Override
    public int getItemCount() {
        return reelList.size();
    }

    // =========================================================
    // PAUSE ALL
    // =========================================================

    public void pauseAllVideos() {

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

            RecyclerView.ViewHolder holder =
                    recyclerView.getChildViewHolder(
                            child
                    );

            if (holder instanceof ReelViewHolder) {

                ((ReelViewHolder) holder)
                        .pauseVideo();
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

        LinearLayoutManagerHelper helper =
                new LinearLayoutManagerHelper(
                        recyclerView
                );

        int position =
                helper.findCurrentPosition();

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        RecyclerView.ViewHolder holder =
                recyclerView.findViewHolderForAdapterPosition(
                        position
                );

        if (holder instanceof ReelViewHolder) {

            ((ReelViewHolder) holder)
                    .playVideo();
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

            RecyclerView.ViewHolder holder =
                    recyclerView.getChildViewHolder(
                            child
                    );

            if (holder instanceof ReelViewHolder) {

                ((ReelViewHolder) holder)
                        .releasePlayer();
            }
        }
    }

    // =========================================================
    // FIND RECYCLER
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

        private Reel currentReel;

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

            currentReel = reel;

            releasePlayer();

            if (reelErrorText != null) {

                reelErrorText.setVisibility(
                        View.GONE
                );
            }

            String username =
                    reel.getUsername();

            if (TextUtils.isEmpty(username)) {

                username =
                        "Sanskriti User";
            }

            if (reelUsernameText != null) {

                reelUsernameText.setText(
                        username
                );
            }

            if (reelBottomUsername != null) {

                reelBottomUsername.setText(
                        username
                );

                reelBottomUsername.setVisibility(
                        View.GONE
                );
            }

            if (reelCaptionText != null) {

                String caption =
                        reel.getCaption();

                if (TextUtils.isEmpty(caption)) {

                    reelCaptionText.setText(
                            ""
                    );

                } else {

                    reelCaptionText.setText(
                            caption
                    );
                }
            }

            if (reelLikesText != null) {

                reelLikesText.setText(
                        formatCount(
                                reel.getLikes()
                        )
                );
            }

            if (reelCommentsText != null) {

                reelCommentsText.setText(
                        formatCount(
                                reel.getComments()
                        )
                );
            }

            if (reelViewsText != null) {

                reelViewsText.setText(
                        formatCount(
                                reel.getViews()
                        )
                );
            }

            updateLikeIcon(
                    reel.isLiked()
            );

            if (reelDeleteButton != null) {

                reelDeleteButton.setVisibility(
                        reel.isOwnReel()
                                ? View.VISIBLE
                                : View.GONE
                );
            }

            if (reelEditButton != null) {

                reelEditButton.setVisibility(
                        reel.isOwnReel()
                                ? View.VISIBLE
                                : View.GONE
                );
            }

            setupPlayer(
                    reel
            );

            setupActions();
        }

        // =====================================================
        // PLAYER
        // =====================================================

        private void setupPlayer(
                Reel reel) {

            String videoUrl =
                    reel.getVideoUrl();

            if (TextUtils.isEmpty(videoUrl)) {

                showVideoError();

                return;
            }

            try {

                player =
                        new ExoPlayer.Builder(
                                context
                        ).build();

                reelPlayerView.setPlayer(
                        player
                );

                reelPlayerView.setUseController(
                        false
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

                                if (reelErrorText == null) {
                                    return;
                                }

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
                                    androidx.media3.common.PlaybackException error) {

                                showVideoError();
                            }
                        }
                );

                player.prepare();

            } catch (Exception e) {

                showVideoError();
            }
        }

        // =====================================================
        // PLAY
        // =====================================================

        public void playVideo() {

            if (player == null) {
                return;
            }

            if (player.getPlaybackState()
                    == Player.STATE_IDLE) {

                player.prepare();
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

                player.pause();

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
        // VIDEO ERROR
        // =====================================================

        private void showVideoError() {

            if (reelErrorText != null) {

                reelErrorText.setText(
                        "Video unavailable"
                );

                reelErrorText.setVisibility(
                        View.VISIBLE
                );
            }
        }

        // =====================================================
        // ACTIONS
        // =====================================================

        private void setupActions() {

            if (reelLikeButton != null) {

                reelLikeButton.setOnClickListener(
                        v -> handleLike()
                );
            }

            if (reelCommentButton != null) {

                reelCommentButton.setOnClickListener(
                        v -> openComments()
                );
            }

            if (reelShareButton != null) {

                reelShareButton.setOnClickListener(
                        v -> shareReel()
                );
            }

            if (reelDeleteButton != null) {

                reelDeleteButton.setOnClickListener(
                        v -> confirmDelete()
                );
            }

            if (reelEditButton != null) {

                reelEditButton.setOnClickListener(
                        v -> editReel()
                );
            }

            /*
             * Instagram-style:
             * Tap video = pause/play.
             */
            reelPlayerView.setOnClickListener(
                    v -> {

                        if (player == null) {
                            return;
                        }

                        if (player.isPlaying()) {

                            pauseVideo();

                        } else {

                            playVideo();
                        }
                    }
            );
        }

        // =====================================================
        // LIKE
        // =====================================================

        private void handleLike() {

            if (currentReel == null) {
                return;
            }

            if (!SupabaseAuthManager.isLoggedIn(
                    context
            )) {

                Toast.makeText(
                        context,
                        "Please login first.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            final boolean oldLiked =
                    currentReel.isLiked();

            if (reelLikeButton != null) {

                reelLikeButton.setEnabled(
                        false
                );
            }

            ReelSupabaseHelper.toggleReelLike(
                    context,
                    currentReel.getId(),
                    oldLiked,
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            currentReel.setLiked(
                                    !oldLiked
                            );

                            int likes =
                                    currentReel.getLikes();

                            if (oldLiked) {

                                likes--;

                            } else {

                                likes++;
                            }

                            currentReel.setLikes(
                                    Math.max(
                                            0,
                                            likes
                                    )
                            );

                            updateLikeIcon(
                                    currentReel.isLiked()
                            );

                            if (reelLikesText != null) {

                                reelLikesText.setText(
                                        formatCount(
                                                currentReel
                                                        .getLikes()
                                        )
                                );
                            }

                            if (reelLikeButton != null) {

                                reelLikeButton.setEnabled(
                                        true
                                );
                            }
                        }

                        @Override
                        public void onError(
                                String message) {

                            if (reelLikeButton != null) {

                                reelLikeButton.setEnabled(
                                        true
                                );
                            }

                            Toast.makeText(
                                    context,
                                    TextUtils.isEmpty(message)
                                            ? "Like update failed."
                                            : message,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        }

        // =====================================================
        // LIKE ICON
        // =====================================================

        private void updateLikeIcon(
                boolean liked) {

            if (reelLikeButton == null) {
                return;
            }

            if (liked) {

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

            if (currentReel == null
                    || TextUtils.isEmpty(
                            currentReel.getId()
                    )) {

                return;
            }

            try {

                Intent intent =
                        new Intent(
                                context,
                                ReelCommentsActivity.class
                        );

                intent.putExtra(
                        "reel_id",
                        currentReel.getId()
                );

                context.startActivity(
                        intent
                );

            } catch (Exception e) {

                Toast.makeText(
                        context,
                        "Comments open nahi ho sake.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        // =====================================================
        // SHARE
        // =====================================================

        private void shareReel() {

            if (currentReel == null) {
                return;
            }

            String videoUrl =
                    currentReel.getVideoUrl();

            if (TextUtils.isEmpty(videoUrl)) {

                Toast.makeText(
                        context,
                        "Video link available nahi hai.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String caption =
                    currentReel.getCaption();

            String shareText;

            if (TextUtils.isEmpty(caption)) {

                shareText =
                        videoUrl;

            } else {

                shareText =
                        caption
                                + "\n\n"
                                + videoUrl;
            }

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
        // DELETE
        // =====================================================

        private void confirmDelete() {

            if (currentReel == null) {
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
                                    deleteReel()
                    )
                    .show();
        }

        private void deleteReel() {

            if (currentReel == null) {
                return;
            }

            final int position =
                    getBindingAdapterPosition();

            if (position
                    == RecyclerView.NO_POSITION) {

                return;
            }

            if (reelDeleteButton != null) {

                reelDeleteButton.setEnabled(
                        false
                );
            }

            ReelSupabaseHelper.deleteReel(
                    context,
                    currentReel.getId(),
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            if (position >= 0
                                    && position
                                    < reelList.size()) {

                                reelList.remove(
                                        position
                                );

                                notifyItemRemoved(
                                        position
                                );
                            }

                            Toast.makeText(
                                    context,
                                    "Reel deleted.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onError(
                                String message) {

                            if (reelDeleteButton != null) {

                                reelDeleteButton.setEnabled(
                                        true
                                );
                            }

                            Toast.makeText(
                                    context,
                                    TextUtils.isEmpty(message)
                                            ? "Reel delete failed."
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        }

        // =====================================================
        // EDIT
        // =====================================================

        private void editReel() {

            if (currentReel == null) {
                return;
            }

            Toast.makeText(
                    context,
                    "Edit Reel screen next step mein add karenge.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================================================
    // POSITION HELPER
    // =========================================================

    private static class LinearLayoutManagerHelper {

        private final RecyclerView recyclerView;

        LinearLayoutManagerHelper(
                RecyclerView recyclerView) {

            this.recyclerView =
                    recyclerView;
        }

        int findCurrentPosition() {

            if (recyclerView == null) {

                return RecyclerView.NO_POSITION;
            }

            View centerView =
                    recyclerView.findChildViewUnder(
                            recyclerView.getWidth() / 2f,
                            recyclerView.getHeight() / 2f
                    );

            if (centerView == null) {

                return RecyclerView.NO_POSITION;
            }

            RecyclerView.ViewHolder holder =
                    recyclerView.getChildViewHolder(
                            centerView
                    );

            if (holder == null) {

                return RecyclerView.NO_POSITION;
            }

            return holder.getBindingAdapterPosition();
        }
    }

    // =========================================================
    // FORMAT COUNT
    // =========================================================

    private String formatCount(int count) {

        count =
                Math.max(
                        0,
                        count
                );

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
}
