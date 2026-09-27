package com.sanskritisathi.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Locale;

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

        Reel reel = reelList.get(position);

        if (reel == null) {
            return;
        }

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

    @Override
    public void onViewDetachedFromWindow(
            @NonNull ReelViewHolder holder) {

        holder.pausePlayer();

        super.onViewDetachedFromWindow(holder);
    }

    // =========================================================
    // COUNT
    // =========================================================

    @Override
    public int getItemCount() {
        return reelList == null
                ? 0
                : reelList.size();
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

            RecyclerView.ViewHolder vh =
                    recyclerView.getChildViewHolder(child);

            if (vh instanceof ReelViewHolder) {

                ((ReelViewHolder) vh)
                        .pausePlayer();
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

        pauseAllVideos();

        int firstCompletelyVisible =
                recyclerView.getChildLayoutPosition(
                        recyclerView.getChildAt(0)
                );

        for (int i = 0;
             i < recyclerView.getChildCount();
             i++) {

            View child =
                    recyclerView.getChildAt(i);

            int position =
                    recyclerView.getChildAdapterPosition(
                            child
                    );

            if (position == RecyclerView.NO_POSITION) {
                continue;
            }

            int[] location =
                    new int[2];

            child.getLocationOnScreen(location);

            int childTop = location[1];
            int childBottom =
                    childTop + child.getHeight();

            int screenHeight =
                    recyclerView.getHeight();

            boolean mostlyVisible =
                    childTop <= screenHeight / 2
                            && childBottom >= screenHeight / 2;

            if (mostlyVisible) {

                RecyclerView.ViewHolder vh =
                        recyclerView.getChildViewHolder(
                                child
                        );

                if (vh instanceof ReelViewHolder) {

                    ((ReelViewHolder) vh)
                            .playPlayer();
                }

                return;
            }
        }

        if (firstCompletelyVisible >= 0) {

            View child =
                    recyclerView.findViewHolderForAdapterPosition(
                            firstCompletelyVisible
                    ) != null
                            ? recyclerView
                            .findViewHolderForAdapterPosition(
                                    firstCompletelyVisible
                            )
                            .itemView
                            : null;

            if (child != null) {

                RecyclerView.ViewHolder vh =
                        recyclerView.getChildViewHolder(
                                child
                        );

                if (vh instanceof ReelViewHolder) {

                    ((ReelViewHolder) vh)
                            .playPlayer();
                }
            }
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

            RecyclerView.ViewHolder vh =
                    recyclerView.getChildViewHolder(child);

            if (vh instanceof ReelViewHolder) {

                ((ReelViewHolder) vh)
                        .releasePlayer();
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
        private final TextView reelCaptionText;

        private final TextView reelBottomUsername;

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

            reelCaptionText =
                    itemView.findViewById(
                            R.id.reelCaptionText
                    );

            reelBottomUsername =
                    itemView.findViewById(
                            R.id.reelBottomUsername
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

            // -------------------------------------------------
            // USERNAME
            // -------------------------------------------------

            String username =
                    reel.getUsername();

            if (TextUtils.isEmpty(username)) {

                username =
                        "Sanskriti User";
            }

            reelUsernameText.setText(username);

            if (reelBottomUsername != null) {

                reelBottomUsername.setText(
                        username
                );

                reelBottomUsername.setVisibility(
                        View.GONE
                );
            }

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

            reelLikesText.setText(
                    formatCount(
                            reel.getLikes()
                    )
            );

            reelCommentsText.setText(
                    formatCount(
                            reel.getComments()
                    )
            );

            reelViewsText.setText(
                    formatCount(
                            reel.getViews()
                    )
            );

            // -------------------------------------------------
            // OWN REEL
            // -------------------------------------------------

            if (reel.isOwnReel()) {

                reelDeleteButton.setVisibility(
                        View.VISIBLE
                );

                reelEditButton.setVisibility(
                        View.VISIBLE
                );

            } else {

                reelDeleteButton.setVisibility(
                        View.GONE
                );

                reelEditButton.setVisibility(
                        View.GONE
                );
            }

            // -------------------------------------------------
            // LIKE ICON
            // -------------------------------------------------

            updateLikeIcon();

            // -------------------------------------------------
            // PLAYER
            // -------------------------------------------------

            setupPlayer(
                    reel.getVideoUrl()
            );

            // -------------------------------------------------
            // CHECK LIKE
            // -------------------------------------------------

            checkLikeState();
        }

        // =====================================================
        // PLAYER
        // =====================================================

        private void setupPlayer(
                String videoUrl) {

            releasePlayer();

            reelErrorText.setVisibility(
                    View.GONE
            );

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

                            if (playbackState
                                    == Player.STATE_READY) {

                                reelErrorText.setVisibility(
                                        View.GONE
                                );
                            }

                            if (playbackState
                                    == Player.STATE_BUFFERING) {

                                // Keep error hidden while buffering.
                                reelErrorText.setVisibility(
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

            player.prepare();

            /*
             * Important:
             * Do NOT autoplay every bound item.
             * ReelActivity controls the currently visible reel.
             */
            player.setPlayWhenReady(false);
        }

        // =====================================================
        // PLAY
        // =====================================================

        public void playPlayer() {

            if (player == null) {
                return;
            }

            player.setPlayWhenReady(true);
        }

        // =====================================================
        // PAUSE
        // =====================================================

        public void pausePlayer() {

            if (player == null) {
                return;
            }

            player.setPlayWhenReady(false);
            player.pause();
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

            reelLikeButton.setEnabled(false);

            boolean oldLiked =
                    currentReel.isLiked();

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

                            if (currentReel.isLiked()) {

                                likes++;

                            } else {

                                likes--;
                            }

                            currentReel.setLikes(
                                    Math.max(
                                            0,
                                            likes
                                    )
                            );

                            reelLikesText.setText(
                                    formatCount(
                                            currentReel.getLikes()
                                    )
                            );

                            updateLikeIcon();

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
        // CHECK LIKE
        // =====================================================

        private void checkLikeState() {

            if (currentReel == null) {
                return;
            }

            if (!SupabaseAuthManager.isLoggedIn(
                    context
            )) {

                currentReel.setLiked(false);

                updateLikeIcon();

                return;
            }

            ReelSupabaseHelper.checkReelLike(
                    context,
                    currentReel.getId(),
                    new ReelSupabaseHelper.LikeCheckCallback() {

                        @Override
                        public void onResult(
                                boolean liked) {

                            if (currentReel == null) {
                                return;
                            }

                            currentReel.setLiked(
                                    liked
                            );

                            updateLikeIcon();
                        }

                        @Override
                        public void onError(
                                String message) {

                            // Keep current local state.
                            updateLikeIcon();
                        }
                    }
            );
        }

        // =====================================================
        // LIKE ICON
        // =====================================================

        private void updateLikeIcon() {

            if (currentReel == null) {
                return;
            }

            if (currentReel.isLiked()) {

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

            if (currentReel == null) {
                return;
            }

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

            if (caption == null) {
                caption = "";
            }

            String shareText =
                    caption
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
        // DELETE
        // =====================================================

        private void confirmDelete() {

            if (currentReel == null) {
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
                                    deleteCurrentReel()
                    )
                    .show();
        }

        private void deleteCurrentReel() {

            if (currentReel == null) {
                return;
            }

            reelDeleteButton.setEnabled(
                    false
            );

            String reelId =
                    currentReel.getId();

            ReelSupabaseHelper.deleteReel(
                    context,
                    reelId,
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

                            reelDeleteButton.setEnabled(
                                    true
                            );

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

        private void editCurrentReel() {

            if (currentReel == null) {
                return;
            }

            final EditText captionInput =
                    new EditText(context);

            captionInput.setHint(
                    "Write a caption..."
            );

            captionInput.setText(
                    currentReel.getCaption()
            );

            captionInput.setSelectAllOnFocus(
                    false
            );

            captionInput.setInputType(
                    InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                            | InputType.TYPE_TEXT_FLAG_MULTI_LINE
            );

            captionInput.setMaxLines(5);

            int padding =
                    (int) (
                            20
                                    * context
                                    .getResources()
                                    .getDisplayMetrics()
                                    .density
                    );

            captionInput.setPadding(
                    padding,
                    padding,
                    padding,
                    padding
            );

            LinearLayout container =
                    new LinearLayout(context);

            container.setOrientation(
                    LinearLayout.VERTICAL
            );

            container.setPadding(
                    padding,
                    0,
                    padding,
                    0
            );

            container.addView(
                    captionInput,
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            );

            AlertDialog dialog =
                    new AlertDialog.Builder(context)
                            .setTitle("Edit Reel")
                            .setView(container)
                            .setNegativeButton(
                                    "Cancel",
                                    null
                            )
                            .setPositiveButton(
                                    "Save",
                                    null
                            )
                            .create();

            dialog.setOnShowListener(
                    d -> {

                        dialog.getButton(
                                AlertDialog.BUTTON_POSITIVE
                        ).setOnClickListener(
                                v -> {

                                    String newCaption =
                                            captionInput
                                                    .getText()
                                                    .toString()
                                                    .trim();

                                    if (newCaption.length() > 1000) {

                                        captionInput.setError(
                                                "Maximum 1000 characters"
                                        );

                                        return;
                                    }

                                    String visibility =
                                            currentReel
                                                    .getVisibility();

                                    if (TextUtils.isEmpty(
                                            visibility
                                    )) {

                                        visibility =
                                                "Public";
                                    }

                                    reelEditButton.setEnabled(
                                            false
                                    );

                                    ReelSupabaseHelper.updateReel(
                                            context,
                                            currentReel.getId(),
                                            newCaption,
                                            visibility,
                                            new ReelSupabaseHelper.ActionCallback() {

                                                @Override
                                                public void onSuccess() {

                                                    dialog.dismiss();

                                                    reelEditButton.setEnabled(
                                                            true
                                                    );

                                                    currentReel =
                                                            replaceCaption(
                                                                    currentReel,
                                                                    newCaption
                                                            );

                                                    reelCaptionText.setText(
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

                                                    reelEditButton.setEnabled(
                                                            true
                                                    );

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
                        );
                    }
            );

            dialog.show();
        }

        // =====================================================
        // CAPTION UPDATE
        // =====================================================

        private Reel replaceCaption(
                Reel oldReel,
                String newCaption) {

            return new Reel(
                    oldReel.getId(),
                    oldReel.getOwnerUid(),
                    oldReel.getUsername(),
                    oldReel.getVideoUrl(),
                    oldReel.getThumbnailUrl(),
                    newCaption,
                    oldReel.getVisibility(),
                    oldReel.getCreatedAt(),
                    oldReel.getLikes(),
                    oldReel.getComments(),
                    oldReel.getViews(),
                    oldReel.isLiked(),
                    oldReel.isOwnReel()
            );
        }

        // =====================================================
        // CLICK LISTENERS
        // =====================================================

        {
            reelLikeButton.setOnClickListener(
                    v -> handleLike()
            );

            reelCommentButton.setOnClickListener(
                    v -> openComments()
            );

            reelShareButton.setOnClickListener(
                    v -> shareReel()
            );

            reelDeleteButton.setOnClickListener(
                    v -> confirmDelete()
            );

            reelEditButton.setOnClickListener(
                    v -> editCurrentReel()
            );

            reelPlayerView.setOnClickListener(
                    v -> {

                        if (player == null) {
                            return;
                        }

                        if (player.isPlaying()) {

                            player.pause();

                        } else {

                            player.play();
                        }
                    }
            );
        }
    }

    // =========================================================
    // COUNT FORMAT
    // =========================================================

    private String formatCount(int count) {

        count = Math.max(0, count);

        if (count < 1000) {
            return String.valueOf(count);
        }

        if (count < 1_000_000) {

            double value =
                    count / 1000.0;

            if (value >= 100) {

                return String.format(
                        Locale.US,
                        "%.0fK",
                        value
                );

            } else {

                return String.format(
                        Locale.US,
                        "%.1fK",
                        value
                );
            }
        }

        double value =
                count / 1_000_000.0;

        if (value >= 100) {

            return String.format(
                    Locale.US,
                    "%.0fM",
                    value
            );

        } else {

            return String.format(
                    Locale.US,
                    "%.1fM",
                    value
            );
        }
    }
}
