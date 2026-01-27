package com.example.test_v2.calendar;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;
import com.example.test_v2.calendar.EventDetailsActivity;
import com.example.test_v2.calendar.HelperEvent;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<HelperEvent> eventList;

    public EventAdapter(List<HelperEvent> eventList) {
        this.eventList = (eventList != null) ? eventList : new ArrayList<>();
    }


    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventDescription, eventTime, eventTag; // Added eventTag

        public EventViewHolder(View itemView) {
            super(itemView);
            eventDescription = itemView.findViewById(R.id.eventTitle);
            eventTime = itemView.findViewById(R.id.eventTime);
            eventTag = itemView.findViewById(R.id.eventTag); // Make sure this ID matches XML
        }
    }


    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        HelperEvent event = eventList.get(position);
        holder.eventDescription.setText(event.getTitle());
        String startTime = event.getStartTime() != null ? event.getStartTime() : "No Start Time";
        String endTime = event.getEndTime() != null ? event.getEndTime() : "No End Time";
        String timeRange = startTime + " - " + endTime;
        holder.eventTime.setText(timeRange);
        if (event.getTag() != null && !event.getTag().isEmpty() && !event.getTag().equals("None")) {
            holder.eventTag.setText("Tag: " + event.getTag());
            holder.eventTag.setVisibility(View.VISIBLE);
        } else {
            holder.eventTag.setVisibility(View.GONE); // Hide tag if none exists
        }
        // Set color based on event tag
        int color = getColorForTag(event.getTag());
        holder.itemView.setBackgroundColor(color);

        // Open event details when clicking an event
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EventDetailsActivity.class);
            intent.putExtra("eventId", event.getID().toString()); // Pass UUID for better event retrieval
            v.getContext().startActivity(intent);
        });


    }



    @Override
    public int getItemCount() {
        return eventList.size();
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