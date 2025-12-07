# GitHub-workflow-monitor

## Overview
This project monitors GitHub workflow runs, jobs and steps for a given repository. When statuses change the tool will print them by command line.

## Repository Structure

```bash
.
├── README.md
├── data                                        # Example data used by the project
├── pom.xml                                     # Maven build file
└── src
    └── main
        ├── java
        │   └── com
        │       └── guilhermejose
        │           ├── App.java                # Main entry point
        │           ├── Config.java             # Configuration (token, repo, etc.)
        │           ├── Monitor.java            # Workflow monitoring logic
        │           ├── client
        │           │   └── GitHubClient.java   # GitHub API wrapper
        │           ├── events                  # Event classes for printing
        │           ├── model                   # Data models (runs, jobs, steps, etc.)
        │           └── repository              # Repository state management
        └── resources
            └── config.json                     # Config file (repo info, tokens, refresh rate)
```

## Prerequisites

This project was built using **Java 22** and **Maven 3.9.11**, so having these versions (or higher) is recommended to compile and run it correctly.  

## Setting up Configuration

Create a `config.json` file in `src/main/resources/` with the following content:

```json
{
  "githubToken":    "",     # GitHub API authentication token
  "owner":          "",     # Repository owner
  "repository":     "",     # Repository name
  "refreshRate":    10      # Seconds between API polling rounds
}
```

Make your own GitHub API token here: https://github.com/settings/tokens

## Build and run

To build the project run:

```mvn clean compile```

To run the project run:

```mvn exec:java -Dexec.mainClass="com.guilhermejose.App"```