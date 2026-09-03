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

        View view = LayoutInflater.from(parent.getContext())
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
                safeText(rss.getTitle(), "समाचार")
        );

        holder.rssDescription.setText(
                safeText(rss.getDescription(), "विवरण उपलब्ध नहीं है।")
        );

        holder.rssDate.setText(
                "🕒 " + safeText(rss.getPubDate(), "तारीख उपलब्ध नहीं है")
        );

        holder.rssSource.setText(
                "Source: " + safeText(rss.getSource(), "PIB")
        );

        View.OnClickListener openNews = v -> openNewsLink(rss.getLink());

        holder.readNews.setOnClickListener(openNews);
        holder.itemView.setOnClickListener(openNews);
    }

    private void openNewsLink(String link) {

        if (link == null || link.trim().isEmpty()) {

            return;
        }

        try {

            Uri uri = Uri.parse(link);

            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    uri
            );

            context.startActivity(intent);

        } catch (Exception ignored) {

            // Browser उपलब्ध नहीं होने पर कुछ नहीं करेंगे
        }
    }

    private String safeText(String value, String fallback) {

        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
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
