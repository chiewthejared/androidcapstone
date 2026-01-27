package com.example.test_v2.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.test_v2.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {
    private static final int EDIT_EVENT_REQUEST_CODE = 100;
    private TextView titleTextView, tagTextView, timeTextView, descriptionTextView;
    private Button editEventButton, deleteEventButton;
    private EventViewModel eventViewModel;
    // Holds the current event ID being observed.
    private String currentEventId;
    private TextView eventDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        titleTextView = findViewById(R.id.eventDetailTitle);
        timeTextView = findViewById(R.id.eventDetailTime);
        tagTextView = findViewById(R.id.eventDetailTag);
        descriptionTextView = findViewById(R.id.eventDetailDescription);
        editEventButton = findViewById(R.id.editEventButton);
        deleteEventButton = findViewById(R.id.deleteEventButton);
        eventDate = findViewById(R.id.eventDetailDate);
        Button backButton = findViewById(R.id.backButton);


        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        String eventIdString = getIntent().getStringExtra("eventId");
        if (eventIdString == null || eventIdString.isEmpty()) {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Set the current event ID.
        currentEventId = eventIdString;

        // Observe the event details.
        observeEvent(currentEventId);

        backButton.setOnClickListener(v -> finish()); // Closes the activity and returns to the previous screen

        editEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, EditEventActivity.class);
            intent.putExtra("uuid", currentEventId);
            startActivityForResult(intent, EDIT_EVENT_REQUEST_CODE);
        });

        deleteEventButton.setOnClickListener(v -> {
            eventViewModel.getEventById(currentEventId).observe(this, event -> {
                if (event != null) {
                    if ("No Repeat".equals(event.getRepeatInterval())) {
                        // If the event does NOT repeat, just ask for a simple delete confirmation
                        new android.app.AlertDialog.Builder(EventDetailsActivity.this)
                                .setTitle("Delete Event")
                                .setMessage("Are you sure you want to delete this event?")
                                .setPositiveButton("Yes", (dialog, which) -> deleteSingleEvent(currentEventId))
                                .setNegativeButton("No", null)
                                .show();
                    } else {
                        // If the event is part of a repeating series, ask about deleting all occurrences
                        new android.app.AlertDialog.Builder(EventDetailsActivity.this)
                                .setTitle("Delete Event")
                                .setMessage("Do you want to delete only this event or all repeating events?")
                                .setPositiveButton("Only this event", (dialog, which) -> deleteSingleEvent(currentEventId))
                                .setNegativeButton("All repeating events", (dialog, which) -> deleteRecurringEvents(currentEventId))
                                .setNeutralButton("Cancel", null)
                                .show();
                    }
                }
            });
        });


    }

    // Helper method to observe an event by its ID.
    private void observeEvent(String eventId) {
        eventViewModel.getEventById(eventId).observe(this, new Observer<HelperEvent>() {
            @Override
            public void onChanged(HelperEvent event) {
                if (event != null) {
                    titleTextView.setText(event.getTitle());
                    timeTextView.setText(event.getStartTime() + " - " + event.getEndTime());
                    tagTextView.setText(event.getTag());
                    descriptionTextView.setText(event.getDescription());
                    String formattedDate = formatDate(event.getDate());
                    eventDate.setText(formattedDate);

// 👇 Hide buttons if linkedId matches
                    if ("DONT SHOW EDIT/DELETE".equals(event.getLinkedId())) {
                        editEventButton.setVisibility(View.GONE);
                        deleteEventButton.setVisibility(View.GONE);
                    } else {
                        editEventButton.setVisibility(View.VISIBLE);
                        deleteEventButton.setVisibility(View.VISIBLE);
                    }

                } else {
                    Toast.makeText(EventDetailsActivity.this, "Event not found!", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    // Helper method to delete an event.
    private void deleteEvent(String eventId) {
        eventViewModel.getEventById(eventId).observe(this, event -> {
            if (event != null) {
                eventViewModel.delete(event);
                Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error deleting event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // When EditEventActivity returns a result, update the current event ID and re-observe the event.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_EVENT_REQUEST_CODE && resultCode == RESULT_OK) {
            String newEventId = data.getStringExtra("updatedEventId");
            if (newEventId != null && !newEventId.isEmpty()) {
                currentEventId = newEventId;
                observeEvent(currentEventId);
            } else {
                Toast.makeText(EventDetailsActivity.this, "Updated event not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteSingleEvent(String eventId) {
        eventViewModel.getEventById(eventId).observe(this, event -> {
            if (event != null) {
                eventViewModel.delete(event);
                Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error deleting event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRecurringEvents(String eventId) {
        eventViewModel.getEventById(eventId).observe(this, event -> {
            if (event != null) {
                if (event.getLinkedId() != null && !event.getLinkedId().isEmpty()) {
                    eventViewModel.deleteEventsByLinkedId(event.getLinkedId()); // NOW RUNS IN BACKGROUND
                    Toast.makeText(this, "All recurring events deleted", Toast.LENGTH_SHORT).show();
                } else {
                    eventViewModel.delete(event);
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                }
                finish();
            } else {
                Toast.makeText(this, "Error deleting events", Toast.LENGTH_SHORT).show();
            }
        });
    }




}
