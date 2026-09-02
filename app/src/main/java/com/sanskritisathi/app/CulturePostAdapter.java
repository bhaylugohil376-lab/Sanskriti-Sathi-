package com.sanskritisathi.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CulturePostAdapter
        extends RecyclerView.Adapter<CulturePostAdapter.PostViewHolder> {

    private final Context context;
    private final ArrayList<CulturePost> postList;

    public CulturePostAdapter(
            Context context,
            ArrayList<CulturePost> postList) {

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

        holder.author.setText(post.getAuthor());
        holder.category.setText(post.getCategory());
        holder.caption.setText(post.getCaption());

        // Profile image
        if (post.getProfileImageResId() != 0) {
            holder.profileImage.setImageResource(
                    post.getProfileImageResId()
            );
        }

        // Post image
        if (post.getPostImageResId() != 0) {
            holder.postImage.setImageResource(
                    post.getPostImageResId()
            );
        }

        updateLikeUI(holder, post);
        updateSaveUI(holder, post);

        // ❤️ LIKE
        holder.postLike.setOnClickListener(v -> {

            post.toggleLike();

            updateLikeUI(holder, post);
        });

        // 💬 COMMENT
        holder.postComment.setOnClickListener(v -> {

            showCommentDialog();
        });

        // 📤 SHARE
        holder.postShare.setOnClickListener(v -> {

            sharePost(post);
        });

        // 🔖 SAVE
        holder.postSave.setOnClickListener(v -> {

            post.toggleSaved();

            updateSaveUI(holder, post);

            if (post.isSaved()) {

                Toast.makeText(
                        context,
                        "Post saved",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        context,
                        "Post unsaved",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // ❤️ Like UI
    private void updateLikeUI(
            PostViewHolder holder,
            CulturePost post) {

        if (post.isLiked()) {
            holder.postLike.setText("♥");
        } else {
            holder.postLike.setText("♡");
        }

        holder.likeCount.setText(
                "❤️ " + post.getLikeCount() + " likes"
        );
    }

    // 🔖 Save UI
    private void updateSaveUI(
            PostViewHolder holder,
            CulturePost post) {

        if (post.isSaved()) {
            holder.postSave.setText("🔖 Saved");
        } else {
            holder.postSave.setText("🔖 Save");
        }
    }

    // 💬 Comment dialog
    private void showCommentDialog() {

        EditText input = new EditText(context);

        input.setHint("अपना comment लिखें");

        input.setPadding(
                30,
                20,
                30,
                10
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

                            String comment = input
                                    .getText()
                                    .toString()
                                    .trim();

                            if (!comment.isEmpty()) {

                                Toast.makeText(
                                        context,
                                        "Comment added",
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

    // 📤 Share
    private void sharePost(CulturePost post) {

        String shareText =
                post.getCategory()
                        + "\n\n"
                        + post.getCaption()
                        + "\n\n"
                        + "Sanskriti Sathi";

        Intent shareIntent =
                new Intent(Intent.ACTION_SEND);

        shareIntent.setType("text/plain");

        shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                shareText
        );

        context.startActivity(
                Intent.createChooser(
                        shareIntent,
                        "Share Post"
                )
        );
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder
            extends RecyclerView.ViewHolder {

        ImageView profileImage;
        ImageView postImage;

        TextView author;
        TextView category;
        TextView caption;
        TextView likeCount;

        TextView postLike;
        TextView postComment;
        TextView postShare;
        TextView postSave;

        public PostViewHolder(
                @NonNull View itemView) {

            super(itemView);

            profileImage =
                    itemView.findViewById(
                            R.id.postProfileImage
                    );

            postImage =
                    itemView.findViewById(
                            R.id.postImage
                    );

            author =
                    itemView.findViewById(
                            R.id.postAuthor
                    );

            category =
                    itemView.findViewById(
                            R.id.postCategory
                    );

            caption =
                    itemView.findViewById(
                            R.id.postCaption
                    );

            likeCount =
                    itemView.findViewById(
                            R.id.postLikeCount
                    );

            postLike =
                    itemView.findViewById(
                            R.id.postLike
                    );

            postComment =
                    itemView.findViewById(
                            R.id.postComment
                    );

            postShare =
                    itemView.findViewById(
                            R.id.postShare
                    );

            postSave =
                    itemView.findViewById(
                            R.id.postSave
                    );
        }
    }
}
