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
        WorkflowRunsResponse resonse = gitHubClient.fetchWorkflowRuns(config.getOwner(), config.getRepository());
        ArrayList<WorkflowRun> runs = (ArrayList<WorkflowRun>) resonse.getWorkflowRuns();

        for (int i = 0; i < runs.size(); i++) {
            System.out.println("Workflow run #" + (i + 1) + ":");
            System.out.println("\t" + runs.get(i).toString());
        }

    }
}
