package com.sanskritisathi.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CulturePostAdapter
        extends RecyclerView.Adapter<CulturePostAdapter.PostViewHolder> {

    private final Context context;
    private final List<CulturePost> postList;

    // Author-wise Follow / Following state
    private final Map<String, Boolean> followingMap = new HashMap<>();

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

        // =========================
        // BASIC POST DATA
        // =========================

        holder.postAuthor.setText(
                safeText(post.getAuthor(), "Sanskriti Sathi")
        );

        holder.postCategory.setText(
                safeText(post.getCategory(), "संस्कृति समाचार")
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
        // FOLLOW / FOLLOWING
        // =========================

        String author = safeText(
                post.getAuthor(),
                "Sanskriti Sathi"
        );

        boolean isFollowing =
                followingMap.containsKey(author)
                        && Boolean.TRUE.equals(
                        followingMap.get(author)
                );

        updateFollowText(
                holder.followStatus,
                isFollowing
        );

        holder.followStatus.setOnClickListener(v -> {

            boolean currentState =
                    followingMap.containsKey(author)
                            && Boolean.TRUE.equals(
                            followingMap.get(author)
                    );

            boolean newState = !currentState;

            followingMap.put(author, newState);

            updateFollowText(
                    holder.followStatus,
                    newState
            );

            if (newState) {

                Toast.makeText(
                        context,
                        "Following " + author,
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        context,
                        "Unfollowed " + author,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // =========================
        // LIKE
        // =========================

        updateLikeUI(holder, post);

        holder.likeButton.setOnClickListener(v -> {

            post.toggleLiked();

            updateLikeUI(holder, post);

            notifyItemChanged(
                    holder.getBindingAdapterPosition()
            );
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
    // FOLLOW UI
    // =====================================================

    private void updateFollowText(
            TextView textView,
            boolean following) {

        if (following) {

            textView.setText("  •  Following");
            textView.setTextColor(
                    0xFF757575
            );

        } else {

            textView.setText("  •  Follow");
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
                "❤️ " + post.getLikeCount() + " likes"
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
    // COMMENT DIALOG
    // =====================================================

    private void showCommentDialog(
            CulturePost post) {

        EditText input = new EditText(context);

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

                                    notifyItemChanged(
                                            holder.getBindingAdapterPosition()
                                    );
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
    // DELETE CONFIRMATION
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
