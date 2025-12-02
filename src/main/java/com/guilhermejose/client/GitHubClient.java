package com.guilhermejose.client;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.guilhermejose.model.JobsResponse;
import com.guilhermejose.model.WorkflowRunsResponse;

public class GitHubClient {
    public final static String BASE_URL = "https://api.github.com";
    
    private static final HttpClient client = HttpClient.newHttpClient();
        private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    private String token;

    private HttpRequest.Builder newRequest(String url) {
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .header("Accept", "application/vnd.github.v3+json");
    }

    public GitHubClient(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getBaseUrl() {
        return BASE_URL;
    }

    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Fetch workflow runs for a given repository.
     * @param owner The owner of the repository
     * @param repo §The repository name
     * @return WorkflowRunsResponse containing the list of workflow runs
     */
    public WorkflowRunsResponse fetchWorkflowRuns(String owner, String repo) {
        String url = String.format("%s/repos/%s/%s/actions/runs", BASE_URL, encode(owner), encode(repo));
        HttpRequest request = newRequest(url).GET().build();

        try {
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                return mapper.readValue(responseBody, WorkflowRunsResponse.class);
            } else {
                System.err.println("Failed to fetch workflow runs. HTTP Status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error during fetching workflow runs: " + e.getMessage());
        }

        return null;
    }

    /**
     * 
     */
    public JobsResponse fetchWorkflowRunInfo(String owner, String repo, long runId) {
        String url = String.format("%s/repos/%s/%s/actions/runs/%d/jobs", BASE_URL, encode(owner), encode(repo), runId);
        HttpRequest request = newRequest(url).GET().build();

        try {
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                return mapper.readValue(responseBody, JobsResponse.class);
            } else {
                System.err.println("Failed to fetch workflow run info. HTTP Status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error during fetching workflow run info: " + e.getMessage());
        }

        return null;
    }


}