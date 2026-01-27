package com.example.test_v2.calendar;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.test_v2.R;
import com.example.test_v2.calendar.MonthlyFragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarDayAdapter extends BaseAdapter {
    private final Context context;
    private final List<String> days;
    private final List<String> eventDates;
    private final LocalDate currentMonth;
    private final OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClicked(String fullDate); // yyyy-M-d
    }

    public CalendarDayAdapter(Context context, List<String> days, List<String> eventDates,
                              LocalDate currentMonth, OnDayClickListener listener) {
        this.context = context;
        this.days = days;
        this.eventDates = eventDates;
        this.currentMonth = currentMonth;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return days.size();
    }

    @Override
    public Object getItem(int position) {
        return days.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView != null
                ? convertView
                : LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);

        TextView dayText = view.findViewById(R.id.calendarDayText);
        String day = days.get(position);
        dayText.setText(day);

        if (!day.isEmpty()) {
            LocalDate fullDate = currentMonth.withDayOfMonth(Integer.parseInt(day));
            String formattedDate = fullDate.format(DateTimeFormatter.ofPattern("yyyy-M-d"));

            if (eventDates.contains(formattedDate)) {
                view.setBackgroundResource(R.drawable.bg_event_highlight);
            } else {
                view.setBackgroundResource(android.R.color.transparent);
            }

            view.setOnClickListener(v -> listener.onDayClicked(formattedDate));
        } else {
            dayText.setText("");
            dayText.setBackgroundColor(0x00000000);
            view.setOnClickListener(null); // disable click for empty cells
        }

        return view;
    }
}
