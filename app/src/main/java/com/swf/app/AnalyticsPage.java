package com.swf.app;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.swf.app.fileAndDatabase.HelperAppDatabase;
import com.swf.app.timer.HelperTimerEvent;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AnalyticsPage extends AppCompatActivity {

    private Spinner spinnerTimeRange, spinnerDayMonthYear, spinnerYear;
    private Spinner spinnerBarWeekFilter, spinnerLineWeekFilter;
    private Button buttonSelectTags, backButton;
    private BarChart barChart;
    private LineChart lineChart;

    private final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    private final String[] WEEKDAY_NAMES = {
            "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    };

    private boolean isUpdatingFilters = false;

    private static class ChartBundle {
        List<String> labels = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();
        List<Entry> lineEntries = new ArrayList<>();
        String title = "";
        boolean dailyView = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analytics_page);

        spinnerTimeRange = findViewById(R.id.spinner_time_range);
        spinnerDayMonthYear = findViewById(R.id.spinner_day_month_year);
        spinnerYear = findViewById(R.id.spinner_year);
        spinnerBarWeekFilter = findViewById(R.id.spinner_bar_week_filter);
        spinnerLineWeekFilter = findViewById(R.id.spinner_line_week_filter);
        buttonSelectTags = findViewById(R.id.button_select_tags);
        backButton = findViewById(R.id.back_button);
        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);

        ArrayAdapter<String> rangeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Monthly", "Yearly")
        );
        rangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeRange.setAdapter(rangeAdapter);
        spinnerTimeRange.setSelection(0);

        buttonSelectTags.setText("Refresh");
        buttonSelectTags.setOnClickListener(v -> fetchAndDisplayCharts());
        backButton.setOnClickListener(v -> finish());

        spinnerTimeRange.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected() {
                updateMainSpinners();
            }
        });

        spinnerDayMonthYear.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected() {
                if (!isUpdatingFilters) {
                    updateWeekFilters();
                    fetchAndDisplayCharts();
                }
            }
        });

        spinnerYear.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected() {
                if (!isUpdatingFilters) {
                    updateWeekFilters();
                    fetchAndDisplayCharts();
                }
            }
        });

        spinnerBarWeekFilter.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected() {
                if (!isUpdatingFilters) {
                    fetchAndDisplayCharts();
                }
            }
        });

        spinnerLineWeekFilter.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected() {
                if (!isUpdatingFilters) {
                    fetchAndDisplayCharts();
                }
            }
        });

        setupChartProperties();
        updateMainSpinners();
    }

    private abstract static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            onSelected();
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) { }

        public abstract void onSelected();
    }

    private void updateMainSpinners() {
        isUpdatingFilters = true;

        String range = String.valueOf(spinnerTimeRange.getSelectedItem());
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        List<String> years = buildYearList(currentYear);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                years
        );
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if ("Monthly".equals(range)) {
            spinnerYear.setVisibility(View.VISIBLE);
            spinnerYear.setAdapter(yearAdapter);

            ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    Arrays.asList(MONTH_NAMES)
            );
            monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDayMonthYear.setAdapter(monthAdapter);
            spinnerDayMonthYear.setSelection(now.getMonthValue() - 1);

            int yearIndex = years.indexOf(String.valueOf(currentYear));
            spinnerYear.setSelection(Math.max(0, yearIndex));
        } else {
            spinnerYear.setVisibility(View.GONE);

            ArrayAdapter<String> yearlyAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    years
            );
            yearlyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDayMonthYear.setAdapter(yearlyAdapter);

            int yearIndex = years.indexOf(String.valueOf(currentYear));
            spinnerDayMonthYear.setSelection(Math.max(0, yearIndex));
        }

        updateWeekFilters();
        isUpdatingFilters = false;
        fetchAndDisplayCharts();
    }

    private void updateWeekFilters() {
        String range = String.valueOf(spinnerTimeRange.getSelectedItem());
        if (!"Monthly".equals(range)) {
            spinnerBarWeekFilter.setVisibility(View.GONE);
            spinnerLineWeekFilter.setVisibility(View.GONE);
            return;
        }

        spinnerBarWeekFilter.setVisibility(View.VISIBLE);
        spinnerLineWeekFilter.setVisibility(View.VISIBLE);

        int selectedYear = getSelectedYear();
        int selectedMonth = getSelectedMonth();
        int weekCount = getMonthWeekCount(selectedYear, selectedMonth);

        List<String> filterItems = new ArrayList<>();
        filterItems.add("All");
        for (int i = 1; i <= weekCount; i++) {
            filterItems.add("Week " + i);
        }

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filterItems
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerBarWeekFilter.setAdapter(filterAdapter);
        spinnerLineWeekFilter.setAdapter(filterAdapter);

        spinnerBarWeekFilter.setSelection(0);
        spinnerLineWeekFilter.setSelection(0);
    }

    private List<String> buildYearList(int currentYear) {
        List<String> years = new ArrayList<>();
        for (int y = currentYear - 5; y <= currentYear + 5; y++) {
            years.add(String.valueOf(y));
        }
        return years;
    }

    private String getCurrentSession() {
        return getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("loggedInPin", "");
    }

    private int getSelectedYear() {
        String range = String.valueOf(spinnerTimeRange.getSelectedItem());
        if ("Monthly".equals(range)) {
            return safeParseInt(String.valueOf(spinnerYear.getSelectedItem()), LocalDate.now().getYear());
        }
        return safeParseInt(String.valueOf(spinnerDayMonthYear.getSelectedItem()), LocalDate.now().getYear());
    }

    private int getSelectedMonth() {
        String monthName = String.valueOf(spinnerDayMonthYear.getSelectedItem());
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            if (MONTH_NAMES[i].equalsIgnoreCase(monthName)) {
                return i + 1;
            }
        }
        return LocalDate.now().getMonthValue();
    }

    private void fetchAndDisplayCharts() {
        new Thread(() -> {
            HelperAppDatabase db = HelperAppDatabase.getDatabase(getApplicationContext());
            List<HelperTimerEvent> events = db.timerEventDao().getAllEventsForUser(getCurrentSession());

            String range = String.valueOf(spinnerTimeRange.getSelectedItem());
            String selectedPeriod = String.valueOf(spinnerDayMonthYear.getSelectedItem());
            String selectedYear = spinnerYear.getVisibility() == View.VISIBLE
                    ? String.valueOf(spinnerYear.getSelectedItem())
                    : selectedPeriod;

            String barFilter = spinnerBarWeekFilter.getVisibility() == View.VISIBLE
                    ? String.valueOf(spinnerBarWeekFilter.getSelectedItem())
                    : "All";
            String lineFilter = spinnerLineWeekFilter.getVisibility() == View.VISIBLE
                    ? String.valueOf(spinnerLineWeekFilter.getSelectedItem())
                    : "All";

            ChartBundle barBundle = buildChartBundle(events, range, selectedPeriod, selectedYear, barFilter);
            ChartBundle lineBundle = buildChartBundle(events, range, selectedPeriod, selectedYear, lineFilter);

            runOnUiThread(() -> renderCharts(barBundle, lineBundle));
        }).start();
    }

    private ChartBundle buildChartBundle(List<HelperTimerEvent> events,
                                         String range,
                                         String selectedPeriod,
                                         String selectedYearStr,
                                         String weekFilter) {
        ChartBundle bundle = new ChartBundle();
        int selectedYear = safeParseInt(selectedYearStr, LocalDate.now().getYear());

        if ("Monthly".equals(range)) {
            int month = monthNameToInt(selectedPeriod);
            YearMonth ym = YearMonth.of(selectedYear, month);
            int weekCount = getMonthWeekCount(selectedYear, month);

            if ("All".equalsIgnoreCase(weekFilter)) {
                bundle.dailyView = false;
                long[] countByWeek = new long[weekCount];
                long[] durationByWeekSeconds = new long[weekCount];

                for (HelperTimerEvent event : events) {
                    LocalDate date = toLocalDate(event.startTimestamp);
                    if (date.getYear() == selectedYear && date.getMonthValue() == month) {
                        int weekIndex = (date.getDayOfMonth() - 1) / 7;
                        if (weekIndex >= weekCount) {
                            weekIndex = weekCount - 1;
                        }
                        countByWeek[weekIndex]++;
                        durationByWeekSeconds[weekIndex] += Math.max(0L, event.totalTimeMs) / 1000L;
                    }
                }

                bundle.labels = new ArrayList<>();
                for (int i = 1; i <= weekCount; i++) {
                    bundle.labels.add("Week " + i);
                }

                for (int i = 0; i < weekCount; i++) {
                    bundle.barEntries.add(new BarEntry(i, countByWeek[i]));
                    bundle.lineEntries.add(new Entry(i, durationByWeekSeconds[i]));
                }
            } else {
                bundle.dailyView = true;
                int weekNum = extractNumber(weekFilter);
                int startDay = (weekNum - 1) * 7 + 1;
                int endDay = Math.min(weekNum * 7, ym.lengthOfMonth());

                long[] countByDay = new long[7];
                long[] durationByDaySeconds = new long[7];

                for (HelperTimerEvent event : events) {
                    LocalDate date = toLocalDate(event.startTimestamp);
                    if (date.getYear() == selectedYear
                            && date.getMonthValue() == month
                            && date.getDayOfMonth() >= startDay
                            && date.getDayOfMonth() <= endDay) {
                        int bucket = date.getDayOfWeek().getValue() - 1; // Mon=0 ... Sun=6
                        if (bucket >= 0 && bucket < 7) {
                            countByDay[bucket]++;
                            durationByDaySeconds[bucket] += Math.max(0L, event.totalTimeMs) / 1000L;
                        }
                    }
                }

                bundle.labels = Arrays.asList(WEEKDAY_NAMES);
                for (int i = 0; i < 7; i++) {
                    bundle.barEntries.add(new BarEntry(i, countByDay[i]));
                    bundle.lineEntries.add(new Entry(i, durationByDaySeconds[i]));
                }
            }

            bundle.title = MONTH_NAMES[month - 1] + " " + selectedYear;
        } else {
            bundle.dailyView = false;
            long[] countByMonth = new long[12];
            long[] durationByMonthSeconds = new long[12];

            for (HelperTimerEvent event : events) {
                LocalDate date = toLocalDate(event.startTimestamp);
                if (date.getYear() == selectedYear) {
                    int index = date.getMonthValue() - 1;
                    countByMonth[index]++;
                    durationByMonthSeconds[index] += Math.max(0L, event.totalTimeMs) / 1000L;
                }
            }

            bundle.labels = Arrays.asList(MONTH_NAMES);
            for (int i = 0; i < 12; i++) {
                bundle.barEntries.add(new BarEntry(i, countByMonth[i]));
                bundle.lineEntries.add(new Entry(i, durationByMonthSeconds[i]));
            }

            bundle.title = String.valueOf(selectedYear);
        }

        return bundle;
    }

    private int getMonthWeekCount(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return (int) Math.ceil(ym.lengthOfMonth() / 7.0);
    }

    private LocalDate toLocalDate(long timestampMs) {
        return Instant.ofEpochMilli(timestampMs)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void renderCharts(ChartBundle barBundle, ChartBundle lineBundle) {
        if (barBundle == null || lineBundle == null) return;

        int maroon = ContextCompat.getColor(this, R.color.port_wine);

        BarDataSet barSet = new BarDataSet(barBundle.barEntries, "Seizure Count");
        barSet.setColor(maroon);
        barSet.setValueTextSize(11f);
        barSet.setDrawValues(false);

        LineDataSet lineSet = new LineDataSet(lineBundle.lineEntries, "Total Duration (sec)");
        lineSet.setColor(maroon);
        lineSet.setCircleColor(maroon);
        lineSet.setCircleRadius(4f);
        lineSet.setDrawCircles(true);
        lineSet.setDrawCircleHole(false);
        lineSet.setLineWidth(2.5f);
        lineSet.setDrawValues(false);
        lineSet.setMode(LineDataSet.Mode.LINEAR);
        lineSet.setHighLightColor(maroon);

        BarData barData = new BarData(barSet);
        barData.setBarWidth(0.65f);

        LineData lineData = new LineData(lineSet);

        barChart.setData(barData);
        lineChart.setData(lineData);

        barChart.getDescription().setText(barBundle.title);
        lineChart.getDescription().setText(lineBundle.title);

        IndexAxisValueFormatter barFormatter = new IndexAxisValueFormatter(barBundle.labels);
        IndexAxisValueFormatter lineFormatter = new IndexAxisValueFormatter(lineBundle.labels);

        barChart.getXAxis().setValueFormatter(barFormatter);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setAxisMinimum(-0.5f);
        barChart.getXAxis().setAxisMaximum(Math.max(0, barBundle.labels.size() - 0.5f));

        lineChart.getXAxis().setValueFormatter(lineFormatter);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setAxisMinimum(-0.5f);
        lineChart.getXAxis().setAxisMaximum(Math.max(0, lineBundle.labels.size() - 0.5f));

        YAxis barLeft = barChart.getAxisLeft();
        barLeft.setAxisMinimum(0f);
        barLeft.setGranularity(1f);
        barChart.getAxisRight().setEnabled(false);

        YAxis lineLeft = lineChart.getAxisLeft();
        lineLeft.setAxisMinimum(0f);
        lineLeft.setGranularity(1f);
        lineLeft.setLabelCount(5, true);
        lineLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == (long) value) {
                    return String.format("%ds", (long) value);
                }
                return String.format("%.1fs", value);
            }
        });
        lineChart.getAxisRight().setEnabled(false);

        barChart.setFitBars(true);
        barChart.invalidate();
        lineChart.invalidate();
    }

    private int safeParseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.replaceAll("[^0-9-]", "").trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private int extractNumber(String value) {
        return safeParseInt(value, 1);
    }

    private int monthNameToInt(String name) {
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            if (MONTH_NAMES[i].equalsIgnoreCase(name)) {
                return i + 1;
            }
        }
        return LocalDate.now().getMonthValue();
    }

    private void setupChartProperties() {
        barChart.getLegend().setEnabled(true);
        lineChart.getLegend().setEnabled(true);

        barChart.setNoDataText("No seizure records available");
        lineChart.setNoDataText("No seizure records available");

        barChart.setDrawGridBackground(false);
        lineChart.setDrawGridBackground(false);

        barChart.getAxisRight().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);

        YAxis barLeft = barChart.getAxisLeft();
        barLeft.setGranularity(1f);
        barLeft.setAxisMinimum(0f);

        YAxis lineLeft = lineChart.getAxisLeft();
        lineLeft.setGranularity(1f);
        lineLeft.setAxisMinimum(0f);
    }
}