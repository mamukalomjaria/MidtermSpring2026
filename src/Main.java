import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    static ArrayList<String> playerNames = new ArrayList<String>();
    static ArrayList<Boolean> humanPlayers = new ArrayList<Boolean>();
    static ArrayList<ArrayList<String>> hands = new ArrayList<ArrayList<String>>();
    static ArrayList<String> deck = new ArrayList<String>();
    static ArrayList<String> discard = new ArrayList<String>();
    static int[] scores = new int[10];
    static int currentPlayer = 0;
    static int direction = 1;
    static String upCard = "";
    static String calledColor = "";
    static boolean quiet = false;
    static Random random = new Random();
    static Scanner scanner = new Scanner(System.in);
    static Display display;

    public static void main(String[] args) {
        int bots = 3;
        int games = 1;
        boolean human = false;
        long seed = System.currentTimeMillis();
        String reportMode = "";
        int reportLimit = 10;

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
            } else if (args[i].equals("--history")) {
                reportMode = "history";
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    reportLimit = Integer.parseInt(args[++i]);
                }
            } else if (args[i].equals("--win-counts")) {
                reportMode = "win-counts";
            } else if (args[i].equals("--high-scores")) {
                reportMode = "high-scores";
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    reportLimit = Integer.parseInt(args[++i]);
                }
            } else if (args[i].equals("--self-test")) {
                selfTest();
                return;
            } else if (args[i].equals("--help")) {
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                System.out.println("Reports: --history [N] | --win-counts | --high-scores [N]");
                return;
            }
        }

        if (!reportMode.isEmpty()) {
            showReport(reportMode, reportLimit);
            return;
        }

        display = new Display(quiet);
        logger.info("Game start: bots=" + bots + " human=" + human + " games=" + games);

        random = new Random(seed);
        setupPlayers(bots, human);

        if (playerNames.size() < 2 || playerNames.size() > 4) {
            logger.warning("Invalid player count: " + playerNames.size());
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        LocalDateTime startedAt = LocalDateTime.now();
        List<RoundResult> roundResults = new ArrayList<RoundResult>();
        for (int g = 1; g <= games; g++) {
            display.showGameHeader(g);
            roundResults.add(playGame(g));
            logger.info("Round end: game=" + g);
        }

        display.showFinalScores(playerNames, scores);
        saveGameHistory(startedAt, LocalDateTime.now(), roundResults);
        logger.info("Game end: games=" + games);
    }

    static void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
            hands.add(new ArrayList<String>());
        }
        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
            hands.add(new ArrayList<String>());
        }
    }

    static RoundResult playGame(int roundNumber) {
        deck.clear();
        String[] colors = {"R", "Y", "G", "B"};
        for (int c = 0; c < colors.length; c++) {
            deck.add(colors[c] + "0");
            for (int n = 1; n <= 9; n++) {
                deck.add(colors[c] + n);
                deck.add(colors[c] + n);
            }
            deck.add(colors[c] + "S");
            deck.add(colors[c] + "S");
            deck.add(colors[c] + "R");
            deck.add(colors[c] + "R");
            deck.add(colors[c] + "+2");
            deck.add(colors[c] + "+2");
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
            logger.info("Player turn: player=" + name + " handSize=" + hand.size());

            display.showUpCard(upCard, calledColor);
            display.showHand(name, hand);

            int chosen = -1;
            if (humanPlayers.get(currentPlayer).booleanValue()) {
                chosen = askHuman(hand);
            } else {
                chosen = chooseBotCard(hand);
            }

            if (chosen == -1) {
                String drawn = draw();
                hand.add(drawn);
                logger.info("Card drawn: player=" + name);
                display.showDraw(name, drawn);
                if (isLegal(drawn, upCard, calledColor)) {
                    if (!humanPlayers.get(currentPlayer).booleanValue()) {
                        chosen = hand.size() - 1;
                    } else {
                        System.out.print("Play drawn card " + drawn + "? y/n: ");
                        String answer = scanner.nextLine();
                        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                            chosen = hand.size() - 1;
                        }
                    }
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    display.showPenalty(name);
                    hand.add(draw());
                    next();
                    continue;
                }

                String card = hand.get(chosen);

                if (!Rules.isLegal(card, upCard, calledColor)) {
                    logger.warning("Invalid input: player=" + name + " card=" + card);
                    display.showIllegalCard(name, card);
                    hand.add(draw());
                    logger.info("Card drawn: player=" + name + " reason=illegal-card-penalty");
                    next();
                    continue;
                }

                hand.remove(chosen);
                discard.add(upCard);
                upCard = card;
                calledColor = "";
                logger.info("Card played: player=" + name + " card=" + card);
                display.showPlay(name, card);

                if (card.equals("W") || card.equals("W4")) {
                    if (humanPlayers.get(currentPlayer).booleanValue()) {
                        calledColor = askColor();
                    } else {
                        calledColor = chooseBotColor(hand);
                    }
                    display.showCallsColor(name, calledColor);
                }

                if (hand.size() == 1) {
                    display.showUno(name);
                }

                if (hand.size() == 0) {
                    int points = 0;
                    for (int i = 0; i < hands.size(); i++) {
                        if (i != currentPlayer) {
                            for (int j = 0; j < hands.get(i).size(); j++) {
                                points += points(hands.get(i).get(j));
                            }
                        }
                    }
                    scores[currentPlayer] += points;
                    logger.info("Round end: winner=" + name + " points=" + points);
                    display.showWin(name, points);
                    return new RoundResult(roundNumber, name, points);
                }

                if (rank(card).equals("SKIP")) {
                    next();
                    next();
                } else if (rank(card).equals("REVERSE")) {
                    direction = direction * -1;
                    if (playerNames.size() == 2) {
                        next();
                        next();
                    } else {
                        next();
                    }
                } else if (rank(card).equals("DRAW_TWO")) {
                    next();
                    hands.get(currentPlayer).add(draw());
                    hands.get(currentPlayer).add(draw());
                    logger.info("Card drawn: player=" + playerNames.get(currentPlayer) + " count=2 reason=draw-two");
                    display.showDrawsTwo(playerNames.get(currentPlayer));
                    next();
                } else if (rank(card).equals("WILD_DRAW_FOUR")) {
                    next();
                    for (int i = 0; i < 4; i++) {
                        hands.get(currentPlayer).add(draw());
                    }
                    logger.info("Card drawn: player=" + playerNames.get(currentPlayer) + " count=4 reason=wild-draw-four");
                    display.showDrawsFour(playerNames.get(currentPlayer));
                    next();
                } else {
                    next();
                }
            } else {
                next();
            }
        }
        logger.warning("Round end: safety limit reached");
        display.showSafetyLimit();
        return new RoundResult(roundNumber, bestScorePlayer(), 0);
    }

    static String draw() {
        if (deck.size() == 0) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.size() == 0) {
            return "W";
        }
        return deck.remove(0);
    }

    static int chooseBotCard(ArrayList<String> hand) {
        for (int i = 0; i < hand.size(); i++) {
            if (Card.rank(hand.get(i)).equals("DRAW_TWO") && Rules.isLegal(hand.get(i), upCard, calledColor))
                return i;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (Card.rank(hand.get(i)).equals("SKIP") && Rules.isLegal(hand.get(i), upCard, calledColor))
                return i;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (Card.rank(hand.get(i)).equals("NUMBER") && Rules.isLegal(hand.get(i), upCard, calledColor))
                return i;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (Card.isWild(hand.get(i)))
                return i;
        }
        return -1;
    }

    static int askHuman(ArrayList<String> hand) {
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
                logger.warning("Invalid input: index out of range");
            } catch (Exception ignored) {
            }
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    if (isLegal(hand.get(i), upCard, calledColor)) {
                        return i;
                    }
                    logger.warning("Invalid input: illegal card code");
                    System.out.println("That card is not legal.");
                }
            }
            logger.warning("Invalid input: card not found");
            System.out.println("Card not found.");
        }
    }

    static String askColor() {
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
            logger.warning("Invalid input: bad color");
            System.out.println("Bad color.");
        }
    }

    static String chooseBotColor(ArrayList<String> hand) {
        int r = 0;
        int y = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < hand.size(); i++) {
            String c = color(hand.get(i));
            if (c.equals("R")) {
                r++;
            } else if (c.equals("Y")) {
                y++;
            } else if (c.equals("G")) {
                g++;
            } else if (c.equals("B")) {
                b++;
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

    static boolean isLegal(String card, String up, String call) {
        return Rules.isLegal(card, up, call);
    }

    static String color(String card)  { return Card.color(card); }

    static String rank(String card)   { return Card.rank(card); }

    static int    number(String card) { return Card.number(card); }

    static int    points(String card) { return Card.points(card); }

    static void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerNames.size() - 1;
        }
    }

    static void saveGameHistory(LocalDateTime startedAt, LocalDateTime completedAt, List<RoundResult> roundResults) {
        GameResult result = new GameResult(
                startedAt,
                completedAt,
                playerNames,
                roundResults,
                finalScoreMap(),
                bestScorePlayer()
        );

        try (GameHistoryRepository repository = new JpaGameHistoryRepository()) {
            repository.save(result);
        }
    }

    static Map<String, Integer> finalScoreMap() {
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < playerNames.size(); i++) {
            result.put(playerNames.get(i), Integer.valueOf(scores[i]));
        }
        return result;
    }

    static String bestScorePlayer() {
        String bestPlayer = playerNames.isEmpty() ? "" : playerNames.get(0);
        int bestScore = playerNames.isEmpty() ? 0 : scores[0];
        for (int i = 1; i < playerNames.size(); i++) {
            if (scores[i] > bestScore) {
                bestScore = scores[i];
                bestPlayer = playerNames.get(i);
            }
        }
        return bestPlayer;
    }

    static void showReport(String reportMode, int limit) {
        try (GameHistoryRepository repository = new JpaGameHistoryRepository()) {
            if (reportMode.equals("history")) {
                System.out.println("Recent games:");
                for (GameSummary game : repository.recentGames(limit)) {
                    System.out.println("Game " + game.getGameId()
                            + " completed=" + game.getCompletedAt()
                            + " rounds=" + game.getRoundsPlayed()
                            + " winner=" + game.getWinnerName());
                }
            } else if (reportMode.equals("win-counts")) {
                System.out.println("Player win counts:");
                for (PlayerWinCount count : repository.playerWinCounts()) {
                    System.out.println(count.getPlayerName() + ": " + count.getWinCount());
                }
            } else if (reportMode.equals("high-scores")) {
                System.out.println("Highest scores:");
                for (HighScore score : repository.highestScores(limit)) {
                    System.out.println(score.getPlayerName()
                            + ": " + score.getScore()
                            + " completed=" + score.getCompletedAt());
                }
            }
        }
    }

    static void selfTest() {
        int passed = 0;
        if (color("R5").equals("R")) passed++; else fail("color R5");
        if (rank("G+2").equals("DRAW_TWO")) passed++; else fail("rank +2");
        if (points("W4") == 50) passed++; else fail("wild points");
        if (isLegal("R2", "R9", "")) passed++; else fail("same color");
        if (isLegal("G9", "R9", "")) passed++; else fail("same number");
        if (isLegal("B3", "W", "B")) passed++; else fail("called color");
        if (!isLegal("B3", "R9", "")) passed++; else fail("illegal mismatch");

        ArrayList<String> h = new ArrayList<String>();
        h.add("B3");
        h.add("R4");
        h.add("W");
        upCard = "R9";
        calledColor = "";
        if (chooseBotCard(h) == 1) passed++; else fail("bot normal before wild");

        ArrayList<String> h2 = new ArrayList<String>();
        h2.add("B1");
        h2.add("B2");
        h2.add("R3");
        if (chooseBotColor(h2).equals("B")) passed++; else fail("bot color");

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
