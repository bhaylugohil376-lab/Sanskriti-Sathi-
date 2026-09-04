package com.sanskritisathi.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ReelCommentsAdapter
        extends RecyclerView.Adapter<ReelCommentsAdapter.CommentViewHolder> {

    public interface CommentActionListener {
        void onDelete(ReelComment comment, int position);
        void onError(String message);
    }

    private final Context context;
    private final List<ReelComment> comments;
    private final CommentActionListener listener;

    private final FirebaseAuth auth =
            FirebaseAuth.getInstance();

    public ReelCommentsAdapter(
            Context context,
            List<ReelComment> comments,
            CommentActionListener listener) {

        this.context = context;
        this.comments = comments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
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

        holder.usernameText.setText(
                comment.getUsername()
        );

        holder.commentText.setText(
                comment.getText()
        );

        holder.profileImage.setImageResource(
                R.drawable.icon_foreground
        );

        // Only the owner of the comment
        // can see the delete button.
        boolean isOwnComment = false;

        if (auth.getCurrentUser() != null) {

            String currentUid =
                    auth.getCurrentUser().getUid();

            String commentUid =
                    comment.getUserId();

            isOwnComment =
                    commentUid != null &&
                    currentUid.equals(commentUid);
        }

        if (isOwnComment) {

            holder.deleteButton.setVisibility(
                    View.VISIBLE
            );

        } else {

            holder.deleteButton.setVisibility(
                    View.GONE
            );
        }

        holder.deleteButton.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition !=
                    RecyclerView.NO_POSITION) {

                listener.onDelete(
                        comment,
                        adapterPosition
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder
            extends RecyclerView.ViewHolder {

        ImageView profileImage;
        ImageButton deleteButton;

        TextView usernameText;
        TextView commentText;

        CommentViewHolder(
                @NonNull View itemView) {

            super(itemView);

            profileImage =
                    itemView.findViewById(
                            R.id.commentProfileImage
                    );

            deleteButton =
                    itemView.findViewById(
                            R.id.commentDeleteButton
                    );

            usernameText =
                    itemView.findViewById(
                            R.id.commentUsernameText
                    );

            commentText =
                    itemView.findViewById(
                            R.id.commentText
                    );
        }
    }
}
