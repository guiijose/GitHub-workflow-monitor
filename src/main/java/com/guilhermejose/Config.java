package com.guilhermejose;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class Config {

    public static final String CONFIG_JSON_PATH = "config.json"; // relative to classpath

    private String githubToken;
    private String repository;
    private String owner;
    private int refreshRate;

    public String getGithubToken() {
        return githubToken;
    }

    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }

    public String getRepository() {
        return repository;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    // Load config from resources
    public static Config load() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = Config.class.getClassLoader().getResourceAsStream(CONFIG_JSON_PATH);
        if (is == null) {
            throw new IOException("Cannot find " + CONFIG_JSON_PATH + " in classpath");
        }
        return mapper.readValue(is, Config.class);
    }

    @Override
    public String toString() {
        return "Config{" +
                "githubToken='" + githubToken + '\'' +
                ", repository='" + repository + '\'' +
                ", refreshRate=" + refreshRate +
                '}';
    }
}
