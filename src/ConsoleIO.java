import java.util.List;
import java.util.Scanner;

class ConsoleIO {
    private final Scanner scanner;
    private final boolean quiet;

    ConsoleIO(Scanner scanner, boolean quiet) {
        this.scanner = scanner;
        this.quiet = quiet;
    }

    void showUpCard(String upCard, String calledColor) {
        if (quiet) {
            return;
        }
        System.out.println("\nUp card: " + upCard + (calledColor.equals("") ? "" : " called " + calledColor));
    }

    void showHand(String name, List<String> hand) {
        if (quiet) {
            return;
        }
        System.out.println(name + " hand: " + formatHand(hand));
    }

    void showDraw(String name, String card) {
        if (quiet) {
            return;
        }
        System.out.println(name + " draws " + card);
    }

    void showInvalidIndex(String name) {
        if (quiet) {
            return;
        }
        System.out.println(name + " selected an invalid index and draws a penalty card.");
    }

    void showIllegalCard(String name, String card) {
        if (quiet) {
            return;
        }
        System.out.println(name + " tried illegal card " + card + " and draws a penalty card.");
    }

    void showPlay(String name, String card) {
        if (quiet) {
            return;
        }
        System.out.println(name + " plays " + card);
    }

    void showCalledColor(String name, String calledColor) {
        if (quiet) {
            return;
        }
        System.out.println(name + " calls " + calledColor);
    }

    void showUno(String name) {
        if (quiet) {
            return;
        }
        System.out.println(name + " says UNO!");
    }

    void showWin(String name, int points) {
        if (quiet) {
            return;
        }
        System.out.println(name + " wins and scores " + points);
    }

    void showDrawPenalty(String name, int count) {
        if (quiet) {
            return;
        }
        switch (count) {
            case 2 -> System.out.println(name + " draws two.");
            case 4 -> System.out.println(name + " draws four.");
            default -> System.out.println(name + " draws " + count + ".");
        }
    }

    void showGameStopped() {
        if (quiet) {
            return;
        }
        System.out.println("Game stopped at safety limit.");
    }

    int askHuman(List<String> hand, String upCard, String calledColor) {
        while (true) {
            System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("DRAW")) {
                return -1;
            }
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) {
                    return index;
                }
            } catch (NumberFormatException ignored) {
            }
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    if (CardRules.isLegal(hand.get(i), upCard, calledColor)) {
                        return i;
                    }
                    System.out.println("That card is not legal.");
                }
            }
            System.out.println("Card not found.");
        }
    }

    boolean confirmPlayDrawnCard(String drawn) {
        System.out.print("Play drawn card " + drawn + "? y/n: ");
        String answer = scanner.nextLine();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R")) {
                return "R";
            }
            if (input.equals("Y")) {
                return "Y";
            }
            if (input.equals("G")) {
                return "G";
            }
            if (input.equals("B")) {
                return "B";
            }
            System.out.println("Bad color.");
        }
    }

    private String formatHand(List<String> cards) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            out.append(i).append(":").append(cards.get(i));
            if (i < cards.size() - 1) {
                out.append(" ");
            }
        }
        return out.toString();
    }
}
