package com.sanskritisathi.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CulturePostAdapter
        extends RecyclerView.Adapter<CulturePostAdapter.PostViewHolder> {

    private final Context context;
    private final List<CulturePost> postList;

    private final Map<String, Boolean> followingMap =
            new HashMap<>();

    public CulturePostAdapter(
            Context context,
            List<CulturePost> postList) {

        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_culture_post,
                        parent,
                        false
                );

        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PostViewHolder holder,
            int position) {

        CulturePost post = postList.get(position);

        String author = safeText(
                post.getAuthor(),
                "Sanskriti Sathi"
        );

        String authorUid = post.getAuthorUid();

        // =========================
        // BASIC DATA
        // =========================

        holder.postAuthor.setText(author);

        holder.postCategory.setText(
                safeText(
                        post.getCategory(),
                        "संस्कृति समाचार"
                )
        );

        holder.postCaption.setText(
                safeText(
                        post.getCaption(),
                        "भारतीय संस्कृति और विरासत के बारे में जानकारी पढ़ें।"
                )
        );

        // =========================
        // IMAGES
        // =========================

        holder.postProfileImage.setImageResource(
                post.getProfileImageResId()
        );

        holder.postImage.setImageResource(
                post.getPostImageResId()
        );

        // =========================
        // OPEN USER PROFILE
        // =========================

        View.OnClickListener profileClick =
                v -> openUserProfile(post);

        holder.postProfileImage.setOnClickListener(profileClick);
        holder.postAuthor.setOnClickListener(profileClick);

        // =========================
        // FOLLOW STATUS
        // =========================

        if (isValidUid(authorUid)) {

            loadFollowStatus(
                    holder,
                    authorUid
            );

            holder.followStatus.setOnClickListener(v ->
                    toggleFollow(
                            holder,
                            authorUid,
                            author
                    )
            );

        } else {

            holder.followStatus.setText("  •  Follow");
            holder.followStatus.setTextColor(
                    0xFF1976D2
            );

            holder.followStatus.setOnClickListener(v ->
                    Toast.makeText(
                            context,
                            "Is profile ka account link available nahi hai.",
                            Toast.LENGTH_SHORT
                    ).show()
            );
        }

        // =========================
        // LIKE
        // =========================

        updateLikeUI(holder, post);

        holder.likeButton.setOnClickListener(v -> {

            post.toggleLiked();

            updateLikeUI(holder, post);
        });

        // =========================
        // COMMENT
        // =========================

        holder.commentButton.setOnClickListener(
                v -> showCommentDialog(post)
        );

        // =========================
        // SHARE
        // =========================

        holder.shareButton.setOnClickListener(
                v -> sharePost(post)
        );

        // =========================
        // SAVE
        // =========================

        updateSaveUI(holder, post);

        holder.saveButton.setOnClickListener(v -> {

            post.toggleSaved();

            updateSaveUI(holder, post);
        });

        // =========================
        // POST MENU
        // =========================

        holder.postMenu.setOnClickListener(
                v -> showPostMenu(holder, post)
        );

        // =========================
        // DELETE
        // =========================

        holder.deletePostButton.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            showDeleteConfirmation(adapterPosition);
        });
    }

    // =====================================================
    // OPEN USER PROFILE
    // =====================================================

    private void openUserProfile(CulturePost post) {

        String uid = post.getAuthorUid();

        if (!isValidUid(uid)) {

            Toast.makeText(
                    context,
                    "User profile link available nahi hai.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null &&
                currentUser.getUid().equals(uid)) {

            context.startActivity(
                    new Intent(
                            context,
                            MyProfileActivity.class
                    )
            );

            return;
        }

        Intent intent =
                new Intent(
                        context,
                        UserProfileActivity.class
                );

        intent.putExtra(
                "user_uid",
                uid
        );

        context.startActivity(intent);
    }

    // =====================================================
    // LOAD FOLLOW STATUS
    // =====================================================

    private void loadFollowStatus(
            PostViewHolder holder,
            String authorUid) {

        FollowFirebaseHelper.checkFollowing(
                authorUid,
                new FollowFirebaseHelper.StatusCallback() {

                    @Override
                    public void onResult(
                            boolean following) {

                        followingMap.put(
                                authorUid,
                                following
                        );

                        updateFollowText(
                                holder.followStatus,
                                following
                        );
                    }

                    @Override
                    public void onError(
                            String message) {

                        updateFollowText(
                                holder.followStatus,
                                false
                        );
                    }
                }
        );
    }

    // =====================================================
    // FOLLOW / UNFOLLOW
    // =====================================================

    private void toggleFollow(
            PostViewHolder holder,
            String authorUid,
            String author) {

        Boolean value =
                followingMap.get(authorUid);

        boolean currentlyFollowing =
                value != null && value;

        holder.followStatus.setEnabled(false);

        if (currentlyFollowing) {

            holder.followStatus.setText(
                    "  •  Unfollowing..."
            );

            FollowFirebaseHelper.unfollowUser(
                    authorUid,
                    new FollowFirebaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            followingMap.put(
                                    authorUid,
                                    false
                            );

                            holder.followStatus.setEnabled(true);

                            updateFollowText(
                                    holder.followStatus,
                                    false
                            );
                        }

                        @Override
                        public void onError(
                                String message) {

                            holder.followStatus.setEnabled(true);

                            updateFollowText(
                                    holder.followStatus,
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

        } else {

            holder.followStatus.setText(
                    "  •  Following..."
            );

            FollowFirebaseHelper.followUser(
                    authorUid,
                    new FollowFirebaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            followingMap.put(
                                    authorUid,
                                    true
                            );

                            holder.followStatus.setEnabled(true);

                            updateFollowText(
                                    holder.followStatus,
                                    true
                            );
                        }

                        @Override
                        public void onError(
                                String message) {

                            holder.followStatus.setEnabled(true);

                            updateFollowText(
                                    holder.followStatus,
                                    false
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
    }

    // =====================================================
    // FOLLOW UI
    // =====================================================

    private void updateFollowText(
            TextView textView,
            boolean following) {

        if (following) {

            textView.setText(
                    "  •  Following"
            );

            textView.setTextColor(
                    0xFF757575
            );

        } else {

            textView.setText(
                    "  •  Follow"
            );

            textView.setTextColor(
                    0xFF1976D2
            );
        }
    }

    // =====================================================
    // LIKE UI
    // =====================================================

    private void updateLikeUI(
            PostViewHolder holder,
            CulturePost post) {

        if (post.isLiked()) {

            holder.likeButton.setText(
                    "❤️  Liked"
            );

            holder.likeButton.setTextColor(
                    0xFFE53935
            );

        } else {

            holder.likeButton.setText(
                    "♡  Like"
            );

            holder.likeButton.setTextColor(
                    0xFF222222
            );
        }

        holder.likeCount.setText(
                "❤️ " +
                        post.getLikeCount() +
                        " likes"
        );
    }

    // =====================================================
    // SAVE UI
    // =====================================================

    private void updateSaveUI(
            PostViewHolder holder,
            CulturePost post) {

        if (post.isSaved()) {

            holder.saveButton.setText("🔖");

            holder.saveButton.setTextColor(
                    0xFFF57C00
            );

        } else {

            holder.saveButton.setText("♡");

            holder.saveButton.setTextColor(
                    0xFF222222
            );
        }
    }

    // =====================================================
    // COMMENT
    // =====================================================

    private void showCommentDialog(
            CulturePost post) {

        EditText input =
                new EditText(context);

        input.setHint(
                "अपनी टिप्पणी लिखें..."
        );

        input.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        input.setMinLines(3);

        int padding = 32;

        input.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        new AlertDialog.Builder(context)
                .setTitle("💬 Comment")
                .setView(input)
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Post",
                        (dialog, which) -> {

                            String comment =
                                    input.getText()
                                            .toString()
                                            .trim();

                            if (!comment.isEmpty()) {

                                Toast.makeText(
                                        context,
                                        "Comment posted",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } else {

                                Toast.makeText(
                                        context,
                                        "Comment खाली है",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .show();
    }

    // =====================================================
    // SHARE
    // =====================================================

    private void sharePost(
            CulturePost post) {

        String text =
                safeText(
                        post.getCaption(),
                        "Sanskriti Sathi"
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
                "Sanskriti Sathi\n\n" + text
        );

        try {

            context.startActivity(
                    Intent.createChooser(
                            shareIntent,
                            "Share Post"
                    )
            );

        } catch (Exception e) {

            Toast.makeText(
                    context,
                    "Share option available nahi hai.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =====================================================
    // POST MENU
    // =====================================================

    private void showPostMenu(
            PostViewHolder holder,
            CulturePost post) {

        String[] options = {
                "🔗 Share",
                "🔖 Save",
                "🚫 Report",
                "🗑️ Delete Post"
        };

        new AlertDialog.Builder(context)
                .setTitle("Post Options")
                .setItems(
                        options,
                        (dialog, which) -> {

                            switch (which) {

                                case 0:
                                    sharePost(post);
                                    break;

                                case 1:

                                    post.toggleSaved();

                                    Toast.makeText(
                                            context,
                                            post.isSaved()
                                                    ? "Post saved"
                                                    : "Post unsaved",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    int savePosition =
                                            holder.getBindingAdapterPosition();

                                    if (savePosition !=
                                            RecyclerView.NO_POSITION) {

                                        notifyItemChanged(
                                                savePosition
                                        );
                                    }

                                    break;

                                case 2:

                                    Toast.makeText(
                                            context,
                                            "Report option selected",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    break;

                                case 3:

                                    int position =
                                            holder.getBindingAdapterPosition();

                                    if (position !=
                                            RecyclerView.NO_POSITION) {

                                        showDeleteConfirmation(
                                                position
                                        );
                                    }

                                    break;
                            }
                        }
                )
                .show();
    }

    // =====================================================
    // DELETE
    // =====================================================

    private void showDeleteConfirmation(
            int position) {

        if (position < 0 ||
                position >= postList.size()) {

            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Delete Post?")
                .setMessage(
                        "Kya aap is post ko delete karna chahte hain?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            postList.remove(position);

                            notifyItemRemoved(
                                    position
                            );

                            Toast.makeText(
                                    context,
                                    "Post deleted",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .show();
    }

    // =====================================================
    // SAFE TEXT
    // =====================================================

    private String safeText(
            String value,
            String fallback) {

        if (value == null ||
                value.trim().isEmpty()) {

            return fallback;
        }

        return value.trim();
    }

    // =====================================================
    // UID VALIDATION
    // =====================================================

    private boolean isValidUid(
            String uid) {

        return uid != null &&
                !uid.trim().isEmpty();
    }

    // =====================================================
    // ITEM COUNT
    // =====================================================

    @Override
    public int getItemCount() {

        return postList == null
                ? 0
                : postList.size();
    }

    // =====================================================
    // VIEW HOLDER
    // =====================================================

    public static class PostViewHolder
            extends RecyclerView.ViewHolder {

        ImageView postProfileImage;
        ImageView postImage;

        TextView postAuthor;
        TextView postCategory;
        TextView followStatus;
        TextView postMenu;

        TextView likeButton;
        TextView commentButton;
        TextView shareButton;
        TextView saveButton;

        TextView likeCount;
        TextView postCaption;

        TextView deletePostButton;

        public PostViewHolder(
                @NonNull View itemView) {

            super(itemView);

            postProfileImage =
                    itemView.findViewById(
                            R.id.postProfileImage
                    );

            postImage =
                    itemView.findViewById(
                            R.id.postImage
                    );

            postAuthor =
                    itemView.findViewById(
                            R.id.postAuthor
                    );

            postCategory =
                    itemView.findViewById(
                            R.id.postCategory
                    );

            followStatus =
                    itemView.findViewById(
                            R.id.followStatus
                    );

            postMenu =
                    itemView.findViewById(
                            R.id.postMenu
                    );

            likeButton =
                    itemView.findViewById(
                            R.id.likeButton
                    );

            commentButton =
                    itemView.findViewById(
                            R.id.commentButton
                    );

            shareButton =
                    itemView.findViewById(
                            R.id.shareButton
                    );

            saveButton =
                    itemView.findViewById(
                            R.id.saveButton
                    );

            likeCount =
                    itemView.findViewById(
                            R.id.likeCount
                    );

            postCaption =
                    itemView.findViewById(
                            R.id.postCaption
                    );

            deletePostButton =
                    itemView.findViewById(
                            R.id.deletePostButton
                    );
        }
    }
}
