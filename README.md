# Midterm UNO CLI

This is a standalone CLI UNO-like game.

The code is written as plausible feature-grown Java: almost everything lives in one procedural `Main` class. It works, but it has mixed responsibilities, duplicated rule logic, primitive-heavy card handling, global state, and condition-heavy gameplay code. The goal is to refactor it safely, not rewrite it.

## Prerequisites

- Java 17 or newer
- Maven 3.9+

## Local Build

```bash
mvn compile
```

Or use the helper script:

```bash
scripts/compile.sh
```

## Local Test

```bash
mvn test
```

Or:

```bash
scripts/test.sh
```

## Local Run

Bot games:

```bash
mvn exec:java -Dexec.args="--bots 3 --games 5 --quiet"
```

Interactive game:

```bash
mvn exec:java -Dexec.args="--human --bots 2 --games 1"
```

Or use the helper script:

```bash
scripts/run.sh --bots 3 --games 5 --quiet
scripts/run.sh --human --bots 2 --games 1
```

Run the packaged JAR:

```bash
java -jar target/uno-cli-1.0.0.jar --bots 3 --games 1 --quiet
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

## Package Creation

```bash
mvn package
```

This creates `target/uno-cli-1.0.0.jar`.

## Docker Build

```bash
docker build -t uno-cli .
```

## Docker Run

```bash
docker run --rm -it uno-cli --bots 3 --games 1 --quiet
```

For an interactive human game:

```bash
docker run --rm -it uno-cli --human --bots 2 --games 1
```

## Logging

Game events are logged with `java.util.logging` to stderr. Logs cover game start, player turns, cards played, cards drawn, invalid input, and round or game end. Player-facing CLI output is unchanged.

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
