package com.swf.capstone26.tiles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.swf.capstone26.R;

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

    // inside TilesAdapter.java, onBindViewHolder(...)
    @Override
    public void onBindViewHolder(@NonNull TileViewHolder holder, int position) {
        TileItem item = items.get(position);

        // Force the ImageView to the desired dp size at runtime
        int desiredDp = 95; // change to taste (120/140/160)
        int px = dpToPx(holder.itemView.getContext(), desiredDp);

        ViewGroup.LayoutParams lp = holder.icon.getLayoutParams();
        lp.width = px;
        lp.height = px;
        holder.icon.setLayoutParams(lp);

        // Ensure scaleType is correct so the drawable fills the view as expected
        holder.icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        holder.icon.setAdjustViewBounds(true);
        holder.icon.setPadding(0,0,0,0);

        holder.icon.setImageResource(item.getIconRes());
        holder.label.setText(item.getLabel());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTileClick(position);
        });
    }

    // helper method (put this in the adapter class)
    private static int dpToPx(Context c, int dp) {
        return Math.round(dp * (c.getResources().getDisplayMetrics().density));
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