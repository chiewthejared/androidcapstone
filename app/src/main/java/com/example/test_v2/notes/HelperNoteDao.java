package com.example.test_v2.notes;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface HelperNoteDao {
    @Insert
    void insert(HelperNote note);

    @Update
    void update(HelperNote note);

    @Delete
    void delete(HelperNote note);

    @Query("SELECT * FROM notes WHERE user_id = :userId ORDER BY created_at DESC")
    List<HelperNote> getAllNotesForUser(String userId);

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    HelperNote getNoteById(int noteId);
}
