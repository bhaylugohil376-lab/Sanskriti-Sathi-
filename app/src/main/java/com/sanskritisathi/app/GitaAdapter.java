package com.sanskritisathi.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GitaAdapter extends RecyclerView.Adapter<GitaAdapter.GitaViewHolder> {

    private final Context context;
    private final ArrayList<Gita> gitaList;

    public GitaAdapter(Context context, ArrayList<Gita> gitaList) {
        this.context = context;
        this.gitaList = gitaList;
    }

    @NonNull
    @Override
    public GitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_gita, parent, false);

        return new GitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GitaViewHolder holder, int position) {
        Gita gita = gitaList.get(position);

        holder.chapter.setText(gita.getChapter());
        holder.title.setText(gita.getTitle());
        holder.description.setText(gita.getDescription());
    }

    @Override
    public int getItemCount() {
        return gitaList.size();
    }

    public static class GitaViewHolder extends RecyclerView.ViewHolder {

        TextView chapter;
        TextView title;
        TextView description;

        public GitaViewHolder(@NonNull View itemView) {
            super(itemView);

            chapter = itemView.findViewById(R.id.gitaChapter);
            title = itemView.findViewById(R.id.gitaTitle);
            description = itemView.findViewById(R.id.gitaDescription);
        }
    }
}
