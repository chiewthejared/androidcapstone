package com.example.test_v2.calendar;

import static com.example.test_v2.DailyFragment.getRecurringDates;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.WeeklyEventAdapter;
import com.example.test_v2.calendar.HelperEvent;
import com.example.test_v2.calendar.HelperEventDao;
import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.tags.Tag;
import com.example.test_v2.tags.TagDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.example.test_v2.R;

public class WeeklyFragment extends Fragment {

    private RecyclerView recyclerView;
    private WeeklyEventAdapter weeklyEventAdapter;
    private List<HelperEvent> filteredEventList;
    private Button previousWeekButton, nextWeekButton;
    private TextView weekDisplayText;
    private FloatingActionButton addEventButton;
    private String startDateOfWeek;
    private String currentUserSession;
    private HelperEventDao eventDao;
    private Spinner tagSpinner;
    String currentSelectedDate;
    private LinearLayout weeklyEventsContainer;



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weekly, container, false);

        currentUserSession = requireContext().getSharedPreferences("UserSession", requireContext().MODE_PRIVATE)
                .getString("loggedInPin", null);

        weeklyEventsContainer = rootView.findViewById(R.id.weeklyEventsContainer);
        previousWeekButton = rootView.findViewById(R.id.previousWeekButton);
        nextWeekButton = rootView.findViewById(R.id.nextWeekButton);
        weekDisplayText = rootView.findViewById(R.id.weekDisplayText);
        addEventButton = rootView.findViewById(R.id.addEventButton);
        tagSpinner = rootView.findViewById(R.id.eventCategorySpinner);

        HelperAppDatabase db = HelperAppDatabase.getDatabase(getContext());
        eventDao = db.eventDao();

        if (startDateOfWeek == null) {
            Calendar calendar = Calendar.getInstance();
            startDateOfWeek = getStartDateOfWeek(calendar);
        }

        weekDisplayText.setText(formatWeekDisplay(startDateOfWeek));

        if (filteredEventList == null) {
            filteredEventList = new ArrayList<>();
        }

        tagSpinner.setVisibility(View.VISIBLE);

        eventDao.getAllEvents().observe(getViewLifecycleOwner(), eventList -> {
            if (eventList == null) eventList = new ArrayList<>();
            String selectedTag = (tagSpinner.getSelectedItem() == null) ? "None" : tagSpinner.getSelectedItem().toString();
            filterEvents(eventList, startDateOfWeek, selectedTag);
        });

        previousWeekButton.setOnClickListener(v -> navigateWeek(-1));
        nextWeekButton.setOnClickListener(v -> navigateWeek(1));
        addEventButton.setOnClickListener(v -> showAddEventDialog());

        loadTagsFromDatabase(tagSpinner);

        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTag = parent.getItemAtPosition(position).toString();
                eventDao.getAllEvents().observe(getViewLifecycleOwner(), eventList -> {
                    if (eventList == null) eventList = new ArrayList<>();
                    filterEvents(eventList, startDateOfWeek, selectedTag);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return rootView;
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void navigateWeek(int direction) {
        try {
            // Ensure date format matches stored format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");

            // Parse current week start date
            LocalDate currentStartDate = LocalDate.parse(startDateOfWeek, formatter);
            LocalDate newStartDate = currentStartDate.plusWeeks(direction);

            // Store updated start date in correct format
            startDateOfWeek = newStartDate.format(formatter);
            Log.d("WeeklyFragment", "Navigated to week: " + startDateOfWeek);

            // Update UI
            weekDisplayText.setText(formatWeekDisplay(startDateOfWeek));

            // Fetch and filter events for the new week
            eventDao.getAllEvents().observe(getViewLifecycleOwner(), eventList -> {
                if (eventList == null) eventList = new ArrayList<>();
                String selectedTag = (tagSpinner.getSelectedItem() == null) ? "None" : tagSpinner.getSelectedItem().toString();
                filterEvents(eventList, startDateOfWeek, selectedTag);
            });

        } catch (Exception e) {
            Log.e("WeeklyFragment", "Error navigating week with date: " + startDateOfWeek, e);
        }
    }




    @RequiresApi(api = Build.VERSION_CODES.O)
    private void filterEvents(List<HelperEvent> eventList, String startDate, String selectedTag) {
        if (filteredEventList == null) {
            filteredEventList = new ArrayList<>();
        }
        filteredEventList.clear();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");

        try {
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = start.plusDays(6);

            Map<String, List<HelperEvent>> groupedEvents = new LinkedHashMap<>();

            for (int i = 0; i < 7; i++) {
                LocalDate day = start.plusDays(i);
                String dateStr = day.format(formatter);
                groupedEvents.put(dateStr, new ArrayList<>());
            }

            for (HelperEvent event : eventList) {
                if (event.getUserSession() == null || !event.getUserSession().equals(currentUserSession)) continue;

                LocalDate eventDate = LocalDate.parse(event.getDate(), formatter);
                if (!eventDate.isBefore(start) && !eventDate.isAfter(end)) {
                    if (selectedTag.equals("None") || event.getTag().equals(selectedTag)) {
                        groupedEvents.get(event.getDate()).add(event);
                    }
                }
            }

            weeklyEventsContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(getContext());

            for (String date : groupedEvents.keySet()) {
                View headerView = inflater.inflate(R.layout.item_date_header, weeklyEventsContainer, false);
                TextView dateHeader = headerView.findViewById(R.id.dateHeader);
                dateHeader.setText(getDayOfWeek(date));
                weeklyEventsContainer.addView(headerView);

                for (HelperEvent event : groupedEvents.get(date)) {
                    View eventView = inflater.inflate(R.layout.item_event, weeklyEventsContainer, false);
                    TextView title = eventView.findViewById(R.id.eventTitle);
                    TextView time = eventView.findViewById(R.id.eventTime);
                    TextView tag = eventView.findViewById(R.id.eventTag);

                    title.setText(event.getTitle());
                    time.setText(event.getStartTime() + " - " + event.getEndTime());

                    if (event.getTag() != null && !event.getTag().isEmpty() && !event.getTag().equals("None")) {
                        tag.setText("Tag: " + event.getTag());
                        tag.setVisibility(View.VISIBLE);
                    } else {
                        tag.setVisibility(View.GONE);
                    }

                    eventView.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), com.example.test_v2.calendar.EventDetailsActivity.class);
                        intent.putExtra("eventId", event.getID());
                        startActivity(intent);
                    });

                    weeklyEventsContainer.addView(eventView);
                }
            }

        } catch (Exception e) {
            Log.e("WeeklyFragment", "Error filtering events for week: " + startDate, e);
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getStartDateOfWeek(Calendar calendar) {
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        return String.format(Locale.getDefault(), "%d-%d-%d",
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }




    @RequiresApi(api = Build.VERSION_CODES.O)
    private String formatWeekDisplay(String startDate) {
        try {
            // Match database stored format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");

            // Parse the start date
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = start.plusDays(6); // Define week end date

            // Format output as "MMM d - MMM d, yyyy"
            return start.format(DateTimeFormatter.ofPattern("MMM d")) + " - " +
                    end.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        } catch (Exception e) {
            Log.e("WeeklyFragment", "Error formatting week display for startDate: " + startDate, e);
            return startDate; // Fallback if there's an error
        }
    }




    private void loadTagsFromDatabase(Spinner tagSpinner) {
        new Thread(() -> {
            TagDao tagDao = HelperAppDatabase.getDatabase(getContext()).tagDao();
            List<Tag> dbTags = tagDao.getAll();
            List<String> tagNames = new ArrayList<>();
            tagNames.add("None");
            for (Tag t : dbTags) {
                tagNames.add(t.name);
            }

            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, tagNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                tagSpinner.setAdapter(adapter);
            });
        }).start();
    }

    public interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

    private void showDatePickerDialog(OnDateSelectedListener listener) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format date as `yyyy-M-d` to match DailyFragment
                    String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    listener.onDateSelected(selectedDate);
                },
                year, month - 1, day // Adjust for DatePickerDialog month indexing (0-based)
        );

        datePickerDialog.show();
    }




    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showAddEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        Spinner tagSpinner = dialogView.findViewById(R.id.eventTagSpinner);
        EditText tagInput = dialogView.findViewById(R.id.eventTagInput);
        Spinner timeIntervalSpinner = dialogView.findViewById(R.id.timeIntervalSpinner);
        EditText dayInputField = dialogView.findViewById(R.id.dayInputField);
        Button saveButton = dialogView.findViewById(R.id.saveEventButton);
        Button eventDateButton = dialogView.findViewById(R.id.eventDateButton);
        Button eventStartTimeButton = dialogView.findViewById(R.id.eventStartTimeButton);
        Button eventEndTimeButton = dialogView.findViewById(R.id.eventEndTimeButton);
        timeIntervalSpinner.setPrompt("Repeat");
        TextView eventTag = dialogView.findViewById(R.id.eventTag);


        // 1) Load tags into the spinner from DB, ignoring TagManager
        loadTagsIntoSpinner(tagSpinner);


        // Setup time-interval spinner
        String[] timeIntervals = {"No Repeat", "Every X Days", "Every Week", "Every Month", "Every Year"};
        ArrayAdapter<String> timeIntervalAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, timeIntervals);
        timeIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeIntervalSpinner.setAdapter(timeIntervalAdapter);

        timeIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) { // "Every X Days"
                    dayInputField.setVisibility(View.VISIBLE);
                } else {
                    dayInputField.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final String[] startTime = {""};
        final String[] endTime = {""};
        final String[] selectedDate2 = {currentSelectedDate};

        // Date selection
        eventDateButton.setOnClickListener(v ->
                showDatePickerDialog(date -> {
                    selectedDate2[0] = date;
                    eventDateButton.setText(date);
                }));

        // Time selection
        eventStartTimeButton.setOnClickListener(v ->
                showTimePickerDialog(true, eventStartTimeButton, startTime));
        eventEndTimeButton.setOnClickListener(v ->
                showTimePickerDialog(false, eventEndTimeButton, endTime));

        AlertDialog dialog = builder.create();

        // Handle "Save" event button
        saveButton.setOnClickListener(v -> {
            Log.d("DEBUG", "Session: " + currentUserSession);
            String title = ((EditText) dialogView.findViewById(R.id.eventTitleInput))
                    .getText().toString().trim();
            String description = ((EditText) dialogView.findViewById(R.id.eventDescriptionInput))
                    .getText().toString().trim();
            String spinnerTag = (tagSpinner.getSelectedItem() == null)
                    ? ""
                    : tagSpinner.getSelectedItem().toString().trim();
            String enteredTag = tagInput.getText().toString().trim();
            String tag = enteredTag.isEmpty() ? spinnerTag : enteredTag;
            String selectedDate = eventDateButton.getText().toString();
            String start = startTime[0];
            String end = endTime[0];
            String timeInterval = timeIntervalSpinner.getSelectedItem().toString();
            String dayInput = dayInputField.getText().toString().trim();

            // Basic validation
            if (title.isEmpty() || description.isEmpty() || tag.isEmpty()
                    || start.isEmpty() || end.isEmpty() || selectedDate.isEmpty()) {
                Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
            } else if (timeInterval.equals("Every X Days") && (dayInput.isEmpty() || !isValidDayInput(dayInput))) {
                Toast.makeText(getContext(), "Please enter a valid number of days", Toast.LENGTH_SHORT).show();
            } else {
                // Recurring dates logic
                List<String> recurringDates;
                if (timeInterval.equals("No Repeat")) {
                    recurringDates = new ArrayList<>();
                    recurringDates.add(selectedDate);
                } else if (timeInterval.equals("Every X Days")) {
                    recurringDates = getRecurringDates(selectedDate, timeInterval, Integer.parseInt(dayInput));
                } else {
                    recurringDates = getRecurringDates(selectedDate, timeInterval, 0);
                }

                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    HelperAppDatabase dbRef = HelperAppDatabase.getDatabase(getContext());
                    String link = UUID.randomUUID().toString();  // Unique ID for this recurring set

                    for (String date : recurringDates) {
                        HelperEvent newEvent = new HelperEvent(
                                UUID.randomUUID().toString(),
                                title,
                                description,
                                date,
                                start,
                                end,
                                tag,
                                timeInterval,
                                dayInput.isEmpty() ? 0 : Integer.parseInt(dayInput),
                                link,
                                currentUserSession

                        );
                        dbRef.eventDao().insert(newEvent);
                    }

                    // Update the currentSelectedDate to the last recurring date
                    currentSelectedDate = recurringDates.get(0);

                    // After inserting events into the database
                    requireActivity().runOnUiThread(() -> {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Event added!", Toast.LENGTH_SHORT).show();

                        // Force reloading of events after adding a new one
                        reloadEvents();
                    });

                });
            }
        });

        dialog.show();
    }

    private void loadTagsIntoSpinner(Spinner tagSpinner) {
        new Thread(() -> {
            TagDao tagDao = HelperAppDatabase.getDatabase(getContext()).tagDao();
            List<Tag> dbTags = tagDao.getAll();
            List<String> tagNames = new ArrayList<>();

            // Ensure "None" is always added at the top
            if (!tagNames.contains("None")) {
                tagNames.add("None");
            }

            for (Tag t : dbTags) {
                if (!tagNames.contains(t.name)) { // Prevent duplicates
                    tagNames.add(t.name);
                }
            }

            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, tagNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                tagSpinner.setAdapter(adapter);

                // Ensure "None" is the default selected option
                tagSpinner.setSelection(0);
            });
        }).start();
    }

    private void showTimePickerDialog(boolean isStartTime, Button button, String[] timeHolder) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minuteOfDay) -> {
                    int hour12Format = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
                    String amPm = (hourOfDay < 12) ? "AM" : "PM";
                    String formattedTime = String.format("%02d:%02d %s", hour12Format, minuteOfDay, amPm);

                    timeHolder[0] = formattedTime;
                    button.setText(formattedTime);
                },
                hour, minute, false
        );
        timePickerDialog.show();
    }

    private boolean isValidDayInput(String dayInput) {
        try {
            int day = Integer.parseInt(dayInput);
            return day > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void reloadEvents() {
        eventDao.getAllEvents().observe(getViewLifecycleOwner(), eventList -> {
            if (isAdded()) {
                // Get the selected tag from spinner
                Spinner tagSpinner = getView().findViewById(R.id.eventCategorySpinner);
                String selectedTag = (tagSpinner.getSelectedItem() == null) ? "None" : tagSpinner.getSelectedItem().toString();

                // Re-filter events for the current date
                filterEvents(eventList, currentSelectedDate, selectedTag);
            }
        });
    }
    private String formatDate(String date) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        return LocalDate.parse(date, inputFormatter).format(outputFormatter);
    }

    private String getDayOfWeek(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDate localDate = LocalDate.parse(date, formatter);
        return localDate.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault());
    }


}