package com.guilhermejose;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class Event implements Comparable<Event> {
    private Instant timestamp;
    private String description;

    public Event(Instant timestamp, String description) {
        if (timestamp == null) {
            System.out.println("Event with description: \"" + description + "\" has timestamp null");
        }
        this.timestamp = timestamp;
        this.description = description;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int compareTo(Event other) {
        return this.timestamp.compareTo(other.timestamp);
    }

    @Override
    public String toString() {
        if (timestamp == null) {
            return "[no timestamp] " + description;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                                    .withZone(java.time.ZoneId.systemDefault());
        return formatter.format(timestamp) + " - " + description;
    }

}