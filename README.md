# Gym CRM

Spring Core module for a gym CRM system. It manages trainees, trainers and trainings, and keeps all data in memory.

## Requirements

- JDK 21
- Maven 3.9+

## Build and test

```
mvn clean install
```

The coverage report (JaCoCo) is generated at `target/site/jacoco/index.html`.

## Run

Run the demo class `com.epam.gymcrm.Main` from the IDE. It starts the Spring context, creates a trainee, a trainer and a training through `GymFacade`, then deletes the trainee.

## Project structure

The project follows hexagonal (onion) architecture. Dependencies point toward the domain model.

| Package | Purpose |
|---|---|
| `domain.model` | Domain model: records `User`, `Trainee`, `Trainer`, `Training`, `TrainingType` and the `Identifiable` interface |
| `application.port.out` | DAO interfaces (ports), including `UsernameLookup` |
| `application.service` | `TraineeService`, `TrainerService`, `TrainingService`, username and password generators |
| `infrastructure.persistence` | In-memory DAO implementations on top of maps |
| `infrastructure.config` | Java-based Spring configuration (`AppConfig`, `StorageConfig`) |
| `infrastructure.storage` | `StorageInitializer` (`BeanPostProcessor`) that loads initial data |
| `infrastructure.facade` | `GymFacade`, single entry point to all services |

## How it works

- Each entity type has its own storage: a separate `Map` bean (`traineeStorage`, `trainerStorage`, `trainingStorage`).
- On startup `StorageInitializer` fills the storages from a CSV file. The path is set in `src/main/resources/application.properties` (`storage.init.file`).
- DAOs are injected into services with `@Autowired` setters. Services are injected into `GymFacade` through the constructor.
- Domain model classes are immutable Java records. Changes are made by creating a copy (`withId`, `withUser`, `withCredentials`).
- Username is `FirstName.LastName`. To check that it is free, the DAO returns only usernames that start with this base (`findUsernamesStartingWith`). If it is already taken by a trainee or a trainer, a serial number is added (`John.Smith1`, `John.Smith2`, ...).
- Password is a random 10-character string.
- Passwords are never written to logs or `toString()` output.

## Initial data

`src/main/resources/data/init-data.csv`. One line per record, the first column is the entity type. The expected columns are described in the comments at the top of the file.