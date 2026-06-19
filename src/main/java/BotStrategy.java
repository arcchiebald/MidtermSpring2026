import java.util.List;

class BotStrategy {
    static int chooseCard(List<String> hand, String upCard, String calledColor) {
        int index = chooseByRank(hand, upCard, calledColor, "DRAW_TWO");
        if (index >= 0) {
            return index;
        }
        index = chooseByRank(hand, upCard, calledColor, "SKIP");
        if (index >= 0) {
            return index;
        }
        index = chooseByRank(hand, upCard, calledColor, "NUMBER");
        if (index >= 0) {
            return index;
        }
        return findWild(hand);
    }

    private static int chooseByRank(List<String> hand, String upCard, String calledColor, String targetRank) {
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (CardRules.rank(card).equals(targetRank) && CardRules.isLegal(card, upCard, calledColor)) {
                return i;
            }
        }
        return -1;
    }

    private static int findWild(List<String> hand) {
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).startsWith("W")) {
                return i;
            }
        }
        return -1;
    }

    static String chooseColor(List<String> hand) {
        int r = 0;
        int y = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < hand.size(); i++) {
            String c = CardRules.color(hand.get(i));
            switch (c) {
                case "R" -> r++;
                case "Y" -> y++;
                case "G" -> g++;
                case "B" -> b++;
                default -> {
                }
            }
        }
        if (r >= y && r >= g && r >= b) {
            return "R";
        } else if (y >= r && y >= g && y >= b) {
            return "Y";
        } else if (g >= r && g >= y && g >= b) {
            return "G";
        } else {
            return "B";
        }
    }
}
