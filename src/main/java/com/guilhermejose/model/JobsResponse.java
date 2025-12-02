package com.guilhermejose.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class JobsResponse {
    @JsonProperty("total_count")
    private int totalCount;

    @JsonProperty("jobs")
    private List<Job> jobs;

    // Getters and Setters
    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}