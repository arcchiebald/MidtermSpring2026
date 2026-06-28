# UNO CLI — Final Project

A command-line UNO game with full rule support, testable game architecture, and optional database persistence.

## Prerequisites

- Java 17 or newer
- Maven 3.9+

## Quick Start

Bot game to 500 points:

```bash
mvn exec:java -Dexec.args="--bots 3 --quiet --seed 42"
```

Interactive game:

```bash
mvn exec:java -Dexec.args="--human --bots 2"
```

## Build and Test

```bash
mvn compile
mvn test
mvn package
```

Helper scripts:

```bash
scripts/compile.sh
scripts/test.sh
scripts/run.sh --bots 3 --quiet
scripts/run.sh --human --bots 2
```

## CLI Options

| Flag | Description |
|------|-------------|
| `--bots N` | Number of bot players (default 3) |
| `--human` | Add a human player |
| `--target N` | Score to win (default 500) |
| `--games N` | Optional round limit |
| `--quiet` | Minimal output |
| `--seed N` | Random seed |
| `--no-persist` | Skip database save |
| `--stats recent\|wins\|highscores` | View saved game stats |

## Card Input

```text
R5   red 5          YS   yellow skip
BR   blue reverse   G+2  green draw two
W    wild           W4   wild draw four
draw                 uno (call UNO with one card)
```

## Architecture

Game rules are separated from the CLI:

- `UnoGame` — state and rule execution (testable without console)
- `CardRules`, `TurnEffects`, `ScoreCalculator`, `DeckFactory` — rule modules
- `Main`, `ConsoleIO` — CLI only

## Documentation

- `docs/rules-supported.md` — implemented rules and variants
- `docs/final-report.md` — architecture, tests, limitations
- `docs/database.md` — persistence setup

## Persistence

Game sessions are saved to an embedded H2 database by default:

```bash
mvn exec:java -Dexec.args="--stats recent --limit 5"
mvn exec:java -Dexec.args="--stats wins"
```

## Docker

```bash
docker build -t uno-cli .
docker run --rm -it uno-cli --bots 3 --quiet
```

## Submission Deliverables

- Source code and tests
- `README.md`
- `docs/rules-supported.md`
- `docs/final-report.md`
