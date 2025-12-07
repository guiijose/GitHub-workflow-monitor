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
        RepositoryStateManager manager = new RepositoryStateManager();
        RepositoryState state = manager.loadState(config.getOwner(), config.getRepository());

        Monitor monitor = new Monitor(gitHubClient, state, manager, config.getOwner(), config.getRepository(), config.getRefreshRate());

        monitor.run();

    }
}
