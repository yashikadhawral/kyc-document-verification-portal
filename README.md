# KYC Document Verification Portal

A small web portal where applicants submit identity details with a document and verifiers review them
through a controlled workflow: Submitted, Under review, then Verified or Rejected.

The portal is deliberately simple. The purpose of this repository is the DevOps pipeline around it:
Git, Jenkins, Selenium, Docker and Ansible.

## Features

Working now:
- Sign in with a role (Applicant, Verifier, Admin)
- Overview page with live counts for each workflow stage
- Data model for users, KYC records and status history
- Health endpoint at `/actuator/health`

Planned (see the issues):
- Create, view, update and search KYC records
- Role-based status workflow with a full history
- Summary dashboard

## Tech stack

Java 17+, Spring Boot 3.5, Thymeleaf, Spring Security, Spring Data JPA, H2 (dev and CI), Maven, Tomcat.

## Getting started

Prerequisites: JDK 17 or newer and Maven 3.9 or newer.

```
mvn spring-boot:run
```

Open http://localhost:8085 and sign in with `applicant1`, `verifier1` or `admin1`.
The demo password is set in `src/main/resources/application.properties`.

## Test and build

```
mvn test
mvn clean package
```

`mvn clean package` builds `target/kyc-portal.war`, which runs with `java -jar` or deploys to Tomcat.

## Project structure

```
src/main/java/com/kycportal   application code (domain, repository, security, config, web)
src/main/resources            Thymeleaf templates, styles, application.properties
src/test/java/com/kycportal   tests
docs/                         project documents and the branching policy
.github/                      issue templates and pull request template
```

Added as the pipeline grows: `Jenkinsfile`, `Dockerfile`, `ansible/`.

## Working on this repository

Branch names, commit messages and pull request rules are in [docs/branching.md](docs/branching.md).

## Pipeline roadmap

| Stage | Tool | Status |
|-------|------|--------|
| Source control | Git and GitHub | In progress |
| Build and CI | Maven and Jenkins | Planned |
| Continuous testing | Selenium | Planned |
| Containers | Docker | Planned |
| Configuration and provisioning | Ansible | Planned |

## Data handling

Use dummy ID numbers only. Never enter a real Aadhaar, PAN or passport number in this project,
its test data or its screenshots. ID numbers are masked in the UI.