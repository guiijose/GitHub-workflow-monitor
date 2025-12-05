package com.guilhermejose.events;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public abstract class Event implements Comparable<Event> {
    protected Instant timestamp;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

    public Event(Instant timestamp) {
        this.timestamp = timestamp;

        if (timestamp == null) this.timestamp = Instant.now();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(Event other) {
        return this.timestamp.compareTo(other.timestamp);
    }

    protected String timestampString() {
        return FORMATTER.format(this.timestamp);
    }


    public abstract String format();
}
