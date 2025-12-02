package com.guilhermejose;

import com.guilhermejose.client.GitHubClient;
import com.guilhermejose.model.*;
import java.util.ArrayList;
public class App 
{
    public static void main( String[] args )
    {
        Config config;
        try {
            config = Config.load();
        } catch (Exception e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
            return;
        }

        GitHubClient gitHubClient = new GitHubClient(config.getGithubToken());
        System.out.println("GitHub Client initialized with token: " + gitHubClient.getToken());
        WorkflowRunsResponse response = gitHubClient.fetchWorkflowRuns(config.getOwner(), config.getRepository());
        ArrayList<WorkflowRun> runs = (ArrayList<WorkflowRun>) response.getWorkflowRuns();
        System.out.println("Total Workflow Runs fetched: " + runs.size());

        for (WorkflowRun run : runs) {
            System.out.println(run);
            JobsResponse jobsResponse = gitHubClient.fetchWorkflowRunInfo(config.getOwner(), config.getRepository(), run.getId());

            for (Job job : jobsResponse.getJobs()) {
                System.out.println("\t" + job);
                for (Step step : job.getSteps()) {
                    System.out.println(step);
                }
            }
        }

    }
}
