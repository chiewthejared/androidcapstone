package com.example.test_v2.doctorInfo;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DoctorItem.class}, version = 1, exportSchema = false)
public abstract class DoctorDatabase extends RoomDatabase {
    private static DoctorDatabase INSTANCE;

    public abstract DoctorDao doctorDao();

    public static synchronized DoctorDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DoctorDatabase.class, "doctor_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}