import java.util.ArrayList;

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

    public void showHand(String playerName, ArrayList<String> hand) {
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

    public void showPenalty(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " selected an invalid index and draws a penalty card.");
    }

    public void showIllegalCard(String playerName, String card) {
        if (quiet) return;
        System.out.println(playerName + " tried illegal card " + card + " and draws a penalty card.");
    }

    public void showDrawsTwo(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " draws two.");
    }

    public void showDrawsFour(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " draws four.");
    }

    public void showSafetyLimit() {
        if (quiet) return;
        System.out.println("Game stopped at safety limit.");
    }

    public void showGameHeader(int gameNumber) {
        if (quiet) return;
        System.out.println("\n=== Game " + gameNumber + " ===");
    }

    public void showFinalScores(ArrayList<String> playerNames, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < playerNames.size(); i++) {
            System.out.println(playerNames.get(i) + ": " + scores[i]);
        }
    }

    private static String joinHand(ArrayList<String> cards) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            out.append(i).append(":").append(cards.get(i));
            if (i < cards.size() - 1) out.append(" ");
        }
        return out.toString();
    }
}