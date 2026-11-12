# Gym CRM System

Backend for a gym: trainees, trainers and their trainings. Java 21, Maven, Spring Boot, Hibernate (JPA), PostgreSQL.
This file is the technical context for AI assistants. Setup instructions and the API reference are in `README.md`.

## Build and test

| Task | Command |
| --- | --- |
| Run all tests | `mvn clean test` |
| Build | `mvn clean package` |
| Run the application | `mvn spring-boot:run` |
| Tests and the 80% coverage check | `mvn clean verify` |

- Database: PostgreSQL, database `gymcrm`. `src/main/resources/db/schema.sql` creates the tables
  and inserts the training types.
- The database password comes from the `DB_PASSWORD` environment variable. Never put it into a file.
- Hibernate runs with `spring.jpa.hibernate.ddl-auto=validate`: it checks the schema and never changes it.
- Tests use in-memory H2. The JaCoCo report is written to `target/site/jacoco` during `mvn test`;
  the 80% line coverage check runs in `mvn verify`.

## Configuration

- The application starts from `GymCrmApplication` (`@SpringBootApplication`). Spring Boot auto-configures the web server,
  the data source (HikariCP), JPA and transactions, so there is no hand-written Spring configuration for them.
- `application.yml` holds the settings common to all environments. The database settings are in one file per Spring profile:
  `application-local.yml`, `application-dev.yml`, `application-stg.yml`, `application-prod.yml`.
- `local` is the default profile. Select another one with `--spring.profiles.active=<name>` or `SPRING_PROFILES_ACTIVE`
  (with Maven: `-Dspring-boot.run.profiles=<name>`).
- Every environment has its own database (`gymcrm`, `gymcrm_dev`, `gymcrm_stg`, `gymcrm_prod`) and its own connection pool size.
  Passwords and hosts come from environment variables (`DB_PASSWORD`, `DB_HOST`, `DB_USER`); `stg` and `prod` require `DB_PASSWORD`.
- Actuator exposes only `health`, `info` and `prometheus` (`management.endpoints.web.exposure.include`).
  The Actuator endpoints are outside `BasicAuthInterceptor`, so they need no credentials.

## Architecture

Hexagonal (ports and adapters). Dependencies point inward only: `infrastructure` -> `application` -> `domain`.

| Package | Contents |
| --- | --- |
| `domain.model` | Immutable records (`Trainee`, `Trainer`, `Training`, `TrainingType`, `User`), value objects (`Credentials`, `AuthenticatedUser`, `Role`, `PasswordChange`, training search criteria) |
| `domain.exception` | `ValidationException`, `NotFoundException`, `ForbiddenOperationException` |
| `application.port.out` | DAO interfaces implemented by the persistence layer |
| `application.service` | Services, use-case commands (`RegisterTrainee`, `RegisterTrainer`, `UpdateTrainee`, `AddTrainingCommand`), read models (`TraineeProfile`, `TrainerProfile`), `AuthenticationException` |
| `infrastructure.web` | Controllers, DTOs, error handling, security, request logging filters |
| `infrastructure.persistence` | DAO implementations, JPA entities, mappers |
| `infrastructure.monitoring` | Custom Actuator health indicators |

Rules:

- Domain models and JPA entities are separate classes, mapped by the mappers in `persistence.mapper`.
- Controllers only map HTTP to a service call and back. Business rules belong to the services.
- Use cases take command objects, not loose arguments.
- The application layer depends on ports, never on JPA, Spring MVC or other infrastructure classes.

## Request flow and security

1. `TransactionIdFilter` puts a `transactionId` into the logging context (MDC) and returns it in a response header.
2. `RestCallLoggingFilter` logs the request and the response (level by status, passwords masked). It skips `/actuator` calls.
   Both filters are `@Component` beans; `@Order` keeps this sequence.
3. `BasicAuthInterceptor` authenticates the caller once through `AuthenticationService.authenticateAndLoad`,
   then checks the active flag and the `{username}` path variable, and stores an `AuthenticatedUser`
   (credentials, role, active flag) in the request.
4. The controller passes the `AuthenticatedUser` to a service.

- Every endpoint requires authentication, except those marked `@PublicEndpoint` (trainee and trainer registration).
- A deactivated account is rejected with 403 unless the method is marked `@AllowInactive`.
- A user can reach only the resource that carries his own `{username}`.
- Services work with an already authenticated user and never check the password again.
  A service checks the role (`AuthenticatedUser.requireRole`) and loads the user's own profile.
- An unknown username and a wrong password give the same 401 response.

## Errors

`GlobalExceptionHandler` maps exceptions to an `ApiError` body (`type`, `details`):

| Exception | HTTP status |
| --- | --- |
| `ValidationException`, bean validation failures | 400 |
| `AuthenticationException` | 401 |
| `ForbiddenOperationException` | 403 |
| `NotFoundException` | 404 |
| Any other exception | 500 (logged at ERROR) |

Spring's own errors (405, 415 and so on) go through the same handler.

## Persistence

- `AbstractJpaDao` holds the `EntityManager` and the shared username lookup. `AbstractProfileDao` extends it
  for trainees and trainers; `TrainingDaoImpl` extends `AbstractJpaDao` too.
- `TrainingTypeDaoImpl` implements `TrainingTypeDao` directly: training types are a fixed reference table
  filled by the schema script and only read.
- Transactions are declared on the service methods (`@Transactional`, `readOnly = true` for reads).
- A trainer's specialization is set at registration and does not change.

## Logging

- SLF4J with Logback. The pattern includes `[%X{transactionId:-}]`.
- INFO for business events, WARN for rejected operations (failed authentication, validation, forbidden),
  ERROR for unexpected failures, DEBUG for details.
- Never log passwords, tokens or the `Authorization` header. Request and response bodies are logged with
  `password` and `newPassword` masked.

## Testing

- JUnit 5, Mockito, Spring MockMvc. DAO tests run on in-memory H2 with `@DataJpaTest` (base class `JpaTestBase`).
- Services are tested with plain Mockito. Controllers are tested with `@WebMvcTest` (base class `ControllerTestBase`)
  and mocked services.
- Test names state the behavior, for example `getReturnsTheProfileWithTrainers`.
- Every new behavior needs unit tests, including the rejected paths (401, 403, 404, validation).
- Line coverage must stay at 80% or higher (JaCoCo, checked by `mvn clean verify`).

## Conventions

- Code, comments, commit messages and documentation are in English.
- New classes and public methods get Javadoc.
- Prefer constructor injection and immutable records.
- Do not commit secrets, credentials or local settings.
