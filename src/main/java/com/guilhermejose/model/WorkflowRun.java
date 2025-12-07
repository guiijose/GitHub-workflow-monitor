package com.guilhermejose.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRun implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;
    
    @JsonProperty("status")
    private Status status;

    @JsonProperty("conclusion")
    private String conclusion;
    
    @JsonProperty("event")
    private String event;
    
    @JsonProperty("run_attempt")
    private Long runAttempt;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("run_started_at")
    private Instant runStartedAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public Long getRunAttempt() { return runAttempt; }
    public void setRunAttempt(Long runAttempt) { this.runAttempt = runAttempt; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getStartedAt() { return runStartedAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    public boolean isActive() { return !(status == Status.COMPLETED); }

    @Override
    public String toString() {
        return "WorkflowRun{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", conclusion='" + conclusion + '\'' +
                ", event='" + event + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", runAttempt='" + runAttempt + '\'' +
                '}';
    }
    
}