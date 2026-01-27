package com.example.test_v2.timer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HelperTimerEventDao {

    @Insert
    void insert(HelperTimerEvent event);

    @Update
    void update(HelperTimerEvent event);

    @Query("SELECT * FROM timer_events WHERE user_id = :userId ORDER BY id DESC")
    List<HelperTimerEvent> getAllEventsForUser(String userId);

    @Query("SELECT * FROM timer_events ORDER BY id DESC")
    List<HelperTimerEvent> getAllEvents();

    @Query("SELECT * FROM timer_events WHERE id = :id LIMIT 1")
    HelperTimerEvent getEventById(int id);

    @Query("DELETE FROM timer_events WHERE id = :id")
    void deleteById(int id);
}
