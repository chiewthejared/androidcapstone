package com.example.test_v2.tiles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.test_v2.R;

import java.util.List;

public class TilesAdapter extends RecyclerView.Adapter<TilesAdapter.TileViewHolder> {

    public interface OnTileClickListener { void onTileClick(int position); }

    private final List<TileItem> items;
    private final Context context;
    private final OnTileClickListener listener;

    public TilesAdapter(List<TileItem> items, Context context, OnTileClickListener listener) {
        this.items = items;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_tile, parent, false);
        return new TileViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TileViewHolder holder, int position) {
        TileItem item = items.get(position);
        holder.icon.setImageResource(item.getIconRes());
        holder.label.setText(item.getLabel());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTileClick(position);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class TileViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView label;
        public TileViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_icon);
            label = itemView.findViewById(R.id.tv_label);
        }
    }
}