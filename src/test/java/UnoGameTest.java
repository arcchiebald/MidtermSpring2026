import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnoGameTest {
    private UnoGame game;

    @BeforeEach
    void setUp() {
        game = UnoGame.createSession(List.of("Alice", "Bob", "Carol"), new Random(42));
    }

    @Test
    void newRoundDealsSevenCardsEach() {
        game.startNewRound();
        for (int i = 0; i < game.getPlayerCount(); i++) {
            assertEquals(7, game.getHand(i).size());
        }
    }

    @Test
    void startingDiscardIsNotWild() {
        game.startNewRound();
        assertFalse(game.getUpCard().startsWith("W"));
    }

    @Test
    void legalPlayByColor() {
        game.setHandForTesting(0, List.of("R3"));
        game.setUpCardForTesting("R9");
        game.setCurrentPlayerForTesting(0);
        UnoGame.PlayResult result = game.playCard(0, 0, null);
        assertTrue(result.success());
        assertEquals("R3", game.getUpCard());
    }

    @Test
    void illegalPlayRejected() {
        game.setHandForTesting(0, List.of("B3"));
        game.setUpCardForTesting("R9");
        game.setCurrentPlayerForTesting(0);
        UnoGame.PlayResult result = game.playCard(0, 0, null);
        assertFalse(result.success());
    }

    @Test
    void wildRequiresChosenColor() {
        game.setHandForTesting(0, List.of("W", "R2"));
        game.setUpCardForTesting("R9");
        game.setCurrentPlayerForTesting(0);
        UnoGame.PlayResult result = game.playCard(0, 0, "G");
        assertTrue(result.success());
        assertEquals("G", game.getCalledColor());
    }

    @Test
    void wildDrawFourDrawsAndSkipsNextPlayer() {
        game.setHandForTesting(0, List.of("W4", "R2"));
        game.setHandForTesting(1, List.of("R3", "R4", "R5", "R6", "R7", "R8", "R9"));
        game.setHandForTesting(2, List.of("G1", "G2", "G3", "G4", "G5", "G6", "G7"));
        game.setUpCardForTesting("R9");
        game.setCurrentPlayerForTesting(0);
        game.setDeckForTesting(List.of("Y1", "Y2", "Y3", "Y4"));
        int bobBefore = game.getHand(1).size();
        UnoGame.PlayResult result = playAndApply(0, 0, "B");
        assertTrue(result.success());
        assertEquals(bobBefore + 4, game.getHand(1).size());
        assertEquals(2, game.getCurrentPlayer());
    }

    @Test
    void skipAdvancesTwoPlayers() {
        game.setHandForTesting(0, List.of("RS", "R2"));
        game.setUpCardForTesting("R9");
        game.setCurrentPlayerForTesting(0);
        playAndApply(0, 0, null);
        assertEquals(2, game.getCurrentPlayer());
    }

    @Test
    void reverseChangesDirectionForThreePlayers() {
        game.setHandForTesting(0, List.of("RR", "R2"));
        game.setUpCardForTesting("R9");
        game.setCurrentPlayerForTesting(0);
        game.setDirectionForTesting(1);
        playAndApply(0, 0, null);
        assertEquals(-1, game.getDirection());
        assertEquals(2, game.getCurrentPlayer());
    }

    @Test
    void reverseActsLikeSkipForTwoPlayers() {
        UnoGame twoPlayer = UnoGame.createSession(List.of("A", "B"), new Random(1));
        twoPlayer.setHandForTesting(0, List.of("RR"));
        twoPlayer.setUpCardForTesting("R9");
        twoPlayer.setCurrentPlayerForTesting(0);
        UnoGame.PlayResult result = twoPlayer.playCard(0, 0, null);
        if (result.playedCard() != null) {
            twoPlayer.applyPlayedCardEffect(result.playedCard());
        }
        assertEquals(0, twoPlayer.getCurrentPlayer());
    }

    @Test
    void drawTwoPenalizesNextPlayer() {
        game.setHandForTesting(0, List.of("R+2", "R3"));
        game.setHandForTesting(1, List.of("R4", "R5", "R6", "R7", "R8", "R9", "Y1"));
        game.setUpCardForTesting("R9");
        game.setCurrentPlayerForTesting(0);
        game.setDeckForTesting(List.of("Y2", "Y3"));
        int before = game.getHand(1).size();
        playAndApply(0, 0, null);
        assertEquals(before + 2, game.getHand(1).size());
        assertEquals(2, game.getCurrentPlayer());
    }

    @Test
    void drawThenPass() {
        game.setHandForTesting(0, List.of("B1"));
        game.setUpCardForTesting("R9");
        game.setCurrentPlayerForTesting(0);
        game.setDeckForTesting(List.of("G2"));
        UnoGame.DrawResult draw = game.drawCard(0);
        assertEquals("G2", draw.card());
        assertFalse(draw.legalToPlay());
        game.passTurn();
        assertEquals(1, game.getCurrentPlayer());
    }

    @Test
    void drawThenPlayIfLegal() {
        game.setHandForTesting(0, List.of("B1"));
        game.setUpCardForTesting("R9");
        game.setCurrentPlayerForTesting(0);
        game.setDeckForTesting(List.of("R2"));
        UnoGame.DrawResult draw = game.drawCard(0);
        assertTrue(draw.legalToPlay());
        UnoGame.PlayResult play = game.playDrawnCardWithColor(0, null);
        assertTrue(play.success());
        assertEquals("R2", game.getUpCard());
    }

    @Test
    void roundWinnerScoresOpponentCards() {
        game.setHandForTesting(0, List.of("R1"));
        game.setHandForTesting(1, List.of("B9", "GS"));
        game.setHandForTesting(2, List.of("W"));
        game.setUpCardForTesting("R9");
        game.setCurrentPlayerForTesting(0);
        UnoGame.PlayResult result = game.playCard(0, 0, null);
        assertNotNull(result.roundResult());
        assertEquals(9 + 20 + 50, result.roundResult().points());
        assertEquals(79, game.getScore(0));
    }

    @Test
    void unoCallPreventsMissedPenalty() {
        game.setHandForTesting(0, List.of("R1", "R2"));
        game.setUpCardForTesting("R9");
        game.setCurrentPlayerForTesting(0);
        assertTrue(game.callUno(0));
        playAndApply(0, 0, null);
        game.passTurn();
        game.setCurrentPlayerForTesting(1);
        List<UnoGame.PenaltyResult> penalties = game.applyMissedUnoPenalties();
        assertTrue(penalties.isEmpty());
    }

    @Test
    void missedUnoDrawsTwoCards() {
        game.setHandForTesting(0, List.of("R1", "R2"));
        game.setUpCardForTesting("R9");
        game.setCurrentPlayerForTesting(0);
        playAndApply(0, 0, null);
        game.passTurn();
        game.setCurrentPlayerForTesting(1);
        List<UnoGame.PenaltyResult> penalties = game.applyMissedUnoPenalties();
        assertEquals(1, penalties.size());
        assertEquals(0, penalties.get(0).playerIndex());
        assertEquals(2, penalties.get(0).cardsDrawn());
        assertEquals(3, game.getHand(0).size());
    }

    @Test
    void canCallUnoOnlyWithTwoCards() {
        game.setHandForTesting(0, List.of("R1", "R2"));
        assertTrue(game.canCallUno(0));
        assertTrue(game.callUno(0));
        game.setHandForTesting(0, List.of("R1"));
        assertFalse(game.callUno(0));
    }

    @Test
    void multiRoundContinuesUntilTargetScore() {
        game.setScoreForTesting(0, 490);
        game.setHandForTesting(0, List.of("R1"));
        game.setHandForTesting(1, List.of("B9", "GS"));
        game.setHandForTesting(2, List.of("W"));
        game.setUpCardForTesting("R5");
        game.setCurrentPlayerForTesting(0);
        game.playCard(0, 0, null);
        assertTrue(game.hasWinner(500));
        assertEquals(0, game.getWinningPlayerIndex(500));
    }

    @Test
    void deckReshufflesFromDiscard() {
        game.setDeckForTesting(new ArrayList<>());
        game.setDiscardForTesting(List.of("R5", "Y3"));
        game.setHandForTesting(0, List.of("R1"));
        String drawn = game.drawFromDeck();
        assertEquals("R5", drawn);
        assertTrue(game.getDiscardForTesting().isEmpty());
    }

    @Test
    void emptyDeckAndDiscardReturnsWildFallback() {
        game.setDeckForTesting(new ArrayList<>());
        game.setDiscardForTesting(new ArrayList<>());
        assertEquals("W", game.drawFromDeck());
    }

    private UnoGame.PlayResult playAndApply(int playerIndex, int cardIndex, String chosenColor) {
        UnoGame.PlayResult result = game.playCard(playerIndex, cardIndex, chosenColor);
        if (result.success() && result.playedCard() != null) {
            game.applyPlayedCardEffect(result.playedCard());
        }
        return result;
    }
}
