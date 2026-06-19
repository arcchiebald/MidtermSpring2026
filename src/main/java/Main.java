import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.LogManager;

public class Main {
    static ArrayList<String> playerNames = new ArrayList<>();
    static ArrayList<Boolean> humanPlayers = new ArrayList<>();
    static ArrayList<ArrayList<String>> hands = new ArrayList<>();
    static ArrayList<String> deck = new ArrayList<>();
    static ArrayList<String> discard = new ArrayList<>();
    static int[] scores = new int[10];
    static int currentPlayer = 0;
    static int direction = 1;
    static String upCard = "";
    static String calledColor = "";
    static boolean quiet = false;
    static Random random = new Random();
    static ConsoleIO io;
    static final GamePersistenceService persistenceService = new GamePersistenceService();

    static record RoundOutcome(String winnerName, int points) {
    }

    public static void main(String[] args) {
        configureLogging();

        int bots = 3;
        int games = 1;
        boolean human = false;
        long seed = System.currentTimeMillis();
        boolean persist = true;
        String statsMode = null;
        int statsLimit = 10;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--bots") && i + 1 < args.length) {
                bots = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--games") && i + 1 < args.length) {
                games = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--human")) {
                human = true;
            } else if (args[i].equals("--quiet")) {
                quiet = true;
            } else if (args[i].equals("--seed") && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            } else if (args[i].equals("--no-persist")) {
                persist = false;
            } else if (args[i].equals("--limit") && i + 1 < args.length) {
                statsLimit = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--stats") && i + 1 < args.length) {
                statsMode = args[++i];
            } else if (args[i].equals("--self-test")) {
                selfTest();
                return;
            } else if (args[i].equals("--help")) {
                printHelp();
                return;
            }
        }

        if (statsMode != null) {
            runStats(statsMode, statsLimit);
            return;
        }

        random = new Random(seed);
        io = new ConsoleIO(new Scanner(System.in), quiet);
        setupPlayers(bots, human);

        if (playerNames.size() < 2 || playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        SessionTracker sessionTracker = new SessionTracker();

        for (int g = 1; g <= games; g++) {
            if (!quiet) {
                System.out.println("\n=== Game " + g + " ===");
            }
            GameLog.gameStart(g, playerNames.size());
            RoundOutcome outcome = playGame();
            if (outcome != null) {
                sessionTracker.recordRound(g, outcome.winnerName(), outcome.points());
            }
        }

        System.out.println("\nFinal scores:");
        for (int i = 0; i < playerNames.size(); i++) {
            System.out.println(playerNames.get(i) + ": " + scores[i]);
        }
        GameLog.sessionEnd();

        if (persist) {
            persistenceService.saveSession(sessionTracker.toRecord(playerNames, scores));
            if (!quiet) {
                System.out.println("Game session saved to database.");
            }
        }
    }

    static void printHelp() {
        System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
        System.out.println("                      [--no-persist]");
        System.out.println("                      [--stats recent|wins|highscores] [--limit N]");
    }

    static void runStats(String mode, int limit) {
        switch (mode) {
            case "recent" -> persistenceService.printRecentGames(limit);
            case "wins" -> persistenceService.printPlayerWinCounts();
            case "highscores" -> persistenceService.printHighestScores(limit);
            default -> {
                System.out.println("Unknown stats mode: " + mode);
                printHelp();
            }
        }
    }

    static void configureLogging() {
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream("logging.properties")) {
            if (in != null) {
                LogManager.getLogManager().readConfiguration(in);
            }
        } catch (IOException ignored) {
        }
    }

    static void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
            hands.add(new ArrayList<>());
        }
        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
            hands.add(new ArrayList<>());
        }
    }

    static RoundOutcome playGame() {
        deck.clear();
        String[] colors = {"R", "Y", "G", "B"};
        for (String color : colors) {
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
        Collections.shuffle(deck, random);
        discard.clear();
        for (int i = 0; i < hands.size(); i++) {
            hands.get(i).clear();
        }
        for (int i = 0; i < playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) {
                hands.get(i).add(draw());
            }
        }
        upCard = draw();
        while (upCard.startsWith("W")) {
            discard.add(upCard);
            upCard = draw();
        }
        calledColor = "";
        direction = 1;
        currentPlayer = random.nextInt(playerNames.size());

        int guard = 0;
        while (guard < 3000) {
            guard++;
            String name = playerNames.get(currentPlayer);
            ArrayList<String> hand = hands.get(currentPlayer);

            GameLog.playerTurn(name);
            io.showUpCard(upCard, calledColor);
            io.showHand(name, hand);

            int chosen = -1;
            if (humanPlayers.get(currentPlayer)) {
                chosen = io.askHuman(name, hand, upCard, calledColor);
            } else {
                chosen = BotStrategy.chooseCard(hand, upCard, calledColor);
            }

            if (chosen == -1) {
                String drawn = draw();
                hand.add(drawn);
                GameLog.cardDrawn(name, drawn);
                io.showDraw(name, drawn);
                if (CardRules.isLegal(drawn, upCard, calledColor)) {
                    if (!humanPlayers.get(currentPlayer)) {
                        chosen = hand.size() - 1;
                    } else if (io.confirmPlayDrawnCard(drawn)) {
                        chosen = hand.size() - 1;
                    }
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    GameLog.invalidInput(name, "invalid card index");
                    io.showInvalidIndex(name);
                    String penalty = draw();
                    hand.add(penalty);
                    GameLog.cardDrawn(name, penalty);
                    next();
                    continue;
                }

                String card = hand.get(chosen);

                if (!CardRules.isLegal(card, upCard, calledColor)) {
                    GameLog.invalidInput(name, "illegal card " + card);
                    io.showIllegalCard(name, card);
                    String penalty = draw();
                    hand.add(penalty);
                    GameLog.cardDrawn(name, penalty);
                    next();
                    continue;
                }

                hand.remove(chosen);
                discard.add(upCard);
                upCard = card;
                calledColor = "";
                GameLog.cardPlayed(name, card);
                io.showPlay(name, card);

                if (card.equals("W") || card.equals("W4")) {
                    if (humanPlayers.get(currentPlayer)) {
                        calledColor = io.askColor(name);
                    } else {
                        calledColor = BotStrategy.chooseColor(hand);
                    }
                    io.showCalledColor(name, calledColor);
                }

                if (hand.size() == 1) {
                    io.showUno(name);
                }

                if (hand.isEmpty()) {
                    int points = ScoreCalculator.scoreOpponents(hands, currentPlayer);
                    scores[currentPlayer] += points;
                    GameLog.roundEnd(name, points);
                    io.showWin(name, points);
                    return new RoundOutcome(name, points);
                }

                applyCardEffect(card);
            } else {
                next();
            }
        }
        GameLog.gameEnd();
        io.showGameStopped();
        return null;
    }

    static void applyCardEffect(String card) {
        TurnEffect effect = TurnEffects.fromCard(card, playerNames.size());
        if (effect.isReverse()) {
            direction = direction * -1;
        }
        if (effect.getDrawCount() > 0) {
            next();
            String penalized = playerNames.get(currentPlayer);
            for (int i = 0; i < effect.getDrawCount(); i++) {
                String drawn = draw();
                hands.get(currentPlayer).add(drawn);
                GameLog.cardDrawn(penalized, drawn);
            }
            io.showDrawPenalty(playerNames.get(currentPlayer), effect.getDrawCount());
            next();
            return;
        }
        for (int i = 0; i < effect.getAdvanceCount(); i++) {
            next();
        }
    }

    static String draw() {
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


    static void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerNames.size() - 1;
        }
    }

    static void selfTest() {
        int passed = 0;
        if (CardRules.color("R5").equals("R")) passed++; else fail("color R5");
        if (CardRules.rank("G+2").equals("DRAW_TWO")) passed++; else fail("rank +2");
        if (CardRules.points("W4") == 50) passed++; else fail("wild points");
        if (CardRules.isLegal("R2", "R9", "")) passed++; else fail("same color");
        if (CardRules.isLegal("G9", "R9", "")) passed++; else fail("same number");
        if (CardRules.isLegal("GS", "RS", "")) passed++; else fail("same action");
        if (CardRules.isLegal("W", "R9", "")) passed++; else fail("wild legal");
        if (CardRules.isLegal("W4", "R9", "")) passed++; else fail("wild draw four legal");
        if (CardRules.isLegal("B3", "W", "B")) passed++; else fail("called color");
        if (!CardRules.isLegal("B3", "R9", "")) passed++; else fail("illegal mismatch");

        TurnEffect skip = TurnEffects.fromCard("RS", 4);
        if (skip.getAdvanceCount() == 2 && skip.getDrawCount() == 0 && !skip.isReverse()) passed++; else fail("skip effect");

        TurnEffect reverseTwo = TurnEffects.fromCard("RR", 2);
        if (reverseTwo.getAdvanceCount() == 2 && reverseTwo.isReverse()) passed++; else fail("reverse effect two players");

        TurnEffect drawTwo = TurnEffects.fromCard("G+2", 4);
        if (drawTwo.getAdvanceCount() == 2 && drawTwo.getDrawCount() == 2) passed++; else fail("draw two effect");

        TurnEffect drawFour = TurnEffects.fromCard("W4", 4);
        if (drawFour.getAdvanceCount() == 2 && drawFour.getDrawCount() == 4) passed++; else fail("draw four effect");

        ArrayList<String> h = new ArrayList<>();
        h.add("B3");
        h.add("R4");
        h.add("W");
        upCard = "R9";
        calledColor = "";
        if (BotStrategy.chooseCard(h, upCard, calledColor) == 1) passed++; else fail("bot normal before wild");

        ArrayList<String> h2 = new ArrayList<>();
        h2.add("B1");
        h2.add("B2");
        h2.add("R3");
        if (BotStrategy.chooseColor(h2).equals("B")) passed++; else fail("bot color");

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
        if (ScoreCalculator.scoreOpponents(scoreHands, 0) == 9 + 20 + 50) passed++; else fail("score opponents");

        deck.clear();
        discard.clear();
        discard.add("R5");
        random = new Random(1);
        String reshuffled = draw();
        if (reshuffled.equals("R5") && discard.isEmpty()) passed++; else fail("draw reshuffle");

        deck.clear();
        discard.clear();
        if (draw().equals("W")) passed++; else fail("draw fallback");

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
