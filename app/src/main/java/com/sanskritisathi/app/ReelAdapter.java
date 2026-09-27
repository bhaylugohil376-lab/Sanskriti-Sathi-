package com.sanskritisathi.app;

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

    private int currentPlayingPosition =
            RecyclerView.NO_POSITION;

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
                || TextUtils.isEmpty(reel.getId())) {

            return position;
        }

        return reel.getId().hashCode();
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

        Reel reel = reelList.get(position);

        holder.bind(reel);
    }

    // =========================================================
    // PLAY AT POSITION
    // =========================================================

    public void playVideoAt(int position) {

        if (position < 0
                || position >= reelList.size()) {
            return;
        }

        RecyclerView recyclerView =
                findRecyclerView();

        if (recyclerView == null) {
            return;
        }

        if (currentPlayingPosition != position) {

            pauseAllVideos();

            currentPlayingPosition =
                    position;
        }

        ReelViewHolder holder =
                (ReelViewHolder)
                        recyclerView.findViewHolderForAdapterPosition(
                                position
                        );

        if (holder != null) {

            holder.playVideo();
        }
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

            RecyclerView.ViewHolder viewHolder =
                    recyclerView.getChildViewHolder(
                            child
                    );

            if (viewHolder instanceof ReelViewHolder) {

                ((ReelViewHolder) viewHolder)
                        .pauseVideo();
            }
        }
    }

    // =========================================================
    // RESUME CURRENT
    // =========================================================

    public void resumeCurrentVideo() {

        if (currentPlayingPosition ==
                RecyclerView.NO_POSITION) {

            return;
        }

        playVideoAt(
                currentPlayingPosition
        );
    }

    // =========================================================
    // RELEASE ALL
    // =========================================================

    public void releaseAllVideos() {

        RecyclerView recyclerView =
                findRecyclerView();

        if (recyclerView != null) {

            for (int i = 0;
                 i < recyclerView.getChildCount();
                 i++) {

                View child =
                        recyclerView.getChildAt(i);

                RecyclerView.ViewHolder viewHolder =
                        recyclerView.getChildViewHolder(
                                child
                        );

                if (viewHolder instanceof ReelViewHolder) {

                    ((ReelViewHolder) viewHolder)
                            .releasePlayer();
                }
            }
        }

        currentPlayingPosition =
                RecyclerView.NO_POSITION;
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
    // RECYCLE
    // =========================================================

    @Override
    public void onViewRecycled(
            @NonNull ReelViewHolder holder) {

        holder.releasePlayer();

        super.onViewRecycled(holder);
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
    // VIEW HOLDER
    // =========================================================

    class ReelViewHolder
            extends RecyclerView.ViewHolder {

        private final PlayerView reelPlayerView;
        private final TextView reelErrorText;

        private final ImageView reelProfileImage;
        private final TextView reelUsernameText;

        private final ImageButton reelEditButton;
        private final ImageButton reelDeleteButton;

        private final ImageButton reelLikeButton;
        private final ImageButton reelCommentButton;
        private final ImageButton reelShareButton;

        private final TextView reelLikesText;
        private final TextView reelCommentsText;
        private final TextView reelViewsText;

        private final TextView reelCaptionText;

        private ExoPlayer player;

        private Reel boundReel;

        ReelViewHolder(
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

            reelEditButton =
                    itemView.findViewById(
                            R.id.reelEditButton
                    );

            reelDeleteButton =
                    itemView.findViewById(
                            R.id.reelDeleteButton
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

            reelCaptionText =
                    itemView.findViewById(
                            R.id.reelCaptionText
                    );

            setupButtons();
        }

        // =====================================================
        // BIND REEL
        // =====================================================

        void bind(Reel reel) {

            boundReel = reel;

            if (reel == null) {
                return;
            }

            String username =
                    reel.getUsername();

            if (TextUtils.isEmpty(username)) {

                username =
                        "Sanskriti User";
            }

            reelUsernameText.setText(
                    username
            );

            String caption =
                    reel.getCaption();

            if (caption == null) {
                caption = "";
            }

            reelCaptionText.setText(
                    caption
            );

            reelLikesText.setText(
                    String.valueOf(
                            Math.max(
                                    0,
                                    reel.getLikes()
                            )
                    )
            );

            reelCommentsText.setText(
                    String.valueOf(
                            Math.max(
                                    0,
                                    reel.getComments()
                            )
                    )
            );

            reelViewsText.setText(
                    String.valueOf(
                            Math.max(
                                    0,
                                    reel.getViews()
                            )
                    )
            );

            updateLikeUI(
                    reel.isLiked()
            );

            reelDeleteButton.setVisibility(
                    reel.isOwnReel()
                            ? View.VISIBLE
                            : View.GONE
            );

            /*
             * Edit button is available only
             * for the owner's reel.
             */

            reelEditButton.setVisibility(
                    reel.isOwnReel()
                            ? View.VISIBLE
                            : View.GONE
            );

            reelErrorText.setVisibility(
                    View.GONE
            );

            setupPlayer(
                    reel.getVideoUrl()
            );

            checkLikeStatus(reel);
        }

        // =====================================================
        // PLAYER
        // =====================================================

        private void setupPlayer(
                String videoUrl) {

            releasePlayer();

            if (TextUtils.isEmpty(videoUrl)) {

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

            player.setRepeatMode(
                    Player.REPEAT_MODE_ONE
            );

            MediaItem mediaItem =
                    MediaItem.fromUri(
                            Uri.parse(videoUrl)
                    );

            player.setMediaItem(
                    mediaItem
            );

            player.addListener(
                    new Player.Listener() {

                        @Override
                        public void onPlaybackStateChanged(
                                int playbackState) {

                            if (playbackState ==
                                    Player.STATE_READY) {

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

                            reelErrorText.setVisibility(
                                    View.VISIBLE
                            );
                        }
                    }
            );

            /*
             * Do not autoplay during bind.
             * ReelActivity decides which reel
             * is currently visible.
             */

            player.setPlayWhenReady(
                    false
            );

            player.prepare();
        }

        // =====================================================
        // PLAY
        // =====================================================

        void playVideo() {

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

        void pauseVideo() {

            if (player == null) {
                return;
            }

            player.pause();
        }

        // =====================================================
        // RELEASE
        // =====================================================

        void releasePlayer() {

            if (player != null) {

                player.pause();

                player.stop();

                player.release();

                player = null;
            }

            reelPlayerView.setPlayer(
                    null
            );
        }

        // =====================================================
        // LIKE STATUS
        // =====================================================

        private void checkLikeStatus(
                Reel reel) {

            if (!SupabaseAuthManager
                    .isLoggedIn(context)) {

                return;
            }

            ReelSupabaseHelper.checkReelLike(
                    context,
                    reel.getId(),
                    new ReelSupabaseHelper
                            .LikeCheckCallback() {

                        @Override
                        public void onResult(
                                boolean liked) {

                            if (boundReel != reel) {
                                return;
                            }

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
                            // Keep current UI.
                        }
                    }
            );
        }

        // =====================================================
        // LIKE UI
        // =====================================================

        private void updateLikeUI(
                boolean liked) {

            if (liked) {

                reelLikeButton.setImageResource(
                        android.R.drawable
                                .btn_star_big_on
                );

                reelLikeButton.setContentDescription(
                        "Unlike"
                );

            } else {

                reelLikeButton.setImageResource(
                        android.R.drawable
                                .btn_star_big_off
                );

                reelLikeButton.setContentDescription(
                        "Like"
                );
            }
        }

        // =====================================================
        // BUTTONS
        // =====================================================

        private void setupButtons() {

            reelLikeButton.setOnClickListener(
                    v -> toggleLike()
            );

            reelCommentButton.setOnClickListener(
                    v -> openComments()
            );

            reelShareButton.setOnClickListener(
                    v -> shareReel()
            );

            reelDeleteButton.setOnClickListener(
                    v -> deleteReel()
            );

            reelEditButton.setOnClickListener(
                    v -> editReel()
            );
        }

        // =====================================================
        // TOGGLE LIKE
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

            final boolean oldLiked =
                    boundReel.isLiked();

            reelLikeButton.setEnabled(
                    false
            );

            ReelSupabaseHelper.toggleReelLike(
                    context,
                    boundReel.getId(),
                    oldLiked,
                    new ReelSupabaseHelper
                            .ActionCallback() {

                        @Override
                        public void onSuccess() {

                            if (boundReel == null) {
                                return;
                            }

                            boolean newLiked =
                                    !oldLiked;

                            boundReel.setLiked(
                                    newLiked
                            );

                            int likes =
                                    boundReel.getLikes();

                            likes =
                                    newLiked
                                            ? likes + 1
                                            : Math.max(
                                                    0,
                                                    likes - 1
                                            );

                            boundReel.setLikes(
                                    likes
                            );

                            reelLikesText.setText(
                                    String.valueOf(
                                            likes
                                    )
                            );

                            updateLikeUI(
                                    newLiked
                            );

                            reelLikeButton.setEnabled(
                                    true
                            );
                        }

                        @Override
                        public void onError(
                                String message) {

                            reelLikeButton.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    context,
                                    TextUtils.isEmpty(
                                            message
                                    )
                                            ? "Like update failed."
                                            : message,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        }

        // =====================================================
        // COMMENTS
        // =====================================================

        private void openComments() {

            if (boundReel == null
                    || TextUtils.isEmpty(
                            boundReel.getId()
                    )) {

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

            if (TextUtils.isEmpty(videoUrl)) {

                Toast.makeText(
                        context,
                        "Video link available nahi hai.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String caption =
                    boundReel.getCaption();

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

        private void deleteReel() {

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
                                    performDelete()
                    )
                    .show();
        }

        private void performDelete() {

            String reelId =
                    boundReel.getId();

            ReelSupabaseHelper.deleteReel(
                    context,
                    reelId,
                    new ReelSupabaseHelper
                            .ActionCallback() {

                        @Override
                        public void onSuccess() {

                            int position =
                                    getBindingAdapterPosition();

                            if (position !=
                                    RecyclerView.NO_POSITION) {

                                reelList.remove(
                                        position
                                );

                                notifyItemRemoved(
                                        position
                                );

                                if (currentPlayingPosition ==
                                        position) {

                                    currentPlayingPosition =
                                            RecyclerView.NO_POSITION;
                                }
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

                            Toast.makeText(
                                    context,
                                    TextUtils.isEmpty(
                                            message
                                    )
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

            if (boundReel == null) {
                return;
            }

            final android.widget.EditText input =
                    new android.widget.EditText(
                            context
                    );

            input.setText(
                    boundReel.getCaption()
            );

            input.setHint(
                    "Write a caption..."
            );

            input.setPadding(
                    30,
                    10,
                    30,
                    10
            );

            new androidx.appcompat.app.AlertDialog.Builder(
                    context
            )
                    .setTitle(
                            "Edit Reel"
                    )
                    .setView(
                            input
                    )
                    .setNegativeButton(
                            "Cancel",
                            null
                    )
                    .setPositiveButton(
                            "Save",
                            (dialog, which) -> {

                                String newCaption =
                                        input.getText()
                                                .toString()
                                                .trim();

                                updateCaption(
                                        newCaption
                                );
                            }
                    )
                    .show();
        }

        private void updateCaption(
                String newCaption) {

            if (boundReel == null) {
                return;
            }

            ReelSupabaseHelper.updateReel(
                    context,
                    boundReel.getId(),
                    newCaption,
                    boundReel.getVisibility(),
                    new ReelSupabaseHelper
                            .ActionCallback() {

                        @Override
                        public void onSuccess() {

                            boundReel.setComments(
                                    boundReel.getComments()
                            );

                            boundReelCaptionUpdate(
                                    newCaption
                            );

                            Toast.makeText(
                                    context,
                                    "Reel updated ✓",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onError(
                                String message) {

                            Toast.makeText(
                                    context,
                                    TextUtils.isEmpty(
                                            message
                                    )
                                            ? "Reel update failed."
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        }

        private void boundReelCaptionUpdate(
                String caption) {

            if (boundReel == null) {
                return;
            }

            reelCaptionText.setText(
                    caption
            );
        }
    }
}
