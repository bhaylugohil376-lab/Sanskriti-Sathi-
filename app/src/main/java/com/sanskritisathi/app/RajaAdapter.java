package com.sanskritisathi.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RajaAdapter extends RecyclerView.Adapter<RajaAdapter.RajaViewHolder> {

    private final List<Raja> rajaList;
    private final List<Raja> allRajas;

    public RajaAdapter(List<Raja> rajaList) {
        this.rajaList = rajaList;
        this.allRajas = new ArrayList<>(rajaList);
    }

    // 🔍 Raja search/filter
    public void filter(String text) {

        String query = text == null ? "" : text.trim().toLowerCase();

        rajaList.clear();

        if (query.isEmpty()) {
            rajaList.addAll(allRajas);
        } else {

            for (Raja raja : allRajas) {

                String name = raja.getName();
                String dynasty = raja.getDynasty();
                String period = raja.getPeriod();
                String kingdom = raja.getKingdom();
                String capital = raja.getCapital();
                String description = raja.getDescription();

                if ((name != null && name.toLowerCase().contains(query))
                        || (dynasty != null && dynasty.toLowerCase().contains(query))
                        || (period != null && period.toLowerCase().contains(query))
                        || (kingdom != null && kingdom.toLowerCase().contains(query))
                        || (capital != null && capital.toLowerCase().contains(query))
                        || (description != null && description.toLowerCase().contains(query))) {

                    rajaList.add(raja);
                }
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RajaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_raja, parent, false);

        return new RajaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RajaViewHolder holder,
            int position) {

        Raja raja = rajaList.get(position);

        holder.rajaName.setText(raja.getName());
        holder.rajaDynasty.setText(raja.getDynasty());
        holder.rajaPeriod.setText(raja.getPeriod());
        holder.rajaKingdom.setText("🏰 राज्य: " + raja.getKingdom());
        holder.rajaCapital.setText("📍 राजधानी: " + raja.getCapital());
        holder.rajaDescription.setText(raja.getDescription());
    }

    @Override
    public int getItemCount() {
        return rajaList.size();
    }

    static class RajaViewHolder extends RecyclerView.ViewHolder {

        TextView rajaName;
        TextView rajaDynasty;
        TextView rajaPeriod;
        TextView rajaKingdom;
        TextView rajaCapital;
        TextView rajaDescription;

        public RajaViewHolder(@NonNull View itemView) {
            super(itemView);

            rajaName = itemView.findViewById(R.id.rajaName);
            rajaDynasty = itemView.findViewById(R.id.rajaDynasty);
            rajaPeriod = itemView.findViewById(R.id.rajaPeriod);
            rajaKingdom = itemView.findViewById(R.id.rajaKingdom);
            rajaCapital = itemView.findViewById(R.id.rajaCapital);
            rajaDescription = itemView.findViewById(R.id.rajaDescription);
        }
    }
}
