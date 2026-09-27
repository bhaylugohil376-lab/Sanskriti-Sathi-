package com.sanskritisathi.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.InputType;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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

    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {
        return reelList.size();
    }

    // =========================================================
    // PAUSE ALL
    // =========================================================

    public void pauseAllVideos() {

        for (int i = 0; i < getItemCount(); i++) {

            RecyclerView recyclerView =
                    findRecyclerView();

            if (recyclerView == null) {
                return;
            }

            RecyclerView.ViewHolder viewHolder =
                    recyclerView.findViewHolderForAdapterPosition(i);

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

        RecyclerView recyclerView =
                findRecyclerView();

        if (recyclerView == null) {
            return;
        }

        LinearLayoutManagerHelper helper =
                new LinearLayoutManagerHelper(recyclerView);

        int position =
                helper.findFirstCompletelyVisible();

        if (position == RecyclerView.NO_POSITION) {

            position =
                    helper.findFirstVisible();
        }

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

        for (int i = 0; i < getItemCount(); i++) {

            RecyclerView.ViewHolder holder =
                    recyclerView.findViewHolderForAdapterPosition(i);

            if (holder instanceof ReelViewHolder) {

                ((ReelViewHolder) holder)
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

        private final PlayerView playerView;
        private final TextView errorText;

        private final ImageView profileImage;
        private final TextView usernameText;

        private final ImageButton editButton;
        private final ImageButton deleteButton;

        private final ImageButton likeButton;
        private final TextView likesText;

        private final ImageButton commentButton;
        private final TextView commentsText;

        private final ImageButton shareButton;
        private final TextView viewsText;

        private final TextView captionText;

        private ExoPlayer player;

        private String boundReelId = "";

        private GestureDetector gestureDetector;

        ReelViewHolder(
                @NonNull View itemView) {

            super(itemView);

            playerView =
                    itemView.findViewById(
                            R.id.reelPlayerView
                    );

            errorText =
                    itemView.findViewById(
                            R.id.reelErrorText
                    );

            profileImage =
                    itemView.findViewById(
                            R.id.reelProfileImage
                    );

            usernameText =
                    itemView.findViewById(
                            R.id.reelUsernameText
                    );

            editButton =
                    itemView.findViewById(
                            R.id.reelEditButton
                    );

            deleteButton =
                    itemView.findViewById(
                            R.id.reelDeleteButton
                    );

            likeButton =
                    itemView.findViewById(
                            R.id.reelLikeButton
                    );

            likesText =
                    itemView.findViewById(
                            R.id.reelLikesText
                    );

            commentButton =
                    itemView.findViewById(
                            R.id.reelCommentButton
                    );

            commentsText =
                    itemView.findViewById(
                            R.id.reelCommentsText
                    );

            shareButton =
                    itemView.findViewById(
                            R.id.reelShareButton
                    );

            viewsText =
                    itemView.findViewById(
                            R.id.reelViewsText
                    );

            captionText =
                    itemView.findViewById(
                            R.id.reelCaptionText
                    );

            setupDoubleTap();
        }

        // =====================================================
        // BIND REEL
        // =====================================================

        void bind(Reel reel) {

            boundReelId =
                    reel.getId() == null
                            ? ""
                            : reel.getId();

            setupPlayer(reel);

            // -------------------------------------------------
            // USERNAME
            // -------------------------------------------------

            String username =
                    reel.getUsername();

            if (TextUtils.isEmpty(username)) {

                username =
                        "Sanskriti User";
            }

            usernameText.setText(
                    username
            );

            // -------------------------------------------------
            // PROFILE
            // -------------------------------------------------

            profileImage.setImageResource(
                    R.drawable.icon_foreground
            );

            // -------------------------------------------------
            // CAPTION
            // -------------------------------------------------

            String caption =
                    reel.getCaption();

            if (caption == null) {
                caption = "";
            }

            captionText.setText(
                    caption
            );

            // -------------------------------------------------
            // COUNTS
            // -------------------------------------------------

            likesText.setText(
                    String.valueOf(
                            Math.max(
                                    0,
                                    reel.getLikes()
                            )
                    )
            );

            commentsText.setText(
                    String.valueOf(
                            Math.max(
                                    0,
                                    reel.getComments()
                            )
                    )
            );

            viewsText.setText(
                    String.valueOf(
                            Math.max(
                                    0,
                                    reel.getViews()
                            )
                    )
            );

            updateLikeIcon(
                    reel.isLiked()
            );

            // -------------------------------------------------
            // OWNER BUTTONS
            // -------------------------------------------------

            if (reel.isOwnReel()) {

                deleteButton.setVisibility(
                        View.VISIBLE
                );

                editButton.setVisibility(
                        View.VISIBLE
                );

            } else {

                deleteButton.setVisibility(
                        View.GONE
                );

                editButton.setVisibility(
                        View.GONE
                );
            }

            // -------------------------------------------------
            // LIKE CHECK
            // -------------------------------------------------

            checkLikeState(reel);

            // -------------------------------------------------
            // BUTTONS
            // -------------------------------------------------

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

            editButton.setOnClickListener(
                    v -> showEditDialog(reel)
            );
        }

        // =====================================================
        // PLAYER
        // =====================================================

        private void setupPlayer(Reel reel) {

            releasePlayer();

            errorText.setVisibility(
                    View.GONE
            );

            String videoUrl =
                    reel.getVideoUrl();

            if (TextUtils.isEmpty(videoUrl)) {

                errorText.setText(
                        "Video unavailable"
                );

                errorText.setVisibility(
                        View.VISIBLE
                );

                return;
            }

            player =
                    new ExoPlayer.Builder(context)
                            .build();

            playerView.setPlayer(
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

                                errorText.setVisibility(
                                        View.GONE
                                );
                            }
                        }

                        @Override
                        public void onPlayerError(
                                @NonNull PlaybackException error) {

                            errorText.setText(
                                    "Video unavailable"
                            );

                            errorText.setVisibility(
                                    View.VISIBLE
                            );
                        }
                    }
            );

            player.prepare();

            /*
             * ReelActivity decides which visible reel
             * should actually play.
             */
            player.setPlayWhenReady(
                    false
            );
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

                player.stop();

                player.release();

                player = null;
            }

            playerView.setPlayer(
                    null
            );
        }

        // =====================================================
        // LIKE STATE
        // =====================================================

        private void checkLikeState(Reel reel) {

            if (!SupabaseAuthManager.isLoggedIn(context)) {
                return;
            }

            ReelSupabaseHelper.checkReelLike(
                    context,
                    reel.getId(),
                    new ReelSupabaseHelper.LikeCheckCallback() {

                        @Override
                        public void onResult(
                                boolean liked) {

                            if (!boundReelId.equals(
                                    reel.getId())) {

                                return;
                            }

                            reel.setLiked(
                                    liked
                            );

                            updateLikeIcon(
                                    liked
                            );
                        }

                        @Override
                        public void onError(
                                String message) {
                            // Keep existing local state.
                        }
                    }
            );
        }

        // =====================================================
        // TOGGLE LIKE
        // =====================================================

        private void toggleLike(Reel reel) {

            if (!SupabaseAuthManager.isLoggedIn(context)) {

                Toast.makeText(
                        context,
                        "Please login first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            likeButton.setEnabled(
                    false
            );

            boolean oldLiked =
                    reel.isLiked();

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

                            boolean newLiked =
                                    !oldLiked;

                            reel.setLiked(
                                    newLiked
                            );

                            int currentLikes =
                                    Math.max(
                                            0,
                                            reel.getLikes()
                                    );

                            if (newLiked) {

                                currentLikes++;

                            } else {

                                currentLikes =
                                        Math.max(
                                                0,
                                                currentLikes - 1
                                        );
                            }

                            reel.setLikes(
                                    currentLikes
                            );

                            likesText.setText(
                                    String.valueOf(
                                            currentLikes
                                    )
                            );

                            updateLikeIcon(
                                    newLiked
                            );
                        }

                        @Override
                        public void onError(
                                String message) {

                            likeButton.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    context,
                                    TextUtils.isEmpty(message)
                                            ? "Like update failed"
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

            if (liked) {

                likeButton.setImageResource(
                        android.R.drawable.btn_star_big_on
                );

                likeButton.setContentDescription(
                        "Unlike"
                );

            } else {

                likeButton.setImageResource(
                        android.R.drawable.btn_star_big_off
                );

                likeButton.setContentDescription(
                        "Like"
                );
            }

            likeButton.setColorFilter(
                    Color.WHITE
            );
        }

        // =====================================================
        // DOUBLE TAP
        // =====================================================

        private void setupDoubleTap() {

            gestureDetector =
                    new GestureDetector(
                            context,
                            new GestureDetector.SimpleOnGestureListener() {

                                @Override
                                public boolean onDown(
                                        MotionEvent event) {

                                    return true;
                                }

                                @Override
                                public boolean onDoubleTap(
                                        MotionEvent event) {

                                    int position =
                                            getBindingAdapterPosition();

                                    if (position
                                            == RecyclerView.NO_POSITION) {

                                        return true;
                                    }

                                    Reel reel =
                                            reelList.get(position);

                                    if (!reel.isLiked()) {

                                        toggleLike(
                                                reel
                                        );
                                    }

                                    return true;
                                }

                                @Override
                                public boolean onSingleTapConfirmed(
                                        MotionEvent event) {

                                    if (player == null) {
                                        return true;
                                    }

                                    if (player.isPlaying()) {

                                        player.pause();

                                    } else {

                                        player.play();
                                    }

                                    return true;
                                }
                            }
                    );

            playerView.setOnTouchListener(
                    (v, event) -> {

                        gestureDetector.onTouchEvent(
                                event
                        );

                        /*
                         * Return false so RecyclerView can still
                         * receive vertical swipe gestures.
                         */
                        return false;
                    }
            );
        }

        // =====================================================
        // COMMENTS
        // =====================================================

        private void openComments(Reel reel) {

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

        // =====================================================
        // SHARE
        // =====================================================

        private void shareReel(Reel reel) {

            String videoUrl =
                    reel.getVideoUrl();

            if (TextUtils.isEmpty(videoUrl)) {

                Toast.makeText(
                        context,
                        "Video link available nahi hai",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String caption =
                    reel.getCaption();

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

        private void confirmDelete(
                Reel reel) {

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

                            if (position
                                    != RecyclerView.NO_POSITION
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
                                    "Reel deleted",
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
                                    TextUtils.isEmpty(message)
                                            ? "Reel delete failed"
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        }

        // =====================================================
        // EDIT REEL
        // =====================================================

        private void showEditDialog(
                Reel reel) {

            LinearLayout layout =
                    new LinearLayout(context);

            layout.setOrientation(
                    LinearLayout.VERTICAL
            );

            int padding =
                    dp(20);

            layout.setPadding(
                    padding,
                    padding,
                    padding,
                    dp(8)
            );

            EditText captionInput =
                    new EditText(context);

            captionInput.setHint(
                    "Caption"
            );

            captionInput.setText(
                    reel.getCaption()
            );

            captionInput.setInputType(
                    InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_FLAG_MULTI_LINE
            );

            captionInput.setMaxLines(
                    5
            );

            layout.addView(
                    captionInput,
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            );

            RadioButton publicRadio =
                    new RadioButton(context);

            publicRadio.setText(
                    "Public"
            );

            RadioButton followersRadio =
                    new RadioButton(context);

            followersRadio.setText(
                    "Followers"
            );

            if ("Followers".equalsIgnoreCase(
                    reel.getVisibility())) {

                followersRadio.setChecked(
                        true
                );

            } else {

                publicRadio.setChecked(
                        true
                );
            }

            layout.addView(
                    publicRadio
            );

            layout.addView(
                    followersRadio
            );

            new AlertDialog.Builder(context)
                    .setTitle(
                            "Edit Reel"
                    )
                    .setView(
                            layout
                    )
                    .setNegativeButton(
                            "Cancel",
                            null
                    )
                    .setPositiveButton(
                            "Save",
                            (dialog, which) -> {

                                String newCaption =
                                        captionInput
                                                .getText()
                                                .toString()
                                                .trim();

                                String visibility =
                                        followersRadio.isChecked()
                                                ? "Followers"
                                                : "Public";

                                updateReel(
                                        reel,
                                        newCaption,
                                        visibility
                                );
                            }
                    )
                    .show();
        }

        // =====================================================
        // UPDATE REEL
        // =====================================================

        private void updateReel(
                Reel reel,
                String caption,
                String visibility) {

            ReelSupabaseHelper.updateReel(
                    context,
                    reel.getId(),
                    caption,
                    visibility,
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            Toast.makeText(
                                    context,
                                    "Reel updated ✓",
                                    Toast.LENGTH_SHORT
                            ).show();

                            int position =
                                    getBindingAdapterPosition();

                            if (position
                                    != RecyclerView.NO_POSITION) {

                                notifyItemChanged(
                                        position
                                );
                            }
                        }

                        @Override
                        public void onError(
                                String message) {

                            Toast.makeText(
                                    context,
                                    TextUtils.isEmpty(message)
                                            ? "Reel update failed"
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        }

        // =====================================================
        // DP
        // =====================================================

        private int dp(int value) {

            return (int) (
                    value
                            * context
                            .getResources()
                            .getDisplayMetrics()
                            .density
            );
        }
    }

    // =========================================================
    // SMALL LAYOUT MANAGER HELPER
    // =========================================================

    private static class LinearLayoutManagerHelper {

        private final RecyclerView recyclerView;

        LinearLayoutManagerHelper(
                RecyclerView recyclerView) {

            this.recyclerView =
                    recyclerView;
        }

        int findFirstCompletelyVisible() {

            View view =
                    recyclerView
                            .getChildAt(0);

            if (view == null) {
                return RecyclerView.NO_POSITION;
            }

            int position =
                    recyclerView
                            .getChildAdapterPosition(
                                    view
                            );

            if (position
                    != RecyclerView.NO_POSITION) {

                int top =
                        view.getTop();

                int bottom =
                        view.getBottom();

                int height =
                        recyclerView.getHeight();

                if (top >= 0
                        && bottom <= height) {

                    return position;
                }
            }

            return RecyclerView.NO_POSITION;
        }

        int findFirstVisible() {

            View view =
                    recyclerView
                            .getChildAt(0);

            if (view == null) {
                return RecyclerView.NO_POSITION;
            }

            return recyclerView
                    .getChildAdapterPosition(
                            view
                    );
        }
    }
}
