import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.LogManager;

public class Main {
    static boolean quiet = false;
    static Random random = new Random();
    static ConsoleIO io;
    static final GamePersistenceService persistenceService = new GamePersistenceService();

    public static void main(String[] args) {
        configureLogging();

        int bots = 3;
        int games = 0;
        int targetScore = UnoGame.DEFAULT_TARGET_SCORE;
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
            } else if (args[i].equals("--target") && i + 1 < args.length) {
                targetScore = Integer.parseInt(args[++i]);
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
        ArrayList<String> playerNames = setupPlayerNames(bots, human);
        ArrayList<Boolean> humanPlayers = setupHumanFlags(bots, human);

        if (playerNames.size() < 2 || playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        SessionTracker sessionTracker = new SessionTracker();
        UnoGame game = UnoGame.createSession(playerNames, random);
        int roundNumber = 0;

        while (!game.hasWinner(targetScore)) {
            if (games > 0 && roundNumber >= games) {
                break;
            }
            roundNumber++;
            if (!quiet) {
                System.out.println("\n=== Round " + roundNumber + " ===");
            }
            GameLog.gameStart(roundNumber, playerNames.size());
            UnoGame.RoundResult outcome = playRound(game, humanPlayers, targetScore);
            if (outcome != null) {
                sessionTracker.recordRound(roundNumber, outcome.winnerName(), outcome.points());
            }
            if (!quiet) {
                printScores(game);
            }
        }

        int winnerIndex = game.getWinningPlayerIndex(targetScore);
        if (winnerIndex >= 0) {
            System.out.println("\nGame over! " + playerNames.get(winnerIndex) + " wins with "
                    + game.getScore(winnerIndex) + " points (target " + targetScore + ").");
        } else {
            System.out.println("\nFinal scores:");
            printScores(game);
        }
        GameLog.sessionEnd();

        if (persist) {
            persistenceService.saveSession(sessionTracker.toRecord(playerNames, game.getScores()));
            if (!quiet) {
                System.out.println("Game session saved to database.");
            }
        }
    }

    static void printHelp() {
        System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--target N] [--human] [--quiet] [--seed N]");
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

    static ArrayList<String> setupPlayerNames(int bots, boolean human) {
        ArrayList<String> names = new ArrayList<>();
        if (human) {
            names.add("You");
        }
        for (int i = 1; i <= bots; i++) {
            names.add("Bot" + i);
        }
        return names;
    }

    static ArrayList<Boolean> setupHumanFlags(int bots, boolean human) {
        ArrayList<Boolean> flags = new ArrayList<>();
        if (human) {
            flags.add(Boolean.TRUE);
        }
        for (int i = 1; i <= bots; i++) {
            flags.add(Boolean.FALSE);
        }
        return flags;
    }

    static void printScores(UnoGame game) {
        for (int i = 0; i < game.getPlayerCount(); i++) {
            System.out.println(game.getPlayerNames().get(i) + ": " + game.getScore(i));
        }
    }

    static UnoGame.RoundResult playRound(UnoGame game, ArrayList<Boolean> humanPlayers, int targetScore) {
        game.startNewRound();
        int guard = 0;

        while (guard < UnoGame.MAX_TURN_GUARD) {
            guard++;
            for (UnoGame.PenaltyResult penalty : game.applyMissedUnoPenalties()) {
                String penalized = game.getPlayerNames().get(penalty.playerIndex());
                GameLog.cardDrawn(penalized, "missed-uno-penalty");
                io.showMissedUnoPenalty(penalized, penalty.cardsDrawn());
            }

            int playerIndex = game.getCurrentPlayer();
            String name = game.getCurrentPlayerName();
            var hand = game.getHand(playerIndex);

            GameLog.playerTurn(name);
            io.showUpCard(game.getUpCard(), game.getCalledColor());
            io.showHand(name, hand);

            int chosen = -1;
            boolean wantsDraw = false;
            if (humanPlayers.get(playerIndex)) {
                ConsoleIO.HumanChoice choice = io.askHuman(name, hand, game.getUpCard(), game.getCalledColor());
                if (choice.type() == ConsoleIO.ChoiceType.UNO) {
                    if (game.callUno(playerIndex)) {
                        io.showUno(name);
                    } else {
                        io.showUnoNotAllowed(name);
                    }
                    continue;
                }
                if (choice.type() == ConsoleIO.ChoiceType.DRAW) {
                    wantsDraw = true;
                } else {
                    chosen = choice.cardIndex();
                }
            } else {
                if (game.canCallUno(playerIndex)) {
                    game.callUno(playerIndex);
                    io.showUno(name);
                }
                chosen = BotStrategy.chooseCard(hand, game.getUpCard(), game.getCalledColor());
            }

            if (wantsDraw || chosen == -1) {
                UnoGame.DrawResult drawResult = game.drawCard(playerIndex);
                GameLog.cardDrawn(name, drawResult.card());
                io.showDraw(name, drawResult.card());

                if (drawResult.legalToPlay()) {
                    if (!humanPlayers.get(playerIndex)) {
                        chosen = game.getHand(playerIndex).size() - 1;
                    } else if (io.confirmPlayDrawnCard(drawResult.card())) {
                        chosen = game.getHand(playerIndex).size() - 1;
                    }
                }

                if (chosen < 0) {
                    game.passTurn();
                    continue;
                }
            }

            if (chosen >= game.getHand(playerIndex).size()) {
                GameLog.invalidInput(name, "invalid card index");
                io.showInvalidIndex(name);
                game.applyIllegalPlayPenalty(playerIndex);
                continue;
            }

            String card = game.getHand(playerIndex).get(chosen);
            if (!CardRules.isLegal(card, game.getUpCard(), game.getCalledColor())) {
                GameLog.invalidInput(name, "illegal card " + card);
                io.showIllegalCard(name, card);
                game.applyIllegalPlayPenalty(playerIndex);
                continue;
            }

            String chosenColor = null;
            if (card.equals("W") || card.equals("W4")) {
                if (humanPlayers.get(playerIndex)) {
                    chosenColor = io.askColor(name);
                } else {
                    chosenColor = BotStrategy.chooseColor(game.getHand(playerIndex));
                }
                io.showCalledColor(name, chosenColor);
            }

            UnoGame.PlayResult result = game.playCard(playerIndex, chosen, chosenColor);
            GameLog.cardPlayed(name, card);
            io.showPlay(name, card);

            if (result.roundResult() != null) {
                UnoGame.RoundResult round = result.roundResult();
                GameLog.roundEnd(round.winnerName(), round.points());
                io.showWin(round.winnerName(), round.points());
                return round;
            }

            if (humanPlayers.get(playerIndex) && game.needsUnoCall(playerIndex)) {
                io.showForgotUnoWarning(name);
            }

            if (result.playedCard() != null) {
                game.applyPlayedCardEffect(result.playedCard());
            }
        }

        GameLog.gameEnd();
        io.showGameStopped();
        return null;
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
        if (DeckFactory.createStandardDeck().size() == DeckFactory.STANDARD_DECK_SIZE) passed++; else fail("deck size");

        TurnEffect skip = TurnEffects.fromCard("RS", 4);
        if (skip.getAdvanceCount() == 2 && skip.getDrawCount() == 0 && !skip.isReverse()) passed++; else fail("skip effect");

        TurnEffect reverseTwo = TurnEffects.fromCard("RR", 2);
        if (reverseTwo.getAdvanceCount() == 2 && reverseTwo.isReverse()) passed++; else fail("reverse effect two players");

        TurnEffect reverseFour = TurnEffects.fromCard("RR", 4);
        if (reverseFour.getAdvanceCount() == 1 && reverseFour.isReverse()) passed++; else fail("reverse effect four players");

        TurnEffect drawTwo = TurnEffects.fromCard("G+2", 4);
        if (drawTwo.getAdvanceCount() == 2 && drawTwo.getDrawCount() == 2) passed++; else fail("draw two effect");

        TurnEffect drawFour = TurnEffects.fromCard("W4", 4);
        if (drawFour.getAdvanceCount() == 2 && drawFour.getDrawCount() == 4) passed++; else fail("draw four effect");

        ArrayList<String> h = new ArrayList<>();
        h.add("B3");
        h.add("R4");
        h.add("W");
        if (BotStrategy.chooseCard(h, "R9", "") == 1) passed++; else fail("bot normal before wild");

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

        UnoGame game = UnoGame.createSession(java.util.List.of("A", "B", "C"), new Random(1));
        game.setHandForTesting(0, java.util.List.of("R1"));
        game.setHandForTesting(1, java.util.List.of("B2"));
        game.setHandForTesting(2, java.util.List.of("G2"));
        game.setUpCardForTesting("R9");
        game.setCurrentPlayerForTesting(0);
        UnoGame.PlayResult win = game.playCard(0, 0, null);
        if (win.roundResult() != null && win.roundResult().points() == 4) passed++; else fail("round win scoring");

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
