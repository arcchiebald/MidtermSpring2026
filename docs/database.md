# Database Persistence

Assignment 5 adds ORM-backed persistence for UNO game history.

## Stack

| Component | Choice |
|-----------|--------|
| Database | H2 (embedded, file-based for local runs) |
| ORM | Jakarta Persistence (JPA) with Hibernate |
| Access layer | `PlayerRepository`, `GameRepository`, `GamePersistenceService` |

Game logic does not contain raw SQL. Queries live in repository classes.

## Schema

Tables:

- `players` — unique player names
- `games` — session start/end timestamps and final winner
- `rounds` — round number, winner, points awarded, completion timestamp
- `scores` — per-player total score for each saved session

See `src/main/resources/db/schema.sql` for the documented DDL.

Hibernate creates/updates tables automatically on startup (`hibernate.hbm2ddl.auto=update`).

## Configuration

Connection settings are read from environment variables:

| Variable | Default |
|----------|---------|
| `UNO_DB_URL` | `jdbc:h2:file:./data/uno;AUTO_SERVER=TRUE` |
| `UNO_DB_USER` | `sa` |
| `UNO_DB_PASSWORD` | *(empty)* |

Do not commit real credentials. Override the variables for non-local databases if needed.

Example PostgreSQL override:

```bash
export UNO_DB_URL='jdbc:postgresql://localhost:5432/uno'
export UNO_DB_USER='uno_app'
export UNO_DB_PASSWORD='your-secret'
```

The H2 database files are written under `./data/` when using the default URL.

## Persisting Game Results

By default, every completed CLI session is saved when the game exits.

Run bot games and persist results:

```bash
mvn exec:java -Dexec.args="--bots 3 --games 3 --quiet --seed 42"
```

Skip persistence:

```bash
mvn exec:java -Dexec.args="--bots 3 --games 1 --quiet --no-persist"
```

Saved data includes:

- player names
- session start/end timestamps
- each round winner and points
- final per-player scores
- session winner (highest total score)

## Query / Report Commands

View recent saved sessions:

```bash
mvn exec:java -Dexec.args="--stats recent --limit 5"
```

View player win counts:

```bash
mvn exec:java -Dexec.args="--stats wins"
```

View highest scores:

```bash
mvn exec:java -Dexec.args="--stats highscores --limit 10"
```

These commands read from the configured database and print reports to stdout.

## Persistence Tests

Tests use an isolated in-memory H2 database. They do not depend on local `./data/` files or manual setup.

Run all tests:

```bash
mvn test
```

Run only persistence tests:

```bash
mvn -Dtest=GamePersistenceTest test
```

## Source Layout

| File | Role |
|------|------|
| `src/main/resources/META-INF/persistence.xml` | JPA unit configuration |
| `src/main/resources/db/schema.sql` | Documented schema |
| `DatabaseSettings.java` | Environment-based JDBC settings |
| `JpaUtil.java` | EntityManager factory and transaction helpers |
| `*Entity.java` | JPA entity mappings |
| `GameRepository.java` | Save and query operations |
| `GamePersistenceService.java` | CLI report formatting |
| `src/test/java/GamePersistenceTest.java` | Isolated persistence tests |
