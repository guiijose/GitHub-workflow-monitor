package com.guilhermejose.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
    QUEUED("queued"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    REQUESTED("requested"),
    WAITING("waiting");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Status fromValue(String value) {
        for (Status status : Status.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return Status.QUEUED; // Default to QUEUED for unknown values
    }
}
