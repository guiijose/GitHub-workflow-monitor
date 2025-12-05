package com.guilhermejose.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.guilhermejose.model.Job; 
import com.guilhermejose.model.WorkflowRun;

import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepositoryState {

    // Map indexed by run id
    private HashMap<Long, WorkflowRun> workflowRuns;

    // Map indexed by run id and job id
    private HashMap<Long, HashMap<Long, Job>> jobs;

    public RepositoryState() {
        this.workflowRuns = new HashMap<>();
        this.jobs = new HashMap<>();
    }

    public void updateJob(Long runId, Job job) {
        if (!this.jobs.containsKey(runId)) {
            this.jobs.put(runId, new HashMap<>());
        }
        this.jobs.get(runId).put(job.getId(), job);
        return;
    }

    public Job getJob(Long runId, Long jobId) {
        if (!this.jobs.containsKey(runId)) return null;
        return this.jobs.get(runId).get(jobId);
    }

    public boolean isThereJob(Long runId, Long jobId) {
        if (!this.jobs.containsKey(runId)) {
            return false;
        }
        return this.jobs.get(runId).containsKey(jobId);
    }

    public boolean updateRun(WorkflowRun run) {
        if (!this.workflowRuns.containsKey(run.getId())) {
            this.workflowRuns.put(run.getId(), run);
            return true;
        }
        return false;
    }

    public boolean isThereRun(Long runId) { return this.workflowRuns.containsKey(runId); }

    public WorkflowRun getRun(Long runId) {
        return this.workflowRuns.get(runId);
    }
}
