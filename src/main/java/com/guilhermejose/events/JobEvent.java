package com.guilhermejose.events;

import com.guilhermejose.model.Status;
import java.time.Instant;

public class JobEvent extends Event {
    private long runId;
    private long jobId;
    private Status status;
    private String conclusion;

    public JobEvent(long runId, long jobId, Status status, String conclusion, Instant timestamp) {
        super(timestamp);
        this.runId = runId;
        this.jobId = jobId;
        this.status = status;
        this.conclusion = conclusion;
    }

    @Override
    public String format() {
        if (status == Status.QUEUED) {
            return String.format("%s JOB RUN QUEUED\trun: %d\tjob: %d",
                    timestampString(), runId, jobId);
        } else if (status == Status.IN_PROGRESS) {
            return String.format("%s JOB RUN STARTED\trun: %d\tjob: %d",
                    timestampString(), runId, jobId);
        } else if (status == Status.COMPLETED) {
            return String.format("%s JOB RUN COMPLETED\trun: %d\tjob: %d\tconclusion: %s",
                    timestampString(), runId, jobId, conclusion);
        } else {
            return "job " + status.getValue(); // unknown status
        }
    }
}
