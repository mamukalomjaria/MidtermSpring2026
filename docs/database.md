# Assignment 5 Database Notes

## Selected Database And Framework

This project uses:

- H2 for local development and tests
- JPA with Hibernate for ORM persistence

The normal application stores history in an H2 file database at:

```text
./uno-history
```

Persistence tests use an isolated in-memory H2 database, so they do not depend on this file or any private machine setup.

## Schema

Hibernate creates and updates the schema from JPA entity mappings in:

```text
src/PlayerEntity.java
src/GameEntity.java
src/RoundEntity.java
src/ScoreEntity.java
```

The schema supports:

- `players`: player names
- `games`: game start/completion timestamps, rounds played, final winner
- `rounds`: round number, round winner, points scored
- `scores`: per-player final score for each game

The persistence unit is configured in:

```text
src/META-INF/persistence.xml
```

## Run Persistence Tests

```bash
mvn test
```

This runs the original characterization tests and `PersistenceTest`.

## Save Game History

Run a game normally. Completed games are persisted automatically.

```bash
mvn exec:java '-Dexec.args=--bots 3 --games 1 --quiet'
```

## View Reports

Recent games:

```bash
mvn exec:java '-Dexec.args=--history 10'
```

Player win counts:

```bash
mvn exec:java '-Dexec.args=--win-counts'
```

Highest scores:

```bash
mvn exec:java '-Dexec.args=--high-scores 10'
```
