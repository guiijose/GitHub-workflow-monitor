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

import java.util.PriorityQueue;

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

        System.out.println("First fetch completed");
    }

    private void handleRun(WorkflowRun run, String prefix) {
        JobsResponse jobsResponse = client.fetchJobs(owner, repo, run.getId());

        if (run.getStatus() == Status.QUEUED)
            eventQueue.add(new Event(run.getUpdatedAt(), prefix + " queued"));
        else if (run.getStatus() == Status.IN_PROGRESS)
            eventQueue.add(new Event(run.getStartedAt(), prefix + " started"));
        else if (run.getStatus() == Status.COMPLETED) {
            eventQueue.add(new Event(run.getStartedAt(), prefix + " started"));
            eventQueue.add(new Event(run.getUpdatedAt(), prefix + " completed"));
        }

        state.updateRun(run);

        for (Job job : jobsResponse.getJobs()) {
            if (job.getStatus() == Status.QUEUED)
                eventQueue.add(new Event(job.getUpdatedAt(), "Job queued"));
            else if (job.getStatus() == Status.IN_PROGRESS)
                eventQueue.add(new Event(job.getStartedAt(), "Job started"));
            else if (job.getStatus() == Status.COMPLETED) {
                eventQueue.add(new Event(job.getStartedAt(), "Job started"));
                eventQueue.add(new Event(job.getCompletedAt(), "Job completed"));
            }

            for (Step step : job.getSteps()) {
                if (step.getStatus() == Status.IN_PROGRESS)
                    eventQueue.add(new Event(step.getStartedAt(), "Step started"));
                else if (step.getStatus() == Status.COMPLETED)
                    eventQueue.add(new Event(step.getCompletedAt(), "Step completed"));
            }

            state.updateJob(run.getId(), job);
        }
    }

    public void newRun(WorkflowRun run) {
        handleRun(run, "Run");
    }

    public void newAttempt(WorkflowRun run) {
        handleRun(run, "Run attempt");
    }


    public void runDiff(WorkflowRun oldRun, WorkflowRun newRun) {
        JobsResponse newJobsResponse = client.fetchJobs(owner, repo, newRun.getId());

        for (Job newJob : newJobsResponse.getJobs()) {
            Job oldJob = state.getJob(newRun.getId(), newJob.getId());

            if (oldJob == null) {
                // New job
                if (newJob.getStatus() == Status.QUEUED)
                    eventQueue.add(new Event(newJob.getUpdatedAt(), "Job queued"));
                else if (newJob.getStatus() == Status.IN_PROGRESS)
                    eventQueue.add(new Event(newJob.getStartedAt(), "Job started"));
                else if (newJob.getStatus() == Status.COMPLETED) {
                    eventQueue.add(new Event(newJob.getStartedAt(), "Job started"));
                    eventQueue.add(new Event(newJob.getCompletedAt(), "Job completed"));
                }

                // All steps are new
                for (Step newStep : newJob.getSteps()) {
                    if (newStep.getStatus() == Status.IN_PROGRESS)
                        eventQueue.add(new Event(newStep.getStartedAt(), "Step started"));
                    else if (newStep.getStatus() == Status.COMPLETED)
                        eventQueue.add(new Event(newStep.getCompletedAt(), "Step completed"));
                }

            } else {
                // Existing job, check for status changes
                if (oldJob.getStatus() != newJob.getStatus()) {
                    if (newJob.getStatus() == Status.QUEUED)
                        eventQueue.add(new Event(newJob.getUpdatedAt(), "Job queued"));
                    else if (newJob.getStatus() == Status.IN_PROGRESS)
                        eventQueue.add(new Event(newJob.getStartedAt(), "Job started"));
                    else if (newJob.getStatus() == Status.COMPLETED) {
                        eventQueue.add(new Event(newJob.getStartedAt(), "Job started"));
                        eventQueue.add(new Event(newJob.getCompletedAt(), "Job completed"));
                    }
                }

                // Check step changes
                for (Step newStep : newJob.getSteps()) {
                    Step oldStep = oldJob.getStep(newStep.getNumber());
                    if (oldStep == null) {
                        // new step
                        if (newStep.getStatus() == Status.IN_PROGRESS)
                            eventQueue.add(new Event(newStep.getStartedAt(), "Step started"));
                        else if (newStep.getStatus() == Status.COMPLETED)
                            eventQueue.add(new Event(newStep.getCompletedAt(), "Step completed"));
                    } else if (oldStep.getStatus() != newStep.getStatus()) {
                        // status changed
                        if (newStep.getStatus() == Status.IN_PROGRESS)
                            eventQueue.add(new Event(newStep.getStartedAt(), "Step started"));
                        else if (newStep.getStatus() == Status.COMPLETED)
                            eventQueue.add(new Event(newStep.getCompletedAt(), "Step completed"));
                    }
                }
            }

            // Update state with latest job snapshot
            state.updateJob(newRun.getId(), newJob);
        }

        // Update run state
        state.updateRun(newRun);
    }

    public void poll() {
        this.eventQueue.clear();

        WorkflowRunsResponse response = this.client.fetchWorkflowRuns(this.owner, this.repo);

        for (WorkflowRun run : response.getWorkflowRuns()) {
            if (!this.state.isThereRun(run.getId())) {
                this.newRun(run);
            } else {
                WorkflowRun oldRun = this.state.getRun(run.getId());
                if (oldRun.getRunAttempt() == run.getRunAttempt()) {
                    this.runDiff(oldRun, run);
                } else {
                    this.newAttempt(run);
                }
            }
        }
    }

    public void printEvents() {
        System.out.println("Printing events");
        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();
            System.out.println(event.toString());
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
