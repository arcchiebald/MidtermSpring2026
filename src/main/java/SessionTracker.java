import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

final class SessionTracker {
    private final LocalDateTime startedAt = LocalDateTime.now();
    private final List<RoundRecord> rounds = new ArrayList<>();

    void recordRound(int roundNumber, String winnerName, int pointsAwarded) {
        rounds.add(new RoundRecord(roundNumber, winnerName, pointsAwarded, LocalDateTime.now()));
    }

    SessionRecord toRecord(List<String> playerNames, int[] finalScores) {
        return new SessionRecord(startedAt, List.copyOf(playerNames), List.copyOf(rounds), finalScores.clone());
    }
}
