import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

class UnoGame {
    static final int DEFAULT_TARGET_SCORE = 500;
    static final int MISSED_UNO_PENALTY = 2;
    static final int STARTING_HAND_SIZE = 7;
    static final int MAX_TURN_GUARD = 3000;

    private final List<String> playerNames;
    private final List<List<String>> hands;
    private final List<String> deck;
    private final List<String> discard;
    private final int[] scores;
    private final Random random;

    private int currentPlayer;
    private int direction = 1;
    private String upCard;
    private String calledColor;
    private final boolean[] unoCalled;
    private final List<Integer> missedUnoQueue;

    record RoundResult(int winnerIndex, String winnerName, int points) {
    }

    record PlayResult(boolean success, String error, RoundResult roundResult, String playedCard) {
        static PlayResult ok(String playedCard) {
            return new PlayResult(true, null, null, playedCard);
        }

        static PlayResult okRoundOver(RoundResult round) {
            return new PlayResult(true, null, round, null);
        }

        static PlayResult fail(String error) {
            return new PlayResult(false, error, null, null);
        }
    }

    record DrawResult(String card, boolean legalToPlay, RoundResult roundResult) {
    }

    record PenaltyResult(int playerIndex, int cardsDrawn) {
    }

    UnoGame(List<String> playerNames, int[] scores, Random random) {
        if (playerNames.size() < 2 || playerNames.size() > 4) {
            throw new IllegalArgumentException("UNO needs 2 to 4 players.");
        }
        this.playerNames = new ArrayList<>(playerNames);
        this.scores = scores;
        this.random = random;
        this.hands = new ArrayList<>();
        this.deck = new ArrayList<>();
        this.discard = new ArrayList<>();
        this.unoCalled = new boolean[playerNames.size()];
        this.missedUnoQueue = new ArrayList<>();
        for (int i = 0; i < playerNames.size(); i++) {
            hands.add(new ArrayList<>());
        }
    }

    static UnoGame createSession(List<String> playerNames, Random random) {
        return new UnoGame(playerNames, new int[playerNames.size()], random);
    }

    void startNewRound() {
        deck.clear();
        deck.addAll(DeckFactory.createStandardDeck());
        Collections.shuffle(deck, random);
        discard.clear();
        for (List<String> hand : hands) {
            hand.clear();
        }
        for (int i = 0; i < playerNames.size(); i++) {
            for (int j = 0; j < STARTING_HAND_SIZE; j++) {
                hands.get(i).add(drawFromDeck());
            }
            unoCalled[i] = false;
        }
        upCard = drawFromDeck();
        while (upCard.startsWith("W")) {
            discard.add(upCard);
            upCard = drawFromDeck();
        }
        calledColor = "";
        direction = 1;
        currentPlayer = random.nextInt(playerNames.size());
        missedUnoQueue.clear();
    }

    List<String> getPlayerNames() {
        return Collections.unmodifiableList(playerNames);
    }

    int getPlayerCount() {
        return playerNames.size();
    }

    List<String> getHand(int playerIndex) {
        return Collections.unmodifiableList(hands.get(playerIndex));
    }

    String getUpCard() {
        return upCard;
    }

    String getCalledColor() {
        return calledColor;
    }

    int getCurrentPlayer() {
        return currentPlayer;
    }

    String getCurrentPlayerName() {
        return playerNames.get(currentPlayer);
    }

    int getDirection() {
        return direction;
    }

    int[] getScores() {
        return scores.clone();
    }

    int getScore(int playerIndex) {
        return scores[playerIndex];
    }

    boolean hasWinner(int targetScore) {
        for (int score : scores) {
            if (score >= targetScore) {
                return true;
            }
        }
        return false;
    }

    int getWinningPlayerIndex(int targetScore) {
        int winner = -1;
        int best = -1;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] >= targetScore && scores[i] > best) {
                best = scores[i];
                winner = i;
            }
        }
        return winner;
    }

    List<PenaltyResult> applyMissedUnoPenalties() {
        List<PenaltyResult> penalties = new ArrayList<>();
        List<Integer> toPenalize = new ArrayList<>(missedUnoQueue);
        missedUnoQueue.clear();
        for (int playerIndex : toPenalize) {
            if (hands.get(playerIndex).size() == 1 && !unoCalled[playerIndex]) {
                for (int i = 0; i < MISSED_UNO_PENALTY; i++) {
                    hands.get(playerIndex).add(drawFromDeck());
                }
                penalties.add(new PenaltyResult(playerIndex, MISSED_UNO_PENALTY));
                unoCalled[playerIndex] = false;
            }
        }
        return penalties;
    }

    boolean callUno(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= playerNames.size()) {
            return false;
        }
        if (hands.get(playerIndex).size() == 2) {
            unoCalled[playerIndex] = true;
            missedUnoQueue.remove(Integer.valueOf(playerIndex));
            return true;
        }
        return false;
    }

    boolean canCallUno(int playerIndex) {
        return hands.get(playerIndex).size() == 2;
    }

    boolean needsUnoCall(int playerIndex) {
        return hands.get(playerIndex).size() == 1 && !unoCalled[playerIndex]
                && missedUnoQueue.contains(playerIndex);
    }

    PlayResult playCard(int playerIndex, int cardIndex, String chosenColor) {
        if (playerIndex != currentPlayer) {
            return PlayResult.fail("Not this player's turn.");
        }
        List<String> hand = hands.get(playerIndex);
        if (cardIndex < 0 || cardIndex >= hand.size()) {
            return PlayResult.fail("Invalid card index.");
        }
        String card = hand.get(cardIndex);
        if (!CardRules.isLegal(card, upCard, calledColor)) {
            return PlayResult.fail("Illegal card.");
        }
        boolean leavingOneCard = hand.size() == 2;
        boolean calledUno = unoCalled[playerIndex];
        hand.remove(cardIndex);
        discard.add(upCard);
        upCard = card;
        calledColor = "";
        if (card.equals("W") || card.equals("W4")) {
            if (chosenColor == null || chosenColor.isEmpty()) {
                return PlayResult.fail("Wild cards require a chosen color.");
            }
            calledColor = chosenColor;
        }
        if (hand.size() == 1 && leavingOneCard && !calledUno) {
            queueMissedUnoCheck(playerIndex);
        }
        unoCalled[playerIndex] = false;
        if (hand.isEmpty()) {
            int points = ScoreCalculator.scoreOpponents(hands, playerIndex);
            scores[playerIndex] += points;
            return PlayResult.okRoundOver(new RoundResult(playerIndex, playerNames.get(playerIndex), points));
        }
        return PlayResult.ok(card);
    }

    void applyPlayedCardEffect(String card) {
        applyCardEffect(card);
    }

    PlayResult playDrawnCard(int playerIndex, String drawnCard) {
        List<String> hand = hands.get(playerIndex);
        int cardIndex = hand.lastIndexOf(drawnCard);
        if (cardIndex < 0) {
            return PlayResult.fail("Drawn card not in hand.");
        }
        return playCard(playerIndex, cardIndex, null);
    }

    DrawResult drawCard(int playerIndex) {
        if (playerIndex != currentPlayer) {
            throw new IllegalStateException("Not this player's turn.");
        }
        String drawn = drawFromDeck();
        hands.get(playerIndex).add(drawn);
        boolean legal = CardRules.isLegal(drawn, upCard, calledColor);
        return new DrawResult(drawn, legal, null);
    }

    PlayResult playDrawnCardWithColor(int playerIndex, String chosenColor) {
        List<String> hand = hands.get(playerIndex);
        String drawn = hand.get(hand.size() - 1);
        if (!CardRules.isLegal(drawn, upCard, calledColor)) {
            return PlayResult.fail("Drawn card is not legal.");
        }
        return playCard(playerIndex, hand.size() - 1, chosenColor);
    }

    void passTurn() {
        advanceTurn();
    }

    PlayResult applyIllegalPlayPenalty(int playerIndex) {
        hands.get(playerIndex).add(drawFromDeck());
        advanceTurn();
        return PlayResult.fail("illegal play penalty");
    }

    String drawFromDeck() {
        if (deck.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.isEmpty()) {
            return "W";
        }
        return deck.remove(0);
    }

    List<String> getDeckForTesting() {
        return deck;
    }

    List<String> getDiscardForTesting() {
        return discard;
    }

    void setDeckForTesting(List<String> cards) {
        deck.clear();
        deck.addAll(cards);
    }

    void setDiscardForTesting(List<String> cards) {
        discard.clear();
        discard.addAll(cards);
    }

    void setUpCardForTesting(String card) {
        upCard = card;
    }

    void setCalledColorForTesting(String color) {
        calledColor = color;
    }

    void setCurrentPlayerForTesting(int index) {
        currentPlayer = index;
    }

    void setDirectionForTesting(int dir) {
        direction = dir;
    }

    void setHandForTesting(int playerIndex, List<String> cards) {
        hands.get(playerIndex).clear();
        hands.get(playerIndex).addAll(cards);
        unoCalled[playerIndex] = false;
    }

    void setScoreForTesting(int playerIndex, int score) {
        scores[playerIndex] = score;
    }

    private void queueMissedUnoCheck(int playerIndex) {
        if (!unoCalled[playerIndex] && !missedUnoQueue.contains(playerIndex)) {
            missedUnoQueue.add(playerIndex);
        }
    }

    void finalizeTurnForMissedUno(int playerIndex) {
        if (hands.get(playerIndex).size() == 1 && !unoCalled[playerIndex]) {
            queueMissedUnoCheck(playerIndex);
        }
    }

    private void applyCardEffect(String card) {
        TurnEffect effect = TurnEffects.fromCard(card, playerNames.size());
        if (effect.isReverse()) {
            direction *= -1;
        }
        if (effect.getDrawCount() > 0) {
            advanceTurn();
            for (int i = 0; i < effect.getDrawCount(); i++) {
                hands.get(currentPlayer).add(drawFromDeck());
            }
            advanceTurn();
            return;
        }
        for (int i = 0; i < effect.getAdvanceCount(); i++) {
            advanceTurn();
        }
    }

    private void advanceTurn() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerNames.size() - 1;
        }
    }
}
