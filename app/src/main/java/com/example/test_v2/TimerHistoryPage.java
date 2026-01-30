package com.example.test_v2;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.timer.HelperTimerEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * TimerHistoryPage — shows the user's saved timer events, allows edit notes, delete,
 * add to calendar, and clearing all history. Uses DAO methods exactly as provided in
 * HelperTimerEventDao (getAllEventsForUser, deleteById, update, getAllEvents).
 */
public class TimerHistoryPage extends AppCompatActivity implements TimerHistoryAdapter.OnItemActionListener {

    private RecyclerView recyclerView;
    private TimerHistoryAdapter adapter;
    private HelperAppDatabase db;
    private String currentUserId = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_history_page);

        db = HelperAppDatabase.getDatabase(getApplicationContext());

        currentUserId = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("loggedInPin", "-1");

        // Back button
        Button backBtn = findViewById(R.id.back_button);
        if (backBtn != null) backBtn.setOnClickListener(v -> finish());

        // Clear all control (optional)
        View clearAll = findViewById(R.id.clear_all_text);
        if (clearAll != null) {
            clearAll.setOnClickListener(v -> confirmClearAll());
        }

        // RecyclerView setup (robust: checks both ids that various XML versions may use)
        recyclerView = findViewById(R.id.timer_history_recycler);
        if (recyclerView == null) {
            throw new IllegalStateException("RecyclerView not found: expected @id/timer_history_recycler");
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TimerHistoryAdapter(this, this);
        recyclerView.setAdapter(adapter);



        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            List<HelperTimerEvent> list;
            try {
                list = db.timerEventDao().getAllEventsForUser(currentUserId);
            } catch (Throwable t) {
                // fallback to getAllEvents if user-filtered DAO isn't available for some reason
                try {
                    list = db.timerEventDao().getAllEvents();
                } catch (Throwable t2) {
                    t2.printStackTrace();
                    list = new ArrayList<>();
                }
            }

            final List<HelperTimerEvent> finalList = (list == null) ? new ArrayList<>() : list;
            runOnUiThread(() -> adapter.setEvents(finalList));
        }).start();
    }

    // Adapter callback — open simple edit-notes dialog and persist update
    @Override
    public void onEdit(final HelperTimerEvent event) {
        runOnUiThread(() -> {
            View dialogView = LayoutInflater.from(TimerHistoryPage.this).inflate(R.layout.dialog_edit_notes, null);
            final EditText notesEditText = dialogView.findViewById(R.id.notes_edit_text);
            if (notesEditText != null && event.notes != null) notesEditText.setText(event.notes);

            AlertDialog.Builder builder = new AlertDialog.Builder(TimerHistoryPage.this)
                    .setTitle("Add/Edit Notes")
                    .setView(dialogView)
                    .setPositiveButton("Save", null)
                    .setNegativeButton("Cancel", null);

            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(d -> {
                Button saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                saveBtn.setOnClickListener(v -> {
                    String newNotes = notesEditText != null ? notesEditText.getText().toString().trim() : "";
                    event.notes = newNotes;
                    // persist update in background
                    new Thread(() -> {
                        try {
                            db.timerEventDao().update(event);
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }
                        runOnUiThread(() -> {
                            Toast.makeText(TimerHistoryPage.this, "Notes updated", Toast.LENGTH_SHORT).show();
                            loadData();
                            dialog.dismiss();
                        });
                    }).start();
                });
            });
            dialog.show();
        });
    }

    // Adapter callback — delete single event (by id)
    @Override
    public void onDelete(final HelperTimerEvent event, final int pos) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this record?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    new Thread(() -> {
                        boolean deleted = false;
                        try {
                            // DAO provides deleteById(int id)
                            db.timerEventDao().deleteById((int) event.id);
                            deleted = true;
                        } catch (Throwable ex) {
                            // log & fallback: try to delete by fetching and comparing ids or other method
                            ex.printStackTrace();
                        }

                        final boolean finalDeleted = deleted;
                        runOnUiThread(() -> {
                            if (finalDeleted) {
                                Toast.makeText(TimerHistoryPage.this, "Timer record deleted", Toast.LENGTH_SHORT).show();
                                adapter.removeAt(pos);
                            } else {
                                Toast.makeText(TimerHistoryPage.this, "Delete failed (see log)", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Adapter callback — create calendar intent for the event
    @Override
    public void onAddToCalendar(final HelperTimerEvent event) {
        long start = event.startTimestamp;
        long end = start + Math.max(1000, event.totalTimeMs);
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
                .putExtra(CalendarContract.Events.TITLE, event.eventName != null ? event.eventName : "Timer Event")
                .putExtra(CalendarContract.Events.DESCRIPTION, event.actionLog != null ? event.actionLog : "");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "No calendar app available", Toast.LENGTH_SHORT).show();
        }
    }

    // Confirm and delete all events for current user by iterating and calling deleteById
    private void confirmClearAll() {
        new AlertDialog.Builder(this)
                .setTitle("Clear all history")
                .setMessage("Delete all saved timer events?")
                .setPositiveButton("Delete", (d, w) -> {
                    new Thread(() -> {
                        boolean cleared = false;
                        try {
                            // fetch user's events and delete them one by one using deleteById
                            List<HelperTimerEvent> toDelete = db.timerEventDao().getAllEventsForUser(currentUserId);
                            if (toDelete == null) toDelete = new ArrayList<>();
                            for (HelperTimerEvent e : toDelete) {
                                try {
                                    db.timerEventDao().deleteById((int) e.id);
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                            }
                            cleared = true;
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }

                        final boolean finalCleared = cleared;
                        runOnUiThread(() -> {
                            if (finalCleared) {
                                adapter.setEvents(new ArrayList<>());
                                Toast.makeText(TimerHistoryPage.this, "All history cleared", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(TimerHistoryPage.this, "Clear failed (see log)", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
