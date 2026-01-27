package com.example.test_v2.calendar;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "events")
public class HelperEvent {

    @PrimaryKey
    @NonNull
    private String ID;

    private String Title;
    private String Description;
    private String Date;
    private String StartTime;
    private String EndTime;
    private String Tag;
    private String userSession; // New field for user association

    // Added fields for recurrence
    private String repeatInterval;  // "No Repeat", "Every X Days", "Every Week", "Every Month", "Every Year"
    private int occurrenceCount;    // Number of times it repeats (e.g., 10 times)

    // Added linkedId field to associate recurring events
    private String linkedId;

    // Constructor including userSession, repeatInterval, and occurrenceCount
    public HelperEvent(@NonNull String ID, String Title, String Description, String Date, String StartTime, String EndTime, String Tag, String repeatInterval, int occurrenceCount, String linkedId, String userSession) {
        this.ID = ID;
        this.Title = Title;
        this.Description = Description;
        this.Date = Date;
        this.StartTime = StartTime;
        this.EndTime = EndTime;
        this.Tag = Tag;
        this.repeatInterval = repeatInterval;
        this.occurrenceCount = occurrenceCount;
        this.linkedId = linkedId;
        this.userSession = userSession;
    }

    // Factory method for creating an event with an auto-generated UUID
    public static HelperEvent create(String Title, String Description, String Date, String StartTime, String EndTime, String Tag, String repeatInterval, int occurrenceCount, String linkedId, String userSession) {
        return new HelperEvent(UUID.randomUUID().toString(), Title, Description, Date, StartTime, EndTime, Tag, repeatInterval, occurrenceCount, linkedId, userSession);
    }

    // Getters
    @NonNull
    public String getID() { return ID; }
    public String getTitle() { return Title; }
    public String getDescription() { return Description; }
    public String getDate() { return Date; }
    public String getStartTime() { return StartTime; }
    public String getEndTime() { return EndTime; }
    public String getTag() { return Tag; }
    public String getUserSession() { return userSession; } // Getter for userSession
    public String getRepeatInterval() { return repeatInterval; }
    public int getOccurrenceCount() { return occurrenceCount; }
    public String getLinkedId() { return linkedId; }

    // Setters
    public void setTitle(String Title) { this.Title = Title; }
    public void setDescription(String Description) { this.Description = Description; }
    public void setDate(String Date) { this.Date = Date; }
    public void setStartTime(String StartTime) { this.StartTime = StartTime; }
    public void setEndTime(String EndTime) { this.EndTime = EndTime; }
    public void setTag(String Tag) { this.Tag = Tag; }
    public void setUserSession(String userSession) { this.userSession = userSession; } // Setter for userSession
    public void setRepeatInterval(String repeatInterval) { this.repeatInterval = repeatInterval; }
    public void setOccurrenceCount(int occurrenceCount) { this.occurrenceCount = occurrenceCount; }
    public void setLinkedId(String linkedId) { this.linkedId = linkedId; }
}
