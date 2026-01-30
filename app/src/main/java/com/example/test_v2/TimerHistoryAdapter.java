package com.example.test_v2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.timer.HelperTimerEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter to show timer history items.
 * Expects item layout ids:
 *  - select_checkbox, date_label, start_time, end_time, duration_text, delete_btn, add_calendar_btn
 */
public class TimerHistoryAdapter extends RecyclerView.Adapter<TimerHistoryAdapter.VH> {

    public interface OnItemActionListener {
        void onEdit(HelperTimerEvent event);          // edit notes / open editor
        void onDelete(HelperTimerEvent event, int pos); // delete single event
        void onAddToCalendar(HelperTimerEvent event);  // add to calendar
    }

    private final List<HelperTimerEvent> items = new ArrayList<>();
    private final Context ctx;
    private final OnItemActionListener listener;

    public TimerHistoryAdapter(Context ctx, OnItemActionListener listener) {
        this.ctx = ctx;
        this.listener = listener;
    }

    public void setEvents(List<HelperTimerEvent> events) {
        items.clear();
        if (events != null) items.addAll(events);
        notifyDataSetChanged();
    }

    public void removeAt(int pos) {
        if (pos < 0 || pos >= items.size()) return;
        items.remove(pos);
        notifyItemRemoved(pos);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timer_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        HelperTimerEvent e = items.get(position);
        Date start = new Date(e.startTimestamp);
        long endMillis = e.startTimestamp + Math.max(0, e.totalTimeMs);
        Date end = new Date(endMillis);

        holder.dateLabel.setText(prettyDate(start));
        holder.startTime.setText(timeOnly(start));
        holder.endTime.setText(timeOnly(end));
        holder.durationText.setText("Duration: " + humanDuration(e.totalTimeMs));

        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(e, holder.getAdapterPosition());
        });

        holder.addCalendarBtn.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCalendar(e);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onEdit(e);
            return true;
        });

        holder.checkBox.setChecked(false);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView dateLabel, startTime, endTime, durationText;
        ImageButton deleteBtn;
        Button addCalendarBtn;

        VH(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.select_checkbox);
            dateLabel = itemView.findViewById(R.id.date_label);
            startTime = itemView.findViewById(R.id.start_time);
            endTime = itemView.findViewById(R.id.end_time);
            durationText = itemView.findViewById(R.id.duration_text);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
            addCalendarBtn = itemView.findViewById(R.id.add_calendar_btn);
        }
    }

    // --- Helpers ---
    private String timeOnly(Date d) {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(d);
    }

    private String prettyDate(Date d) {
        Calendar cal = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d);
        if (cal.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
            return "Today";
        }
        return new SimpleDateFormat("MMM d", Locale.getDefault()).format(d);
    }

    private String humanDuration(long ms) {
        long s = ms / 1000;
        if (s < 60) return s + "s";
        long m = s / 60;
        s = s % 60;
        if (m < 60) return String.format(Locale.getDefault(), "%dm %02ds", m, s);
        long h = m / 60;
        m = m % 60;
        return String.format(Locale.getDefault(), "%dh %02dm", h, m);
    }
}
