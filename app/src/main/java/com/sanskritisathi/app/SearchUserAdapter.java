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

import java.util.ArrayList;
import java.util.List;

public class SearchUserAdapter
        extends RecyclerView.Adapter<SearchUserAdapter.UserViewHolder> {

    private final Context context;
    private final List<SearchUser> users = new ArrayList<>();

    public SearchUserAdapter(Context context) {
        this.context = context;
    }

    public void setUsers(List<SearchUser> newUsers) {

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

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_search_user,
                        parent,
                        false
                );

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull UserViewHolder holder,
            int position
    ) {

        SearchUser user = users.get(position);

        holder.displayName.setText(
                user.getDisplayName()
        );

        holder.username.setText(
                "@" + user.getUsername()
        );

        holder.itemView.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            context,
                            ProfileActivity.class
                    );

            intent.putExtra(
                    "user_id",
                    user.getId()
            );

            intent.putExtra(
                    "username",
                    user.getUsername()
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

        ImageView avatar;
        TextView displayName;
        TextView username;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar =
                    itemView.findViewById(
                            R.id.userAvatar
                    );

            displayName =
                    itemView.findViewById(
                            R.id.userDisplayName
                    );

            username =
                    itemView.findViewById(
                            R.id.userUsername
                    );
        }
    }
}
