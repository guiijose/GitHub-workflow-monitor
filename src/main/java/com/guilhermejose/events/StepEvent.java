package com.guilhermejose.events;

import java.time.Instant;
import com.guilhermejose.model.Status;

// Step events
public class StepEvent extends Event {
    private long runId;
    private long jobId;
    private int stepNumber;
    private Status status; // started, completed
    private String conclusion;

    public StepEvent(Long runId, Long jobId, int stepNumber, Status status, Instant timestamp, String conclusion) {
        super(timestamp);
        this.runId = runId;
        this.jobId = jobId;
        this.stepNumber = stepNumber;
        this.status = status;
        this.conclusion = conclusion;
    }

    @Override
    public String format() {
        if (status == Status.IN_PROGRESS || status == Status.QUEUED) {
            return String.format(
                "%s STEP %s\tstep: %d\tjob: %d\trun: %d",
                timestampString(),
                (status == Status.IN_PROGRESS) ? "STARTED" : "QUEUED",
                stepNumber,
                jobId,
                runId
            );
        } else if (status == Status.COMPLETED) {
            return String.format(
                "%s STEP COMPLETED\tstep: %d\tjob: %d\trun: %d\tconclusion: %s",
                timestampString(),
                stepNumber,
                jobId,
                runId,
                conclusion
            );
        }

        return "step " + status.getValue();
    }
}
