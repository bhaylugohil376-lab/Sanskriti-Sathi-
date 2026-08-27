package com.sanskritisathi.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TempleAdapter extends RecyclerView.Adapter<TempleAdapter.TempleViewHolder> {

    private Context context;
    private ArrayList<Temple> templeList;

    public TempleAdapter(Context context, ArrayList<Temple> templeList) {
        this.context = context;
        this.templeList = templeList;
    }

    @Override
    public TempleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_temple, parent, false);

        return new TempleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TempleViewHolder holder, int position) {

        Temple temple = templeList.get(position);

        holder.templeImage.setImageResource(temple.getImageResId());
        holder.templeName.setText(temple.getName());
        holder.templeDescription.setText(temple.getDescription());
    }

    @Override
    public int getItemCount() {
        return templeList.size();
    }

    static class TempleViewHolder extends RecyclerView.ViewHolder {

        ImageView templeImage;
        TextView templeName;
        TextView templeDescription;

        public TempleViewHolder(View itemView) {
            super(itemView);

            templeImage = itemView.findViewById(R.id.templeImage);
            templeName = itemView.findViewById(R.id.templeName);
            templeDescription = itemView.findViewById(R.id.templeDescription);
        }
    }
}
