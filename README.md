# Gym CRM System

Gym CRM built with Spring MVC and Hibernate (JPA) on PostgreSQL.
Manages trainees, trainers and trainings. The application is available as a REST API (embedded Tomcat) and as a console demo.

## Tech stack

- Java 21, Maven
- Spring Framework 6.1 (Core, ORM, transactions, MVC), no Spring Boot yet
- Embedded Tomcat 10.1, Jackson (JSON), Hibernate Validator (request validation)
- Hibernate 6.5 with PostgreSQL, HikariCP connection pool
- SLF4J + Logback, JUnit 5, Mockito, Spring MockMvc, H2 (tests only), JaCoCo
- Swagger (OpenAPI) annotations on the controllers; the Swagger UI will come with the Spring Boot migration

## Architecture

Hexagonal (onion) layout, dependencies point towards the domain model:

- `domain.model` - records (`Trainee`, `Trainer`, `Training`, `TrainingType`, `User`), value objects (`Credentials`, `PasswordChange`, training search criteria), read models (`TraineeProfile`, `TrainerProfile`) and the `UserProfile` interface shared by trainee and trainer
- `domain.exception` - `ValidationException` (400), `NotFoundException` (404)
- `application.port.out` - DAO interfaces (`ProfileDao` is the part common to the trainee and trainer DAOs)
- `application.service` - business logic, authentication, validation, transactions (`@Transactional`); `AbstractProfileService` holds the account logic shared by trainees and trainers
- `infrastructure.persistence` - Hibernate DAOs (`AbstractProfileDao` is their common base), JPA entities (`entity` package) and one mapper per entity (`mapper` package)
- `infrastructure.config` - Spring configuration of the services and persistence (data source, Hibernate, transactions)
- `infrastructure.web` - the REST layer: `WebConfig`, `controller`, `dto`, `security` (Basic auth interceptor), `error` (global exception handler), `logging` (servlet filters)
- `infrastructure.facade` - `GymFacade`, a single entry point for the 18 operations of the console demo

JPA entities are separate classes in the infrastructure layer, so the domain model stays free of persistence annotations.
Ports that change data (`delete`, `updateTrainers`, saving a `Training`) take domain objects instead of ids, so a row loaded by a service is not loaded again by the DAO.
Controllers call the services directly and know nothing about Hibernate.

## Database

Create a database and run the schema:

```sql
CREATE DATABASE gymcrm;
```

then execute `src/main/resources/db/schema.sql` against `gymcrm`.
The script creates the tables and fills the constant `training_type` table
(`1 FITNESS`, `2 YOGA`, `3 ZUMBA`, `4 STRETCHING`, `5 RESISTANCE`).

Connection settings are in `src/main/resources/application.properties`.
The password is read from the `DB_PASSWORD` environment variable (default `postgres`) and is never committed.
Hibernate runs with `hbm2ddl.auto=validate`: it checks the schema but does not change it.

## Run

REST API:

```
export DB_PASSWORD=your_password
mvn clean compile
```

then run `com.epam.gymcrm.RestApplication` from the IDE. It listens on port 8080 (use `-Dserver.port=9090` to change it).
Restart it after every code change.

Console demo: run `com.epam.gymcrm.Main`. It goes through the main operations once using `GymFacade`.

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
| 15 | `PATCH /trainees/{username}/active` | Activate or deactivate the trainee, body `{"isActive": true}` | yes |
| 16 | `PATCH /trainers/{username}/active` | Activate or deactivate the trainer, body `{"isActive": false}` | yes |
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
The Swagger UI is not enabled yet; it will be added with the Spring Boot migration.

## Logging

- Every request gets a transaction id. It is taken from the `X-Transaction-Id` header if the caller sent a valid one (up to 64 characters: letters, digits, `.`, `_`, `-`), otherwise a UUID is generated. The id is written in every log line of the request (the square brackets in the log pattern) and is returned in the `X-Transaction-Id` response header.
- Every REST call is logged in one line: method and URL with query, status, time, request body and response body (the error message for a failed call).
  The level depends on the status: INFO below 400, WARN for 4xx, ERROR for 5xx.
- Passwords (`password`, `newPassword`) are masked as `***`, headers are never logged (so `Authorization` is never in the log), long bodies are cut after 1000 characters.
  A request rejected before its body was read (for example 401) is logged without a body.
- The application logs at INFO by default; DEBUG for `com.epam.gymcrm` is switched on only in tests (`src/test/resources/logback-test.xml`).
- Failures in the services and DAOs (validation, authentication, entity not found) are logged as WARN with the username or id before the exception is thrown.

## Tests

```
mvn clean test
```

- Service and facade tests use Mockito mocks.
- Controller tests use Spring MockMvc with the real controllers, interceptor and exception handler and mocked services.
- DAO tests are integration tests on in-memory H2 with the same Hibernate configuration.
- `QueryCountTest` uses Hibernate statistics to check that the training lists run one query each (no N+1)
  and that saving a training or changing trainers does not load the trainee and trainers again.
- `ServiceLoggingTest` checks that failures are logged as warnings with the username; the logging filters have their own tests.
- `ApiDocumentationTest` checks that every controller and endpoint has its OpenAPI description.
- JaCoCo report: `target/site/jacoco/index.html`.

## Console demo operations

`GymFacade` keeps the 18 service operations in one place; `Main` runs them as a demo. The REST API covers the same functions.

1. Create trainer profile
2. Create trainee profile
3. Trainee username/password matching
4. Trainer username/password matching
5. Select trainer profile by username
6. Select trainee profile by username
7. Trainee password change
8. Trainer password change
9. Update trainer profile
10. Update trainee profile
11. Activate/de-activate trainee
12. Activate/de-activate trainer
13. Delete trainee profile by username (trainings are deleted by cascade)
14. Get trainee trainings list by criteria (from date, to date, trainer name, training type)
15. Get trainer trainings list by criteria (from date, to date, trainee name)
16. Add training
17. Get trainers not assigned to a trainee
18. Update trainee's trainers list
