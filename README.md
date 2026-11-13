# Gym CRM System

Gym CRM built with Spring Boot and Hibernate (JPA) on PostgreSQL.
Manages trainees, trainers and trainings through a REST API.

## Tech stack

- Java 21, Maven
- Spring Boot 3.5 (Spring Framework 6.2): web (MVC, embedded Tomcat), data JPA, validation, Actuator
- Jackson (JSON), Hibernate Validator (request validation)
- Hibernate with PostgreSQL, HikariCP connection pool
- SLF4J + Logback, JUnit 5, Mockito, Spring MockMvc, H2 (tests only), JaCoCo
- Swagger (OpenAPI) annotations on the controllers

## Architecture

Hexagonal (onion) layout, dependencies point towards the domain model:

- `domain.model` - records (`Trainee`, `Trainer`, `Training`, `TrainingType`, `User`), value objects (`Credentials`, `PasswordChange`, training search criteria), read models (`TraineeProfile`, `TrainerProfile`) and the `UserProfile` interface shared by trainee and trainer
- `domain.exception` - `ValidationException` (400), `NotFoundException` (404), `ForbiddenOperationException` (403)
- `application.port.out` - DAO interfaces (`ProfileDao` is the part common to the trainee and trainer DAOs) and `UsageMetrics`, the port through which the services report business events to be counted
- `application.service` - business logic, authentication (`AuthenticationException`, 401), validation, transactions (`@Transactional`); `AbstractProfileService` holds the account logic shared by trainees and trainers
- `infrastructure.persistence` - Hibernate DAOs (`AbstractProfileDao` is their common base), JPA entities (`entity` package) and one mapper per entity (`mapper` package)
- `infrastructure.monitoring` - the custom Actuator health indicators and the Micrometer implementation of `UsageMetrics`
- `infrastructure.web` - the REST layer: `WebConfig`, `controller`, `dto`, `security` (Basic auth interceptor), `error` (global exception handler), `logging` (servlet filters, registered as Spring beans)

JPA entities are separate classes in the infrastructure layer, so the domain model stays free of persistence annotations.
Ports that change data (`delete`, `updateTrainers`, saving a `Training`) take domain objects instead of ids, so a row loaded by a service is not loaded again by the DAO.
Controllers call the services directly and know nothing about Hibernate.

## Database

Create a database and run the schema:

```sql
CREATE DATABASE gymcrm;
```

then execute `src/main/resources/db/schema.sql` against `gymcrm`.
The other environments use their own databases (`gymcrm_dev`, `gymcrm_stg`, `gymcrm_prod`), see [Environments](#environments).
The script creates the tables and fills the constant `training_type` table
(`1 FITNESS`, `2 YOGA`, `3 ZUMBA`, `4 STRETCHING`, `5 RESISTANCE`).

Connection settings depend on the environment, see [Environments](#environments).
Hibernate runs with `ddl-auto: validate`: it checks the schema but does not change it.

## Run

```
export DB_PASSWORD=your_password
mvn spring-boot:run
```

This starts the `local` profile. The API listens on port 8080 (use `--server.port=9090` to change it).
An executable jar is built with `mvn clean package` and started with `java -jar target/gym-crm-1.0-SNAPSHOT.jar`.

## Environments

The application has four Spring profiles: `local`, `dev`, `stg` and `prod`. Each one has its own file with the database
settings in `src/main/resources` (`application-local.yml`, `application-dev.yml`, `application-stg.yml`, `application-prod.yml`);
`application.yml` holds the settings common to all of them. Without an active profile, `local` is used.

| Profile | Database | Host | User | Password | Connection pool |
|---------|----------|------|------|----------|-----------------|
| `local` | `gymcrm` | `localhost` | `postgres` | `DB_PASSWORD`, default `postgres` | 5 |
| `dev` | `gymcrm_dev` | `DB_HOST`, default `localhost` | `DB_USER`, default `postgres` | `DB_PASSWORD`, required | 5 |
| `stg` | `gymcrm_stg` | `DB_HOST`, default `localhost` | `DB_USER`, default `postgres` | `DB_PASSWORD`, required | 10 |
| `prod` | `gymcrm_prod` | `DB_HOST`, default `localhost` | `DB_USER`, default `postgres` | `DB_PASSWORD`, required | 20 |

Create the database of the environment and run the schema script against it (see Database).
Passwords are read from environment variables and are never committed. Only `local` has a default password;
`dev`, `stg` and `prod` have none: set `DB_PASSWORD`, otherwise the database rejects the connection and the application
does not start, so a misconfigured deployment fails at once.

Select a profile in one of three ways (set `DB_PASSWORD` first for `dev`, `stg` and `prod`):

```
mvn spring-boot:run -Dspring-boot.run.profiles=dev
java -jar target/gym-crm-1.0-SNAPSHOT.jar --spring.profiles.active=dev
SPRING_PROFILES_ACTIVE=dev java -jar target/gym-crm-1.0-SNAPSHOT.jar
```

The active profile is written at the start of the log: `The following 1 profile is active: "dev"`.

## Monitoring

Spring Boot Actuator exposes three endpoints on the application port (all other Actuator endpoints are closed):

| Endpoint | What it shows |
|----------|---------------|
| `GET /actuator/health` | Overall status and the status of every check |
| `GET /actuator/info` | Application name and description |
| `GET /actuator/prometheus` | Metrics in the Prometheus format |

The probes `/actuator/health/liveness` and `/actuator/health/readiness` are available too.

Besides the standard checks (database, disk space), the application has two health indicators of its own:

- `trainingTypes`: `UP` if the training types table is filled (trainers cannot be registered without it), `DOWN` if it is empty or cannot be read. Detail: `count`.
- `connectionPool`: state of the HikariCP connection pool. `DOWN` when all connections are busy and callers wait for one. Details: `active`, `idle`, `total`, `max`, `waiting`.

The application also counts three business events. They are custom Prometheus metrics (counters) and appear in `/actuator/prometheus`:

| Metric | What it counts |
|--------|----------------|
| `gymcrm_registrations_total{role="trainee"}` and `{role="trainer"}` | Created accounts, one series per role |
| `gymcrm_trainings_added_total` | Trainings saved |
| `gymcrm_auth_failures_total` | Requests with an unknown username or a wrong password |

A counter starts at 0 when the application starts and grows until the next restart.

The Actuator endpoints do not need the Basic authentication header and are not written to the REST call log.
In a real deployment they must be closed from outside (a separate management port or a firewall).

## REST API

### Authentication

Every endpoint except the two registrations needs the HTTP Basic header:

```
Authorization: Basic base64(username:password)
```

With curl: `curl -u username:password ...`.

- Missing or wrong credentials: `401` with `WWW-Authenticate: Basic realm="gym-crm"`.
- A deactivated user gets `403` on every endpoint except `PATCH .../active`, so he can switch himself back on.
- The user can work only with his own data: for `/trainees/{username}` and `/trainers/{username}` the username in the path must be the one from the header, otherwise `403`.
  A training can be added only by its trainee or its trainer.

### Endpoints

| # | Method and path | What it does | Auth |
|---|-----------------|--------------|------|
| 1 | `POST /trainees` | Register a trainee, returns `{username, password}` | no |
| 2 | `POST /trainers` | Register a trainer (`specializationId` is a training type id), returns `{username, password}` | no |
| 3 | `GET /login` | Check the credentials | yes |
| 4 | `PUT /login` | Change the password, body `{"newPassword": "..."}` | yes |
| 5 | `GET /trainees/{username}` | Trainee profile with his trainers | yes |
| 6 | `PUT /trainees/{username}` | Update the trainee profile | yes |
| 7 | `DELETE /trainees/{username}` | Delete the trainee (hard delete, his trainings are deleted too) | yes |
| 8 | `GET /trainers/{username}` | Trainer profile with his trainees | yes |
| 9 | `PUT /trainers/{username}` | Update the trainer profile (the specialization is not changed) | yes |
| 10 | `GET /trainees/{username}/not-assigned-trainers` | Active trainers that are not assigned to the trainee | yes |
| 11 | `PUT /trainees/{username}/trainers` | Replace the trainee's trainers, body `{"trainers": [{"username": "..."}]}` | yes |
| 12 | `GET /trainees/{username}/trainings` | Trainee's trainings, filters: `periodFrom`, `periodTo` (`yyyy-MM-dd`), `trainerName`, `trainingType` | yes |
| 13 | `GET /trainers/{username}/trainings` | Trainer's trainings, filters: `periodFrom`, `periodTo`, `traineeName` | yes |
| 14 | `POST /trainings` | Add a training | yes |
| 15 | `PATCH /trainees/{username}/active` | Switch the trainee's active flag on every call, body `{"isActive": true}` (required) | yes |
| 16 | `PATCH /trainers/{username}/active` | Switch the trainer's active flag on every call, body `{"isActive": true}` (required) | yes |
| 17 | `GET /training-types` | All training types | yes |

Notes:

- The username and the password are generated on registration (`First.Last`, with a serial number if taken; 10 random characters). The password is returned only once.
- `PATCH .../active` switches the flag to the opposite value on every call (it is not idempotent, as the task requires). The `isActive` field is required in the body, but the result does not depend on it.
- A training type is sent as an id when a trainer is registered and returned as `{"id": 2, "name": "YOGA"}`.
  The type of a new training is the specialization of its trainer.
- The name filters match the first name only. All filters are optional.
- "Not assigned trainers" are only active trainers.
- Registration returns `200` with the credentials.
- Training types are available only after login, so register a trainee first, then ask `GET /training-types` for the ids.

### Examples

```bash
# register a trainee, the response has the generated username and password
curl -X POST localhost:8080/trainees -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Smith","dateOfBirth":"2000-01-01","address":"Astana"}'

# register a trainer with the YOGA specialization (id 2)
curl -X POST localhost:8080/trainers -H "Content-Type: application/json" \
  -d '{"firstName":"Anna","lastName":"Brown","specializationId":2}'

# profile of the trainee
curl -u John.Smith:PASSWORD localhost:8080/trainees/John.Smith

# assign trainers
curl -u John.Smith:PASSWORD -X PUT localhost:8080/trainees/John.Smith/trainers \
  -H "Content-Type: application/json" -d '{"trainers":[{"username":"Anna.Brown"}]}'

# add a training
curl -u John.Smith:PASSWORD -X POST localhost:8080/trainings -H "Content-Type: application/json" \
  -d '{"traineeUsername":"John.Smith","trainerUsername":"Anna.Brown","trainingName":"Morning yoga","trainingDate":"2026-11-01","trainingDuration":60}'

# trainings of the trainee in a period
curl -u John.Smith:PASSWORD "localhost:8080/trainees/John.Smith/trainings?periodFrom=2026-01-01&trainingType=YOGA"
```

### Errors

Every error has the same JSON body and never contains a stack trace:

```json
{"type": "VALIDATION_ERROR", "details": ["firstName must not be blank"]}
```

| Status | `type` | When |
|--------|--------|------|
| 400 | `VALIDATION_ERROR` | invalid body, parameter or business rule (for example an unknown training type id) |
| 400 | `MALFORMED_REQUEST` | the body is missing or is not valid JSON |
| 401 | `UNAUTHORIZED` | missing or wrong credentials |
| 403 | `FORBIDDEN` | the path username is not the caller's, the account is deactivated, or the caller may not add this training |
| 404 | `NOT_FOUND` | the trainee, trainer or training type does not exist |
| 500 | `INTERNAL_ERROR` | unexpected error (the details are only in the server log) |

Other errors of Spring MVC (wrong method `405`, wrong content type `415`) use the same body.

### API documentation

The controllers are annotated with Swagger (OpenAPI) annotations: a tag per controller and a summary, description and possible responses per endpoint.

## Logging

- Every request gets a transaction id. It is taken from the `X-Transaction-Id` header if the caller sent a valid one (up to 64 characters: letters, digits, `.`, `_`, `-`), otherwise a UUID is generated. The id is written in every log line of the request (the square brackets in the log pattern) and is returned in the `X-Transaction-Id` response header.
- Every REST call is logged in one line: method, path, status and time. The level depends on the status: INFO below 400, WARN for 4xx, ERROR for 5xx.
- The query string and the request and response bodies (the error message for a failed call) can hold personal data, so they are logged in a second line at DEBUG only. Production logs at INFO do not contain them.
- Passwords (`password`, `newPassword`) are masked as `***`, headers are never logged (so `Authorization` is never in the log), long bodies are cut after 1000 characters.
  A request rejected before its body was read (for example 401) has no body in the DEBUG line.
- The application logs at INFO by default; DEBUG for `com.epam.gymcrm` is switched on in tests (`src/test/resources/logback-test.xml`) and can be switched on for a run with `--logging.level.com.epam.gymcrm=DEBUG`.
- Failures in the services and DAOs (validation, authentication, entity not found) are logged as WARN with the username or id before the exception is thrown.
- Calls to `/actuator/**` are not logged: health checks and metric scrapes come every few seconds.

## Tests

```
mvn clean test
```

- Service tests use Mockito mocks.
- Controller tests use `@WebMvcTest`: the real controllers, interceptor, filters and exception handler with mocked services.
- DAO tests use `@DataJpaTest` on in-memory H2.
- `QueryCountTest` uses Hibernate statistics to check that the training lists run one query each (no N+1)
  and that saving a training or changing trainers does not load the trainee and trainers again.
- `ServiceLoggingTest` checks that failures are logged as warnings with the username; the logging filters have their own tests.
- `ApiDocumentationTest` checks that every controller and endpoint has its OpenAPI description.
- `EnvironmentProfilesTest` checks that every profile points to its own database.
- The health indicators are unit tested with Mockito; `MicrometerUsageMetricsTest` checks the counters on an in-memory registry and `UsageMetricsReportingTest` checks that the services report the events (and only successful ones).
- `mvn clean verify` runs the tests and fails if line coverage drops below 80%.
- JaCoCo report: `target/site/jacoco/index.html`.
