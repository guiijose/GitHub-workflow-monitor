package com.guilhermejose;

import com.guilhermejose.client.GitHubClient;
import com.guilhermejose.repository.RepositoryState;
import com.guilhermejose.repository.RepositoryStateManager;

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
        RepositoryState state = new RepositoryState();
        RepositoryStateManager manager = new RepositoryStateManager("");

        Monitor monitor = new Monitor(gitHubClient, state, manager, config.getOwner(), config.getRepository(), config.getRefreshRate());

        monitor.run();

    }
}
