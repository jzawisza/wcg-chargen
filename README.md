# WCG (Wicked Cool Games) Character Generator

A web application for creating characters compatible with the Old School Fantasy (OSF) system to be published by Wicked Cool Games.

## Application Structure

This application consists of a Spring Boot backend and a React frontend.  The React source code is located in the `src/main/frontend` folder: see the README.md file there for details on how to run the frontend locally.

## Building and Running the Backend

To build the Spring Boot backend, run `./mvnw clean test` in the current directory.  Java 25 is required to build the application.

To run the backend locally in the IntelliJ IDE, create a new [run configuration](https://www.jetbrains.com/help/idea/run-debug-configuration.html) of type Application with the following parameters.

* Main Class: `com.wcg.chargen.backend.BackendApplication`

You can then run the backend from the IDE.  The backend defaults to running on http://localhost:5000.

**NOTE**: if running on a Mac, you may need to turn off `System Settings > General > AirDrop & Handoff > AirPlay Receiver`, as it [listens on port 5000](https://stackoverflow.com/questions/72369320/why-always-something-is-running-at-port-5000-on-my-mac).

## Release Artifacts and Deployments

This application is currently deployed to AWS Elastic Beanstalk, and is accessible at https://wcgchargen-env.eba-bcmfahc2.us-east-1.elasticbeanstalk.com/ .  This test deployment uses a self-signed certificate, so you will see security warnings in your browser: these are expected.

Both PDF and Google Sheets character sheet generation are supported, but the [Google OAuth support](https://console.cloud.google.com/) is currently set to Testing mode, meaning that Google accounts must be manually added to the OAuth 2.0 client by an administrator before Google Sheets generation will work.

To build a release artifact, run `./mvnw clean install` in the current directory.  This will create a ZIP file artifact suitable for uploading to Elastic Beanstalk.  Some custom steps are performed at various Maven build phrases to generate a suitable artifact:

- The `package` phase generates a JAR file which contains both the Spring Boot backend and the React frontend.  The [frontend-maven-plugin](https://github.com/eirslett/frontend-maven-plugin) and [maven-resources-plugin](https://maven.apache.org/plugins/maven-resources-plugin/) Maven plugins are used for this, following [these instructions](https://dev.to/arpan_banerjee7/run-react-frontend-and-springboot-backend-on-the-same-port-and-package-them-as-a-single-artifact-14pa) with some adaptations.
- The `install` phase creates a ZIP file containing the JAR file and the `.ebextensions` and `.platform` directories in `src/main/eb-files`.  Those directories contain configuration files needed by Elastic Beanstalk, which enable [HTTPS on a single instance](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/https-singleinstance.html) for a [Java application](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/https-singleinstance-java.html).  The [Maven assembly plugin](https://maven.apache.org/plugins/maven-assembly-plugin/) is used for this.
  - As it turns out, the steps from the AWS website provide incorrect information on how to configure nginx.  The nginx configuration files [need to go under .platform, not .ebextensions](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/platforms-linux-extend.example.html).
  - The `https-instance.config` file requires the certificate and private key to be present for HTTPS to work.  Obviously, the version of that file checked into source control does not contain the actual certificate or private key.  Before deploying, those values must be copy-pasted into that file.

The ZIP file generated can be uploaded to Elastic Beanstalk using the AWS UI.  For now, deployments will be infrequent, so manual deployments will suffice.