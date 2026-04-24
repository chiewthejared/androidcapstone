package com.swf.capstone26.calendar;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.swf.capstone26.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarMonthAdapter extends RecyclerView.Adapter<CalendarMonthAdapter.DayViewHolder> {

    public interface OnDateClickListener {
        void onDateClick(LocalDate date);
    }

    private List<CalendarDay> days = new ArrayList<>();
    private LocalDate selectedDate;
    private final OnDateClickListener listener;

    public CalendarMonthAdapter(OnDateClickListener listener) {
        this.listener = listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void submit(List<CalendarDay> newDays, LocalDate selected) {
        this.days = newDays;
        this.selectedDate = selected;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(days.get(position), selectedDate, listener, this);
    }

    @Override
    public int getItemCount() { return days.size(); }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setSelectedDate(LocalDate date) {
        LocalDate old = this.selectedDate;
        this.selectedDate = date;
        for (int i = 0; i < days.size(); i++) {
            LocalDate d = days.get(i).getDate();
            if (d != null && (d.equals(old) || d.equals(date))) {
                notifyItemChanged(i);
            }
        }
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        private final TextView calendarDayText;
        private final View viewSelectedCircle;
        private final View viewDotBlue;
        private final View viewDotRed;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            calendarDayText = itemView.findViewById(R.id.calendarDayText);
            viewSelectedCircle = itemView.findViewById(R.id.viewSelectedCircle);
            viewDotBlue = itemView.findViewById(R.id.viewDotBlue);
            viewDotRed = itemView.findViewById(R.id.viewDotRed);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void bind(CalendarDay day, LocalDate selected,
                         OnDateClickListener listener, CalendarMonthAdapter adapter) {
            if (day.getDate() == null || !day.isCurrentMonth()) {
                calendarDayText.setText("");
                calendarDayText.setEnabled(false);
                viewSelectedCircle.setVisibility(View.GONE);
                viewDotBlue.setVisibility(View.GONE);
                viewDotRed.setVisibility(View.GONE);
                itemView.setOnClickListener(null);
            } else {
                calendarDayText.setText(String.valueOf(day.getDate().getDayOfMonth()));
                calendarDayText.setEnabled(true);

                // 蓝点：有普通 event
                viewDotBlue.setVisibility(day.hasRecord() ? View.VISIBLE : View.GONE);
                // 红点：有癫痫记录
                viewDotRed.setVisibility(day.hasSeizure() ? View.VISIBLE : View.GONE);

                viewSelectedCircle.setVisibility(
                        day.getDate().equals(selected) ? View.VISIBLE : View.GONE
                );

                itemView.setOnClickListener(v -> {
                    adapter.setSelectedDate(day.getDate());
                    if (listener != null) listener.onDateClick(day.getDate());
                });
            }
        }
    }
}