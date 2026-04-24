package com.swf.capstone26.calendar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.swf.capstone26.R;
import com.swf.capstone26.fileAndDatabase.HelperAppDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalendarPage extends AppCompatActivity {

    private TextView tvMonthCenter;
    private RecyclerView rvCalendar;
    private RecyclerView rvTodayLogs;
    private TextView tvLogsTitle;
    private TextView tvRecordsBadge;

    private CalendarMonthAdapter calendarAdapter;
    private MonthlyLogsAdapter logsAdapter;

    private HelperEventDao eventDao;

    private YearMonth currentMonth;
    private LocalDate selectedDate;

    private final Set<LocalDate> daysWithRecords = new HashSet<>();
    private final Set<LocalDate> seizureDays = new HashSet<>();
    private final DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy");

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_page);


        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvMonth = findViewById(R.id.tvMonth);
        tvMonth.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        btnBack.setOnClickListener(v -> finish());

        tvMonthCenter = findViewById(R.id.tvMonthCenter);
        rvCalendar = findViewById(R.id.rvCalendar);
        ImageButton btnPrev = findViewById(R.id.btnPrevMonth);
        ImageButton btnNext = findViewById(R.id.btnNextMonth);
        tvLogsTitle = findViewById(R.id.tvLogsTitle);
        rvTodayLogs = findViewById(R.id.rvTodayLogs);
        tvRecordsBadge = findViewById(R.id.btnRecords);

        selectedDate = LocalDate.now();
        currentMonth = YearMonth.from(selectedDate);

        // 日历 adapter
        calendarAdapter = new CalendarMonthAdapter(date -> {
            selectedDate = date;
            updateLogsTitle();
            loadLogsFor(date);
        });
        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        rvCalendar.setAdapter(calendarAdapter);

        // Logs adapter
        logsAdapter = new MonthlyLogsAdapter(event -> {
            Intent intent = new Intent(this, EventDetailsActivity.class);
            intent.putExtra("eventId", event.getID());
            startActivity(intent);
        });
        rvTodayLogs.setLayoutManager(new LinearLayoutManager(this));
        rvTodayLogs.setAdapter(logsAdapter);

        // 上下月切换
        btnPrev.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            renderMonth();
        });
        btnNext.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            renderMonth();
        });

        // + 按钮跳转新建 Event
        FloatingActionButton fabAdd = findViewById(R.id.fabAddEvent);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditEventActivity.class);
            String dateStr = selectedDate.getYear() + "-"
                    + selectedDate.getMonthValue() + "-"
                    + selectedDate.getDayOfMonth();
            intent.putExtra("prefilledDate", dateStr);
            startActivity(intent);
        });

        // 连接数据库
        HelperAppDatabase db = HelperAppDatabase.getDatabase(this);
        eventDao = db.eventDao();
        String myPin = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("loggedInPin", "");

        // 监听所有 events，区分普通（蓝点）和癫痫（红点）
        eventDao.getAllEvents().observe(this, allEvents -> {
            daysWithRecords.clear();
            seizureDays.clear();
            int count = 0;

            for (HelperEvent event : allEvents) {
                if (event.getUserSession() == null
                        || !event.getUserSession().equals(myPin)) continue;
                try {
                    String[] parts = event.getDate().split("-");
                    LocalDate date = LocalDate.of(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2])
                    );
                    // tag 是 "Seizure" 的走红点，其余走蓝点
                    if ("Seizure".equals(event.getTag())) {
                        seizureDays.add(date);
                    } else {
                        daysWithRecords.add(date);
                    }
                    count++;
                } catch (Exception ignored) {}
            }

            tvRecordsBadge.setText(count + " Records");
            renderMonth();
            updateLogsTitle();
            loadLogsFor(selectedDate);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void renderMonth() {
        tvMonthCenter.setText(currentMonth.atDay(1).format(monthFmt));
        if (!YearMonth.from(selectedDate).equals(currentMonth)) {
            selectedDate = currentMonth.atDay(1);
        }
        calendarAdapter.submit(buildMonthGrid(currentMonth), selectedDate);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<CalendarDay> buildMonthGrid(YearMonth ym) {
        List<CalendarDay> out = new ArrayList<>();
        LocalDate first = ym.atDay(1);
        int daysInMonth = ym.lengthOfMonth();
        int leadingBlanks = first.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < leadingBlanks; i++)
            out.add(new CalendarDay(null, false, false, false));

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = ym.atDay(day);
            boolean hasEvent = daysWithRecords.contains(date);
            boolean hasSeizure = seizureDays.contains(date);
            out.add(new CalendarDay(date, true, hasEvent, hasSeizure));
        }

        int remainder = out.size() % 7;
        if (remainder != 0)
            for (int i = 0; i < 7 - remainder; i++)
                out.add(new CalendarDay(null, false, false, false));

        return out;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateLogsTitle() {
        if (selectedDate.equals(LocalDate.now())) {
            tvLogsTitle.setText("Today's Events");
        } else {
            tvLogsTitle.setText("Events on " + selectedDate);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadLogsFor(LocalDate date) {
        String myPin = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("loggedInPin", "");
        String dateStr = date.getYear() + "-"
                + date.getMonthValue() + "-"
                + date.getDayOfMonth();

        eventDao.getAllEvents().observe(this, allEvents -> {
            List<HelperEvent> dayEvents = new ArrayList<>();
            for (HelperEvent e : allEvents) {
                if (e.getUserSession() != null
                        && e.getUserSession().equals(myPin)
                        && dateStr.equals(e.getDate())) {
                    dayEvents.add(e);
                }
            }
            logsAdapter.submit(dayEvents);
        });
    }
}