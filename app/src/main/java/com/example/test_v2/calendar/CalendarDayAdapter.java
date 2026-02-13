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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarDayAdapter extends BaseAdapter {
    private final Context context;
    private final List<String> days;
    private final List<String> eventDates;
    private final LocalDate currentMonth;
    private final OnDayClickListener listener;

    private String selectedDate;

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

        // 默认选中今天（如果不在本月，后面 MonthlyFragment 会覆盖）
        this.selectedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d"));
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }

    @Override public int getCount() { return days.size(); }
    @Override public Object getItem(int position) { return days.get(position); }
    @Override public long getItemId(int position) { return position; }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView != null
                ? convertView
                : LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);

        TextView dayText = view.findViewById(R.id.calendarDayText);
        View dot = view.findViewById(R.id.viewDot);
        View selectedCircle = view.findViewById(R.id.viewSelectedCircle);

        String day = days.get(position);

        if (day == null || day.isEmpty()) {
            dayText.setText("");
            dot.setVisibility(View.GONE);
            selectedCircle.setVisibility(View.GONE);
            view.setOnClickListener(null);
            return view;
        }

        dayText.setText(day);

        LocalDate fullDate = currentMonth.withDayOfMonth(Integer.parseInt(day));
        String formattedDate = fullDate.format(DateTimeFormatter.ofPattern("yyyy-M-d"));

        boolean hasEvent = eventDates.contains(formattedDate);
        dot.setVisibility(hasEvent ? View.VISIBLE : View.GONE);

        boolean isSelected = formattedDate.equals(selectedDate);
        selectedCircle.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        dayText.setTextColor(isSelected ? 0xFFFFFFFF : 0xFF111827);

        view.setOnClickListener(v -> {
            selectedDate = formattedDate;
            notifyDataSetChanged();
            listener.onDayClicked(formattedDate);
        });

        return view;
    }
}