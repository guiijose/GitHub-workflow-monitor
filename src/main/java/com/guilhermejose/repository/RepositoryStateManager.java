package com.guilhermejose.repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RepositoryStateManager {

    private final String directoryPath = "data/";

    public RepositoryStateManager() {
        // Create directory if it doesn't exist
        Path dir = Paths.get(directoryPath);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create data directory", e);
            }
        }
    }

    public RepositoryState loadState(String owner, String repo) {
        Path file = Paths.get(directoryPath, owner + "_" + repo + "_state.bin");
        if (!Files.exists(file)) return new RepositoryState();

        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            System.out.println("Loading existing state...");
            return (RepositoryState) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new RepositoryState();
        }
    }

    public void saveState(RepositoryState state, String owner, String repo) {
        Path file = Paths.get(directoryPath, owner + "_" + repo + "_state.bin");

        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(state);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
