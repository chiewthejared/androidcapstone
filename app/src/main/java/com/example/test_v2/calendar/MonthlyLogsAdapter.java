package com.example.test_v2.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;

import java.util.ArrayList;
import java.util.List;

public class MonthlyLogsAdapter extends RecyclerView.Adapter<MonthlyLogsAdapter.VH> {

    public interface OnLogClickListener {
        void onLogClick(HelperEvent event);
    }

    private final List<HelperEvent> items = new ArrayList<>();
    private final OnLogClickListener listener;

    public MonthlyLogsAdapter(OnLogClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<HelperEvent> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monthly_log, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        HelperEvent e = items.get(position);
        h.tvTitle.setText(e.getTitle());
        h.tvTime.setText(e.getStartTime() + " - " + e.getEndTime());
        h.tvTag.setText(e.getTag() == null ? "" : e.getTag());

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLogClick(e);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvTag;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvLogTitle);
            tvTime = itemView.findViewById(R.id.tvLogTime);
            tvTag = itemView.findViewById(R.id.tvLogTag);
        }
    }
}
