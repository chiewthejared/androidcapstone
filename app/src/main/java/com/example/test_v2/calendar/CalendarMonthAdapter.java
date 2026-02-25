package com.example.test_v2.calendar;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;

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
        CalendarDay day = days.get(position);
        holder.bind(day, selectedDate, listener);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        private final TextView calendarDayText;
        private final View viewSelectedCircle;
        private final View viewDot;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            calendarDayText = itemView.findViewById(R.id.calendarDayText);
            viewSelectedCircle = itemView.findViewById(R.id.viewSelectedCircle);
            viewDot = itemView.findViewById(R.id.viewDot);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void bind(CalendarDay day, LocalDate selected, OnDateClickListener listener) {
            if (day.getDate() == null || !day.isCurrentMonth()) {
                calendarDayText.setText("");
                calendarDayText.setEnabled(false);
                viewSelectedCircle.setVisibility(View.GONE);
                viewDot.setVisibility(View.GONE);
                itemView.setOnClickListener(null);
            } else {
                calendarDayText.setText(String.valueOf(day.getDate().getDayOfMonth()));
                calendarDayText.setEnabled(true);

                viewDot.setVisibility(day.hasRecord() ? View.VISIBLE : View.GONE);

                if (day.getDate().equals(selected)) {
                    viewSelectedCircle.setVisibility(View.VISIBLE);
                } else {
                    viewSelectedCircle.setVisibility(View.GONE);
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDateClick(day.getDate());
                    }
                });
            }
        }
    }
}