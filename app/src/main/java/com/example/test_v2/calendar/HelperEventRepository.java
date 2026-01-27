package com.example.test_v2.calendar;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.test_v2.fileAndDatabase.HelperAppDatabase;

import java.util.List;

public class HelperEventRepository {

    private HelperEventDao helperEventDao;
    private LiveData<List<HelperEvent>> allEvents;

    public HelperEventRepository(Application application) {
        HelperAppDatabase db = HelperAppDatabase.getDatabase(application);
        helperEventDao = db.eventDao();
        allEvents = helperEventDao.getAllEvents();
    }

    // Insert a new event
    public void insert(HelperEvent event) {
        HelperAppDatabase.getDatabaseWriteExecutor().execute(() -> helperEventDao.insert(event));
    }

    // Update an existing event
    public void update(HelperEvent event) {
        HelperAppDatabase.getDatabaseWriteExecutor().execute(() -> helperEventDao.update(event));
    }

    // Delete an event
    public void delete(HelperEvent event) {
        HelperAppDatabase.getDatabaseWriteExecutor().execute(() -> helperEventDao.delete(event));
    }

    // Get event by its ID
    public LiveData<HelperEvent> getEventById(String eventId) {
        return helperEventDao.getEventById(eventId);
    }

    // Get all events
    public LiveData<List<HelperEvent>> getAllEvents() {
        return allEvents;
    }


    // Method to get all events by linkedId
    public LiveData<List<HelperEvent>> getEventsByLinkedId(String linkedId) {
        return helperEventDao.getEventsByLinkedIdSync(linkedId);
    }

    // Method to get future events by linkedId (for "following" events)
    public LiveData<List<HelperEvent>> getFutureEventsByLinkedId(String linkedId, String currentDate) {
        return helperEventDao.getFutureEventsByLinkedId(linkedId, currentDate);
    }
    public void deleteEventsByLinkedId(String linkedId) {
        HelperAppDatabase.getDatabaseWriteExecutor().execute(() -> helperEventDao.deleteEventsByLinkedId(linkedId));
    }




}
