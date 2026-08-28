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

public class DeviDevtaAdapter
        extends RecyclerView.Adapter<DeviDevtaAdapter.DeviDevtaViewHolder> {

    private final Context context;
    private final ArrayList<DeviDevta> deviDevtaList;

    public DeviDevtaAdapter(
            Context context,
            ArrayList<DeviDevta> deviDevtaList) {

        this.context = context;
        this.deviDevtaList = deviDevtaList;
    }

    @NonNull
    @Override
    public DeviDevtaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_devi_devta, parent, false);

        return new DeviDevtaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull DeviDevtaViewHolder holder,
            int position) {

        DeviDevta item = deviDevtaList.get(position);

        holder.deviDevtaImage.setImageResource(
                item.getImageResId());

        holder.deviDevtaName.setText(
                item.getName());

        holder.deviDevtaDescription.setText(
                item.getDescription());
    }

    @Override
    public int getItemCount() {
        return deviDevtaList.size();
    }

    public static class DeviDevtaViewHolder
            extends RecyclerView.ViewHolder {

        ImageView deviDevtaImage;
        TextView deviDevtaName;
        TextView deviDevtaDescription;

        public DeviDevtaViewHolder(@NonNull View itemView) {
            super(itemView);

            deviDevtaImage = itemView.findViewById(
                    R.id.deviDevtaImage);

            deviDevtaName = itemView.findViewById(
                    R.id.deviDevtaName);

            deviDevtaDescription = itemView.findViewById(
                    R.id.deviDevtaDescription);
        }
    }
}
