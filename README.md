# WCG (Wicked Cool Games) Character Generator

A web application for creating characters compatible with the Old School Fantasy (OSF) system to be published by Wicked Cool Games.

## Application Structure

This application consists of a Spring Boot backend and a React frontend.  The React source code is located in the `src/main/frontend` folder: see the README.md file there for details on how to run the frontend locally.

## Building and Running the Backend

To build the Spring Boot backend, run `./mvnw clean package` in the current directory.  Java 21 is required to build the application.

To run the backend locally in the IntelliJ IDE, create a new [run configuration](https://www.jetbrains.com/help/idea/run-debug-configuration.html) of type Application with the following parameters.

* Main Class: `com.wcg.chargen.backend.BackendApplication`

You can then run the backend from the IDE.  The backend defaults to running on http://localhost:8080.

## Building a Release Artifact

The release artifact for this application consists of a JAR file containing both the Spring Boot backend and the React frontend.  This JAR file is created by running the `./mvnw clean install` command.

The [frontend-maven-plugin](https://github.com/eirslett/frontend-maven-plugin) and [maven-resources-plugin](https://maven.apache.org/plugins/maven-resources-plugin/) Maven plugins are used for this, following [these instructions](https://dev.to/arpan_banerjee7/run-react-frontend-and-springboot-backend-on-the-same-port-and-package-them-as-a-single-artifact-14pa) with some adaptations.