package com.guilhermejose;

import com.guilhermejose.model.Job;
import com.guilhermejose.model.JobsResponse;
import com.guilhermejose.model.Status;
import com.guilhermejose.model.Step;
import com.guilhermejose.model.WorkflowRun;
import com.guilhermejose.model.WorkflowRunsResponse;
import com.guilhermejose.repository.RepositoryState;
import com.guilhermejose.repository.RepositoryStateManager;
import com.guilhermejose.client.GitHubClient;
import com.guilhermejose.events.Event;
import com.guilhermejose.events.JobEvent;
import com.guilhermejose.events.StepEvent;
import com.guilhermejose.events.WorkflowRunEvent;

import java.util.PriorityQueue;
import java.time.Instant;

public class Monitor {
    

    // Client to make api calls
    private GitHubClient client;

    // Repository state and manager
    private RepositoryState state;
    private RepositoryStateManager stateManager;

    private String owner;
    private String repo;
    private int refreshRateSeconds;

    private PriorityQueue<Event> eventQueue;

    public Monitor(GitHubClient client, RepositoryState state, RepositoryStateManager stateManager, String owner, String repo, int refreshRateSeconds) {
        this.client = client;
        this.state = state;
        this.stateManager = stateManager;
        this.owner = owner;
        this.repo = repo;
        this.refreshRateSeconds = refreshRateSeconds;
        this.eventQueue = new PriorityQueue<Event>();

        firstFetch();
    }


    public void firstFetch() {
        WorkflowRunsResponse response = this.client.fetchWorkflowRuns(this.owner, this.repo);
        for (WorkflowRun run : response.getWorkflowRuns()) {
            if (run.isActive()) {
                JobsResponse jobsResponse = this.client.fetchJobs(this.owner, this.repo, run.getId());
                for (Job job : jobsResponse.getJobs()) {
                    this.state.updateJob(run.getId(), job);
                }
            }
            this.state.updateRun(run);
        }
    }

private void handleRun(WorkflowRun run) {
    Long runAttempt = run.getRunAttempt();
    JobsResponse jobsResponse = client.fetchJobs(owner, repo, run.getId());

    // Emit run events
    if (run.getStatus() == Status.QUEUED) {
        eventQueue.add(new WorkflowRunEvent(run.getId(), runAttempt, Status.QUEUED, run.getUpdatedAt(), null, run.getHeadSha(), run.getHeadBranch()));
    } else if (run.getStatus() == Status.IN_PROGRESS) {
        eventQueue.add(new WorkflowRunEvent(run.getId(), runAttempt, Status.IN_PROGRESS, run.getStartedAt(), null, run.getHeadSha(), run.getHeadBranch()));
    } else if (run.getStatus() == Status.COMPLETED) {
        eventQueue.add(new WorkflowRunEvent(run.getId(), runAttempt, Status.IN_PROGRESS, run.getStartedAt(), null, run.getHeadSha(), run.getHeadBranch()));
        eventQueue.add(new WorkflowRunEvent(run.getId(), runAttempt, Status.COMPLETED, run.getUpdatedAt(), run.getConclusion(), run.getHeadSha(), run.getHeadBranch()));
    }

    state.updateRun(run);

    for (Job job : jobsResponse.getJobs()) {
        Status jobStatus = job.getStatus();
        String jobConclusion = job.getConclusion();

        if (jobStatus == Status.QUEUED) {
            eventQueue.add(new JobEvent(run.getId(), job.getId(), Status.QUEUED, null, job.getUpdatedAt(), job.getHeadSha(), job.getHeadBranch()));
        } else if (jobStatus == Status.IN_PROGRESS) {
            eventQueue.add(new JobEvent(run.getId(), job.getId(), Status.IN_PROGRESS, null, job.getStartedAt(), job.getHeadSha(), job.getHeadBranch()));
        } else if (jobStatus == Status.COMPLETED) {
            eventQueue.add(new JobEvent(run.getId(), job.getId(), Status.IN_PROGRESS, null, job.getStartedAt(), job.getHeadSha(), job.getHeadBranch()));
            eventQueue.add(new JobEvent(run.getId(), job.getId(), Status.COMPLETED, jobConclusion, job.getCompletedAt(), job.getHeadSha(), job.getHeadBranch()));
        }

        state.updateJob(run.getId(), job);

        for (Step step : job.getSteps()) {
            Status stepStatus = step.getStatus();
            String stepConclusion = step.getConclusion();

            if (stepStatus == Status.IN_PROGRESS) {
                eventQueue.add(new StepEvent(run.getId(), job.getId(), step.getNumber(), Status.IN_PROGRESS, step.getStartedAt(), null));
            } else if (stepStatus == Status.COMPLETED) {
                eventQueue.add(new StepEvent(run.getId(), job.getId(), step.getNumber(), Status.IN_PROGRESS, step.getStartedAt(), null));
                eventQueue.add(new StepEvent(run.getId(), job.getId(), step.getNumber(), Status.COMPLETED, step.getCompletedAt(), stepConclusion));
            }
        }
    }
}



public void runDiff(WorkflowRun oldRun, WorkflowRun newRun) {
    JobsResponse newJobsResponse = client.fetchJobs(owner, repo, newRun.getId());

    for (Job newJob : newJobsResponse.getJobs()) {
        Job oldJob = state.getJob(newRun.getId(), newJob.getId());

        if (oldJob == null) {
            // New job
            if (newJob.getStatus() == Status.COMPLETED) {
                // Print both started and completed
                eventQueue.add(new JobEvent(newRun.getId(), newJob.getId(), Status.IN_PROGRESS, null, newJob.getStartedAt(), newJob.getHeadSha(), newJob.getHeadBranch()));
                eventQueue.add(new JobEvent(newRun.getId(), newJob.getId(), newJob.getStatus(), newJob.getConclusion(), newJob.getCompletedAt(), newJob.getHeadSha(), newJob.getHeadBranch()));
            } else {
                eventQueue.add(new JobEvent(newRun.getId(), newJob.getId(), newJob.getStatus(), null, 
                    newJob.getStatus() == Status.QUEUED ? newJob.getUpdatedAt() : newJob.getStartedAt(), newJob.getHeadSha(), newJob.getHeadBranch()));
            }
        } else if (oldJob.getStatus() != newJob.getStatus()) {
            // Existing job, status changed → print only the new status
            Instant timestamp = newJob.getStatus() == Status.COMPLETED ? newJob.getCompletedAt() :
                                newJob.getStatus() == Status.IN_PROGRESS ? newJob.getStartedAt() :
                                newJob.getUpdatedAt();
            eventQueue.add(new JobEvent(newRun.getId(), newJob.getId(), newJob.getStatus(), newJob.getStatus() == Status.COMPLETED ? newJob.getConclusion() : null, timestamp, newJob.getHeadSha(), newJob.getHeadBranch()));
        }

        // Steps
        for (Step newStep : newJob.getSteps()) {
            Step oldStep = oldJob == null ? null : oldJob.getStep(newStep.getNumber());

            if (oldStep == null) {
                // New step
                if (newStep.getStatus() == Status.COMPLETED) {
                    eventQueue.add(new StepEvent(newRun.getId(), newJob.getId(), newStep.getNumber(), Status.IN_PROGRESS, newStep.getStartedAt(), null));
                    eventQueue.add(new StepEvent(newRun.getId(), newJob.getId(), newStep.getNumber(), newStep.getStatus(), newStep.getCompletedAt(), newStep.getConclusion()));
                } else {
                    Instant ts = newStep.getStatus() == Status.IN_PROGRESS ? newStep.getStartedAt() : newStep.getCompletedAt();
                    eventQueue.add(new StepEvent(newRun.getId(), newJob.getId(), newStep.getNumber(), newStep.getStatus(), ts, null));
                }
            } else if (oldStep.getStatus() != newStep.getStatus()) {
                // Existing step, status changed → print only new status
                Instant ts = newStep.getStatus() == Status.COMPLETED ? newStep.getCompletedAt() : newStep.getStartedAt();
                eventQueue.add(new StepEvent(newRun.getId(), newJob.getId(), newStep.getNumber(), newStep.getStatus(), ts, newStep.getStatus() == Status.COMPLETED ? newStep.getConclusion() : null));
            }
        }

        state.updateJob(newRun.getId(), newJob);
    }

    state.updateRun(newRun);
}





    public void poll() {
        this.eventQueue.clear();

        WorkflowRunsResponse response = this.client.fetchWorkflowRuns(this.owner, this.repo);

        for (WorkflowRun run : response.getWorkflowRuns()) {
            if (!state.isThereRun(run.getId())) {
                handleRun(run);
            } else {
                WorkflowRun oldRun = state.getRun(run.getId());
                if (oldRun.getRunAttempt().equals(run.getRunAttempt())) {
                    runDiff(oldRun, run);
                } else {
                    handleRun(run);
                }
            }
        }


        this.stateManager.saveState(this.state, this.owner, this.repo);
    }

    public void printEvents() {
        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();
            System.out.println(event.format());
        }
    }

    public void run() {
        while (true) {
            try {
                poll();          // Fetch new runs/jobs and populate eventQueue
                printEvents();   // Print events for this poll
                Thread.sleep(refreshRateSeconds * 1000L);  // Wait before next poll
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Monitor stopped.");
                break;
            } catch (Exception e) {
                e.printStackTrace(); // Handle API or other runtime errors
            }
        }
    }


}
