package com.example.test_v2.calendar;

import java.time.LocalDate;

public class CalendarDay {
    private final LocalDate date;
    private final boolean isCurrentMonth;
    private final boolean hasRecord;

    public CalendarDay(LocalDate date, boolean isCurrentMonth, boolean hasRecord) {
        this.date = date;
        this.isCurrentMonth = isCurrentMonth;
        this.hasRecord = hasRecord;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public boolean hasRecord() {
        return hasRecord;
    }
}