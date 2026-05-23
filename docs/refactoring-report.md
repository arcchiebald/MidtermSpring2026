# Refactoring Report

## Characterized behavior
- Legal play matching by color, number, action type, and called color.
- Wild and wild draw four are always legal.
- Skip, reverse, draw two, and wild draw four effects on turn flow.
- Bot choice order favors draw two, then skip, then numbers, then wild.
- Scoring sums opponent hands with standard point values.
- Draw pile reshuffle and the fallback W card when both piles are empty.

## Worst design problems
- Single Main class with global mutable state.
- Duplicated legal-play checks in the turn loop and bot logic.
- Turn effects, scoring, and I/O interleaved in one method.
- Console prompts mixed with rule validation.

## Refactorings performed
- Extracted CardRules, TurnEffects, TurnEffect, ScoreCalculator, BotStrategy, and ConsoleIO.
- Centralized legal-play checks and card parsing in CardRules.
- Moved effect application into applyCardEffect and TurnEffects.
- Moved input and output prompts into ConsoleIO.
- Expanded selfTest into characterization checks for rules and edge cases.

## Behavior intentionally preserved
- Same prompts and output strings.
- Humans may type draw even with legal plays.
- Invalid index still causes a penalty card and turn loss.
- Bots still auto-play a drawn card when legal.
- Safety limit behavior and draw fallback card.

## Remaining risks
- Global state in Main still couples many behaviors.
- ConsoleIO still calls rule checks when validating card codes.
- No full-game deterministic integration test yet.
