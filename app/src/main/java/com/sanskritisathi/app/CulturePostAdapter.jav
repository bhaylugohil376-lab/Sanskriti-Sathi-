package com.sanskritisathi.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

        holder.likeButton.setOnClickListener(v -> {

            String current = holder.likeCount
                    .getText()
                    .toString();

            if (current.startsWith("♡")) {
                holder.likeButton.setText("♥");
            } else {
                holder.likeButton.setText("♡");
            }
        });

        holder.saveButton.setOnClickListener(v -> {

            if (holder.saveButton.getText()
                    .toString().equals("🔖")) {

                holder.saveButton.setText("🔖");
            }
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
