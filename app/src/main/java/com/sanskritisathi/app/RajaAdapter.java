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
import java.util.Locale;

public class RajaAdapter extends RecyclerView.Adapter<RajaAdapter.RajaViewHolder> {

    private final Context context;

    // Original complete list
    private final ArrayList<Raja> originalList;

    // Currently displayed list
    private final ArrayList<Raja> rajaList;

    // Click listener
    private final OnRajaClickListener listener;

    public interface OnRajaClickListener {
        void onRajaClick(Raja raja);
    }

    // Constructor used by RajaActivity
    public RajaAdapter(
            Context context,
            ArrayList<Raja> rajaList,
            OnRajaClickListener listener) {

        this.context = context;
        this.originalList = new ArrayList<>(rajaList);
        this.rajaList = new ArrayList<>(rajaList);
        this.listener = listener;
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

        holder.rajaImage.setImageResource(
                raja.getImageResId());

        holder.rajaName.setText(
                raja.getName());

        holder.rajaDynasty.setText(
                "वंश: " + raja.getDynasty());

        holder.rajaPeriod.setText(
                "काल: " + raja.getPeriod());

        holder.rajaKingdom.setText(
                "राज्य: " + raja.getKingdom());

        holder.rajaCapital.setText(
                "राजधानी: " + raja.getCapital());

        holder.rajaHistory.setText(
                raja.getHistory());

        holder.rajaWars.setText(
                "⚔️ युद्ध एवं संघर्ष\n" + raja.getWars());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRajaClick(raja);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rajaList.size();
    }

    // =========================================================
    // SEARCH FILTER
    // =========================================================

    public void filterList(String query) {

        String searchText = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        rajaList.clear();

        if (searchText.isEmpty()) {

            rajaList.addAll(originalList);

        } else {

            for (Raja raja : originalList) {

                String name = raja.getName() == null
                        ? ""
                        : raja.getName().toLowerCase(Locale.ROOT);

                String dynasty = raja.getDynasty() == null
                        ? ""
                        : raja.getDynasty().toLowerCase(Locale.ROOT);

                String kingdom = raja.getKingdom() == null
                        ? ""
                        : raja.getKingdom().toLowerCase(Locale.ROOT);

                String capital = raja.getCapital() == null
                        ? ""
                        : raja.getCapital().toLowerCase(Locale.ROOT);

                if (name.contains(searchText)
                        || dynasty.contains(searchText)
                        || kingdom.contains(searchText)
                        || capital.contains(searchText)) {

                    rajaList.add(raja);
                }
            }
        }

        notifyDataSetChanged();
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public static class RajaViewHolder
            extends RecyclerView.ViewHolder {

        ImageView rajaImage;
        TextView rajaName;
        TextView rajaDynasty;
        TextView rajaPeriod;
        TextView rajaKingdom;
        TextView rajaCapital;
        TextView rajaHistory;
        TextView rajaWars;

        public RajaViewHolder(@NonNull View itemView) {
            super(itemView);

            rajaImage = itemView.findViewById(R.id.rajaImage);
            rajaName = itemView.findViewById(R.id.rajaName);
            rajaDynasty = itemView.findViewById(R.id.rajaDynasty);
            rajaPeriod = itemView.findViewById(R.id.rajaPeriod);
            rajaKingdom = itemView.findViewById(R.id.rajaKingdom);
            rajaCapital = itemView.findViewById(R.id.rajaCapital);
            rajaHistory = itemView.findViewById(R.id.rajaHistory);
            rajaWars = itemView.findViewById(R.id.rajaWars);
        }
    }
}
