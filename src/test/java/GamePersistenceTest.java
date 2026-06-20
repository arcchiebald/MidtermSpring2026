import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GamePersistenceTest {
    private final GameRepository gameRepository = new GameRepository();

    @BeforeEach
    void setUp() {
        JpaUtil.configureForTests("jdbc:h2:mem:uno_test_" + UUID.randomUUID()
                + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
    }

    @AfterEach
    void tearDown() {
        JpaUtil.close();
    }

    @Test
    void savesSessionWithPlayersRoundsScoresAndWinner() {
        SessionRecord session = new SessionRecord(
                LocalDateTime.of(2026, 6, 10, 12, 0),
                List.of("Bot1", "Bot2", "Bot3"),
                List.of(
                        new RoundRecord(1, "Bot1", 25, LocalDateTime.of(2026, 6, 10, 12, 5)),
                        new RoundRecord(2, "Bot2", 40, LocalDateTime.of(2026, 6, 10, 12, 10))
                ),
                new int[]{65, 40, 0}
        );

        gameRepository.saveSession(session);

        List<RecentGameView> recentGames = gameRepository.findRecentGames(5);
        assertEquals(1, recentGames.size());
        assertEquals("Bot1", recentGames.get(0).winnerName());
        assertEquals(2, recentGames.get(0).roundsPlayed());
        assertEquals(65, recentGames.get(0).topScore());
    }

    @Test
    void reportsPlayerWinCounts() {
        saveSampleSession("Bot1", new int[]{50, 10, 0});
        saveSampleSession("Bot2", new int[]{10, 60, 0});

        List<PlayerWinCountView> wins = gameRepository.findPlayerWinCounts();
        assertEquals(2, wins.size());
        assertEquals("Bot1", wins.get(0).playerName());
        assertEquals(1, wins.get(0).winCount());
        assertEquals("Bot2", wins.get(1).playerName());
        assertEquals(1, wins.get(1).winCount());
    }

    @Test
    void reportsHighestScores() {
        saveSampleSession("Bot1", new int[]{50, 10, 0});
        saveSampleSession("Bot2", new int[]{10, 80, 0});

        List<HighestScoreView> highestScores = gameRepository.findHighestScores(3);
        assertFalse(highestScores.isEmpty());
        assertEquals("Bot2", highestScores.get(0).playerName());
        assertEquals(80, highestScores.get(0).score());
    }

    @Test
    void reusesExistingPlayersAcrossSessions() {
        saveSampleSession("Bot1", new int[]{30, 0, 0});
        saveSampleSession("Bot1", new int[]{45, 0, 0});

        List<PlayerWinCountView> wins = gameRepository.findPlayerWinCounts();
        assertEquals(1, wins.size());
        assertEquals("Bot1", wins.get(0).playerName());
        assertEquals(2, wins.get(0).winCount());
        assertTrue(gameRepository.findRecentGames(10).size() >= 2);
    }

    private void saveSampleSession(String roundWinner, int[] finalScores) {
        List<String> players = List.of("Bot1", "Bot2", "Bot3");
        SessionRecord session = new SessionRecord(
                LocalDateTime.now(),
                players,
                List.of(new RoundRecord(1, roundWinner, 20, LocalDateTime.now())),
                finalScores
        );
        gameRepository.saveSession(session);
    }
}
