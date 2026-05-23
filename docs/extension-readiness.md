# Extension Readiness

## Best supported extension
Smarter bot strategy (evaluate hand strength, avoid helping the next player).

## Where to implement
- Update BotStrategy.chooseCard and BotStrategy.chooseColor.
- Optionally introduce a strategy interface and inject it in Main.

## What is still difficult
- Main still owns global state and the full turn loop.
- ConsoleIO and game logic are still coupled, which makes headless simulation harder.
