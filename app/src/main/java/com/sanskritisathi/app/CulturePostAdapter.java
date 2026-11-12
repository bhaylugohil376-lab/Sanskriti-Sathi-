package com.sanskritisathi.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CulturePostAdapter extends RecyclerView.Adapter<CulturePostAdapter.Holder> {
    private final Context context;
    private final List<CulturePost> items = new ArrayList<>();

    public CulturePostAdapter(Context context, List<CulturePost> posts) {
        this.context = context;
        setPosts(posts);
    }

    public void setPosts(List<CulturePost> posts) {
        items.clear();
        if (posts != null) items.addAll(posts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_culture_post, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CulturePost post = items.get(position);

        holder.author.setText(post.getAuthor() == null ? "Sanskriti member" : post.getAuthor());
        holder.category.setText(post.getCategory() == null ? "Culture" : post.getCategory());
        holder.caption.setText(post.getCaption() == null ? "" : post.getCaption());
        holder.likes.setText(String.valueOf(Math.max(0, post.getLikeCount())));

        holder.profile.setImageResource(R.drawable.icon_foreground);
        String imageUrl = post.getImageUrl();
        if (imageUrl != null && (imageUrl.startsWith("https://") || imageUrl.startsWith("http://"))) {
            Glide.with(holder.image)
                    .load(imageUrl)
                    .placeholder(R.drawable.icon_foreground)
                    .error(R.drawable.icon_foreground)
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.icon_foreground);
        }

        // Do not simulate likes or saves locally. Enable only when the
        // corresponding authenticated Supabase action is configured.
        holder.like.setVisibility(View.GONE);
        holder.save.setVisibility(View.GONE);

        holder.comment.setOnClickListener(v -> {
            if (post.getId() == null || post.getId().trim().isEmpty()) return;
            Intent intent = new Intent(context, PostCommentsActivity.class);
            intent.putExtra("postId", post.getId());
            intent.putExtra("postOwnerUid", post.getAuthorUid());
            context.startActivity(intent);
        });

        holder.share.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String text = (post.getCaption() == null ? "" : post.getCaption());
            if (imageUrl != null && imageUrl.startsWith("https://")) text += "\n" + imageUrl;
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            context.startActivity(Intent.createChooser(shareIntent, "Share Post"));
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView profile;
        final ImageView image;
        final TextView author, category, caption, likes, like, comment, share, save;

        Holder(View view) {
            super(view);
            profile = view.findViewById(R.id.postProfileImage);
            image = view.findViewById(R.id.postImage);
            author = view.findViewById(R.id.postAuthor);
            category = view.findViewById(R.id.postCategory);
            caption = view.findViewById(R.id.postCaption);
            likes = view.findViewById(R.id.likeCount);
            like = view.findViewById(R.id.likeButton);
            comment = view.findViewById(R.id.commentButton);
            share = view.findViewById(R.id.shareButton);
            save = view.findViewById(R.id.saveButton);
        }
    }
}
