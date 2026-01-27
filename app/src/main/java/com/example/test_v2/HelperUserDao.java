package com.example.test_v2;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HelperUserDao {

    @Insert
    void insert(HelperUserAccount account);

    @Update
    void update(HelperUserAccount account);
    @Query("SELECT * FROM HelperUserAccount WHERE pin = :hashedPin LIMIT 1")
    HelperUserAccount getUserByPin(String hashedPin);

    @Query("SELECT * FROM HelperUserAccount")
    List<HelperUserAccount> getAllUsers();  // Add this method for debugging

    // Add additional queries here if needed in the future
}