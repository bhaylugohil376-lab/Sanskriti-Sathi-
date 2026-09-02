package com.sanskritisathi.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TempleAdapter extends RecyclerView.Adapter<TempleAdapter.TempleViewHolder> {

    private final List<Temple> templeList;

    public TempleAdapter(List<Temple> templeList) {
        this.templeList = templeList;
    }

    @NonNull
    @Override
    public TempleViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_temple, parent, false);

        return new TempleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TempleViewHolder holder,
            int position) {

        Temple temple = templeList.get(position);

        holder.templeName.setText(temple.getName());
        holder.templeLocation.setText(temple.getLocation());
        holder.templeDescription.setText(temple.getDescription());

        if (temple.getImageResId() != 0) {
            holder.templeImage.setImageResource(temple.getImageResId());
        }
    }

    @Override
    public int getItemCount() {
        return templeList.size();
    }

    public static class TempleViewHolder extends RecyclerView.ViewHolder {

        ImageView templeImage;
        TextView templeName;
        TextView templeLocation;
        TextView templeDescription;

        public TempleViewHolder(@NonNull View itemView) {
            super(itemView);

            templeImage = itemView.findViewById(R.id.templeImage);
            templeName = itemView.findViewById(R.id.templeName);
            templeLocation = itemView.findViewById(R.id.templeLocation);
            templeDescription = itemView.findViewById(R.id.templeDescription);
        }
    }
}
