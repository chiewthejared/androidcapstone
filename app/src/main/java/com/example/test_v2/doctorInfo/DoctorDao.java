package com.example.test_v2.doctorInfo;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface DoctorDao {

    @Insert
    void insert(DoctorItem doctor);

    @Update
    void update(DoctorItem doctor);

    @Delete
    void delete(DoctorItem doctor);

    @Query("SELECT * FROM doctors WHERE userId = :userId")
    LiveData<List<DoctorItem>> getDoctorsByUser(String userId);
}