package com.sanskritisathi.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private final Context context;
    private final List<Story> storyList;

    public StoryAdapter(Context context, List<Story> storyList) {
        this.context = context;
        this.storyList = storyList;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_story, parent, false);

        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StoryViewHolder holder,
            int position) {

        Story story = storyList.get(position);

        holder.username.setText(story.getUsername());

        // Orange Story ring
        GradientDrawable ring = new GradientDrawable();
        ring.setShape(GradientDrawable.OVAL);
        ring.setStroke(
                dpToPx(3),
                0xFFFF9800
        );

        holder.profileImage.setBackground(ring);

        /*
         * Firebase Story image.
         *
         * Agar Firebase URL hai to Storage se image
         * download karke Story thumbnail mein show hogi.
         */
        String imageUrl = story.getStoryImage();

        if (imageUrl != null &&
                (imageUrl.startsWith("http://") ||
                 imageUrl.startsWith("https://"))) {

            FirebaseStorage.getInstance()
                    .getReferenceFromUrl(imageUrl)
                    .getBytes(2 * 1024 * 1024)
                    .addOnSuccessListener(bytes -> {

                        android.graphics.Bitmap bitmap =
                                android.graphics.BitmapFactory
                                        .decodeByteArray(
                                                bytes,
                                                0,
                                                bytes.length
                                        );

                        if (bitmap != null) {
                            holder.profileImage
                                    .setImageBitmap(bitmap);
                        }
                    })
                    .addOnFailureListener(e ->
                            holder.profileImage.setImageResource(
                                    R.drawable.icon_foreground
                            )
                    );

        } else {

            // Local/default profile image
            int resourceId =
                    getDrawableResource(imageUrl);

            if (resourceId != 0) {
                holder.profileImage.setImageResource(
                        resourceId
                );
            } else {
                holder.profileImage.setImageResource(
                        R.drawable.icon_foreground
                );
            }
        }

        holder.itemView.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            Intent intent = new Intent(
                    context,
                    StoryActivity.class
            );

            intent.putExtra(
                    "story_position",
                    adapterPosition
            );

            context.startActivity(intent);
        });
    }

    private int getDrawableResource(String name) {

        if (name == null || name.isEmpty()) {
            return 0;
        }

        return context.getResources().getIdentifier(
                name,
                "drawable",
                context.getPackageName()
        );
    }

    private int dpToPx(int dp) {

        return Math.round(
                dp * context.getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImage;
        TextView username;

        StoryViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(
                    R.id.storyProfileImage
            );

            username = itemView.findViewById(
                    R.id.storyUsername
            );
        }
    }
}
