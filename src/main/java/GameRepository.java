import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

record RoundRecord(int roundNumber, String winnerName, int pointsAwarded, LocalDateTime endedAt) {
}

record SessionRecord(
        LocalDateTime startedAt,
        List<String> playerNames,
        List<RoundRecord> rounds,
        int[] finalScores
) {
}

record RecentGameView(
        long gameId,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        String winnerName,
        int roundsPlayed,
        int topScore
) {
}

record PlayerWinCountView(String playerName, long winCount) {
}

record HighestScoreView(String playerName, int score, long gameId, LocalDateTime completedAt) {
}

final class GameRepository {
    private final PlayerRepository playerRepository = new PlayerRepository();

    void saveSession(SessionRecord session) {
        JpaUtil.inTransaction(entityManager -> {
            List<PlayerEntity> players = new ArrayList<>();
            for (String name : session.playerNames()) {
                players.add(playerRepository.findOrCreate(entityManager, name));
            }

            PlayerEntity sessionWinner = determineSessionWinner(players, session.finalScores());
            GameEntity game = new GameEntity(session.startedAt(), LocalDateTime.now(), sessionWinner);
            entityManager.persist(game);

            for (RoundRecord round : session.rounds()) {
                PlayerEntity roundWinner = playerRepository.findOrCreate(entityManager, round.winnerName());
                entityManager.persist(new RoundEntity(
                        game,
                        round.roundNumber(),
                        roundWinner,
                        round.pointsAwarded(),
                        round.endedAt()
                ));
            }

            for (int i = 0; i < players.size(); i++) {
                entityManager.persist(new ScoreEntity(game, players.get(i), session.finalScores()[i]));
            }
        });
    }

    List<RecentGameView> findRecentGames(int limit) {
        return JpaUtil.query(entityManager -> entityManager.createQuery("""
                        SELECT g.id, g.startedAt, g.endedAt, g.winner.name,
                               (SELECT COUNT(r) FROM RoundEntity r WHERE r.game = g),
                               (SELECT MAX(s.totalScore) FROM ScoreEntity s WHERE s.game = g)
                        FROM GameEntity g
                        ORDER BY g.endedAt DESC
                        """, Object[].class)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(row -> new RecentGameView(
                        (Long) row[0],
                        (LocalDateTime) row[1],
                        (LocalDateTime) row[2],
                        (String) row[3],
                        ((Long) row[4]).intValue(),
                        row[5] == null ? 0 : ((Integer) row[5])
                ))
                .toList());
    }

    List<PlayerWinCountView> findPlayerWinCounts() {
        return JpaUtil.query(entityManager -> entityManager.createQuery("""
                        SELECT g.winner.name, COUNT(g)
                        FROM GameEntity g
                        WHERE g.winner IS NOT NULL
                        GROUP BY g.winner.name
                        ORDER BY COUNT(g) DESC, g.winner.name ASC
                        """, Object[].class)
                .getResultList()
                .stream()
                .map(row -> new PlayerWinCountView((String) row[0], (Long) row[1]))
                .toList());
    }

    List<HighestScoreView> findHighestScores(int limit) {
        return JpaUtil.query(entityManager -> entityManager.createQuery("""
                        SELECT s.player.name, s.totalScore, s.game.id, s.game.endedAt
                        FROM ScoreEntity s
                        ORDER BY s.totalScore DESC, s.game.endedAt DESC
                        """, Object[].class)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(row -> new HighestScoreView(
                        (String) row[0],
                        (Integer) row[1],
                        (Long) row[2],
                        (LocalDateTime) row[3]
                ))
                .toList());
    }

    private PlayerEntity determineSessionWinner(List<PlayerEntity> players, int[] finalScores) {
        int winnerIndex = 0;
        for (int i = 1; i < finalScores.length; i++) {
            if (finalScores[i] > finalScores[winnerIndex]) {
                winnerIndex = i;
            }
        }
        return players.get(winnerIndex);
    }
}
