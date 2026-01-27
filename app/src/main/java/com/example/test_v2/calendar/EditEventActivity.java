package com.example.test_v2.calendar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.R;
import com.example.test_v2.tags.Tag;
import com.example.test_v2.tags.TagDao;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;

public class EditEventActivity extends AppCompatActivity {
    private EditText titleInput, descriptionInput, tagInput;
    private Button eventDateButton, eventStartTimeButton, eventEndTimeButton, saveButton, addTagButton;
    private Spinner tagSpinner, editOptionSpinner;
    private ArrayAdapter<String> tagAdapter;
    private String selectedDate = "";
    private String selectedStartTime = "";
    private String selectedEndTime = "";
    private EventViewModel eventViewModel;
    private String eventId;
    private String link;
    private String repeat;
    private int occurrence;
    private SharedPreferences userSession;

    private void loadTagsFromDatabase(Spinner tagSpinner, String selectedTag) {
        new Thread(() -> {
            TagDao tagDao = HelperAppDatabase.getDatabase(this).tagDao();
            List<Tag> dbTags = tagDao.getAll();
            List<String> tagNames = new ArrayList<>();
            tagNames.add("None");
            for (Tag t : dbTags) {
                tagNames.add(t.name);
            }
            runOnUiThread(() -> {
                tagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tagNames);
                tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                tagSpinner.setAdapter(tagAdapter);
                int position = tagAdapter.getPosition(selectedTag);
                if (position >= 0) {
                    tagSpinner.setSelection(position);
                }
            });
        }).start();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        // Use consistent SharedPreferences name "UserSession"
        userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        titleInput = findViewById(R.id.eventTitleInput);
        eventDateButton = findViewById(R.id.eventDateButton);
        eventStartTimeButton = findViewById(R.id.eventStartTimeButton);
        eventEndTimeButton = findViewById(R.id.eventEndTimeButton);
        tagSpinner = findViewById(R.id.eventTagSpinner);
        tagInput = findViewById(R.id.eventTagInput);
        addTagButton = findViewById(R.id.addTagButton);
        descriptionInput = findViewById(R.id.eventDescriptionInput);
        saveButton = findViewById(R.id.saveEventButton);
        editOptionSpinner = findViewById(R.id.eventEditOptionSpinner);
        Button cancelButton = findViewById(R.id.cancelEventButton);

        cancelButton.setOnClickListener(v -> finish());
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        eventId = getIntent().getStringExtra("uuid");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            eventViewModel.getEventById(eventId).observe(this, event -> {
                if (event != null) {
                    titleInput.setText(event.getTitle());
                    descriptionInput.setText(event.getDescription());
                    selectedDate = event.getDate();
                    selectedStartTime = event.getStartTime();
                    selectedEndTime = event.getEndTime();
                    eventDateButton.setText(selectedDate);
                    eventStartTimeButton.setText(selectedStartTime);
                    eventEndTimeButton.setText(selectedEndTime);
                    repeat = event.getRepeatInterval();
                    occurrence = event.getOccurrenceCount();
                    link = event.getLinkedId();
                    loadTagsFromDatabase(tagSpinner, event.getTag());
                    if ("No Repeat".equals(event.getRepeatInterval())) {
                        editOptionSpinner.setVisibility(View.GONE);
                    } else {
                        editOptionSpinner.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        // Date picker with non–zero-padded format
        eventDateButton.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(EditEventActivity.this,
                    (dateView, selectedYear, selectedMonth, selectedDay) -> {
                        // Use format "yyyy-M-d"
                        selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        eventDateButton.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Start time picker (12-hour format)
        eventStartTimeButton.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(EditEventActivity.this,
                    (timeView, selectedHour, selectedMinute) -> {
                        int hour12 = selectedHour % 12;
                        if (hour12 == 0) { hour12 = 12; }
                        String amPm = selectedHour < 12 ? "AM" : "PM";
                        selectedStartTime = String.format("%02d:%02d %s", hour12, selectedMinute, amPm);
                        eventStartTimeButton.setText(selectedStartTime);
                    }, hour, minute, false);
            timePickerDialog.show();
        });

        // End time picker (12-hour format)
        eventEndTimeButton.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(EditEventActivity.this,
                    (timeView, selectedHour, selectedMinute) -> {
                        int hour12 = selectedHour % 12;
                        if (hour12 == 0) { hour12 = 12; }
                        String amPm = selectedHour < 12 ? "AM" : "PM";
                        selectedEndTime = String.format("%02d:%02d %s", hour12, selectedMinute, amPm);
                        eventEndTimeButton.setText(selectedEndTime);
                    }, hour, minute, false);
            timePickerDialog.show();
        });

        // Save button: update event based on edit option.
        saveButton.setOnClickListener(view -> {
            // Use the actual logged-in session value from SharedPreferences
            String finalSession = userSession.getString("loggedInPin", "");
            String finalTag = tagSpinner.getSelectedItem().toString();
            HelperEvent updatedEvent = new HelperEvent(
                    eventId,
                    titleInput.getText().toString().trim(),
                    descriptionInput.getText().toString().trim(),
                    selectedDate,
                    selectedStartTime,
                    selectedEndTime,
                    finalTag,
                    repeat,
                    occurrence,
                    link,
                    finalSession
            );

            String editOption = editOptionSpinner.getSelectedItem().toString();
            switch (editOption) {
                case "Just this event":
                    eventViewModel.update(updatedEvent);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedEventId", eventId);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                    break;
                case "All repeating events":
                    updateAllRecurringEvents(updatedEvent);
                    break;
                default:
                    eventViewModel.update(updatedEvent);
                    Intent defaultIntent = new Intent();
                    defaultIntent.putExtra("updatedEventId", eventId);
                    setResult(RESULT_OK, defaultIntent);
                    finish();
                    break;
            }
        });
    }

    // Update all recurring events: delete every event with the same linked ID and re-create the full series.
    private void updateAllRecurringEvents(HelperEvent updatedEvent) {
        LiveData<List<HelperEvent>> eventsLiveData = eventViewModel.getEventsByLinkedId(link);
        Observer<List<HelperEvent>> observer = new Observer<List<HelperEvent>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChanged(List<HelperEvent> events) {
                eventsLiveData.removeObserver(this);
                if (events != null) {
                    for (HelperEvent event : events) {
                        eventViewModel.delete(event);
                    }
                    createNewRecurringEvents(updatedEvent);
                } else {
                    finish();
                }
            }
        };
        eventsLiveData.observe(EditEventActivity.this, observer);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNewRecurringEvents(HelperEvent updatedEvent) {
        // Use the consistent date format "yyyy-M-d"
        List<String> recurringDates = getRecurringDates(updatedEvent.getDate(), updatedEvent.getRepeatInterval(), updatedEvent.getOccurrenceCount());
        String newLinkedId = updatedEvent.getLinkedId();
        final String[] firstEventIdHolder = new String[1];

        Executors.newSingleThreadExecutor().execute(() -> {
            for (String date : recurringDates) {
                String newId = UUID.randomUUID().toString();
                if (firstEventIdHolder[0] == null) {
                    firstEventIdHolder[0] = newId;
                }
                HelperEvent newEvent = new HelperEvent(
                        newId,
                        updatedEvent.getTitle(),
                        updatedEvent.getDescription(),
                        date,
                        updatedEvent.getStartTime(),
                        updatedEvent.getEndTime(),
                        updatedEvent.getTag(),
                        updatedEvent.getRepeatInterval(),
                        updatedEvent.getOccurrenceCount(),
                        newLinkedId,
                        userSession.getString("loggedInPin", "")
                );
                eventViewModel.insert(newEvent);
            }
            runOnUiThread(() -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedEventId", firstEventIdHolder[0]);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        });
    }

    // Generate recurring dates from the base date until baseDate + 5 years using the format "yyyy-M-d"
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<String> getRecurringDates(String firstDate, String intervalType, int intervalValue) {
        List<String> recurringDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDate baseDate = LocalDate.parse(firstDate, formatter);
        LocalDate endDate = baseDate.plusYears(5);
        LocalDate currentDate = baseDate;
        while (!currentDate.isAfter(endDate)) {
            recurringDates.add(currentDate.format(formatter));
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
        return recurringDates;
    }

    // Generate exactly 'count' recurring dates starting from firstDate using "yyyy-M-d"
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<String> getRecurringDatesCount(String firstDate, String intervalType, int count) {
        List<String> recurringDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDate currentDate = LocalDate.parse(firstDate, formatter);
        for (int i = 0; i < count; i++) {
            recurringDates.add(currentDate.format(formatter));
            switch (intervalType) {
                case "Every X Days":
                    currentDate = currentDate.plusDays(1);
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
        return recurringDates;
    }
}
