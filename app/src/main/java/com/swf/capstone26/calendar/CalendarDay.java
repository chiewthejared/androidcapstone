package com.swf.capstone26.calendar;

import java.time.LocalDate;

public class CalendarDay {
    private final LocalDate date;
    private final boolean isCurrentMonth;
    private final boolean hasRecord;
    private final boolean hasSeizure; // 红点（癫痫）

    public CalendarDay(LocalDate date, boolean isCurrentMonth, boolean hasRecord, boolean hasSeizure) {
        this.date = date;
        this.isCurrentMonth = isCurrentMonth;
        this.hasRecord = hasRecord;
        this.hasSeizure = hasSeizure;
    }

    // 向后兼容旧的三参数构造
    public CalendarDay(LocalDate date, boolean isCurrentMonth, boolean hasRecord) {
        this(date, isCurrentMonth, hasRecord, false);
    }

    public LocalDate getDate() { return date; }
    public boolean isCurrentMonth() { return isCurrentMonth; }
    public boolean hasRecord() { return hasRecord; }
    public boolean hasSeizure() { return hasSeizure; }
}