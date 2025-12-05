package com.guilhermejose.events;

import java.time.Instant;
import com.guilhermejose.model.Status;

// Step events
public class StepEvent extends Event {
    private long runId;
    private long jobId;
    private String stepName;
    private Status status; // started, completed
    private String conclusion;

    public StepEvent(Long runId, Long jobId, String stepName, Status status, Instant timestamp, String conclusion) {
        super(timestamp);
        this.runId = runId;
        this.jobId = jobId;
        this.stepName = stepName;
        this.status = status;
        this.conclusion = conclusion;
    }

    @Override
    public String format() {
        if (status == Status.IN_PROGRESS) {
            return String.format(
                "%s STEP STARTED\tstep: %s\tjob: %d\trun: %d",
                timestampString(),
                stepName,
                jobId,
                runId
            );
        } else if (status == Status.COMPLETED) {
            return String.format(
                "%s STEP COMPLETED\tstep: %s\tjob: %d\trun: %d\tconclusion: %s",
                timestampString(),
                stepName,
                jobId,
                runId,
                conclusion
            );
        }

        return "step " + status.getValue();
    }
}
