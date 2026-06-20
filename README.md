# Midterm UNO CLI

This is a standalone CLI UNO-like game.

The code is written as plausible feature-grown Java: almost everything lives in one procedural `Main` class. It works, but it has mixed responsibilities, duplicated rule logic, primitive-heavy card handling, global state, and condition-heavy gameplay code. The goal is to refactor it safely, not rewrite it.

## Assignment 4 Build Commands

Run these commands from the project root.

### Local Build

```bash
mvn compile
```

### Local Test

```bash
mvn test
```

### Local Run

```bash
mvn exec:java
```

Example bot game:

```bash
mvn exec:java '-Dexec.args=--bots 3 --games 5 --quiet'
```

Interactive game:

```bash
mvn exec:java '-Dexec.args=--human --bots 2 --games 1'
```

### Package Creation

```bash
mvn package
```

The runnable JAR is created at:

```text
target/midterm-uno-cli-1.0.0.jar
```

Run the packaged application:

```bash
java -jar target/midterm-uno-cli-1.0.0.jar --bots 3 --games 1
```

### Docker Build

```bash
docker build -t midterm-uno-cli:1.0 .
```

### Docker Run

```bash
docker run --rm midterm-uno-cli:1.0 --bots 3 --games 1 --quiet
```

For an interactive human game:

```bash
docker run --rm -it midterm-uno-cli:1.0 --human --bots 2 --games 1
```

## Legacy Script Commands

The original shell scripts are still available.

### Compile

```bash
scripts/compile.sh
```

### Run Bot Games

```bash
scripts/run.sh --bots 3 --games 5 --quiet
```

### Run Interactive Game

```bash
scripts/run.sh --human --bots 2 --games 1
```

Card input examples:

```text
R5   red 5
YS   yellow skip
BR   blue reverse
G+2  green draw two
W    wild
W4   wild draw four
draw draw a card
```

### Characterization Checks

```bash
scripts/test.sh
```

## Submission

Submit your work through GitHub:

1. Fork this repository to your GitHub account.
2. Clone your fork locally.
3. Complete the midterm work in your fork.
4. Commit your changes with clear commit messages.
5. Push your branch to GitHub.
6. Open a pull request from your fork back to the original repository.

Your pull request must include:

* refactored source code
* characterization tests
* `docs/refactoring-report.md`
* `docs/extension-readiness.md`

Do not submit a zip file instead of a pull request unless the instructor explicitly asks for it.

## Rules

See `docs/rules.html` for the implemented game rules.

## Midterm Materials

* `docs/midterm-exam.md`: midterm brief
* `docs/rubric.md`: grading rubric
* `docs/refactoring-guide.md`: suggested refactoring path
