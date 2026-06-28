import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * GameLogic owns UNO state transitions without console input.
 * Main can use this class, and tests can exercise rules directly.
 */
public class GameLogic {
    public static final int DEFAULT_TARGET_SCORE = 500;

    private ArrayList<String> deck = new ArrayList<String>();
    private ArrayList<String> discard = new ArrayList<String>();
    private ArrayList<ArrayList<String>> hands;
    private ArrayList<String> playerNames;
    private Set<Integer> unoCalledPlayers = new HashSet<Integer>();
    private int currentPlayer = 0;
    private int direction = 1;
    private String upCard = "";
    private String calledColor = "";
    private Random random;
    private int[] scores;

    public GameLogic(ArrayList<String> playerNames, long seed) {
        if (playerNames == null || playerNames.size() < 2) {
            throw new IllegalArgumentException("UNO needs at least two players.");
        }
        this.playerNames = new ArrayList<String>(playerNames);
        this.hands = new ArrayList<ArrayList<String>>();
        this.scores = new int[playerNames.size()];
        this.random = new Random(seed);

        for (int i = 0; i < playerNames.size(); i++) {
            this.hands.add(new ArrayList<String>());
        }
    }

    public static ArrayList<String> buildDeck() {
        ArrayList<String> cards = new ArrayList<String>();
        String[] colors = {"R", "Y", "G", "B"};

        for (int c = 0; c < colors.length; c++) {
            cards.add(colors[c] + "0");
            for (int n = 1; n <= 9; n++) {
                cards.add(colors[c] + n);
                cards.add(colors[c] + n);
            }
            cards.add(colors[c] + "S");
            cards.add(colors[c] + "S");
            cards.add(colors[c] + "R");
            cards.add(colors[c] + "R");
            cards.add(colors[c] + "+2");
            cards.add(colors[c] + "+2");
        }

        for (int i = 0; i < 4; i++) {
            cards.add("W");
            cards.add("W4");
        }
        return cards;
    }

    public void initializeRound() {
        deck = buildDeck();
        discard.clear();
        unoCalledPlayers.clear();
        Collections.shuffle(deck, random);

        for (int i = 0; i < hands.size(); i++) {
            hands.get(i).clear();
            for (int j = 0; j < 7; j++) {
                hands.get(i).add(draw());
            }
        }

        upCard = draw();
        while (!Card.rank(upCard).equals("NUMBER")) {
            discard.add(upCard);
            upCard = draw();
        }

        calledColor = "";
        direction = 1;
        currentPlayer = random.nextInt(playerNames.size());
    }

    public String draw() {
        if (deck.size() == 0) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.size() == 0) {
            throw new IllegalStateException("No cards are available to draw.");
        }
        return deck.remove(0);
    }

    public String drawForCurrentPlayer() {
        String drawn = draw();
        hands.get(currentPlayer).add(drawn);
        return drawn;
    }

    public boolean canCurrentPlayerPass() {
        ArrayList<String> hand = hands.get(currentPlayer);
        for (int i = 0; i < hand.size(); i++) {
            if (Rules.isLegal(hand.get(i), upCard, calledColor)) {
                return false;
            }
        }
        return true;
    }

    public String playCard(int index) {
        ArrayList<String> hand = hands.get(currentPlayer);
        if (index < 0 || index >= hand.size()) {
            throw new IllegalArgumentException("Card index out of range.");
        }
        String card = hand.get(index);
        if (!Rules.isLegal(card, upCard, calledColor)) {
            throw new IllegalArgumentException("Illegal card: " + card);
        }
        hand.remove(index);
        discard.add(upCard);
        upCard = card;
        calledColor = "";
        unoCalledPlayers.remove(Integer.valueOf(currentPlayer));
        return card;
    }

    public void applyCardEffect(String chosenColor) {
        String rank = Card.rank(upCard);
        if (rank.equals("WILD") || rank.equals("WILD_DRAW_FOUR")) {
            setCalledColor(chosenColor);
        }

        if (rank.equals("SKIP")) {
            applySkip();
        } else if (rank.equals("REVERSE")) {
            applyReverse();
        } else if (rank.equals("DRAW_TWO")) {
            applyDrawTwo();
        } else if (rank.equals("WILD_DRAW_FOUR")) {
            applyWildDrawFour();
        } else {
            next();
        }
    }

    public void applySkip() {
        next();
        next();
    }

    public void applyReverse() {
        direction = direction * -1;
        if (playerNames.size() == 2) {
            next();
            next();
        } else {
            next();
        }
    }

    public void applyDrawTwo() {
        next();
        drawCardsForCurrentPlayer(2);
        next();
    }

    public void applyWildDrawFour() {
        next();
        drawCardsForCurrentPlayer(4);
        next();
    }

    public void drawCardsForCurrentPlayer(int count) {
        for (int i = 0; i < count; i++) {
            hands.get(currentPlayer).add(draw());
        }
    }

    public void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerNames.size() - 1;
        }
    }

    public boolean hasUno(int playerIndex) {
        return hands.get(playerIndex).size() == 1;
    }

    public void callUno(int playerIndex) {
        if (!hasUno(playerIndex)) {
            throw new IllegalStateException("Player does not have exactly one card.");
        }
        unoCalledPlayers.add(Integer.valueOf(playerIndex));
    }

    public boolean missedUno(int playerIndex) {
        return hasUno(playerIndex) && !unoCalledPlayers.contains(Integer.valueOf(playerIndex));
    }

    public void applyMissedUnoPenalty(int playerIndex) {
        if (!missedUno(playerIndex)) {
            return;
        }
        int originalPlayer = currentPlayer;
        currentPlayer = playerIndex;
        drawCardsForCurrentPlayer(2);
        currentPlayer = originalPlayer;
        unoCalledPlayers.remove(Integer.valueOf(playerIndex));
    }

    public boolean isRoundOver() {
        for (int i = 0; i < hands.size(); i++) {
            if (hands.get(i).size() == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isGameOver(int targetScore) {
        return targetScoreReached(targetScore);
    }

    public int winnerIndex() {
        for (int i = 0; i < hands.size(); i++) {
            if (hands.get(i).size() == 0) {
                return i;
            }
        }
        return -1;
    }

    public int calculateRoundScore() {
        int winner = winnerIndex();
        if (winner < 0) {
            throw new IllegalStateException("Cannot score a round before a player wins.");
        }
        int points = 0;
        for (int i = 0; i < hands.size(); i++) {
            if (i != winner) {
                for (String card : hands.get(i)) {
                    points += Card.points(card);
                }
            }
        }
        return points;
    }

    public void awardRoundScore() {
        int winner = winnerIndex();
        if (winner < 0) {
            throw new IllegalStateException("Cannot award points before a player wins.");
        }
        addScore(winner, calculateRoundScore());
    }

    public boolean targetScoreReached(int targetScore) {
        if (targetScore < 1) {
            throw new IllegalArgumentException("Target score must be positive.");
        }
        return getHighestScore() >= targetScore;
    }

    public String getUpCard() {
        return upCard;
    }

    public String getCalledColor() {
        return calledColor;
    }

    public void setCalledColor(String color) {
        if (color == null || !(color.equals("R") || color.equals("Y") || color.equals("G") || color.equals("B"))) {
            throw new IllegalArgumentException("Color must be R, Y, G, or B.");
        }
        this.calledColor = color;
    }

    public ArrayList<String> getCurrentHand() {
        return hands.get(currentPlayer);
    }

    public ArrayList<String> getHand(int playerIndex) {
        return hands.get(playerIndex);
    }

    public ArrayList<String> getDeck() {
        return deck;
    }

    public ArrayList<String> getDiscard() {
        return discard;
    }

    public String getCurrentPlayerName() {
        return playerNames.get(currentPlayer);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayer;
    }

    public int getDirection() {
        return direction;
    }

    public void addScore(int playerIndex, int points) {
        scores[playerIndex] += points;
    }

    public int getScore(int playerIndex) {
        return scores[playerIndex];
    }

    public int[] getAllScores() {
        return scores.clone();
    }

    public int getLeadingPlayerIndex() {
        int leader = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[leader]) {
                leader = i;
            }
        }
        return leader;
    }

    public String getLeadingPlayerName() {
        return playerNames.get(getLeadingPlayerIndex());
    }

    public int getHighestScore() {
        int max = 0;
        for (int score : scores) {
            if (score > max) {
                max = score;
            }
        }
        return max;
    }

    public List<String> getPlayerNames() {
        return Collections.unmodifiableList(playerNames);
    }

    void setCurrentPlayerForTest(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    void setDirectionForTest(int direction) {
        this.direction = direction;
    }

    void setUpCardForTest(String upCard) {
        this.upCard = upCard;
    }

    void clearHandForTest(int playerIndex) {
        hands.get(playerIndex).clear();
    }
}
