import java.util.logging.Level;
import java.util.logging.Logger;

class GameLog {
    private static final Logger LOGGER = Logger.getLogger("uno");

    static void gameStart(int gameNumber, int playerCount) {
        LOGGER.log(Level.INFO, "Game {0} started with {1} players", new Object[]{gameNumber, playerCount});
    }

    static void playerTurn(String playerName) {
        LOGGER.log(Level.INFO, "Turn: {0}", playerName);
    }

    static void cardPlayed(String playerName, String card) {
        LOGGER.log(Level.INFO, "{0} played {1}", new Object[]{playerName, card});
    }

    static void cardDrawn(String playerName, String card) {
        LOGGER.log(Level.INFO, "{0} drew {1}", new Object[]{playerName, card});
    }

    static void invalidInput(String playerName, String reason) {
        LOGGER.log(Level.WARNING, "Invalid input from {0}: {1}", new Object[]{playerName, reason});
    }

    static void roundEnd(String winnerName, int points) {
        LOGGER.log(Level.INFO, "Round ended. Winner: {0}, points scored: {1}", new Object[]{winnerName, points});
    }

    static void gameEnd() {
        LOGGER.log(Level.INFO, "Game ended at safety limit");
    }

    static void sessionEnd() {
        LOGGER.log(Level.INFO, "Session ended");
    }
}
