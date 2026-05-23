import java.util.List;

class ScoreCalculator {
    static int scoreOpponents(List<? extends List<String>> hands, int winnerIndex) {
        int points = 0;
        for (int i = 0; i < hands.size(); i++) {
            if (i == winnerIndex) {
                continue;
            }
            List<String> hand = hands.get(i);
            for (int j = 0; j < hand.size(); j++) {
                points += CardRules.points(hand.get(j));
            }
        }
        return points;
    }
}
