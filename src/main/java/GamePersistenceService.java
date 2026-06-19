import java.util.List;

final class GamePersistenceService {
    private final GameRepository gameRepository = new GameRepository();

    void saveSession(SessionRecord session) {
        gameRepository.saveSession(session);
    }

    void printRecentGames(int limit) {
        List<RecentGameView> games = gameRepository.findRecentGames(limit);
        if (games.isEmpty()) {
            System.out.println("No saved games yet.");
            return;
        }
        System.out.println("Recent games:");
        for (RecentGameView game : games) {
            System.out.printf(
                    "  #%d  %s -> %s  winner=%s  rounds=%d  top score=%d%n",
                    game.gameId(),
                    game.startedAt(),
                    game.endedAt(),
                    game.winnerName(),
                    game.roundsPlayed(),
                    game.topScore()
            );
        }
    }

    void printPlayerWinCounts() {
        List<PlayerWinCountView> wins = gameRepository.findPlayerWinCounts();
        if (wins.isEmpty()) {
            System.out.println("No winners recorded yet.");
            return;
        }
        System.out.println("Player win counts:");
        for (PlayerWinCountView win : wins) {
            System.out.printf("  %s: %d%n", win.playerName(), win.winCount());
        }
    }

    void printHighestScores(int limit) {
        List<HighestScoreView> scores = gameRepository.findHighestScores(limit);
        if (scores.isEmpty()) {
            System.out.println("No scores recorded yet.");
            return;
        }
        System.out.println("Highest scores:");
        for (HighestScoreView score : scores) {
            System.out.printf(
                    "  %s: %d (game #%d, completed %s)%n",
                    score.playerName(),
                    score.score(),
                    score.gameId(),
                    score.completedAt()
            );
        }
    }
}
