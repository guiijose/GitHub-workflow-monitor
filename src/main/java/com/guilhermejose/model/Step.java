package com.guilhermejose.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.io.Serializable;

public class Step implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    private String name;

    @JsonProperty("status")
    private Status status;

    @JsonProperty("conclusion")
    private String conclusion;

    @JsonProperty("number")
    private int number;

    @JsonProperty("started_at")
    private Instant startedAt;

    @JsonProperty("completed_at")
    private Instant completedAt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "Step{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", conclusion='" + conclusion + '\'' +
                ", number=" + number +
                '}';
    }
}