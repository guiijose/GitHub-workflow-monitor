package com.guilhermejose.repository;

public class RepositoryStateManager {
    
    private final String directoryPath;

    public RepositoryStateManager(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public RepositoryState loadState(String owner, String repo) {
        return null;
    }

    public void saveState(RepositoryState state, String owner, String repo) {
        return;
    }
}
