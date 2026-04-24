package com.swf.capstone26.calendar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.swf.capstone26.fileAndDatabase.HelperAppDatabase;
import com.swf.capstone26.R;
import com.swf.capstone26.doctorInfo.DoctorDatabase;
import com.swf.capstone26.doctorInfo.DoctorItem;
import com.swf.capstone26.tags.Tag;
import com.swf.capstone26.tags.TagDao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class EditEventActivity extends AppCompatActivity {

    private EditText titleInput, descriptionInput, tagInput;
    private Button eventDateButton, eventStartTimeButton, eventEndTimeButton, saveButton;
    private Spinner tagSpinner, editOptionSpinner, spinnerDoctor;
    private CheckBox cbDoctorAppointment;
    private LinearLayout layoutDoctorSection;
    private EditText etDoctorNameManual;
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
    private boolean isNewEvent = false;

    // Doctor list from DB
    private List<DoctorItem> doctorList = new ArrayList<>();
    private ArrayAdapter<String> doctorAdapter;

    private void loadTagsFromDatabase(Spinner tagSpinner, String selectedTag) {
        new Thread(() -> {
            TagDao tagDao = HelperAppDatabase.getDatabase(this).tagDao();
            List<Tag> dbTags = tagDao.getAll();
            List<String> tagNames = new ArrayList<>();
            tagNames.add("None");
            for (Tag t : dbTags) tagNames.add(t.name);
            runOnUiThread(() -> {
                tagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tagNames);
                tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                tagSpinner.setAdapter(tagAdapter);
                int position = tagAdapter.getPosition(selectedTag);
                if (position >= 0) tagSpinner.setSelection(position);
            });
        }).start();
    }

    private void loadDoctorsFromDatabase() {
        String myPin = userSession.getString("loggedInPin", "");
        DoctorDatabase.getDatabase(this).doctorDao()
                .getDoctorsByUser(myPin)
                .observe(this, doctors -> {
                    doctorList.clear();
                    List<String> names = new ArrayList<>();
                    names.add("Select a doctor...");
                    if (doctors != null) {
                        doctorList.addAll(doctors);
                        for (DoctorItem d : doctors) names.add(d.name);
                    }
                    doctorAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, names);
                    doctorAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    spinnerDoctor.setAdapter(doctorAdapter);
                });
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        userSession = getSharedPreferences("UserSession", MODE_PRIVATE);

        titleInput = findViewById(R.id.eventTitleInput);
        descriptionInput = findViewById(R.id.eventDescriptionInput);
        eventDateButton = findViewById(R.id.eventDateButton);
        eventStartTimeButton = findViewById(R.id.eventStartTimeButton);
        eventEndTimeButton = findViewById(R.id.eventEndTimeButton);
        tagSpinner = findViewById(R.id.eventTagSpinner);
        tagInput = findViewById(R.id.eventTagInput);
        saveButton = findViewById(R.id.saveEventButton);
        editOptionSpinner = findViewById(R.id.eventEditOptionSpinner);
        cbDoctorAppointment = findViewById(R.id.cbDoctorAppointment);
        layoutDoctorSection = findViewById(R.id.layoutDoctorSection);
        spinnerDoctor = findViewById(R.id.spinnerDoctor);
        etDoctorNameManual = findViewById(R.id.etDoctorNameManual);

        Button cancelButton = findViewById(R.id.cancelEventButton);
        cancelButton.setOnClickListener(v -> finish());

        // Doctor checkbox toggle
        cbDoctorAppointment.setOnCheckedChangeListener((btn, checked) -> {
            layoutDoctorSection.setVisibility(checked ? View.VISIBLE : View.GONE);
        });

        // Load doctors
        loadDoctorsFromDatabase();

        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        eventId = getIntent().getStringExtra("uuid");

        if (eventId == null || eventId.isEmpty()) {
            // ===== 新建模式 =====
            isNewEvent = true;
            eventId = UUID.randomUUID().toString();
            link = UUID.randomUUID().toString();
            repeat = "No Repeat";
            occurrence = 1;
            editOptionSpinner.setVisibility(View.GONE);

            String prefilledDate = getIntent().getStringExtra("prefilledDate");
            if (prefilledDate != null) {
                selectedDate = prefilledDate;
                eventDateButton.setText(selectedDate);
            }

            loadTagsFromDatabase(tagSpinner, "None");

        } else {
            // ===== 编辑模式 =====
            isNewEvent = false;
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
                    editOptionSpinner.setVisibility(
                            "No Repeat".equals(event.getRepeatInterval()) ? View.GONE : View.VISIBLE
                    );
                }
            });
        }

        // Date picker
        eventDateButton.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this,
                    (dateView, y, m, d) -> {
                        selectedDate = y + "-" + (m + 1) + "-" + d;
                        eventDateButton.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Start time picker
        eventStartTimeButton.setOnClickListener(view -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (tv, h, min) -> {
                int h12 = h % 12 == 0 ? 12 : h % 12;
                String ap = h < 12 ? "AM" : "PM";
                selectedStartTime = String.format("%02d:%02d %s", h12, min, ap);
                eventStartTimeButton.setText(selectedStartTime);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
        });

        // End time picker
        eventEndTimeButton.setOnClickListener(view -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (tv, h, min) -> {
                int h12 = h % 12 == 0 ? 12 : h % 12;
                String ap = h < 12 ? "AM" : "PM";
                selectedEndTime = String.format("%02d:%02d %s", h12, min, ap);
                eventEndTimeButton.setText(selectedEndTime);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
        });

        // Save button
        saveButton.setOnClickListener(view -> {
            String finalSession = userSession.getString("loggedInPin", "");
            String finalTag = tagSpinner.getSelectedItem().toString();

            // 处理 doctor appointment 标题
            String title = titleInput.getText().toString().trim();
            if (cbDoctorAppointment.isChecked()) {
                String manualName = etDoctorNameManual.getText().toString().trim();
                if (!manualName.isEmpty()) {
                    title = "Dr. " + manualName + (title.isEmpty() ? "" : " - " + title);
                } else if (spinnerDoctor.getSelectedItemPosition() > 0) {
                    DoctorItem selected = doctorList.get(spinnerDoctor.getSelectedItemPosition() - 1);
                    title = "Dr. " + selected.name + (title.isEmpty() ? "" : " - " + title);
                }
                // doctor appointment 也用蓝色，tag 标记为 "DoctorAppointment"
                finalTag = "DoctorAppointment";
            }

            HelperEvent updatedEvent = new HelperEvent(
                    eventId,
                    title,
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

            if (isNewEvent) {
                eventViewModel.insert(updatedEvent);
                setResult(RESULT_OK);
                finish();
                return;
            }

            String editOption = editOptionSpinner.getSelectedItem().toString();
            switch (editOption) {
                case "Just this event":
                    eventViewModel.update(updatedEvent);
                    Intent r1 = new Intent();
                    r1.putExtra("updatedEventId", eventId);
                    setResult(RESULT_OK, r1);
                    finish();
                    break;
                case "All repeating events":
                    updateAllRecurringEvents(updatedEvent);
                    break;
                default:
                    eventViewModel.update(updatedEvent);
                    Intent r2 = new Intent();
                    r2.putExtra("updatedEventId", eventId);
                    setResult(RESULT_OK, r2);
                    finish();
                    break;
            }
        });
    }

    private void updateAllRecurringEvents(HelperEvent updatedEvent) {
        LiveData<List<HelperEvent>> eventsLiveData = eventViewModel.getEventsByLinkedId(link);
        Observer<List<HelperEvent>> observer = new Observer<List<HelperEvent>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChanged(List<HelperEvent> events) {
                eventsLiveData.removeObserver(this);
                if (events != null) {
                    for (HelperEvent event : events) eventViewModel.delete(event);
                    createNewRecurringEvents(updatedEvent);
                } else {
                    finish();
                }
            }
        };
        eventsLiveData.observe(this, observer);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNewRecurringEvents(HelperEvent updatedEvent) {
        List<String> recurringDates = getRecurringDates(
                updatedEvent.getDate(),
                updatedEvent.getRepeatInterval(),
                updatedEvent.getOccurrenceCount());
        String newLinkedId = updatedEvent.getLinkedId();
        final String[] firstId = new String[1];

        Executors.newSingleThreadExecutor().execute(() -> {
            for (String date : recurringDates) {
                String newId = UUID.randomUUID().toString();
                if (firstId[0] == null) firstId[0] = newId;
                eventViewModel.insert(new HelperEvent(
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
                ));
            }
            runOnUiThread(() -> {
                Intent r = new Intent();
                r.putExtra("updatedEventId", firstId[0]);
                setResult(RESULT_OK, r);
                finish();
            });
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<String> getRecurringDates(String firstDate, String intervalType, int intervalValue) {
        List<String> dates = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDate base = LocalDate.parse(firstDate, fmt);
        LocalDate end = base.plusYears(5);
        LocalDate cur = base;
        while (!cur.isAfter(end)) {
            dates.add(cur.format(fmt));
            switch (intervalType) {
                case "Every X Days": cur = cur.plusDays(intervalValue); break;
                case "Every Week":   cur = cur.plusWeeks(1); break;
                case "Every Month":  cur = cur.plusMonths(1); break;
                case "Every Year":   cur = cur.plusYears(1); break;
                default: throw new IllegalArgumentException("Invalid interval type");
            }
        }
        return dates;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<String> getRecurringDatesCount(String firstDate, String intervalType, int count) {
        List<String> dates = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDate cur = LocalDate.parse(firstDate, fmt);
        for (int i = 0; i < count; i++) {
            dates.add(cur.format(fmt));
            switch (intervalType) {
                case "Every X Days": cur = cur.plusDays(1); break;
                case "Every Week":   cur = cur.plusWeeks(1); break;
                case "Every Month":  cur = cur.plusMonths(1); break;
                case "Every Year":   cur = cur.plusYears(1); break;
                default: throw new IllegalArgumentException("Invalid interval type");
            }
        }
        return dates;
    }
}