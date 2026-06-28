import java.util.List;

public class Display {

    private final boolean quiet;

    public Display(boolean quiet) {
        this.quiet = quiet;
    }

    public void showUpCard(String upCard, String calledColor) {
        if (quiet) return;
        String suffix = calledColor.isEmpty() ? "" : " called " + calledColor;
        System.out.println("\nUp card: " + upCard + suffix);
    }

    public void showHand(String playerName, List<String> hand) {
        if (quiet) return;
        System.out.println(playerName + " hand: " + joinHand(hand));
    }

    public void showDraw(String playerName, String card) {
        if (quiet) return;
        System.out.println(playerName + " draws " + card);
    }

    public void showPlay(String playerName, String card) {
        if (quiet) return;
        System.out.println(playerName + " plays " + card);
    }

    public void showCallsColor(String playerName, String color) {
        if (quiet) return;
        System.out.println(playerName + " calls " + color);
    }

    public void showUno(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " says UNO!");
    }

    public void showWin(String playerName, int points) {
        if (quiet) return;
        System.out.println(playerName + " wins and scores " + points);
    }

    public void showMissedUnoPenalty(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " missed UNO and draws two penalty cards.");
    }

    public void showTargetWinner(String playerName, int score, int targetScore) {
        System.out.println(playerName + " reaches " + score + " points and wins the match (target " + targetScore + ").");
    }


    public void showSafetyLimit() {
        if (quiet) return;
        System.out.println("Game stopped at safety limit.");
    }

    public void showGameHeader(int gameNumber) {
        if (quiet) return;
        System.out.println("\n=== Game " + gameNumber + " ===");
    }

    public void showFinalScores(List<String> playerNames, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < playerNames.size(); i++) {
            System.out.println(playerNames.get(i) + ": " + scores[i]);
        }
    }

    private static String joinHand(List<String> cards) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            out.append(i).append(":").append(cards.get(i));
            if (i < cards.size() - 1) out.append(" ");
        }
        return out.toString();
    }
}
