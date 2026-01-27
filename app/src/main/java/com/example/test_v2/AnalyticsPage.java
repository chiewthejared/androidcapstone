package com.example.test_v2;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test_v2.calendar.HelperEvent;
import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.tags.Tag;
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
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AnalyticsPage extends AppCompatActivity {

    private Spinner spinnerTimeRange, spinnerDayMonthYear, spinnerYear;
    private Button buttonSelectTags, backButton, testButton;
    private BarChart barChart;
    private LineChart lineChart;

    private List<String> allTags = new ArrayList<>();
    private boolean[] checkedTags;
    private List<String> selectedTags = new ArrayList<>();

    // Distinct colors for each tag
    private final int[] TAG_COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN,
            Color.rgb(255, 165, 0),   // Orange
            Color.rgb(128, 0, 128)    // Purple
    };

    // Month names for dropdown and formatting in year view
    private final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analytics_page);

        spinnerTimeRange = findViewById(R.id.spinner_time_range);
        spinnerDayMonthYear = findViewById(R.id.spinner_day_month_year);
        spinnerYear = findViewById(R.id.spinner_year);
        buttonSelectTags = findViewById(R.id.button_select_tags);
        backButton = findViewById(R.id.back_button);
        testButton = findViewById(R.id.test_button);
        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);

        // Only "Month" and "Year" views now
        ArrayAdapter<String> rangeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Month", "Year")
        );
        rangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeRange.setAdapter(rangeAdapter);

        spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSpinners();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerDayMonthYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchAndDisplayCharts();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Only used in month view – refresh charts when year selection changes.
                if ("Month".equals(spinnerTimeRange.getSelectedItem())) {
                    fetchAndDisplayCharts();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        buttonSelectTags.setOnClickListener(v -> showTagSelectionDialog());
        backButton.setOnClickListener(v -> finish());
        testButton.setOnClickListener(v -> populateTestData());

        // Load all tags in background.
        new Thread(() -> {
            List<Tag> tags = HelperAppDatabase.getDatabase(getApplicationContext()).tagDao().getAll();
            for (Tag t : tags) {
                allTags.add(t.name);
            }
            checkedTags = new boolean[allTags.size()];
        }).start();

        setupChartProperties();
        updateSpinners();
    }

    /**
     * Updates the spinners:
     * - If time range is "Month", spinnerDayMonthYear shows months and spinnerYear is visible.
     * - If time range is "Year", spinnerDayMonthYear shows years and spinnerYear is hidden.
     */
    private void updateSpinners() {
        String range = (String) spinnerTimeRange.getSelectedItem();
        List<String> data = new ArrayList<>();
        if ("Month".equals(range)) {
            data.addAll(Arrays.asList(MONTH_NAMES));
            spinnerYear.setVisibility(View.VISIBLE);
            // Populate spinnerYear with years (e.g., 2023-2030)
            List<String> years = new ArrayList<>();
            for (int y = 2023; y <= 2030; y++) {
                years.add(String.valueOf(y));
            }
            ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
            yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerYear.setAdapter(yearAdapter);
        } else {
            for (int y = 2023; y <= 2030; y++) {
                data.add(String.valueOf(y));
            }
            spinnerYear.setVisibility(View.GONE);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayMonthYear.setAdapter(adapter);
    }

    /**
     * Shows the multi-choice dialog for selecting tags.
     */
    private void showTagSelectionDialog() {
        if (allTags.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setMessage("No tags available.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Tags");
        builder.setMultiChoiceItems(allTags.toArray(new String[0]), checkedTags, (dialog, which, isChecked) -> {
            checkedTags[which] = isChecked;
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedTags.clear();
            for (int i = 0; i < allTags.size(); i++) {
                if (checkedTags[i]) {
                    selectedTags.add(allTags.get(i));
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fetchAndDisplayCharts();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Retrieves the current user session ID.
     */
    private String getCurrentSession() {
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        return preferences.getString("loggedInPin", "");
    }

    /**
     * Queries events for the current user, filters by selected tags & month/year,
     * then updates the charts.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void fetchAndDisplayCharts() {
        new Thread(() -> {
            String range = (String) spinnerTimeRange.getSelectedItem();
            String selection = (String) spinnerDayMonthYear.getSelectedItem();
            String session = getCurrentSession();

            // Retrieve all events for the user.
            List<HelperEvent> allEvents = HelperAppDatabase
                    .getDatabase(getApplicationContext())
                    .eventDao()
                    .getAllEventsRawForUser(session);

            List<HelperEvent> filtered = new ArrayList<>();
            for (HelperEvent ev : allEvents) {
                // Filter by tag (case-insensitive)
                if (selectedTags.isEmpty() || selectedTags.stream().anyMatch(tag -> tag.equalsIgnoreCase(ev.getTag()))) {
                    if (fitsRange(ev.getDate(), range, selection)) {
                        filtered.add(ev);
                    }
                }
            }

            // Build data for charts using filtered events.
            HashMap<String, HashMap<Integer, Integer>> dataMap = new HashMap<>();
            for (String tag : selectedTags) {
                dataMap.put(tag, new HashMap<>());
            }
            for (HelperEvent ev : filtered) {
                String t = ev.getTag();
                dataMap.putIfAbsent(t, new HashMap<>());
                int xVal = computeXValue(ev.getDate(), range, selection);
                int count = dataMap.get(t).getOrDefault(xVal, 0);
                dataMap.get(t).put(xVal, count + 1);
            }

            BarData barData = buildBarData(dataMap);
            LineData lineData = buildLineData(dataMap);

            runOnUiThread(() -> {
                barChart.setData(barData);
                lineChart.setData(lineData);

                // Adjust X axis based on selected view.
                XAxis xBar = barChart.getXAxis();
                XAxis xLine = lineChart.getXAxis();

                if ("Month".equals(spinnerTimeRange.getSelectedItem())) {
                    int month = monthNameToInt((String) spinnerDayMonthYear.getSelectedItem());
                    int year;
                    try {
                        year = Integer.parseInt(spinnerYear.getSelectedItem().toString());
                    } catch (NumberFormatException e) {
                        year = LocalDate.now().getYear();
                    }
                    YearMonth yearMonth = YearMonth.of(year, month);
                    int daysInMonth = yearMonth.lengthOfMonth();

                    xBar.setAxisMinimum(1f);
                    xBar.setAxisMaximum(daysInMonth + 0.5f);
                    xLine.setAxisMinimum(1f);
                    xLine.setAxisMaximum(daysInMonth + 0.5f);

                    ValueFormatter dayFormatter = new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return String.valueOf((int) value);
                        }
                    };
                    xBar.setValueFormatter(dayFormatter);
                    xLine.setValueFormatter(dayFormatter);
                } else {
                    xBar.setAxisMinimum(1f);
                    xBar.setAxisMaximum(12.5f);
                    xLine.setAxisMinimum(1f);
                    xLine.setAxisMaximum(12.5f);

                    ValueFormatter monthFormatter = new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int index = (int) value - 1;
                            if (index >= 0 && index < MONTH_NAMES.length) {
                                return MONTH_NAMES[index];
                            }
                            return "";
                        }
                    };
                    xBar.setValueFormatter(monthFormatter);
                    xLine.setValueFormatter(monthFormatter);
                }

                barChart.invalidate();
                lineChart.invalidate();
            });
        }).start();
    }

    /**
     * Test button: Populates the charts with fake data.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void populateTestData() {
        String range = (String) spinnerTimeRange.getSelectedItem();
        int maxX = "Month".equals(range) ? 28 : 12;
        List<String> testTags = Arrays.asList("Work", "Personal", "Urgent", "None");
        HashMap<String, HashMap<Integer, Integer>> dataMap = new HashMap<>();
        for (String tag : testTags) {
            HashMap<Integer, Integer> counts = new HashMap<>();
            for (int x = 1; x <= maxX; x++) {
                int count = (int) (Math.random() * 10);
                counts.put(x, count);
            }
            dataMap.put(tag, counts);
        }
        BarData barData = buildBarData(dataMap);
        LineData lineData = buildLineData(dataMap);
        runOnUiThread(() -> {
            barChart.setData(barData);
            lineChart.setData(lineData);

            XAxis xBar = barChart.getXAxis();
            XAxis xLine = lineChart.getXAxis();
            if ("Month".equals(spinnerTimeRange.getSelectedItem())) {
                int month = monthNameToInt((String) spinnerDayMonthYear.getSelectedItem());
                int year = LocalDate.now().getYear();
                YearMonth yearMonth = YearMonth.of(year, month);
                int daysInMonth = yearMonth.lengthOfMonth();

                xBar.setAxisMinimum(1f);
                xBar.setAxisMaximum(daysInMonth + 0.5f);
                xLine.setAxisMinimum(1f);
                xLine.setAxisMaximum(daysInMonth + 0.5f);

                ValueFormatter dayFormatter = new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.valueOf((int) value);
                    }
                };
                xBar.setValueFormatter(dayFormatter);
                xLine.setValueFormatter(dayFormatter);
            } else {
                xBar.setAxisMinimum(1f);
                xBar.setAxisMaximum(12.5f);
                xLine.setAxisMinimum(1f);
                xLine.setAxisMaximum(12.5f);

                ValueFormatter monthFormatter = new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        int index = (int) value - 1;
                        if (index >= 0 && index < MONTH_NAMES.length) {
                            return MONTH_NAMES[index];
                        }
                        return "";
                    }
                };
                xBar.setValueFormatter(monthFormatter);
                xLine.setValueFormatter(monthFormatter);
            }

            barChart.invalidate();
            lineChart.invalidate();
        });
    }

    /**
     * Returns true if the event's date is in the selected month (current year) or year.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean fitsRange(String eventDate, String range, String selection) {
        try {
            LocalDate d = LocalDate.parse(eventDate, DateTimeFormatter.ofPattern("[yyyy-M-d][yyyy-MM-dd]"));
            if ("Month".equals(range)) {
                int monthIndex = monthNameToInt(selection);
                int year;
                try {
                    year = Integer.parseInt(spinnerYear.getSelectedItem().toString());
                } catch (NumberFormatException e) {
                    year = LocalDate.now().getYear();
                }
                return (d.getYear() == year) && (d.getMonthValue() == monthIndex);
            }
            else {
                int year = Integer.parseInt(selection);
                return d.getYear() == year;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Computes the X-axis value.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private int computeXValue(String eventDate, String range, String selection) {
        try {
            LocalDate d = LocalDate.parse(eventDate, DateTimeFormatter.ofPattern("[yyyy-M-d][yyyy-MM-dd]"));
            return "Month".equals(range) ? d.getDayOfMonth() : d.getMonthValue();
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Builds BarData for the chart.
     */
    private BarData buildBarData(HashMap<String, HashMap<Integer, Integer>> dataMap) {
        List<BarDataSet> sets = new ArrayList<>();
        int index = 0;
        for (String tag : dataMap.keySet()) {
            List<BarEntry> entries = new ArrayList<>();
            for (int xVal : dataMap.get(tag).keySet()) {
                float x = xVal + (0.1f * index);
                entries.add(new BarEntry(x, dataMap.get(tag).get(xVal)));
            }
            BarDataSet set = new BarDataSet(entries, tag);
            set.setColor(TAG_COLORS[index % TAG_COLORS.length]);
            set.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value);
                }
            });
            sets.add(set);
            index++;
        }
        BarData barData = new BarData();
        for (BarDataSet s : sets) {
            barData.addDataSet(s);
        }
        barData.setBarWidth(0.8f / Math.max(1, sets.size()));
        return barData;
    }

    /**
     * Builds LineData for the chart.
     */
    private LineData buildLineData(HashMap<String, HashMap<Integer, Integer>> dataMap) {
        List<LineDataSet> sets = new ArrayList<>();
        int index = 0;
        for (String tag : dataMap.keySet()) {
            List<Entry> entries = new ArrayList<>();
            for (int xVal : dataMap.get(tag).keySet()) {
                float x = xVal + (0.1f * index);
                entries.add(new Entry(x, dataMap.get(tag).get(xVal)));
            }
            LineDataSet lineSet = new LineDataSet(entries, tag);
            lineSet.setColor(TAG_COLORS[index % TAG_COLORS.length]);
            lineSet.setCircleColor(TAG_COLORS[index % TAG_COLORS.length]);
            lineSet.setLineWidth(2f);
            lineSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value);
                }
            });
            sets.add(lineSet);
            index++;
        }
        LineData lineData = new LineData();
        for (LineDataSet s : sets) {
            lineData.addDataSet(s);
        }
        return lineData;
    }

    /**
     * Configures common chart properties.
     */
    private void setupChartProperties() {
        YAxis leftAxisBar = barChart.getAxisLeft();
        leftAxisBar.setGranularity(1f);
        leftAxisBar.setGranularityEnabled(true);
        leftAxisBar.setAxisMinimum(0f);
        leftAxisBar.setTextSize(14f);
        barChart.getAxisRight().setEnabled(false);
        XAxis xBar = barChart.getXAxis();
        xBar.setGranularity(1f);
        xBar.setGranularityEnabled(true);
        xBar.setTextSize(14f);

        YAxis leftAxisLine = lineChart.getAxisLeft();
        leftAxisLine.setGranularity(1f);
        leftAxisLine.setGranularityEnabled(true);
        leftAxisLine.setAxisMinimum(0f);
        leftAxisLine.setTextSize(14f);
        lineChart.getAxisRight().setEnabled(false);
        XAxis xLine = lineChart.getXAxis();
        xLine.setGranularity(1f);
        xLine.setGranularityEnabled(true);
        xLine.setTextSize(14f);
    }

    /**
     * Converts a month name to its corresponding integer.
     */
    private int monthNameToInt(String name) {
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            if (MONTH_NAMES[i].equalsIgnoreCase(name)) {
                return i + 1;
            }
        }
        return 1;
    }
}
