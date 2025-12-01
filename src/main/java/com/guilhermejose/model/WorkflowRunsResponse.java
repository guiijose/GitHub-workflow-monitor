package com.guilhermejose.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

/**
 * Represents the response from GitHub API for workflow runs.
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRunsResponse {
    
    @JsonProperty("total_count")
    private int totalCount;

    @JsonProperty("workflow_runs")
    private ArrayList<WorkflowRun> workflowRuns;

    public ArrayList<WorkflowRun> getWorkflowRuns() { return workflowRuns; }
    public void setWorkflowRuns(ArrayList<WorkflowRun> workflowRuns) { this.workflowRuns = workflowRuns; }
    
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
}
