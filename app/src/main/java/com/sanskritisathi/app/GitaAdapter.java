package com.sanskritisathi.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GitaAdapter
        extends RecyclerView.Adapter<GitaAdapter.GitaViewHolder> {

    private final Context context;
    private final ArrayList<Gita> gitaList;

    public GitaAdapter(
            Context context,
            ArrayList<Gita> gitaList) {

        this.context = context;
        this.gitaList = gitaList;
    }

    @NonNull
    @Override
    public GitaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_gita,
                        parent,
                        false
                );

        return new GitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull GitaViewHolder holder,
            int position) {

        Gita gita = gitaList.get(position);

        holder.chapter.setText(gita.getChapter());
        holder.title.setText(gita.getTitle());

        holder.introduction.setText(
                "परिचय\n" + gita.getIntroduction()
        );

        holder.teachings.setText(
                "मुख्य शिक्षाएँ\n" + gita.getTeachings()
        );

        holder.explanation.setText(
                "सरल व्याख्या\n" + gita.getExplanation()
        );

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    context,
                    GitaDetailActivity.class
            );

            intent.putExtra(
                    GitaDetailActivity.EXTRA_CHAPTER,
                    gita.getChapter()
            );

            intent.putExtra(
                    GitaDetailActivity.EXTRA_TITLE,
                    gita.getTitle()
            );

            intent.putExtra(
                    GitaDetailActivity.EXTRA_INTRODUCTION,
                    gita.getIntroduction()
            );

            intent.putExtra(
                    GitaDetailActivity.EXTRA_TEACHINGS,
                    gita.getTeachings()
            );

            intent.putExtra(
                    GitaDetailActivity.EXTRA_EXPLANATION,
                    gita.getExplanation()
            );

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return gitaList.size();
    }

    public static class GitaViewHolder
            extends RecyclerView.ViewHolder {

        TextView chapter;
        TextView title;
        TextView introduction;
        TextView teachings;
        TextView explanation;

        public GitaViewHolder(
                @NonNull View itemView) {

            super(itemView);

            chapter = itemView.findViewById(
                    R.id.gitaChapter
            );

            title = itemView.findViewById(
                    R.id.gitaTitle
            );

            introduction = itemView.findViewById(
                    R.id.gitaIntroduction
            );

            teachings = itemView.findViewById(
                    R.id.gitaTeachings
            );

            explanation = itemView.findViewById(
                    R.id.gitaExplanation
            );
        }
    }
}
