# **Attendance Management System**

## **Overview :**

The Attendance Management System is a secure, scalable, and enterprise-grade backend application built with Spring Boot
for managing employee attendance, leave workflows, and overtime tracking.
It provides a structured approval process between Employees, Managers, and HR, ensuring organizational compliance and
operational efficiency.
The project follows clean architecture principles and industry best practices for backend development.

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen)
![Build](https://img.shields.io/badge/Build-Maven-orange)
![License](https://img.shields.io/badge/License-MIT-lightgrey)


## **Core Features :**

* Submit leave requests
* Track request status (Pending / Approved / Rejected)
* View leave history
* Monitor attendance and overtime
* Review team leave requests
* Approve or reject leave and justification requests
* Monitor team attendance
* Supervise overtime records
* Automatic presence tracking
* Absence monitoring
* Historical attendance and leave reporting

## **Technology Stack :**

| Category    | Technology              |
|-------------|-------------------------|
| Language    | Java 21                 |
| Framework   | Spring Boot 4.0.2       |
| Build Tool  | Maven                   |
| Database    | PostgreSQL              |
| ORM         | Spring Data JPA         |
| Security    | Spring Security         |
| Validation  | Spring Validation       |
| Boilerplate | Lombok                  |
| Testing     | JUnit, Spring Boot Test |

## **Architecture :**

* Controller Layer → Handles REST API requests
* Service Layer → Business logic & validations
* Repository Layer → Data persistence with JPA
* Security Layer → Authentication & role-based authorization

It also includes :

* DTO layer
* Exception handling layer
* Security filters
* Mapper layer
* Utility layer
* Infrastructure layer (Docker)

This architecture ensures :
* Separation of concerns
* Maintainability
* Scalability
* Clean code structure

## **Security :**
* Role-Based Access Control (RBAC)
* Endpoint protection (not public should have a valid JWT)
* Secure authentication via Spring Security
* Validation of business rules before persistence
* Restricted access to sensitive operations

## **Prerequisites :**

* JDK 21
* Maven 3.3.4
* PostgreSQL 16

## **How to Run :**

1. Clone the repository : **git clone https://gitlab.com/woridgesaas/employe/ms-attendance-management.git**
2. Build the project : **mvn clean install**
3. Run the application : **mvn spring-boot:run** or run the packaged JAR: **java -jar
   target/attendance-management-0.0.1-SNAPSHOT.jar**

## **Testing :**

Run all tests using : **mvn test**

## **API Base URL :**
* http://localhost:8080/api

## **Project Structure :**

```bash
attendance-management
│
├── config/            # Application configuration classes (Security, CORS, Beans, etc.)
├── controller/        # REST controllers (API endpoints)
├── dto/               # Data Transfer Objects (Request/Response)
├── enumeration/       # Enums
├── exception/         # Global exception handling & custom exceptions
├── filter/            # Security filters (JWT filter)
├── mapper/            # Conversion logic
├── model/             # JPA entities (database models)
├── repository/        # Spring Data JPA repositories
├── service/           # Service layer (Business contracts)
  ├── implementation/  # Service implementations (Business logic methods)      
├── utils/             # Utility classes (Helpers, common reusable logic)
└── resources/         # application.yml/properties, static files, templates
```

## **CI/CD Pipeline :**

This project uses GitLab CI/CD to automate the build, test, and release process following a GitFlow workflow. The pipeline ensures that:
* Every branch is validated
* Only stable code from main generates a release artifact
* Sprint releases are safely stored

## **Pipeline Stages :** 

The pipeline consists of three stages : **build → test → package**

#### **1. Build Stage :**

* Compiles the project using Maven (**mvn clean compile**)
* Ensures the code compiles successfully
* Runs on all branches
* Stores temporary build artifacts for internal use

**Purpose :**
Detect compilation errors early and validate project structure.

#### **2. Test Stage :**

* Executes unit tests (**mvn test**)
* Uses compiled output from the build stage
* Runs on all branches

**Purpose:**
Validate business logic and prevent regressions.

#### **3. Package Stage :**

* Generates the final executable .jar file (**mvn package -DskipTests**)

Runs only when:
* A Merge Request targets the main branch or a commit is pushed directly to main
* Produces a permanent release artifact : **attendance-management-release**

**Purpose :**
Ensure that only stable and validated code produces official application releases.

## **Performance Optimization :**
The pipeline includes :
* Maven dependency caching (**.m2/repository**)
* Artifact reuse between stages (**needs**)
* Controlled artifact retention policy

This improves build performance and ensures efficient CI execution.
