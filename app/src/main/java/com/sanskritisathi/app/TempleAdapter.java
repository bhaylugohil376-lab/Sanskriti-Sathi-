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

public class TempleAdapter
        extends RecyclerView.Adapter<TempleAdapter.TempleViewHolder> {

    private final Context context;
    private final List<Temple> templeList;

    public TempleAdapter(
            Context context,
            List<Temple> templeList) {

        this.context = context;
        this.templeList = templeList;
    }

    @NonNull
    @Override
    public TempleViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_temple,
                        parent,
                        false
                );

        return new TempleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TempleViewHolder holder,
            int position) {

        Temple temple = templeList.get(position);

        holder.templeImage.setImageResource(
                temple.getImageResId()
        );

        holder.templeName.setText(
                temple.getName()
        );

        holder.templeLocation.setText(
                "स्थान: " + temple.getLocation()
        );

        holder.templeHistory.setText(
                "इतिहास\n" + temple.getHistory()
        );

        holder.templeImportance.setText(
                "धार्मिक महत्व\n" +
                temple.getReligiousImportance()
        );

        holder.templeDeity.setText(
                "मुख्य देवी/देवता\n" +
                temple.getMainDeity()
        );

        holder.templeFestivals.setText(
                "प्रमुख पर्व\n" +
                temple.getFestivals()
        );

        holder.templeTraditions.setText(
                "विशेष परंपराएँ\n" +
                temple.getTraditions()
        );

        holder.templeArchitecture.setText(
                "वास्तुकला\n" +
                temple.getArchitecture()
        );

        holder.templeStories.setText(
                "प्रमुख कथाएँ\n" +
                temple.getStories()
        );
    }

    @Override
    public int getItemCount() {
        return templeList.size();
    }

    public static class TempleViewHolder
            extends RecyclerView.ViewHolder {

        ImageView templeImage;
        TextView templeName;
        TextView templeLocation;
        TextView templeHistory;
        TextView templeImportance;
        TextView templeDeity;
        TextView templeFestivals;
        TextView templeTraditions;
        TextView templeArchitecture;
        TextView templeStories;

        public TempleViewHolder(
                @NonNull View itemView) {

            super(itemView);

            templeImage = itemView.findViewById(
                    R.id.templeImage
            );

            templeName = itemView.findViewById(
                    R.id.templeName
            );

            templeLocation = itemView.findViewById(
                    R.id.templeLocation
            );

            templeHistory = itemView.findViewById(
                    R.id.templeHistory
            );

            templeImportance = itemView.findViewById(
                    R.id.templeImportance
            );

            templeDeity = itemView.findViewById(
                    R.id.templeDeity
            );

            templeFestivals = itemView.findViewById(
                    R.id.templeFestivals
            );

            templeTraditions = itemView.findViewById(
                    R.id.templeTraditions
            );

            templeArchitecture = itemView.findViewById(
                    R.id.templeArchitecture
            );

            templeStories = itemView.findViewById(
                    R.id.templeStories
            );
        }
    }
}
