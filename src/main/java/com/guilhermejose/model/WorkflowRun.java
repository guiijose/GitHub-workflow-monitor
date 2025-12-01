package com.guilhermejose.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRun {

    private Long id;
    private String name;
    private String status;
    private String conclusion;
    private String event;

    @JsonProperty("created_at")
    private String createdAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    @Override
    public String toString() {
        return "WorkflowRun{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", conclusion='" + conclusion + '\'' +
                ", event='" + event + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
    
}