package com.example.test_v2.notes;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "notes")
public class HelperNote {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "file_path")
    public String filePath; // Stores file URI if applicable

    @ColumnInfo(name = "created_at")
    public String createdAt;

    @ColumnInfo(name = "type")
    public String type;
}
