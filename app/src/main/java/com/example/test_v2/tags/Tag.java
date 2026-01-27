package com.example.test_v2.tags;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tags")
public class Tag {
    @PrimaryKey
    @NonNull
    public String name;

    public Tag(@NonNull String name) {
        this.name = name;
    }
}
