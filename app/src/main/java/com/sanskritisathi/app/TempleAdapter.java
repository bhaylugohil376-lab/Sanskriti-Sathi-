package com.sanskritisathi.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TempleAdapter
        extends RecyclerView.Adapter<TempleAdapter.TempleViewHolder> {

    private final Context context;
    private final ArrayList<Temple> templeList;

    public TempleAdapter(
            Context context,
            ArrayList<Temple> templeList) {

        this.context = context;
        this.templeList = templeList;
    }

    @NonNull
    @Override
    public TempleViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_temple, parent, false);

        return new TempleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TempleViewHolder holder,
            int position) {

        Temple temple = templeList.get(position);

        holder.templeImage.setImageResource(
                temple.getImageResId());

        holder.templeName.setText(
                temple.getName());

        holder.templeLocation.setText(
                "📍 " + temple.getLocation());

        holder.templeDescription.setText(
                temple.getDescription());
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
        TextView templeDescription;

        public TempleViewHolder(@NonNull View itemView) {
            super(itemView);

            templeImage = itemView.findViewById(
                    R.id.templeImage);

            templeName = itemView.findViewById(
                    R.id.templeName);

            templeLocation = itemView.findViewById(
                    R.id.templeLocation);

            templeDescription = itemView.findViewById(
                    R.id.templeDescription);
        }
    }
}
