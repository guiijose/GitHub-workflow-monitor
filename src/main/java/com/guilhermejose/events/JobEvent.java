package com.guilhermejose.events;

import com.guilhermejose.model.Status;
import java.time.Instant;

public class JobEvent extends Event {
    private long runId;
    private long jobId;
    private Status status;
    private String conclusion;
    private String commitSHA;
    private String headBranch;

    public JobEvent(long runId, long jobId, Status status, String conclusion, Instant timestamp, String commitSHA, String headBranch) {
        super(timestamp);
        this.runId = runId;
        this.jobId = jobId;
        this.status = status;
        this.conclusion = conclusion;
        this.commitSHA = commitSHA;
        this.headBranch = headBranch;
    }

    @Override
    public String format() {
        if (status == Status.QUEUED) {
            return String.format("%s JOB RUN QUEUED\trun: %d\tjob: %d\tSHA: %s \tbranch: %s",
                    timestampString(), runId, jobId, commitSHA, headBranch);
        } else if (status == Status.IN_PROGRESS) {
            return String.format("%s JOB RUN STARTED\trun: %d\tjob: %d\tSHA: %s \tbranch: %s",
                    timestampString(), runId, jobId, commitSHA, headBranch);
        } else if (status == Status.COMPLETED) {
            return String.format("%s JOB RUN COMPLETED\trun: %d\tjob: %d\tconclusion: %s\tSHA: %s\tbranch: %s",
                    timestampString(), runId, jobId, conclusion, commitSHA, headBranch);
        } else {
            return ""; // unknown status
        }
    }
}
