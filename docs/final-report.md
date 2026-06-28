# Final Project Report

## Implemented UNO Rules

This project implements a full UNO-like ruleset based on the course reference document. All major rule features from the final project rubric are supported:

- **Deck composition**: Standard 108-card deck via `DeckFactory` (four colors, numbered cards, Skip, Reverse, Draw Two, Wild, Wild Draw Four).
- **Legal play validation**: `CardRules.isLegal()` checks color, number, action type, and wild playability.
- **Action cards**: Skip, Reverse, Draw Two handled through `TurnEffects` and applied in `UnoGame`.
- **Wild cards**: Player chooses active color; chosen color affects subsequent legal-play checks.
- **Wild Draw Four**: Same as Wild plus next player draws four and loses turn.
- **Draw/pass**: Player draws one card when unable or unwilling to play; drawn card may be played immediately if legal.
- **UNO call and penalty**: UNO must be called when the player has **exactly two cards left**, **before** playing the card that would leave one card. If the player plays from two cards down to one without calling UNO first, they are flagged and draw two penalty cards at the start of the next player's turn. There is no post-play confirmation prompt.
- **Round scoring**: Winner receives point values of opponents' remaining cards.
- **Multi-round game**: Rounds continue until a player reaches the target score (default 500, set with `--target`).

See `docs/rules-supported.md` for variants and simplifications.

## How to Play from the CLI

### Bot game (default)

```bash
mvn exec:java -Dexec.args="--bots 3 --quiet --seed 42"
```

### Interactive human game

```bash
mvn exec:java -Dexec.args="--human --bots 2"
```

### Options

| Flag | Description |
|------|-------------|
| `--bots N` | Number of bot players (default 3) |
| `--human` | Include a human player named "You" |
| `--target N` | Score target to win the game (default 500) |
| `--games N` | Optional cap on number of rounds |
| `--quiet` | Suppress turn-by-turn output |
| `--seed N` | Random seed for reproducibility |

### Card input

During your turn, enter a card index, card code, `draw`, or `uno`:

```text
R5   red 5
YS   yellow skip
BR   blue reverse
G+2  green draw two
W    wild
W4   wild draw four
draw draw a card
uno  call UNO when you have exactly 2 cards, before you play one
```

### UNO timing

UNO must be said at this exact moment:

1. You have **exactly 2 cards** in your hand.
2. You are about to play one of them (the play would leave you with 1 card).
3. You type **`uno` on that same turn, before you play the card**.

Example: hand `0:YS 1:Y3` → type `uno`, then on the next prompt play `0` or `YS`.

If you play first without calling UNO, the game warns you and applies a **2-card penalty** when the next player's turn begins. Calling UNO with 1 or 3+ cards is not allowed. Playing your last two cards to win the round does not trigger a penalty because the round ends immediately.

## Architecture

The project separates game logic from CLI interaction:

| Class | Responsibility |
|-------|----------------|
| `UnoGame` | Game state, turns, card play, UNO tracking, round lifecycle |
| `DeckFactory` | Standard deck creation |
| `CardRules` | Card parsing, legality, point values |
| `TurnEffects` | Action card effect definitions |
| `ScoreCalculator` | Round scoring |
| `BotStrategy` | Simple bot card and color selection |
| `Main` | Argument parsing, round loop, wiring CLI to engine |
| `ConsoleIO` | Prompts, output, input validation |

All rule execution is testable through `UnoGame` without console input. `Main` only reads user choices and displays game state.

## Tests Added

| Test class | Coverage |
|------------|----------|
| `DeckFactoryTest` | 108-card deck, colors, card types |
| `UnoGameTest` | Legality, Skip, Reverse, Draw Two, Wild Draw Four, draw/pass, UNO penalty, scoring, target score, deck reshuffle |
| `MainTest` | CardRules, TurnEffects, BotStrategy, ScoreCalculator (characterization tests) |
| `GamePersistenceTest` | Database persistence (Assignment 5) |

Run all tests:

```bash
mvn test
```

## Limitations

- No Wild Draw Four challenge rule.
- No Draw Two / Draw Four stacking.
- Bot strategy is simple (prefers action cards, then numbers, then wilds).
- Missed-UNO detection is automated at turn boundaries rather than allowing other players to call it out.
- UNO must be called with exactly two cards left; there is no late UNO call after the player is already down to one card.
- Action cards on the starting discard do not trigger their effects.
- Maximum four players, minimum two.
