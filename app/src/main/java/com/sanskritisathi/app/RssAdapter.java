package com.sanskritisathi.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RssAdapter
        extends RecyclerView.Adapter<RssAdapter.RssViewHolder> {

    private final Context context;
    private final ArrayList<Rss> rssList;

    public RssAdapter(
            Context context,
            ArrayList<Rss> rssList) {

        this.context = context;
        this.rssList = rssList;
    }

    @NonNull
    @Override
    public RssViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_rss,
                        parent,
                        false
                );

        return new RssViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RssViewHolder holder,
            int position) {

        Rss rss = rssList.get(position);

        holder.rssTitle.setText(
                rss.getTitle()
        );

        holder.rssDescription.setText(
                rss.getDescription()
        );

        holder.rssDate.setText(
                "🕒 " + rss.getPubDate()
        );

        holder.rssSource.setText(
                "Source: " + rss.getSource()
        );

        View.OnClickListener openNews = v -> {

            String link = rss.getLink();

            if (link != null && !link.trim().isEmpty()) {

                try {

                    Intent intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(link)
                    );

                    context.startActivity(intent);

                } catch (Exception ignored) {
                    // No browser available
                }
            }
        };

        holder.readNews.setOnClickListener(openNews);
        holder.itemView.setOnClickListener(openNews);
    }

    @Override
    public int getItemCount() {
        return rssList.size();
    }

    public static class RssViewHolder
            extends RecyclerView.ViewHolder {

        TextView rssTitle;
        TextView rssDescription;
        TextView rssDate;
        TextView rssSource;
        TextView readNews;

        public RssViewHolder(
                @NonNull View itemView) {

            super(itemView);

            rssTitle = itemView.findViewById(
                    R.id.rssTitle
            );

            rssDescription = itemView.findViewById(
                    R.id.rssDescription
            );

            rssDate = itemView.findViewById(
                    R.id.rssDate
            );

            rssSource = itemView.findViewById(
                    R.id.rssSource
            );

            readNews = itemView.findViewById(
                    R.id.readNews
            );
        }
    }
}
