package com.sanskritisathi.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

        View view = LayoutInflater.from(context).inflate(
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

        holder.profileImage.setImageResource(
                post.getProfileImageResId()
        );

        holder.author.setText(
                post.getAuthor()
        );

        holder.category.setText(
                post.getCategory()
        );

        holder.postImage.setImageResource(
                post.getPostImageResId()
        );

        holder.likeCount.setText(
                post.getLikeCount() + " likes"
        );

        holder.caption.setText(
                post.getCaption()
        );

        holder.likeButton.setText("♡");
        holder.saveButton.setText("🔖");

        // Like
        holder.likeButton.setOnClickListener(v -> {

            if (holder.likeButton.getText().toString().equals("♡")) {

                holder.likeButton.setText("♥");

                String text = holder.likeCount
                        .getText()
                        .toString();

                try {
                    int likes = Integer.parseInt(
                            text.replace(" likes", "").trim()
                    );

                    holder.likeCount.setText(
                            (likes + 1) + " likes"
                    );

                } catch (Exception ignored) {
                }

            } else {

                holder.likeButton.setText("♡");

                String text = holder.likeCount
                        .getText()
                        .toString();

                try {
                    int likes = Integer.parseInt(
                            text.replace(" likes", "").trim()
                    );

                    if (likes > 0) {
                        holder.likeCount.setText(
                                (likes - 1) + " likes"
                        );
                    }

                } catch (Exception ignored) {
                }
            }
        });

        // Save
        holder.saveButton.setOnClickListener(v -> {

            if (holder.saveButton
                    .getText()
                    .toString()
                    .equals("🔖")) {

                holder.saveButton.setText("✓");

                Toast.makeText(
                        context,
                        "Post saved",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                holder.saveButton.setText("🔖");

                Toast.makeText(
                        context,
                        "Post removed from saved",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Share
        holder.shareButton.setOnClickListener(v -> {

            Intent shareIntent = new Intent(
                    Intent.ACTION_SEND
            );

            shareIntent.setType("text/plain");

            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    post.getCaption()
            );

            context.startActivity(
                    Intent.createChooser(
                            shareIntent,
                            "Share Sanskriti Sathi"
                    )
            );
        });

        // Comment
        holder.commentButton.setOnClickListener(v -> {

            Toast.makeText(
                    context,
                    "Comments feature coming soon",
                    Toast.LENGTH_SHORT
            ).show();
        });
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
        TextView likeButton;
        TextView commentButton;
        TextView shareButton;
        TextView saveButton;
        TextView likeCount;
        TextView caption;

        public PostViewHolder(
                @NonNull View itemView) {

            super(itemView);

            profileImage = itemView.findViewById(
                    R.id.postProfileImage
            );

            postImage = itemView.findViewById(
                    R.id.postImage
            );

            author = itemView.findViewById(
                    R.id.postAuthor
            );

            category = itemView.findViewById(
                    R.id.postCategory
            );

            likeButton = itemView.findViewById(
                    R.id.postLike
            );

            commentButton = itemView.findViewById(
                    R.id.postComment
            );

            shareButton = itemView.findViewById(
                    R.id.postShare
            );

            saveButton = itemView.findViewById(
                    R.id.postSave
            );

            likeCount = itemView.findViewById(
                    R.id.postLikeCount
            );

            caption = itemView.findViewById(
                    R.id.postCaption
            );
        }
    }
}
