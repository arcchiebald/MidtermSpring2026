class TurnEffects {
    static TurnEffect fromCard(String card, int playerCount) {
        String rank = CardRules.rank(card);
        if (rank.equals("SKIP")) {
            return new TurnEffect(2, 0, false);
        }
        if (rank.equals("REVERSE")) {
            int advanceCount = playerCount == 2 ? 2 : 1;
            return new TurnEffect(advanceCount, 0, true);
        }
        if (rank.equals("DRAW_TWO")) {
            return new TurnEffect(2, 2, false);
        }
        if (rank.equals("WILD_DRAW_FOUR")) {
            return new TurnEffect(2, 4, false);
        }
        return new TurnEffect(1, 0, false);
    }
}
