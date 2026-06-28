# Midterm UNO CLI - Final Project Version

This project is a command-line UNO game extended for the final project. It uses the standard Maven directory layout, a testable `GameLogic` rules engine, JUnit 5 tests, and a Docker build for a runnable packaged application.

## Project Layout

```text
src/main/java       production Java code
src/main/resources  JPA configuration and application resources
src/test/java       JUnit 5 tests
```

## What Is Implemented

- 108-card UNO deck composition
- legal play validation by color, number, action type, and wild cards
- Skip, Reverse, Draw Two, Wild, and Wild Draw Four effects
- draw-one/pass flow where a drawn legal card may be played immediately
- UNO one-card detection with a two-card missed-call penalty for human players
- round scoring from remaining cards
- multi-round play with a target score, default `500`
- bot-only and human-vs-bot CLI play
- JPA/Hibernate game-history persistence

See `docs/rules-supported.md` for the exact rule choices and simplifications.

## Requirements

- Java 21
- Maven 3.9+ (or Docker)

## Build And Test

Run from the project root:

```bash
mvn compile
mvn test
mvn package
```

`mvn test` uses Maven Surefire to discover and run the JUnit tests under `src/test/java`.

The runnable shaded JAR is created at:

```text
target/midterm-uno-cli-1.0.0.jar
```

Run it directly:

```bash
java -jar target/midterm-uno-cli-1.0.0.jar --bots 3 --games 20 --target 500 --quiet
```

## Run With Maven

Bot-only game:

```bash
mvn exec:java '-Dexec.args=--bots 3 --games 20 --target 500 --quiet'
```

Interactive game:

```bash
mvn exec:java '-Dexec.args=--human --bots 2 --games 20 --target 500'
```

Useful options:

```text
--human           include one human player named You
--bots N          add N bot players
--games N         maximum number of rounds to play
--target N        stop when a player reaches this score, default 500
--quiet           suppress turn-by-turn display
--seed N          use a deterministic random seed
--history [N]     show recent persisted games
--win-counts      show persisted win counts
--high-scores [N] show persisted high scores
```

## Docker

Build the application image:

```bash
docker build -t midterm-uno-cli:1.0 .
```

Run a bot-only game:

```bash
docker run --rm midterm-uno-cli:1.0 --bots 3 --games 20 --target 500 --quiet
```

Run an interactive human game:

```bash
docker run --rm -it midterm-uno-cli:1.0 --human --bots 2 --games 20 --target 500
```

The Docker build runs the Maven tests and packages the shaded JAR before creating the JRE-only runtime image.

## Legacy Script Commands

The shell scripts preserve the earlier command flow while using Maven internally:

```bash
scripts/compile.sh
scripts/test.sh
scripts/run.sh --bots 3 --games 20 --target 500 --quiet
scripts/run.sh --human --bots 2 --games 20 --target 500
```

## Card Input

```text
R5   red 5
YS   yellow skip
BR   blue reverse
G+2  green draw two
W    wild
W4   wild draw four
draw draw one card
0    play the card at hand index 0
```

When a human plays down to one card, the CLI asks whether to call UNO. Answer `y`, `yes`, or `uno` to avoid the missed-call penalty.

## Documentation

- `docs/rules-supported.md`: implemented rules and variants
- `docs/final-report.md`: final project report and rubric evidence
- `docs/database.md`: persistence notes
- `docs/refactoring-report.md`: earlier refactoring assignment report
- `docs/extension-readiness.md`: earlier extension notes
