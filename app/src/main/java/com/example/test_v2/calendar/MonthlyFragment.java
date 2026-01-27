package com.example.test_v2.calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.test_v2.R;
import com.example.test_v2.fileAndDatabase.HelperAppDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MonthlyFragment extends Fragment {

    private TextView monthYearText;
    private GridView calendarGrid;
    private Button prevMonthButton, nextMonthButton;
    private HelperEventDao eventDao;
    public static LocalDate currentMonth = LocalDate.now();
    private List<String> eventDates = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly, container, false);

        monthYearText = view.findViewById(R.id.monthYearText);
        calendarGrid = view.findViewById(R.id.calendarGrid);
        prevMonthButton = view.findViewById(R.id.prevMonthButton);
        nextMonthButton = view.findViewById(R.id.nextMonthButton);

        HelperAppDatabase db = HelperAppDatabase.getDatabase(getContext());
        eventDao = db.eventDao();

        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String passedDate = prefs.getString("selectedDate", null);
        if (passedDate != null) {
            currentMonth = LocalDate.parse(passedDate);
        }

        monthYearText.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        monthYearText.setOnClickListener(v -> showMonthYearPicker());

        prevMonthButton.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            loadCalendar();
        });

        nextMonthButton.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            loadCalendar();
        });

        loadCalendar();

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadCalendar() {
        eventDao.getAllEvents().observe(getViewLifecycleOwner(), allEvents -> {
            eventDates.clear();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
            for (HelperEvent event : allEvents) {
                if (event.getUserSession().equals(requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                        .getString("loggedInPin", ""))) {
                    eventDates.add(event.getDate());
                }
            }

            List<String> dayCells = buildDayCells(currentMonth);
            CalendarDayAdapter adapter = new CalendarDayAdapter(
                    requireContext(),
                    dayCells,
                    eventDates,
                    currentMonth,
                    this::showEventsForDay
            );
            calendarGrid.setAdapter(adapter);
        });

        monthYearText.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<String> buildDayCells(LocalDate month) {
        List<String> cells = new ArrayList<>();
        LocalDate firstOfMonth = month.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        int shift = dayOfWeek % 7;
        for (int i = 0; i < shift; i++) {
            cells.add("");
        }

        int lengthOfMonth = month.lengthOfMonth();
        for (int i = 1; i <= lengthOfMonth; i++) {
            cells.add(String.valueOf(i));
        }

        return cells;
    }

    private void showMonthYearPicker() {
        MonthYearPickerDialog pickerDialog = new MonthYearPickerDialog((year, month) -> {
            currentMonth = LocalDate.of(year, month + 1, 1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                loadCalendar();
            }
        });

        pickerDialog.show(getParentFragmentManager(), "MonthYearPickerDialog");
    }

    private void showEventsForDay(String selectedDate) {
        HelperAppDatabase db = HelperAppDatabase.getDatabase(getContext());
        HelperEventDao dao = db.eventDao();

        dao.getAllEvents().observe(getViewLifecycleOwner(), eventList -> {
            List<HelperEvent> dayEvents = new ArrayList<>();
            for (HelperEvent event : eventList) {
                if (event.getDate().equals(selectedDate)) {
                    dayEvents.add(event);
                }
            }

            if (dayEvents.isEmpty()) {
                AlertDialog.Builder emptyBuilder = new AlertDialog.Builder(getContext());
                emptyBuilder.setTitle("No Events");
                emptyBuilder.setMessage("There are no events for this day.");
                emptyBuilder.setNegativeButton("Close", null);
                emptyBuilder.show();
                return;
            }

            LayoutInflater inflater = LayoutInflater.from(getContext());
            View popupView = inflater.inflate(R.layout.pop_up_events_for_day, null);
            ViewGroup container = popupView.findViewById(R.id.eventCardContainer);

            for (HelperEvent event : dayEvents) {
                View card = inflater.inflate(R.layout.item_event, container, false);

                TextView title = card.findViewById(R.id.eventTitle);
                TextView time = card.findViewById(R.id.eventTime);
                TextView tag = card.findViewById(R.id.eventTag);

                title.setText(event.getTitle());
                time.setText(event.getStartTime() + " - " + event.getEndTime());
                tag.setText("Tag: " + event.getTag());

                card.setOnClickListener(v -> openEventDetail(event));
                container.addView(card);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(popupView);
            builder.setTitle("Events on " + selectedDate);
            builder.setNegativeButton("Close", null);
            builder.show();
        });
    }


    private void openEventDetail(HelperEvent event) {
        Intent intent = new Intent(getContext(), EventDetailsActivity.class);
        intent.putExtra("eventId", event.getID());
        startActivity(intent);
    }

}
