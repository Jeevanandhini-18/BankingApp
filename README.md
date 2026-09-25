# Mini Banking API - Week 1

A minimal Spring Boot service for the Mini Banking / Open Banking Consent Management project.

## Week 1 outcomes

- Spring Boot REST API
- PostgreSQL connection through Spring JDBC
- Automated endpoint test using H2
- Docker Compose setup for PostgreSQL and the application
- Postman collection for the two starter endpoints

## Prerequisites

- Java 17 or later
- Maven 3.9 or later, or IntelliJ IDEA / VS Code with Maven support
- Docker Desktop for the Compose workflow
- Git
- Postman (optional)

This workspace currently has Java 26 and Git available. Maven and Docker were not available on PATH when this project was created, so install them before running the commands below.

## Run the tests

```powershell
mvn test
```

The tests use an in-memory H2 database, so PostgreSQL does not need to be running for the test suite.

## Run locally with PostgreSQL

Start only the database:

```powershell
docker compose up -d postgres
mvn spring-boot:run
```

Or build the JAR and run it:

```powershell
mvn clean package
java -jar target/mini-banking-0.0.1-SNAPSHOT.jar
```

The application reads these defaults:

- URL: `jdbc:postgresql://localhost:5432/banking`
- Database: `banking`
- Username: `banking`
- Password: `banking`
- Port: `8080`

For real projects, use environment variables or a secret manager instead of committing passwords.

## Run everything with Docker Compose

```powershell
mvn clean package
docker compose up --build
```

Stop the containers:

```powershell
docker compose down
```

Add `-v` to the `down` command only when you intentionally want to delete the PostgreSQL volume and its data.

## API examples

### `GET /health`

Checks that the application can execute a simple SQL query.

```json
{
  "status": "UP",
  "database": "UP"
}
```

If PostgreSQL is unavailable, the endpoint remains reachable and returns `DOWN` for both fields.

### `GET /api/info`

Returns basic service metadata.

```json
{
  "application": "Mini Banking API",
  "version": "0.0.1-SNAPSHOT",
  "message": "Week 1 banking service is running"
}
```

## Postman

Import `postman/mini-banking-week1.postman_collection.json` into Postman. Run the `Health` request first, then `API Info`.

## Git workflow

```powershell
git init
git add .
git commit -m "Create week 1 mini banking service"
git branch -M main
git remote add origin <your-repository-url>
git push -u origin main
```

Useful daily commands:

```powershell
git status
git diff
git add .
git commit -m "Describe the change"
git log --oneline --decorate -5
```

## Suggested Week 1 demonstration

1. Show the project structure and explain `pom.xml` dependencies.
2. Run `mvn test`.
3. Start PostgreSQL and the application.
4. Call `/health` and explain the SQL connectivity check.
5. Call `/api/info` and explain the JSON response.
6. Import and run the Postman collection.
7. Show `git status`, a commit, and the README.
