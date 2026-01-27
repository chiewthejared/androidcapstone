package com.example.test_v2.calendar;

import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private static List<HelperEvent> eventList = new ArrayList<>();  // Shared event list

    public static List<HelperEvent> getEvents() {
        return eventList;
    }

    public static void addEvent(HelperEvent event) {
        eventList.add(event);
    }

    public static void removeEvent(HelperEvent event) {
        eventList.remove(event);
    }

    public static void clearEvents() {
        eventList.clear();
    }


}
