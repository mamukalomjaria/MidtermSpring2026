# Final Project Report

## Overview

This project extends the midterm into a complete command-line UNO product. It includes the full 108-card deck, legal-play validation, action and wild effects, draw/pass flow, UNO calls, round scoring, target-score matches, persistence, JUnit tests, Maven packaging, and Docker execution.

## Rules Implemented

The authoritative rule state is in `GameLogic`. `Card` parses cards and calculates point values, while `Rules` performs legal-play validation. The implementation supports Skip, Reverse, Draw Two, Wild, Wild Draw Four, immediate play of an eligible drawn card, a two-card missed-UNO penalty, standard remaining-hand scoring, and multi-round target detection.

The opening discard is always a number card. Reverse acts like Skip for two players. Wild Draw Four challenges and draw-card stacking are intentionally not implemented. Full variant details are in `docs/rules-supported.md`.

## CLI Play

Build and start an interactive game from the project root:

```bash
mvn package
java -jar target/midterm-uno-cli-1.0.0.jar --human --bots 2 --games 20 --target 500
```

A player may enter a hand index, a card code such as `R5`, `YS`, `G+2`, `W`, or `W4`, or `draw`. Wild cards prompt for `R`, `Y`, `G`, or `B`. Invalid card input and invalid colors are explained and prompted again instead of terminating the game.

Bot-only games use the same rule engine:

```bash
java -jar target/midterm-uno-cli-1.0.0.jar --bots 3 --games 20 --target 500 --quiet --seed 7
```

## Architecture

- `Card`: card representation helpers and scoring values
- `Rules`: stateless legal-play validation
- `GameLogic`: authoritative deck, hands, turns, direction, action effects, UNO state, and scoring
- `Main`: argument parsing, human/bot decisions, match orchestration, and persistence
- `Display`: all user-facing game output
- `JpaGameHistoryRepository`: database storage and report queries

`Main` creates one `GameLogic` instance for the match and drives it directly across every round. `GameLogic` is the single owner of cumulative scores, target completion, deck construction, and action-card rules. This avoids parallel rule or score state while keeping gameplay testable without console input.

## Tests

The project uses JUnit 5 in the conventional `src/test/java` directory and Maven Surefire. The suite covers:

- all 108 deck cards and multiplicities
- legal and illegal plays
- Skip and Reverse turn order
- Draw Two and Wild Draw Four draw/skip effects
- Wild color selection
- draw/pass and immediate play of a drawn legal card
- UNO detection, calls, and missed-call penalty
- round end, card values, scoring, and target score
- opening discard behavior
- bot selection and discard recycling
- JPA game-history persistence and reports

Verification command:

```bash
mvn clean test
```

The verified suite contains 18 tests with zero failures, errors, or skipped tests.

## Rubric Evidence

| Rubric area | Implementation evidence | Test evidence |
|---|---|---|
| Deck composition | `GameLogic.buildDeck` | `buildsTheClassic108CardDeck` |
| Legal validation | `Rules.isLegal`, `GameLogic.playCard` | legality tests and `rejectsIllegalPlaysWithoutChangingState` |
| Skip / Reverse | `applySkip`, `applyReverse` | `skipAndReverseChangeTurnOrder` |
| Draw Two / Wild Draw Four | `applyDrawTwo`, `applyWildDrawFour` | `drawCardsAddCardsAndSkipTheAffectedPlayer` |
| Wild | `setCalledColor`, `applyCardEffect` | `wildChosenColorControlsTheNextLegalPlay` |
| Draw/pass | `drawForCurrentPlayer`, CLI pass flow | `aDrawnLegalCardCanBePlayedImmediately` |
| UNO | `hasUno`, `callUno`, `applyMissedUnoPenalty` | `missedUnoDrawsTwoWhileACallAvoidsThePenalty` |
| Scoring/target | one match-long `GameLogic`, guarded scoring APIs, match loop | cumulative-score and unfinished-round tests |
| Architecture/CLI | `Main` orchestrates `GameLogic`; `Display` owns output | engine tests require no console input |
| Documentation | README and both required final docs | command flow verified by Maven/JAR runs |

## Limitations

The project does not include Wild Draw Four challenges, draw stacking, network play, a graphical interface, or advanced bot strategy. The database uses Hibernate's built-in connection pool, which is appropriate for this course CLI but not a production server deployment.