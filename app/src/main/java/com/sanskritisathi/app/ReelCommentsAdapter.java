package com.sanskritisathi.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReelCommentsAdapter
        extends RecyclerView.Adapter<ReelCommentsAdapter.CommentViewHolder> {

    private final Context context;
    private final List<ReelComment> comments;

    public ReelCommentsAdapter(
            Context context,
            List<ReelComment> comments) {

        this.context = context;

        this.comments =
                comments == null
                        ? new ArrayList<>()
                        : comments;
    }

    public void setComments(
            List<ReelComment> newComments) {

        comments.clear();

        if (newComments != null) {
            comments.addAll(newComments);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(context)
                        .inflate(
                                R.layout.item_reel_comment,
                                parent,
                                false
                        );

        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CommentViewHolder holder,
            int position) {

        ReelComment comment =
                comments.get(position);

        String username =
                comment.getUsername();

        if (username == null ||
                username.trim().isEmpty()) {

            username = "Sanskriti User";
        }

        holder.usernameText.setText(
                username
        );

        String text =
                comment.getText();

        if (text == null) {
            text = "";
        }

        holder.commentText.setText(
                text
        );
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder
            extends RecyclerView.ViewHolder {

        TextView usernameText;
        TextView commentText;

        CommentViewHolder(
                @NonNull View itemView) {

            super(itemView);

            usernameText =
                    itemView.findViewById(
                            R.id.commentUsername
                    );

            commentText =
                    itemView.findViewById(
                            R.id.commentText
                    );
        }
    }
}
