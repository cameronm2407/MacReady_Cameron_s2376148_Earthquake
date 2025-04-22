// Name                 Cameron MacReady
// Student ID           s2376148
// Programme of Study   BSc Computing

package com.example.earthquake;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EarthquakeAdapter extends RecyclerView.Adapter<EarthquakeAdapter.ViewHolder> {

    private final List<EarthquakeItem> earthquakeList;
    private final OnEarthquakeClickListener listener;

    // Constructor now takes the click listener interface instead of Context
    public EarthquakeAdapter(List<EarthquakeItem> earthquakeList, OnEarthquakeClickListener listener) {
        this.earthquakeList = earthquakeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EarthquakeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_earthquake, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EarthquakeAdapter.ViewHolder holder, int position) {
        EarthquakeItem eq = earthquakeList.get(position);

        holder.eqLocation.setText(eq.getLocation());
        holder.eqMagnitude.setText("Magnitude - " + eq.getMagnitude());
        holder.eqDepth.setText("Depth: " + eq.getDepth() + " km");
        holder.eqDate.setText(eq.getPubDate());

        double magnitude = eq.getMagnitude();
        int color;

        if (magnitude < 1.0) {
            color = Color.parseColor("#E0F7FA"); // light blue
        } else if (magnitude < 2.0) {
            color = Color.parseColor("#FFF9C4"); // light yellow
        } else if (magnitude < 3.0) {
            color = Color.parseColor("#FFE082"); // light orange
        } else if (magnitude < 4.0) {
            color = Color.parseColor("#FFAB91"); // soft red
        } else {
            color = Color.parseColor("#EF5350"); // bright red
        }

        holder.containerLayout.setBackgroundColor(color);
        // Use the listener instead of launching Intent directly
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEarthquakeClick(eq);
            }
        });
    }

    @Override
    public int getItemCount() {
        return earthquakeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eqLocation, eqMagnitude, eqDepth, eqDate;
        View containerLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eqLocation = itemView.findViewById(R.id.eqLocation);
            eqMagnitude = itemView.findViewById(R.id.eqMagnitude);
            eqDate = itemView.findViewById(R.id.eqDate);
            eqDepth = itemView.findViewById(R.id.eqDepth);
            containerLayout = itemView.findViewById(R.id.containerLayout);
        }
    }

    // 👇👇👇 Add this interface for click handling
    public interface OnEarthquakeClickListener {
        void onEarthquakeClick(EarthquakeItem earthquake);
    }
}
