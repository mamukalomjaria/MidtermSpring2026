# Extension Readiness

## Which Extension Would Your Design Support Best?

**Adding a smarter bot strategy** is the easiest extension now.

## Where Would That Change Be Implemented?

The current bot logic lives in `Main.chooseBotCard()` and `Main.chooseBotColor()`. Both already call `Rules.isLegal()` from the extracted `Rules` class, so a smarter bot can be written by:

1. Creating a `BotStrategy` interface with a `chooseCard(List<String> hand, String upCard, String calledColor)` method
2. Moving the current priority logic (`DRAW_TWO > SKIP > NUMBER > WILD`) into a `DefaultBotStrategy` implementation
3. Writing a `SmartBotStrategy` that, for example, counts cards in other players' hands or avoids playing wilds early

The `Display` class already exists separately from game logic, so the bot strategy has no dependency on console output.

## What Part of Your Design Still Makes Change Difficult?

- **Global state**: `upCard`, `calledColor`, and `currentPlayer` are still static fields on `Main`. A `BotStrategy` that needs to look ahead would need these passed as parameters or wrapped in a `GameState` object.
- **Card effects**: skip, reverse, draw two, and wild draw four effects are still inline if-chains in `playGame()`. Adding a new card type means editing the loop directly.
- **No `GameState` object**: the biggest remaining obstacle. Extracting a `GameState` class to hold `upCard`, `calledColor`, `direction`, `currentPlayer`, and all hands would make the game fully testable without running the CLI, and would allow proper dependency injection for both bot strategies and display.