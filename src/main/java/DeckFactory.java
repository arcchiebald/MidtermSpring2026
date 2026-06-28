import java.util.ArrayList;
import java.util.List;

class DeckFactory {
    static final int STANDARD_DECK_SIZE = 108;
    private static final String[] COLORS = {"R", "Y", "G", "B"};

    static List<String> createStandardDeck() {
        List<String> deck = new ArrayList<>(STANDARD_DECK_SIZE);
        for (String color : COLORS) {
            deck.add(color + "0");
            for (int n = 1; n <= 9; n++) {
                deck.add(color + n);
                deck.add(color + n);
            }
            deck.add(color + "S");
            deck.add(color + "S");
            deck.add(color + "R");
            deck.add(color + "R");
            deck.add(color + "+2");
            deck.add(color + "+2");
        }
        for (int i = 0; i < 4; i++) {
            deck.add("W");
            deck.add("W4");
        }
        return deck;
    }
}
