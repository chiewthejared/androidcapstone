package com.example.test_v2;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.calendar.EventDetailsActivity;
import com.example.test_v2.calendar.HelperEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WeeklyEventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DATE_HEADER = 0;
    private static final int VIEW_TYPE_EVENT = 1;

    private final Map<String, List<HelperEvent>> groupedEvents;
    private final List<String> sortedDates; // Maintain order of date headers

    public WeeklyEventAdapter(List<HelperEvent> eventList) {
        groupedEvents = groupEventsByDate(eventList);
        sortedDates = new ArrayList<>(groupedEvents.keySet());

        // Debugging log to verify data in adapter
        Log.d("WeeklyAdapter", "Total unique dates: " + sortedDates.size());
        for (String date : sortedDates) {
            Log.d("WeeklyAdapter", "Date: " + date + " -> Events: " + groupedEvents.get(date).size());
        }
    }


    @Override
    public int getItemViewType(int position) {
        // We interleave: 1 header, N events, 1 header, N events...
        int count = 0;
        for (String date : sortedDates) {
            if (position == count) return VIEW_TYPE_DATE_HEADER;
            count++; // header
            List<HelperEvent> events = groupedEvents.get(date);
            if (position < count + events.size()) return VIEW_TYPE_EVENT;
            count += events.size();
        }
        return VIEW_TYPE_DATE_HEADER;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int index = 0;

        for (String date : sortedDates) {
            if (index == position) {
                ((DateHeaderViewHolder) holder).dateHeader.setText(formatDate(date));
                return;
            }
            index++;

            List<HelperEvent> events = groupedEvents.get(date);
            for (HelperEvent event : events) {
                if (index == position && holder instanceof EventViewHolder) {
                    EventViewHolder eventHolder = (EventViewHolder) holder;
                    eventHolder.eventDescription.setText(event.getTitle());
                    String startTime = event.getStartTime() != null ? event.getStartTime() : "No Start Time";
                    String endTime = event.getEndTime() != null ? event.getEndTime() : "No End Time";
                    eventHolder.eventTime.setText(startTime + " - " + endTime);

                    if (event.getTag() != null && !event.getTag().isEmpty() && !event.getTag().equals("None")) {
                        eventHolder.eventTag.setText("Tag: " + event.getTag());
                        eventHolder.eventTag.setVisibility(View.VISIBLE);
                    } else {
                        eventHolder.eventTag.setVisibility(View.GONE);
                    }

                    // Set background and click listener
                    eventHolder.itemView.setBackgroundColor(getColorForTag(event.getTag()));
                    eventHolder.itemView.setOnClickListener(v -> {
                        Intent intent = new Intent(v.getContext(), EventDetailsActivity.class);
                        intent.putExtra("eventId", event.getID());
                        v.getContext().startActivity(intent);
                    });
                    return;
                }
                index++;
            }
        }
    }




    @Override
    public int getItemCount() {
        int count = 0;
        for (String date : sortedDates) {
            count += 1; // header
            count += groupedEvents.get(date).size(); // events
        }
        return count;
    }


    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView dateHeader;

        public DateHeaderViewHolder(View itemView) {
            super(itemView);
            dateHeader = itemView.findViewById(R.id.dateHeader);
        }
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventDescription, eventTime, eventTag;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventDescription = itemView.findViewById(R.id.eventTitle);
            eventTime = itemView.findViewById(R.id.eventTime);
            eventTag = itemView.findViewById(R.id.eventTag);
        }
    }

    private Map<String, List<HelperEvent>> groupEventsByDate(List<HelperEvent> eventList) {
        Map<String, List<HelperEvent>> groupedEvents = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");

        // Get the current week start (assumes you’re showing the current week)
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() % 7);

        // Add all 7 days as keys
        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            String dateStr = day.format(formatter);
            groupedEvents.put(dateStr, new ArrayList<>());
        }

        // Assign events to the correct date
        for (HelperEvent event : eventList) {
            String eventDate = event.getDate();
            if (groupedEvents.containsKey(eventDate)) {
                groupedEvents.get(eventDate).add(event);
            }
        }

        return groupedEvents;
    }


    private String formatDate(String date) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        return LocalDate.parse(date, inputFormatter).format(outputFormatter);
    }

    private int getColorForTag(String tag) {
        switch (tag) {
            case "work":
                return Color.RED;
            case "personal":
                return Color.GREEN;
            case "urgent":
                return Color.YELLOW;
            default:
                return Color.GRAY;
        }
    }
}