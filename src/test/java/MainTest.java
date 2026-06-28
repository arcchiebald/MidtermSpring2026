import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
    @Test
    void reverseEffectFourPlayers() {
        TurnEffect reverseFour = TurnEffects.fromCard("RR", 4);
        assertEquals(1, reverseFour.getAdvanceCount());
        assertTrue(reverseFour.isReverse());
    }

    @Test
    void standardDeckSize() {
        assertEquals(108, DeckFactory.createStandardDeck().size());
    }

    @Test
    void colorR5() {
        assertEquals("R", CardRules.color("R5"));
    }

    @Test
    void rankDrawTwo() {
        assertEquals("DRAW_TWO", CardRules.rank("G+2"));
    }

    @Test
    void wildPoints() {
        assertEquals(50, CardRules.points("W4"));
    }

    @Test
    void legalSameColor() {
        assertTrue(CardRules.isLegal("R2", "R9", ""));
    }

    @Test
    void legalSameNumber() {
        assertTrue(CardRules.isLegal("G9", "R9", ""));
    }

    @Test
    void legalSameAction() {
        assertTrue(CardRules.isLegal("GS", "RS", ""));
    }

    @Test
    void legalWild() {
        assertTrue(CardRules.isLegal("W", "R9", ""));
    }

    @Test
    void legalWildDrawFour() {
        assertTrue(CardRules.isLegal("W4", "R9", ""));
    }

    @Test
    void legalCalledColor() {
        assertTrue(CardRules.isLegal("B3", "W", "B"));
    }

    @Test
    void illegalMismatch() {
        assertFalse(CardRules.isLegal("B3", "R9", ""));
    }

    @Test
    void skipEffect() {
        TurnEffect skip = TurnEffects.fromCard("RS", 4);
        assertEquals(2, skip.getAdvanceCount());
        assertEquals(0, skip.getDrawCount());
        assertFalse(skip.isReverse());
    }

    @Test
    void reverseEffectTwoPlayers() {
        TurnEffect reverseTwo = TurnEffects.fromCard("RR", 2);
        assertEquals(2, reverseTwo.getAdvanceCount());
        assertTrue(reverseTwo.isReverse());
    }

    @Test
    void drawTwoEffect() {
        TurnEffect drawTwo = TurnEffects.fromCard("G+2", 4);
        assertEquals(2, drawTwo.getAdvanceCount());
        assertEquals(2, drawTwo.getDrawCount());
    }

    @Test
    void drawFourEffect() {
        TurnEffect drawFour = TurnEffects.fromCard("W4", 4);
        assertEquals(2, drawFour.getAdvanceCount());
        assertEquals(4, drawFour.getDrawCount());
    }

    @Test
    void botNormalBeforeWild() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3");
        hand.add("R4");
        hand.add("W");
        assertEquals(1, BotStrategy.chooseCard(hand, "R9", ""));
    }

    @Test
    void botColor() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B1");
        hand.add("B2");
        hand.add("R3");
        assertEquals("B", BotStrategy.chooseColor(hand));
    }

    @Test
    void scoreOpponents() {
        ArrayList<ArrayList<String>> scoreHands = new ArrayList<>();
        ArrayList<String> winner = new ArrayList<>();
        winner.add("R5");
        ArrayList<String> other1 = new ArrayList<>();
        other1.add("B9");
        other1.add("GS");
        ArrayList<String> other2 = new ArrayList<>();
        other2.add("W");
        scoreHands.add(winner);
        scoreHands.add(other1);
        scoreHands.add(other2);
        assertEquals(9 + 20 + 50, ScoreCalculator.scoreOpponents(scoreHands, 0));
    }

    @Test
    void drawReshuffle() {
        UnoGame game = UnoGame.createSession(java.util.List.of("A", "B"), new Random(1));
        game.setDeckForTesting(new ArrayList<>());
        game.setDiscardForTesting(new ArrayList<>(java.util.List.of("R5", "Y3")));
        String reshuffled = game.drawFromDeck();
        assertEquals("R5", reshuffled);
        assertTrue(game.getDiscardForTesting().isEmpty());
    }

    @Test
    void drawFallback() {
        UnoGame game = UnoGame.createSession(java.util.List.of("A", "B"), new Random(1));
        game.setDeckForTesting(new ArrayList<>());
        game.setDiscardForTesting(new ArrayList<>());
        assertEquals("W", game.drawFromDeck());
    }
}
