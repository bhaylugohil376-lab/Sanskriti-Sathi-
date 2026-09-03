package com.sanskritisathi.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviDevtaAdapter
        extends RecyclerView.Adapter<DeviDevtaAdapter.DeviDevtaViewHolder> {

    private final Context context;
    private final List<DeviDevta> deviDevtaList;

    public DeviDevtaAdapter(
            Context context,
            List<DeviDevta> deviDevtaList) {

        this.context = context;
        this.deviDevtaList = deviDevtaList;
    }

    @NonNull
    @Override
    public DeviDevtaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_devi_devta,
                        parent,
                        false
                );

        return new DeviDevtaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull DeviDevtaViewHolder holder,
            int position) {

        DeviDevta item = deviDevtaList.get(position);

        holder.deviDevtaImage.setImageResource(
                item.getImageResId()
        );

        holder.deviDevtaName.setText(
                safeText(item.getName())
        );

        holder.deviDevtaTradition.setText(
                "परंपरा: " + safeText(item.getTradition())
        );

        holder.deviDevtaDescription.setText(
                safeText(item.getDescription())
        );

        holder.deviDevtaImportance.setText(
                safeText(item.getImportance())
        );

        holder.deviDevtaTemples.setText(
                safeText(item.getTemples())
        );

        holder.deviDevtaFestivals.setText(
                safeText(item.getFestivals())
        );

        holder.deviDevtaStories.setText(
                safeText(item.getStories())
        );
    }

    private String safeText(String text) {

        if (text == null || text.trim().isEmpty()) {
            return "जानकारी उपलब्ध नहीं है।";
        }

        return text.trim();
    }

    @Override
    public int getItemCount() {
        return deviDevtaList == null
                ? 0
                : deviDevtaList.size();
    }

    public static class DeviDevtaViewHolder
            extends RecyclerView.ViewHolder {

        ImageView deviDevtaImage;
        TextView deviDevtaName;
        TextView deviDevtaTradition;
        TextView deviDevtaDescription;
        TextView deviDevtaImportance;
        TextView deviDevtaTemples;
        TextView deviDevtaFestivals;
        TextView deviDevtaStories;

        public DeviDevtaViewHolder(
                @NonNull View itemView) {

            super(itemView);

            deviDevtaImage = itemView.findViewById(
                    R.id.deviDevtaImage
            );

            deviDevtaName = itemView.findViewById(
                    R.id.deviDevtaName
            );

            deviDevtaTradition = itemView.findViewById(
                    R.id.deviDevtaTradition
            );

            deviDevtaDescription = itemView.findViewById(
                    R.id.deviDevtaDescription
            );

            deviDevtaImportance = itemView.findViewById(
                    R.id.deviDevtaImportance
            );

            deviDevtaTemples = itemView.findViewById(
                    R.id.deviDevtaTemples
            );

            deviDevtaFestivals = itemView.findViewById(
                    R.id.deviDevtaFestivals
            );

            deviDevtaStories = itemView.findViewById(
                    R.id.deviDevtaStories
            );
        }
    }
}
