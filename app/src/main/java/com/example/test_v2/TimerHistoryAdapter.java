package com.example.test_v2;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;
import com.example.test_v2.calendar.HelperEvent;
import com.example.test_v2.fileAndDatabase.HelperAppDatabase;
import com.example.test_v2.timer.HelperTimerEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Adapter for displaying and managing Timer history items.
 */
public class TimerHistoryAdapter extends RecyclerView.Adapter<TimerHistoryAdapter.ViewHolder> {

    private List<HelperTimerEvent> events;
    private OnItemActionListener listener;
    private Context context;
    private HelperAppDatabase db;

    public interface OnItemActionListener {
        // "Add Notes"
        void onEdit(HelperTimerEvent event);
        // "Delete"
        void onDelete(HelperTimerEvent event);
    }

    public TimerHistoryAdapter(Context context, List<HelperTimerEvent> events, OnItemActionListener listener) {
        this.context = context;
        this.events = events;
        this.listener = listener;
        this.db = HelperAppDatabase.getDatabase(context.getApplicationContext());
    }

    public void setEvents(List<HelperTimerEvent> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimerHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timer_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimerHistoryAdapter.ViewHolder holder, int position) {
        HelperTimerEvent event = events.get(position);

        // Show eventName or default to "Timer Event"
        String nameToShow = (event.eventName == null || event.eventName.isEmpty())
                ? "Timer Event"
                : event.eventName;
        holder.eventNameText.setText(nameToShow);

        // Clicking the name => rename dialog
        holder.eventNameText.setOnClickListener(v -> {
            showRenameEventDialog(v, event);
        });

        // Show "Started: ..." line with total time appended
        Date date = new Date(event.startTimestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateStr = sdf.format(date);

        // Convert totalTimeMs => HH:MM:SS
        long totalSecs = event.totalTimeMs / 1000;
        int sec = (int)(totalSecs % 60);
        int min = (int)((totalSecs / 60) % 60);
        int hrs = (int)(totalSecs / 3600);
        String timeString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, min, sec);

        holder.dateText.setText("Started: " + dateStr + " (" + timeString + " total)");

        // Add Notes
        holder.addNotesButton.setOnClickListener(v -> {
            listener.onEdit(event);
        });

        // View Events
        holder.viewEventsButton.setOnClickListener(v -> {
            showActionLogDialog(v, event.actionLog);
        });

        // Delete
        holder.deleteButton.setOnClickListener(v -> {
            listener.onDelete(event);
        });

        // ============================
        // ADD TO CALENDAR FUNCTION
        // ============================
        holder.addToCalendarButton.setOnClickListener(v -> {
            String eventName = event.eventName;
            if (eventName == null || eventName.isEmpty()) {
                eventName = "Timer Event";
            }

            long startMs = event.startTimestamp;
            Date dateObj = new Date(startMs);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

            String dateStringLocal = dateFormat.format(dateObj);
            String timeStringLocal = timeFormat.format(dateObj);

            String currentUserSession = event.userId;
            if (currentUserSession == null || currentUserSession.isEmpty()) {
                currentUserSession = "-1";
            }

            // 1) Use the existing actionLog as the description
            String descForCalendar = event.actionLog;
            if (descForCalendar == null || descForCalendar.isEmpty()) {
                descForCalendar = "Imported from Timer";
                // fallback if there's no actionLog
            }

            // 2) Create the new HelperEvent, passing the actionLog as description
            HelperEvent calendarEvent = new HelperEvent(
                    UUID.randomUUID().toString(),
                    eventName,
                    descForCalendar,
                    dateStringLocal,
                    timeStringLocal,
                    timeStringLocal,
                    "None",
                    "No Repeat",
                    1,
                    UUID.randomUUID().toString(),
                    currentUserSession
            );

            new Thread(() -> {
                HelperAppDatabase dbRef = HelperAppDatabase.getDatabase(context.getApplicationContext());
                dbRef.eventDao().insert(calendarEvent);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Event added to Calendar!", Toast.LENGTH_SHORT).show()
                );
            }).start();
        });
    }

    @Override
    public int getItemCount() {
        return (events == null) ? 0 : events.size();
    }

    private void showRenameEventDialog(View anchor, HelperTimerEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(anchor.getContext());
        builder.setTitle("Rename Timer Event");

        final EditText input = new EditText(anchor.getContext());
        input.setText(event.eventName == null ? "" : event.eventName);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                event.eventName = newName;
                // Update in DB
                new Thread(() -> {
                    db.timerEventDao().update(event);
                    ((View) anchor.getParent()).post(this::notifyDataSetChanged);
                }).start();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showActionLogDialog(View anchor, String actionLog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(anchor.getContext());
        builder.setTitle("Recorded Events");
        builder.setMessage(
                (actionLog == null || actionLog.isEmpty())
                        ? "No events recorded."
                        : actionLog
        );
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameText, dateText;
        Button addNotesButton, viewEventsButton, deleteButton;
        Button addToCalendarButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameText       = itemView.findViewById(R.id.timer_event_name);
            dateText            = itemView.findViewById(R.id.timer_item_date);
            addNotesButton      = itemView.findViewById(R.id.add_notes_button);
            viewEventsButton    = itemView.findViewById(R.id.view_events_button);
            deleteButton        = itemView.findViewById(R.id.delete_button);
            addToCalendarButton = itemView.findViewById(R.id.add_to_calendar_button);
        }
    }
}
