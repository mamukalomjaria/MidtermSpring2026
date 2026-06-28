# UNO Rules Supported

This document maps the implementation to `Final_Project_UNO_rules_reference.md` and records every gameplay simplification.

## Implemented Rules

### Deck Composition

`GameLogic.buildDeck()` creates the classic 108-card deck: four colors; one zero per color; two of each 1-9, Skip, Reverse, and Draw Two per color; four Wild cards; and four Wild Draw Four cards.

Each player receives seven cards. The opening discard is redrawn until it is a number card. This uses the reference's documented option to start with a normal card instead of applying an opening action or wild effect.

### Legal Plays

`Rules.isLegal` accepts a card that matches the active color, number, or action type. Wild and Wild Draw Four are always playable in this course version. Illegal plays are rejected without changing the hand or top card. After a wild, the selected color controls the next legal-play check.

### Action And Wild Cards

- Skip advances past the next player.
- Reverse changes direction with three or four players.
- Reverse acts as Skip with two players, giving the player who used it the next turn.
- Draw Two makes the next player draw two and lose the turn.
- Wild selects the active color.
- Wild Draw Four selects the active color, makes the next player draw four, and skips that player.

### Draw And Pass

A player may draw one card. If it is legal, a bot plays it immediately and a human may choose to play it immediately. Otherwise the turn passes. A player may also choose to draw instead of playing a card already in the hand.

### UNO Call And Penalty

After a player plays down to one card, the one-card state is detected. Bots call UNO automatically. Human players are prompted; a missed call immediately draws two penalty cards. This immediate prompt is the detection window used by this implementation.

### Round Scoring And Match Target

A round ends when a hand is empty. The winner receives the value of every card left in opponents' hands: number cards use face value, colored action cards score 20, and wild cards score 50.

One `GameLogic` instance owns cumulative scores across all rounds. Rounds continue until that engine reports that a player reached the target score or the configured maximum round count is reached. The default target is 500; `--target N` changes it.

## Documented Simplifications

- No Wild Draw Four challenge rule.
- No Draw Two or Wild Draw Four stacking.
- The opening discard is always a number card.
- Missed UNO is resolved immediately after the play rather than through a later challenge window.
- Bots call UNO automatically.
- Bots use a simple action-first strategy, preferring Wild Draw Four, Draw Two, Skip, Reverse, numbers, then Wild.
- The interface is text-only.
- A 3000-turn safety limit prevents a malformed round from running forever.

## Test Evidence

`src/test/java/GameLogicFinalTest.java` directly tests complete deck counts, illegal-play rejection, Skip, Reverse for two and three players, Draw Two, Wild, Wild Draw Four, draw-and-immediate-play, UNO calls and penalties, scoring, score persistence across rounds, target detection, scoring preconditions, and opening discard behavior.

`src/test/java/UnoTest.java` covers card parsing, legal-play combinations, bot choices, point values, and discard recycling. `PersistenceTest.java` verifies saved game history and reports. All tests are JUnit 5 tests discovered by Maven Surefire with `mvn test`.