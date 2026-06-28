# Supported UNO Rules

This document lists which rules from the course reference (`Final_Project_UNO_rules_reference.md`) are implemented in this project and notes any variants or simplifications.

## Implemented Rules

| Rule | Status | Notes |
|------|--------|-------|
| Deck composition (108 cards) | Implemented | Four colors, numbers 0–9, Skip, Reverse, Draw Two, Wild, Wild Draw Four |
| Legal play validation | Implemented | Match by color, number, or action type; wild cards always legal |
| Skip | Implemented | Next player loses turn |
| Reverse | Implemented | Direction reverses for 3+ players; acts like Skip in 2-player games |
| Draw Two | Implemented | Next player draws 2 and loses turn; no stacking |
| Wild | Implemented | Player chooses active color |
| Wild Draw Four | Implemented | Player chooses color; next player draws 4 and loses turn |
| Draw/pass behavior | Implemented | Draw one card; may play immediately if legal (human confirms); otherwise pass |
| UNO call and penalty | Implemented | Call `uno` with 2 cards left before playing; missed call draws 2 at next turn start |
| Round scoring | Implemented | Winner scores opponents' remaining cards (numbers = face value, actions = 20, wilds = 50) |
| Multi-round target score | Implemented | Game continues until a player reaches 500 points (configurable via `--target`) |

## Variants and Simplifications

### Starting discard card

If the initial discard is a Wild or Wild Draw Four, it is placed on the discard pile and a new card is drawn until a non-wild card appears. Action cards (Skip, Reverse, Draw Two) are **not** re-drawn; their effects are not applied at round start.

### Two-player Reverse

In a two-player game, Reverse is treated like Skip: the opponent loses their turn and play returns to the reversing player.

### Draw/pass

When a player cannot or chooses not to play, they draw one card. If the drawn card is legal, the player may play it immediately (bots always play; humans are prompted). If they decline or the card is not legal, the turn passes.

### UNO call timing

- Call UNO when you have **exactly 2 cards**, **before** you play one of them.
- Type `uno` on your turn, then play your card on a later prompt in the same turn.
- Bots call UNO automatically when they have two cards and are about to play.
- If you play from two cards down to one without calling UNO first, you are flagged for a missed-UNO check.
- At the start of the next player's turn, flagged players who still have one card draw two penalty cards.
- There is no confirmation prompt after you play; you must remember to call UNO while you still have two cards.

### Not implemented

- Wild Draw Four challenge rule
- Draw Two / Draw Four stacking
- Other players calling out a missed UNO before the next turn

## Architecture

Game rules live in testable classes separate from the CLI:

- `DeckFactory` — deck composition
- `CardRules` — legality and card values
- `TurnEffects` — action card turn effects
- `ScoreCalculator` — round scoring
- `UnoGame` — game state and rule execution
- `Main` + `ConsoleIO` — CLI interaction only
