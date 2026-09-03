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

        int profileRes = getDrawableResource(story.getProfileImage());

        if (profileRes != 0) {
            holder.profileImage.setImageResource(profileRes);
        }

        // Story ring
        GradientDrawable ring = new GradientDrawable();
        ring.setShape(GradientDrawable.OVAL);
        ring.setStroke(
                dpToPx(3),
                0xFFFF9800
        );

        holder.profileImage.setBackground(ring);

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    context,
                    StoryActivity.class
            );

            intent.putExtra(
                    "story_position",
                    holder.getBindingAdapterPosition()
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
