package com.example.test_v2;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
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
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.test_v2.calendar.HelperEvent;
import com.example.test_v2.calendar.EventAdapter;
import com.example.test_v2.calendar.HelperEventDao;
import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.tags.Tag;
import com.example.test_v2.tags.TagDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DailyFragment extends Fragment {

    private RecyclerView recyclerView;
    private com.example.test_v2.calendar.EventAdapter eventAdapter;
    private List<HelperEvent> filteredEventList = new ArrayList<>();
    private Button selectDateButton;
    private FloatingActionButton addEventButton;
    private String currentSelectedDate;
    String currentUserSession;

    // We'll use this adapter to display tags in the Spinner
    private ArrayAdapter<String> tagAdapter;

    private HelperEventDao eventDao;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_daily, container, false);
        currentUserSession = getContext().getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("loggedInPin", null).toString();
        recyclerView = rootView.findViewById(R.id.recyclerViewDaily);
        selectDateButton = rootView.findViewById(R.id.selectDateButton);
        addEventButton = rootView.findViewById(R.id.addEventButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Spinner for filtering by tag
        Spinner tagSpinner = rootView.findViewById(R.id.eventCategorySpinner);

        // Get database instance
        HelperAppDatabase db = HelperAppDatabase.getDatabase(getContext());
        eventDao = db.eventDao();

        // Initialize date selection
        if (currentSelectedDate == null || currentSelectedDate.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            // Use non–zero-padded format to match filtering ("yyyy-M-d")
            currentSelectedDate = String.format("%d-%d-%d",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));
        }
        selectDateButton.setText(formatDate(currentSelectedDate));
        // Observe all events in the database
        eventDao.getAllEvents().observe(getViewLifecycleOwner(), eventList -> {
            if (isAdded()) {
                // Filter events by current date + selected tag
                String selectedTag = (tagSpinner.getSelectedItem() == null)
                        ? "None"
                        : tagSpinner.getSelectedItem().toString();
                filterEvents(eventList, currentSelectedDate, selectedTag);
            }
        });

        // Initialize EventAdapter with an empty list
        eventAdapter = new EventAdapter(filteredEventList);
        recyclerView.setAdapter(eventAdapter);

        // Handle "Select Date" button click
        selectDateButton.setOnClickListener(v -> showDatePickerDialog(date -> {
            currentSelectedDate = date;
            selectDateButton.setText(formatDate(currentSelectedDate));

            Log.d("DEBUG", "Selected date 1: " + currentSelectedDate);
            // Re-filter events after date changes
            eventDao.getAllEvents().observe(getViewLifecycleOwner(), new Observer<List<HelperEvent>>() {
                @Override
                public void onChanged(List<HelperEvent> eventList) {
                    filterEvents(eventList, currentSelectedDate, tagSpinner.getSelectedItem().toString());
                }
            });
            Log.d("DEBUG", "Selected date 2: " + currentSelectedDate);
        }));

        // Set up the Spinner with tags from the database
        loadTagsFromDatabase(tagSpinner);

        // When user selects a tag in the Spinner, re-filter events
        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTag = parent.getItemAtPosition(position).toString();
                eventDao.getAllEvents().observe(getViewLifecycleOwner(), eventList ->
                        filterEvents(eventList, currentSelectedDate, selectedTag)
                );
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Handle "Add Event" button
        addEventButton.setOnClickListener(v -> showAddEventDialog());

        return rootView;
    }

    private String formatDate(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

            Date parsedDate = inputFormat.parse(date);
            return (parsedDate != null) ? outputFormat.format(parsedDate) : date;
        } catch (ParseException e) {
            e.printStackTrace();
            return date; // Return the original date if parsing fails
        }
    }

    /**
     * Loads all tags from the DB, ensures "None" is always included, and sets up the Spinner adapter.
     */
    private void loadTagsFromDatabase(Spinner tagSpinner) {
        new Thread(() -> {
            // Retrieve all Tag objects
            TagDao tagDao = HelperAppDatabase.getDatabase(getContext()).tagDao();
            List<Tag> dbTags = tagDao.getAll();

            // Convert them to a List<String> and ensure "None" is always at the top
            List<String> tagNames = new ArrayList<>();

            if (!tagNames.contains("None")) { // Ensure "None" is included
                tagNames.add("None");
            }

            for (Tag t : dbTags) {
                if (!tagNames.contains(t.name)) { // Avoid duplicates
                    tagNames.add(t.name);
                }
            }

            // Update the Spinner adapter on the main thread
            requireActivity().runOnUiThread(() -> {
                tagAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, tagNames);
                tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                tagSpinner.setAdapter(tagAdapter);
            });
        }).start();
    }

    /**
     * Filter events by date and tag.
     */
    private void filterEvents(List<HelperEvent> eventList, String selectedDate, String selectedTag) {

        Log.d("DEBUG", "Session 1: " + currentUserSession);

        Iterator<HelperEvent> iterator = eventList.iterator();
        while (iterator.hasNext()) {
            HelperEvent event = iterator.next();
            Log.d("DEBUG", "Session 2: " + event.getUserSession());
            if (event.getUserSession() == null || !event.getUserSession().equals(currentUserSession)) {
                iterator.remove(); // Remove event if user doesn't match
            }
        }
        List<HelperEvent> filteredByDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            filteredByDate = filterEventsByDate(eventList, selectedDate);
        }

        // Filter by tag if selectedTag is not "None"
        List<HelperEvent> filteredEvents;

        if (!selectedTag.equals("None")) {
            filteredEvents = filterEventsByTag(filteredByDate, selectedTag);
        } else {
            filteredEvents = filteredByDate;
        }

        filteredEventList.clear();
        filteredEventList.addAll(filteredEvents);
        eventAdapter.notifyDataSetChanged(); // Update RecyclerView
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<HelperEvent> filterEventsByDate(List<HelperEvent> eventList, String selectedDate) {
        List<HelperEvent> filteredList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d"); // match format in DB
        for (HelperEvent event : eventList) {
            LocalDate eventDate = LocalDate.parse(event.getDate(), formatter);
            LocalDate selectedDateObj = LocalDate.parse(selectedDate, formatter);
            if (eventDate.equals(selectedDateObj)) {
                filteredList.add(event);
            }
        }
        return filteredList;
    }

    private List<HelperEvent> filterEventsByTag(List<HelperEvent> eventList, String selectedTag) {
        List<HelperEvent> filteredList = new ArrayList<>();
        for (HelperEvent event : eventList) {
            if (event.getTag().equals(selectedTag)) {
                filteredList.add(event);
            }
        }
        return filteredList;
    }

    /**
     * Show a date picker dialog, then call the callback with the chosen date.
     */
    private void showDatePickerDialog(OnDateSelectedListener listener) {
        Calendar calendar = Calendar.getInstance();

        // Parse the currently selected date
        String[] dateParts = currentSelectedDate.split("-");
        int selectedYear = Integer.parseInt(dateParts[0]);
        int selectedMonth = Integer.parseInt(dateParts[1]) - 1; // Calendar months start from 0
        int selectedDay = Integer.parseInt(dateParts[2]);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear1, selectedMonth1, selectedDay1) -> {
                    // Use non-zero-padded format: "yyyy-M-d"
                    String selectedDate = selectedYear1 + "-" + (selectedMonth1 + 1) + "-" + selectedDay1;
                    listener.onDateSelected(selectedDate);
                },
                selectedYear, selectedMonth, selectedDay // Preselect the currently selected date
        );
        datePickerDialog.show();
    }

    public interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

    /**
     * Show the "Add Event" dialog and handle event creation + new tag creation.
     */
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
        // Load tags into the spinner from DB
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
            public void onNothingSelected(AdapterView<?> parent) {}
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

                    // Update the currentSelectedDate to the first recurring date
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

    /**
     * Loads tags from DB into the local Spinner in the "Add Event" dialog.
     */
    private void loadTagsIntoSpinner(Spinner tagSpinner) {
        new Thread(() -> {
            TagDao tagDao = HelperAppDatabase.getDatabase(getContext()).tagDao();
            List<Tag> dbTags = tagDao.getAll();
            List<String> tagNames = new ArrayList<>();

            if (!tagNames.contains("None")) {
                tagNames.add("None");
            }

            for (Tag t : dbTags) {
                if (!tagNames.contains(t.name)) {
                    tagNames.add(t.name);
                }
            }

            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, tagNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                tagSpinner.setAdapter(adapter);
                tagSpinner.setSelection(0);
            });
        }).start();
    }

    private boolean isValidDayInput(String dayInput) {
        try {
            int day = Integer.parseInt(dayInput);
            return day > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<String> getRecurringDates(String firstDate, String intervalType, int intervalValue) {
        List<String> recurringDates = new ArrayList<>();
        // Use input formatter that accepts single-digit months/days.
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");
        // Use output formatter with the same non–zero-padded format.
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDate baseDate = LocalDate.parse(firstDate, inputFormatter);
        LocalDate endDate = baseDate.plusYears(5);
        LocalDate currentDate = baseDate;
        while (!currentDate.isAfter(endDate)) {
            recurringDates.add(currentDate.format(outputFormatter));
            switch (intervalType) {
                case "Every X Days":
                    currentDate = currentDate.plusDays(intervalValue);
                    break;
                case "Every Week":
                    currentDate = currentDate.plusWeeks(1);
                    break;
                case "Every Month":
                    currentDate = currentDate.plusMonths(1);
                    break;
                case "Every Year":
                    currentDate = currentDate.plusYears(1);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid interval type");
            }
        }
        Log.d("DEBUG", recurringDates.toString());
        return recurringDates;
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

    private void reloadEvents() {
        eventDao.getAllEvents().observe(getViewLifecycleOwner(), eventList -> {
            if (isAdded()) {
                Spinner tagSpinner = getView().findViewById(R.id.eventCategorySpinner);
                String selectedTag = (tagSpinner.getSelectedItem() == null) ? "None" : tagSpinner.getSelectedItem().toString();
                filterEvents(eventList, currentSelectedDate, selectedTag);
            }
        });
    }
}
