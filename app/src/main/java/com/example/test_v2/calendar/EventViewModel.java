package com.example.test_v2.calendar;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.test_v2.fileAndDatabase.HelperAppDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventViewModel extends AndroidViewModel {
    private HelperEventRepository eventRepository;

    public EventViewModel(Application application) {
        super(application);
        eventRepository = new HelperEventRepository(application);
    }

    public LiveData<HelperEvent> getEventById(String eventId) {
        return eventRepository.getEventById(eventId);
    }

    public LiveData<List<HelperEvent>> getAllEvents() {
        return eventRepository.getAllEvents();
    }

    public void insert(HelperEvent event) {
        eventRepository.insert(event);
    }


    public void delete(HelperEvent event) {
        eventRepository.delete(event);
    }



    public void updateAllFollowingRepeatingEvents(String linkedId, HelperEvent updatedEvent, String selectedDate) {
        // Observe the LiveData returned by the Room database
        eventRepository.getEventsByLinkedId(linkedId).observeForever(events -> {
            if (events != null) {
                for (HelperEvent event : events) {
                    if (isFutureEvent(event, selectedDate)) {
                        event.setTitle(updatedEvent.getTitle());
                        event.setDescription(updatedEvent.getDescription());
                        event.setStartTime(updatedEvent.getStartTime());
                        event.setEndTime(updatedEvent.getEndTime());
                        event.setTag(updatedEvent.getTag());
                        event.setDate(updatedEvent.getDate()); // or update according to your logic
                        eventRepository.update(event); // Update event in the database
                    }
                }
            }
        });
    }

    // Helper method to check if the event is a future event
    private boolean isFutureEvent(HelperEvent event, String selectedDate) {
        // Convert the event date and selectedDate to Date objects and compare
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date eventDate = format.parse(event.getDate());
            Date selectedDateParsed = format.parse(selectedDate);
            return eventDate.after(selectedDateParsed);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void update(HelperEvent event) {
        Log.d("EventViewModel", "Updating event: " + event.getID());

        // Ensure the event is not null
        if (event != null) {
            // Assuming you have access to a Room database instance and EventDao
            HelperEventDao eventDao = HelperAppDatabase.getDatabase(getApplication()).eventDao();

            // Run the update operation in a background thread
            new Thread(() -> {
                // Perform the update operation in the database
                eventDao.update(event);

                // Log the success
                Log.d("EventViewModel", "Event updated successfully: " + event.getID());
            }).start();
        } else {
            Log.e("EventViewModel", "Event is null, cannot update.");
        }
    }


    public LiveData<List<HelperEvent>> getEventsByLinkedId(String linkedId) {
        return eventRepository.getEventsByLinkedId(linkedId);
    }

    public LiveData<List<HelperEvent>> getFutureEventsByLinkedId(String linkedId, String currentDate) {
        return eventRepository.getFutureEventsByLinkedId(linkedId, currentDate);
    }

    public void deleteEventsByLinkedId(String linkedId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> eventRepository.deleteEventsByLinkedId(linkedId));
    }






}
