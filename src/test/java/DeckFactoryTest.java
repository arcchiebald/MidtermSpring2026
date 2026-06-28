import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeckFactoryTest {
    @Test
    void standardDeckHas108Cards() {
        assertEquals(108, DeckFactory.createStandardDeck().size());
    }

    @Test
    void deckContainsAllColorsAndCardTypes() {
        List<String> deck = DeckFactory.createStandardDeck();
        assertEquals(1, count(deck, "R0"));
        assertEquals(1, count(deck, "Y0"));
        assertEquals(1, count(deck, "G0"));
        assertEquals(1, count(deck, "B0"));
        assertEquals(4, count(deck, "W"));
        assertEquals(4, count(deck, "W4"));
        assertEquals(8, countRank(deck, "S"));
        assertEquals(8, countRank(deck, "R"));
        assertEquals(8, countRank(deck, "+2"));
        assertEquals(76, countNumberCards(deck));
    }

    private int count(List<String> deck, String card) {
        int total = 0;
        for (String c : deck) {
            if (c.equals(card)) {
                total++;
            }
        }
        return total;
    }

    private int countRank(List<String> deck, String suffix) {
        int total = 0;
        for (String c : deck) {
            if (c.endsWith(suffix)) {
                total++;
            }
        }
        return total;
    }

    private int countNumberCards(List<String> deck) {
        int total = 0;
        for (String c : deck) {
            if (CardRules.rank(c).equals("NUMBER")) {
                total++;
            }
        }
        return total;
    }
}
