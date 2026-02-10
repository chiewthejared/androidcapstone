package com.example.test_v2.calendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;
import com.example.test_v2.fileAndDatabase.HelperAppDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import android.widget.ImageButton;

public class MonthlyFragment extends Fragment {

    private TextView monthYearText;
    private GridView calendarGrid;
    private ImageButton prevMonthButton, nextMonthButton;

    private TextView tvLogsTitle;
    private RecyclerView rvLogs;
    private MonthlyLogsAdapter logsAdapter;

    private HelperEventDao eventDao;

    public static LocalDate currentMonth = LocalDate.now();
    private String selectedDateStr; // yyyy-M-d
    private final List<String> eventDates = new ArrayList<>();

    private final DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy");
    private final DateTimeFormatter logTitleFmt = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly, container, false);

        monthYearText = view.findViewById(R.id.monthYearText);
        calendarGrid = view.findViewById(R.id.calendarGrid);
        prevMonthButton = view.findViewById(R.id.prevMonthButton);
        nextMonthButton = view.findViewById(R.id.nextMonthButton);

        tvLogsTitle = view.findViewById(R.id.tvLogsTitle);
        rvLogs = view.findViewById(R.id.rvLogs);

        logsAdapter = new MonthlyLogsAdapter(this::openEventDetail);
        rvLogs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLogs.setAdapter(logsAdapter);

        HelperAppDatabase db = HelperAppDatabase.getDatabase(getContext());
        eventDao = db.eventDao();

        // 读取上次选择日期（如果没有就今天）
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String passedDate = prefs.getString("selectedDate", null);
        if (passedDate != null) {
            selectedDateStr = passedDate;
            currentMonth = LocalDate.parse(passedDate); // 用它定位月份
        } else {
            selectedDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d"));
        }

        monthYearText.setText(currentMonth.format(monthFmt));
        monthYearText.setOnClickListener(v -> showMonthYearPicker());

        prevMonthButton.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1).withDayOfMonth(1);
            loadCalendar();
        });

        nextMonthButton.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1).withDayOfMonth(1);
            loadCalendar();
        });

        loadCalendar();
        loadLogsForSelectedDay(); // 先加载一次下方列表

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadCalendar() {
        monthYearText.setText(currentMonth.format(monthFmt));

        eventDao.getAllEvents().observe(getViewLifecycleOwner(), allEvents -> {
            eventDates.clear();

            String myPin = requireContext()
                    .getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    .getString("loggedInPin", "");

            for (HelperEvent event : allEvents) {
                if (event.getUserSession() != null && event.getUserSession().equals(myPin)) {
                    // 只存日期字符串：yyyy-M-d
                    eventDates.add(event.getDate());
                }
            }

            List<String> dayCells = buildDayCells(currentMonth);

            CalendarDayAdapter adapter = new CalendarDayAdapter(
                    requireContext(),
                    dayCells,
                    eventDates,
                    currentMonth,
                    fullDate -> {
                        // fullDate 形如 yyyy-M-d
                        selectedDateStr = fullDate;

                        // 存一下（可选）
                        requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                                .edit().putString("selectedDate", selectedDateStr).apply();

                        // 刷新下方 logs
                        loadLogsForSelectedDay();
                    }
            );

            calendarGrid.setAdapter(adapter);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<String> buildDayCells(LocalDate month) {
        List<String> cells = new ArrayList<>();
        LocalDate firstOfMonth = month.withDayOfMonth(1);

        // Java: MONDAY=1 ... SUNDAY=7
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        int shift = dayOfWeek % 7; // Sunday(7)->0, Monday(1)->1...

        for (int i = 0; i < shift; i++) cells.add("");

        int lengthOfMonth = month.lengthOfMonth();
        for (int i = 1; i <= lengthOfMonth; i++) cells.add(String.valueOf(i));

        // 补齐到完整周（否则底部不齐）
        int remainder = cells.size() % 7;
        if (remainder != 0) {
            int trailing = 7 - remainder;
            for (int i = 0; i < trailing; i++) cells.add("");
        }

        return cells;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadLogsForSelectedDay() {
        LocalDate sel = LocalDate.parse(selectedDateStr, DateTimeFormatter.ofPattern("yyyy-M-d"));
        tvLogsTitle.setText("Logs on " + sel.format(logTitleFmt));

        eventDao.getAllEvents().observe(getViewLifecycleOwner(), allEvents -> {
            String myPin = requireContext()
                    .getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    .getString("loggedInPin", "");

            List<HelperEvent> dayEvents = new ArrayList<>();
            for (HelperEvent e : allEvents) {
                if (e.getUserSession() != null
                        && e.getUserSession().equals(myPin)
                        && selectedDateStr.equals(e.getDate())) {
                    dayEvents.add(e);
                }
            }

            logsAdapter.submit(dayEvents);
        });
    }

    private void showMonthYearPicker() {
        MonthYearPickerDialog pickerDialog = new MonthYearPickerDialog((year, month) -> {
            // month: 0-11
            currentMonth = LocalDate.of(year, month + 1, 1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                loadCalendar();
            }
        });

        pickerDialog.show(getParentFragmentManager(), "MonthYearPickerDialog");
    }

    private void openEventDetail(HelperEvent event) {
        Intent intent = new Intent(getContext(), EventDetailsActivity.class);
        intent.putExtra("eventId", event.getID());
        startActivity(intent);
    }
}