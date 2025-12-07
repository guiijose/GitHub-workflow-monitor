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

    Status runStatus = run.getStatus();
    String runConclusion = run.getConclusion();

    if (runStatus == Status.QUEUED) {
        eventQueue.add(new WorkflowRunEvent(
                run.getId(),
                runAttempt,
                Status.QUEUED,
                run.getUpdatedAt(),
                null
        ));
    } else if (runStatus == Status.IN_PROGRESS) {
        eventQueue.add(new WorkflowRunEvent(
                run.getId(),
                runAttempt,
                Status.IN_PROGRESS,
                run.getStartedAt(),
                null
        ));
    } else if (runStatus == Status.COMPLETED) {

        eventQueue.add(new WorkflowRunEvent(
                run.getId(),
                runAttempt,
                Status.IN_PROGRESS,
                run.getStartedAt(),
                null
        ));

        eventQueue.add(new WorkflowRunEvent(
                run.getId(),
                runAttempt,
                Status.COMPLETED,
                run.getUpdatedAt(),
                runConclusion
        ));
    }

    state.updateRun(run);

    for (Job job : jobsResponse.getJobs()) {
        Status jobStatus = job.getStatus();
        String jobConclusion = job.getConclusion();

        if (jobStatus == Status.QUEUED) {
            eventQueue.add(new JobEvent(
                    run.getId(),
                    job.getId(),
                    Status.QUEUED,
                    null,
                    job.getUpdatedAt()
            ));
        } else if (jobStatus == Status.IN_PROGRESS) {
            eventQueue.add(new JobEvent(
                    run.getId(),
                    job.getId(),
                    Status.IN_PROGRESS,
                    null,
                    job.getStartedAt()
            ));
        } else if (jobStatus == Status.COMPLETED) {
            eventQueue.add(new JobEvent(
                    run.getId(),
                    job.getId(),
                    Status.IN_PROGRESS,
                    null,
                    job.getStartedAt()
            ));
            
            eventQueue.add(new JobEvent(
                    run.getId(),
                    job.getId(),
                    Status.COMPLETED,
                    jobConclusion,
                    job.getCompletedAt()
            ));
        }

        state.updateJob(run.getId(), job);

        // ------ STEPS ------
        for (Step step : job.getSteps()) {
            Status stepStatus = step.getStatus();
            String stepConclusion = step.getConclusion();

            if (stepStatus == Status.IN_PROGRESS) {
                eventQueue.add(new StepEvent(
                        run.getId(),
                        job.getId(),
                        step.getName(),
                        Status.IN_PROGRESS,
                        step.getStartedAt(),
                        null
                ));
            } else if (stepStatus == Status.COMPLETED) {
                // Emit start
                eventQueue.add(new StepEvent(
                        run.getId(),
                        job.getId(),
                        step.getName(),
                        Status.IN_PROGRESS,
                        step.getStartedAt(),
                        null
                ));
                // Emit completed
                eventQueue.add(new StepEvent(
                        run.getId(),
                        job.getId(),
                        step.getName(),
                        Status.COMPLETED,
                        step.getCompletedAt(),
                        stepConclusion
                ));
            }
        }
    }
}



    public void newRun(WorkflowRun run) {
        handleRun(run);
    }

    public void newAttempt(WorkflowRun run) {
        handleRun(run);
    }


public void runDiff(WorkflowRun oldRun, WorkflowRun newRun) {
    JobsResponse newJobsResponse = client.fetchJobs(owner, repo, newRun.getId());

    for (Job newJob : newJobsResponse.getJobs()) {
        Job oldJob = state.getJob(newRun.getId(), newJob.getId());

        if (oldJob == null) {
            // New job
            if (newJob.getStatus() == Status.COMPLETED) {
                // Print both started and completed
                eventQueue.add(new JobEvent(newRun.getId(), newJob.getId(), Status.IN_PROGRESS, null, newJob.getStartedAt()));
                eventQueue.add(new JobEvent(newRun.getId(), newJob.getId(), newJob.getStatus(), newJob.getConclusion(), newJob.getCompletedAt()));
            } else {
                eventQueue.add(new JobEvent(newRun.getId(), newJob.getId(), newJob.getStatus(), null, 
                    newJob.getStatus() == Status.QUEUED ? newJob.getUpdatedAt() : newJob.getStartedAt()));
            }
        } else if (oldJob.getStatus() != newJob.getStatus()) {
            // Existing job, status changed → print only the new status
            Instant timestamp = newJob.getStatus() == Status.COMPLETED ? newJob.getCompletedAt() :
                                newJob.getStatus() == Status.IN_PROGRESS ? newJob.getStartedAt() :
                                newJob.getUpdatedAt();
            eventQueue.add(new JobEvent(newRun.getId(), newJob.getId(), newJob.getStatus(), newJob.getStatus() == Status.COMPLETED ? newJob.getConclusion() : null, timestamp));
        }

        // Steps
        for (Step newStep : newJob.getSteps()) {
            Step oldStep = oldJob == null ? null : oldJob.getStep(newStep.getNumber());

            if (oldStep == null) {
                // New step
                if (newStep.getStatus() == Status.COMPLETED) {
                    eventQueue.add(new StepEvent(newRun.getId(), newJob.getId(), newStep.getName(), Status.IN_PROGRESS, newStep.getStartedAt(), null));
                    eventQueue.add(new StepEvent(newRun.getId(), newJob.getId(), newStep.getName(), newStep.getStatus(), newStep.getCompletedAt(), newStep.getConclusion()));
                } else {
                    Instant ts = newStep.getStatus() == Status.IN_PROGRESS ? newStep.getStartedAt() : newStep.getCompletedAt();
                    eventQueue.add(new StepEvent(newRun.getId(), newJob.getId(), newStep.getName(), newStep.getStatus(), ts, null));
                }
            } else if (oldStep.getStatus() != newStep.getStatus()) {
                // Existing step, status changed → print only new status
                Instant ts = newStep.getStatus() == Status.COMPLETED ? newStep.getCompletedAt() : newStep.getStartedAt();
                eventQueue.add(new StepEvent(newRun.getId(), newJob.getId(), newStep.getName(), newStep.getStatus(), ts, newStep.getStatus() == Status.COMPLETED ? newStep.getConclusion() : null));
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
            if (!this.state.isThereRun(run.getId())) {
                this.handleRun(run);
            } else {
                WorkflowRun oldRun = this.state.getRun(run.getId());
                if (oldRun.getRunAttempt() == run.getRunAttempt()) {
                    this.runDiff(oldRun, run);
                } else {
                    this.handleRun(run);
                }
            }
        }

        this.stateManager.saveState(this.state, this.owner, this.repo);
    }

    public void printEvents() {
        System.out.println("=========================================");
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
