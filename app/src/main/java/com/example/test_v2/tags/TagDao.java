package com.example.test_v2.tags;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TagDao {
    @Query("SELECT * FROM tags")
    List<Tag> getAll();

    @Insert
    void insert(Tag tag);

    @Delete
    void delete(Tag tag);
}
