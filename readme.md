# Reactive Spring Boot CRUD API

This project is a lightweight, non-blocking CRUD API built with Spring Boot 3, Spring WebFlux, and Spring Data R2DBC. It uses an H2 in-memory database for testing purposes.

## 🚀 Features
- **Non-blocking I/O**: Built on Project Reactor for high concurrency.
- **Reactive Persistence**: Uses R2DBC for asynchronous database access.
- **In-memory Database**: H2 database for quick setup and testing.
- **Multi-stage Docker Build**: Optimized for production.

## 🛠 Tech Stack
- **Java 17**
- **Spring Boot 3.2.5**
- **Spring WebFlux**
- **Spring Data R2DBC**
- **Maven**
- **Lombok**
- **H2 Database**

## 📋 API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks` | Get all tasks |
| GET | `/api/tasks/{id}` | Get task by ID |
| POST | `/api/tasks` | Create a new task |
| PUT | `/api/tasks/{id}` | Update an existing task |
| DELETE | `/api/tasks/{id}` | Delete a task |

### Sample JSON Body (POST/PUT)
```json
{
  "title": "Complete Project",
  "description": "Finish the Docker and Maven setup",
  "completed": false
}
```

## 🏗 Build & Run

### Using Maven
1. Build the project:
   ```bash
   mvn clean package
   ```
2. Run the application:
   ```bash
   mvn spring-boot:run
   ```
   The API will be available at `http://localhost:2000/api/tasks`

### Using Docker
1. Build the image:
   ```bash
   docker build -t crud-api .
   ```
2. Run the container:
   ```bash
   docker run -p 2000:2000 crud-api
   ```
   The API will be available at `http://localhost:2000/api/tasks`

---

## 🐳 Dockerfile Explained In-Depth

The `DockerFile` uses a **multi-stage build** strategy. This is a best practice that separates the environment used to compile the code from the environment used to run it.

### Stage 1: The Build Environment (`AS build`)
```dockerfile
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests
```
- **Base Image**: Uses a full Maven image with JDK 17.
- **Dependency Caching**: We copy `pom.xml` and run `dependency:go-offline` **before** copying the source code. Docker caches layers; if your `pom.xml` hasn't changed, Docker skips downloading dependencies in subsequent builds, saving significant time.
- **Compilation**: The code is compiled and packaged into a JAR file in `/app/target/`.

### Stage 2: The Runtime Environment
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/target/crud-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 2000
ENTRYPOINT ["java", "-jar", "app.jar"]
```
- **Lean Base Image**: We switch to a JRE (Java Runtime Environment) image. It doesn't contain the compiler or Maven, making the final image ~200MB smaller and more secure.
- **Security**: 
    - `addgroup` & `adduser`: By default, Docker containers run as `root`. This is a security risk. We create a system user named `spring`.
    - `USER spring:spring`: The application now runs with limited privileges. If the app is compromised, the attacker doesn't have root access to the container.
- **Cleanliness**: We only `COPY --from=build` the final JAR. The source code, build logs, and Maven cache are left behind in the first stage.
- **Execution**: `ENTRYPOINT` ensures that the Java process receives OS signals directly (like `SIGTERM`), allowing Spring Boot to shut down gracefully.
