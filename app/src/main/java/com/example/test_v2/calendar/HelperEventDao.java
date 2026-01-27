package com.example.test_v2.calendar;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HelperEventDao {
    @Insert
    void insert(HelperEvent event);

    @Update
    void update(HelperEvent event);

    @Delete
    void delete(HelperEvent event);

    @Query("SELECT * FROM events WHERE ID = :eventId LIMIT 1")
    LiveData<HelperEvent> getEventById(String eventId);

    @Query("SELECT * FROM events WHERE linkedId = :linkedId")
    LiveData<List<HelperEvent>> getEventsByLinkedIdSync(String linkedId);


    @Query("SELECT * FROM events WHERE linkedId = :linkedId AND date >= :currentDate")
    LiveData<List<HelperEvent>> getFutureEventsByLinkedId(String linkedId, String currentDate);


    @Query("SELECT * FROM events")
    LiveData<List<HelperEvent>> getAllEvents();

    @Query("DELETE FROM events WHERE linkedId = :linkedId")
    void deleteEventsByLinkedId(String linkedId);

    @Query("SELECT * FROM events")
    List<HelperEvent> getAllEventsRaw();

    @Query("SELECT * FROM events WHERE userSession = :session")
    List<HelperEvent> getAllEventsRawForUser(String session);




}
