package com.sanskritisathi.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostCommentAdapter
        extends RecyclerView.Adapter<PostCommentAdapter.CommentViewHolder> {

    private final Context context;
    private final List<PostCommentsActivity.PostComment> commentList;

    public PostCommentAdapter(
            Context context,
            List<PostCommentsActivity.PostComment> commentList
    ) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_post_comment,
                parent,
                false
        );

        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CommentViewHolder holder,
            int position
    ) {
        PostCommentsActivity.PostComment comment =
                commentList.get(position);

        String author = comment.getAuthor();
        String text = comment.getText();

        holder.authorText.setText(
                author == null || author.trim().isEmpty()
                        ? "Sanskriti Sathi User"
                        : author
        );

        holder.commentText.setText(
                text == null
                        ? ""
                        : text
        );

        if (comment.getCreatedAt() != null) {

            Date date =
                    comment.getCreatedAt()
                            .toDate();

            holder.timeText.setText(
                    new SimpleDateFormat(
                            "dd MMM, hh:mm a",
                            Locale.getDefault()
                    ).format(date)
            );

        } else {

            holder.timeText.setText("Just now");
        }
    }

    @Override
    public int getItemCount() {
        return commentList == null
                ? 0
                : commentList.size();
    }

    static class CommentViewHolder
            extends RecyclerView.ViewHolder {

        TextView authorText;
        TextView commentText;
        TextView timeText;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            authorText = itemView.findViewById(
                    R.id.commentAuthor
            );

            commentText = itemView.findViewById(
                    R.id.commentText
            );

            timeText = itemView.findViewById(
                    R.id.commentTime
            );
        }
    }
}
