package com.example.test_v2.calendar;

import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;

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

    private CalendarMonthAdapter calendarAdapter;   // 你已经有的月历 RecyclerView adapter
    private MonthlyLogsAdapter logsAdapter;         // 你已经有的 logs adapter

    private YearMonth currentMonth;
    private LocalDate selectedDate;

    private final Set<LocalDate> daysWithRecords = new HashSet<>();
    private final DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy");

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_page);

        tvMonthCenter = findViewById(R.id.tvMonthCenter);
        rvCalendar = findViewById(R.id.rvCalendar);
        ImageButton btnPrev = findViewById(R.id.btnPrevMonth);
        ImageButton btnNext = findViewById(R.id.btnNextMonth);

        tvLogsTitle = findViewById(R.id.tvLogsTitle);
        rvTodayLogs = findViewById(R.id.rvTodayLogs);

        selectedDate = LocalDate.now();
        currentMonth = YearMonth.from(selectedDate);

        // TODO：接你数据库后删掉，这里先让你看到“有记录的小点/高亮”
        daysWithRecords.add(selectedDate);
        daysWithRecords.add(selectedDate.minusDays(2));
        daysWithRecords.add(selectedDate.plusDays(5));

        // calendar grid
        calendarAdapter = new CalendarMonthAdapter(date -> {
            selectedDate = date;
            updateLogsTitle();
            loadLogsFor(date); // TODO: 这里接数据库
        });
        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        rvCalendar.setAdapter(calendarAdapter);

        // logs list
        logsAdapter = new MonthlyLogsAdapter(event -> {
            // TODO: 点击某条 log 打开详情页（你之前 MonthlyFragment 已经有 openEventDetail）
        });
        rvTodayLogs.setLayoutManager(new LinearLayoutManager(this));
        rvTodayLogs.setAdapter(logsAdapter);

        btnPrev.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            renderMonth();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            renderMonth();
        });

        renderMonth();
        updateLogsTitle();
        loadLogsFor(selectedDate);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void renderMonth() {
        tvMonthCenter.setText(currentMonth.atDay(1).format(monthFmt));

        if (!YearMonth.from(selectedDate).equals(currentMonth)) {
            selectedDate = currentMonth.atDay(1);
        }

        List<CalendarDay> grid = buildMonthGrid(currentMonth, daysWithRecords);
        calendarAdapter.submit(grid, selectedDate);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<CalendarDay> buildMonthGrid(YearMonth ym, Set<LocalDate> recordDays) {
        List<CalendarDay> out = new ArrayList<>();

        LocalDate first = ym.atDay(1);
        int daysInMonth = ym.lengthOfMonth();

        int firstDow = first.getDayOfWeek().getValue(); // 1..7 (Mon..Sun)
        int leadingBlanks = firstDow % 7;              // Sun->0

        for (int i = 0; i < leadingBlanks; i++) {
            out.add(new CalendarDay(null, false, false));
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = ym.atDay(day);
            boolean has = recordDays != null && recordDays.contains(date);
            out.add(new CalendarDay(date, true, has));
        }

        int remainder = out.size() % 7;
        if (remainder != 0) {
            for (int i = 0; i < 7 - remainder; i++) {
                out.add(new CalendarDay(null, false, false));
            }
        }

        return out;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateLogsTitle() {
        if (selectedDate.equals(LocalDate.now())) {
            tvLogsTitle.setText("Today's Logs");
        } else {
            tvLogsTitle.setText("Logs on " + selectedDate);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadLogsFor(LocalDate date) {
        // TODO: 用你的 Room 查询替换这里
        // 目前先给空列表，不崩
        logsAdapter.submit(new ArrayList<>());
    }
}