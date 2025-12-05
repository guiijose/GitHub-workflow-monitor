package com.guilhermejose.events;

import com.guilhermejose.model.Status;
import java.time.Instant;

// Workflow run queued / started / completed
public class WorkflowRunEvent extends Event {
    private long runId;
    private long runAttempt; // optional, only for new attempts
    private Status status; // queued, started, completed
    private String conclusion;

    public WorkflowRunEvent(long runId, long runAttempt, Status status, Instant timestamp, String conclusion) {
        super(timestamp);
        this.runId = runId;
        this.runAttempt = runAttempt;
        this.status = status;
        this.conclusion = conclusion;
    }

    @Override
    public String format() {
        if (status == Status.QUEUED) {
            if (runAttempt > 0) {
                return String.format("%s NEW WORKFLOW RUN ATTEMPT QUEUED\tid: %d\tattempt: %d",
                        timestampString(), runId, runAttempt);
            } else {
                return String.format("%s WORKFLOW RUN QUEUED\tid: %d",
                        timestampString(), runId);
            }
        } else if (status == Status.IN_PROGRESS) {
            if (runAttempt > 0) {
                return String.format("%s NEW WORKFLOW RUN ATTEMPT\tid: %d\tattempt: %d",
                        timestampString(), runId, runAttempt);
            } else {
                return String.format("%s WORKFLOW RUN STARTED\tid: %d",
                        timestampString(), runId);
            }
        } else if (status == Status.COMPLETED) {
            return String.format("%s WORKFLOW RUN COMPLETED\tid: %d\tconclusion: %s",
                    timestampString(), runId, conclusion);
        }
        return "workflow run " + status.getValue(); // fallback for unknown status
    }

}
