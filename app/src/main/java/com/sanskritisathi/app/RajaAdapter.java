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

public class RajaAdapter extends RecyclerView.Adapter<RajaAdapter.RajaViewHolder> {

    private final Context context;
    private final ArrayList<Raja> rajaList;

    public RajaAdapter(Context context, ArrayList<Raja> rajaList) {
        this.context = context;
        this.rajaList = rajaList;
    }

    @NonNull
    @Override
    public RajaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_raja, parent, false);

        return new RajaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RajaViewHolder holder,
            int position) {

        Raja raja = rajaList.get(position);

        // Large Raja image
        holder.rajaImage.setImageResource(
                raja.getImageResId());

        // English Raja name
        holder.rajaName.setText(
                raja.getName());

        // Period
        holder.rajaPeriod.setText(
                "Period: " + raja.getPeriod());

        // Detailed Hindi history
        holder.rajaHistory.setText(
                raja.getHistory());

        // Wars / opponents
        holder.rajaWars.setText(
                "युद्ध एवं संघर्ष\n" + raja.getWars());
    }

    @Override
    public int getItemCount() {
        return rajaList.size();
    }

    public static class RajaViewHolder
            extends RecyclerView.ViewHolder {

        ImageView rajaImage;
        TextView rajaName;
        TextView rajaPeriod;
        TextView rajaHistory;
        TextView rajaWars;

        public RajaViewHolder(@NonNull View itemView) {
            super(itemView);

            rajaImage = itemView.findViewById(
                    R.id.rajaImage);

            rajaName = itemView.findViewById(
                    R.id.rajaName);

            rajaPeriod = itemView.findViewById(
                    R.id.rajaPeriod);

            rajaHistory = itemView.findViewById(
                    R.id.rajaHistory);

            rajaWars = itemView.findViewById(
                    R.id.rajaWars);
        }
    }
}
