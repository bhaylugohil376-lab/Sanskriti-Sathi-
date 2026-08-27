package com.sanskritisathi.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RajaAdapter extends RecyclerView.Adapter<RajaAdapter.RajaViewHolder> {

    private Context context;
    private ArrayList<Raja> rajaList;

    public RajaAdapter(Context context, ArrayList<Raja> rajaList) {
        this.context = context;
        this.rajaList = rajaList;
    }

    @Override
    public RajaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_raja, parent, false);

        return new RajaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RajaViewHolder holder, int position) {
        Raja raja = rajaList.get(position);

        holder.nameText.setText(raja.getName());
        holder.descriptionText.setText(raja.getDescription());
    }

    @Override
    public int getItemCount() {
        return rajaList.size();
    }

    static class RajaViewHolder extends RecyclerView.ViewHolder {

        TextView nameText;
        TextView descriptionText;

        public RajaViewHolder(View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.rajaName);
            descriptionText = itemView.findViewById(R.id.rajaDescription);
        }
    }
}
