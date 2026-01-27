package com.example.test_v2.timer;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "timer_events")
public class HelperTimerEvent {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "start_timestamp")
    public long startTimestamp;

    @ColumnInfo(name = "total_time_ms")
    public long totalTimeMs;

    @ColumnInfo(name = "intervals_json")
    public String intervalsJson;


    @ColumnInfo(name = "notes")
    public String notes;

    @ColumnInfo(name = "action_log")
    public String actionLog;

    @ColumnInfo(name = "event_name")
    public String eventName;
}
