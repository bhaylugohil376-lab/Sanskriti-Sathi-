package com.sanskritisathi.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RajaAdapter extends RecyclerView.Adapter<RajaAdapter.RajaViewHolder> {

    private final List<Raja> rajaList;

    public RajaAdapter(List<Raja> rajaList) {
        this.rajaList = rajaList;
    }

    @NonNull
    @Override
    public RajaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_raja, parent, false);

        return new RajaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RajaViewHolder holder, int position) {
        Raja raja = rajaList.get(position);

        holder.rajaName.setText(raja.getName());
        holder.rajaDynasty.setText(raja.getDynasty());
        holder.rajaPeriod.setText(raja.getPeriod());
        holder.rajaDescription.setText(raja.getDescription());
    }

    @Override
    public int getItemCount() {
        return rajaList.size();
    }

    public static class RajaViewHolder extends RecyclerView.ViewHolder {

        TextView rajaName;
        TextView rajaDynasty;
        TextView rajaPeriod;
        TextView rajaDescription;

        public RajaViewHolder(@NonNull View itemView) {
            super(itemView);

            rajaName = itemView.findViewById(R.id.rajaName);
            rajaDynasty = itemView.findViewById(R.id.rajaDynasty);
            rajaPeriod = itemView.findViewById(R.id.rajaPeriod);
            rajaDescription = itemView.findViewById(R.id.rajaDescription);
        }
    }
}
