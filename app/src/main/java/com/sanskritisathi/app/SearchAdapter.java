package com.sanskritisathi.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.UserViewHolder> {

    private final Context context;
    private final List<UserSearchItem> users = new ArrayList<>();

    public SearchAdapter(Context context) {
        this.context = context;
    }

    public void setUsers(List<UserSearchItem> newUsers) {
        users.clear();

        if (newUsers != null) {
            users.addAll(newUsers);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        LinearLayout row = new LinearLayout(
                context
        );

        row.setOrientation(
                LinearLayout.VERTICAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                20,
                18,
                20,
                18
        );

        row.setBackgroundColor(
                Color.WHITE
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                0,
                0,
                10
        );

        row.setLayoutParams(params);

        TextView name = new TextView(context);

        name.setTextSize(17);
        name.setTextColor(
                Color.rgb(59, 33, 23)
        );
        name.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView username = new TextView(context);

        username.setTextSize(14);
        username.setTextColor(
                Color.rgb(138, 75, 42)
        );

        TextView bio = new TextView(context);

        bio.setTextSize(13);
        bio.setTextColor(
                Color.rgb(120, 100, 90)
        );

        bio.setMaxLines(2);

        row.addView(name);

        LinearLayout.LayoutParams usernameParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        usernameParams.topMargin = 4;

        row.addView(
                username,
                usernameParams
        );

        LinearLayout.LayoutParams bioParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        bioParams.topMargin = 4;

        row.addView(
                bio,
                bioParams
        );

        return new UserViewHolder(
                row,
                name,
                username,
                bio
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull UserViewHolder holder,
            int position
    ) {

        UserSearchItem user =
                users.get(position);

        holder.name.setText(
                user.name.isEmpty()
                        ? "Sanskriti Sathi User"
                        : user.name
        );

        String username = user.username;

        if (username.isEmpty()) {
            holder.username.setText("@user");
        } else if (username.startsWith("@")) {
            holder.username.setText(username);
        } else {
            holder.username.setText(
                    "@" + username
            );
        }

        if (user.bio.isEmpty()) {
            holder.bio.setText(
                    "Apni Sanskriti se judein."
            );
        } else {
            holder.bio.setText(user.bio);
        }

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    context,
                    UserProfileActivity.class
            );

            intent.putExtra(
                    "user_uid",
                    user.uid
            );

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder
            extends RecyclerView.ViewHolder {

        TextView name;
        TextView username;
        TextView bio;

        UserViewHolder(
                @NonNull View itemView,
                TextView name,
                TextView username,
                TextView bio
        ) {
            super(itemView);

            this.name = name;
            this.username = username;
            this.bio = bio;
        }
    }

    public static class UserSearchItem {

        public final String uid;
        public final String name;
        public final String username;
        public final String bio;

        public UserSearchItem(
                String uid,
                String name,
                String username,
                String bio
        ) {
            this.uid = uid;
            this.name = name;
            this.username = username;
            this.bio = bio;
        }
    }
}
