# Refactoring Report

## What Behavior Did You Characterize Before Refactoring?

Before touching any code, I added `UnoTest.java` with 20 characterization tests covering:

- **Color matching**: same-color cards are legal; different-color cards are not (without a called color)
- **Number matching**: same-number cards across colors are legal
- **Action type matching**: skip on skip, reverse on reverse, draw two on draw two
- **Wild and Wild Draw Four**: always legal regardless of up card
- **Called color**: after a wild, the called color overrides the up card's color
- **Illegal mismatch**: cards with no matching color, number, or rank are rejected
- **Scoring**: number cards score face value; action cards score 20; wilds score 50
- **Bot priority**: draw two > skip > number > wild
- **Bot color choice**: picks the color it holds the most of
- **Edge case**: wild played on wild is legal (this surprised me — the code handles it through `card.startsWith("W")` alone, ignoring the up card entirely)

## What Were the Worst Design Problems?

1. **Duplicated legality logic**: the same 5-condition legality check appeared in `isLegal()`, `chooseBotCard()`, and inline in the main game loop. Any rule change required updating three places.
2. **Mixed console output and game logic**: every `System.out.println` was guarded by `if (!quiet)` inline in the game loop, making the loop harder to read and impossible to test without capturing stdout.
3. **Global mutable state**: `upCard`, `calledColor`, `direction`, `currentPlayer`, and all hands are static fields, making parallel testing impossible.
4. **Primitive card representation**: cards are bare strings like `"R5"` and `"W4"`. Every piece of code that needs color, rank, or number has to re-parse the string.
5. **Long game loop**: `playGame()` is ~120 lines mixing turn management, legality checking, card effects, scoring, and output.

## Which Refactorings Did You Perform?

1. **Extract `Card` class** (Extract Class): moved `color()`, `rank()`, `number()`, `points()` into a static helper. `Main` delegates to `Card`.
2. **Extract `Rules` class** (Extract Class + Move Method): moved `isLegal()` into `Rules`. Eliminated the duplicated inline legality blocks in `chooseBotCard()` by calling `Rules.isLegal()`.
3. **Extract `Display` class** (Extract Class + Split Phase): moved all `System.out.println` calls into a `Display` object. The game loop now calls named methods like `display.showPlay()` instead of inline print statements.

Each step was committed separately and the test suite ran green after every commit.

## What Behavior Did You Intentionally Preserve?

- Humans can type `draw` even when holding a legal card (no must-play enforcement)
- Typing an illegal **index** causes a penalty draw + turn loss (different from typing an illegal card code)
- Typing an illegal **card code** prompts again without penalty
- All hands are printed on every turn (no hidden information)
- Bots automatically play a drawn card if it's legal
- Wild played on wild is legal (an implementation quirk)
- The 3000-turn safety limit

## What Risks Remain?

- `playGame()` is still long; card effects (skip, reverse, draw two, wild draw four) are still handled by if-chains inside the loop
- Global state (`upCard`, `calledColor`, `currentPlayer`, `direction`) makes it impossible to run two games concurrently or write isolated tests for the full turn loop
- The bot strategy (`chooseBotCard`) is still tangled with `Main`'s static state
- No test exercises the full game loop end-to-end without I/O