import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final int TURN_LIMIT = 3000;

    private static final ArrayList<String> playerNames = new ArrayList<String>();
    private static final ArrayList<Boolean> humanPlayers = new ArrayList<Boolean>();
    private static Scanner scanner = new Scanner(System.in);
    private static Display display;

    public static void main(String[] args) {
        Options options;
        try {
            options = Options.parse(args);
        } catch (IllegalArgumentException exception) {
            System.out.println("Error: " + exception.getMessage());
            printUsage();
            return;
        }

        if (options.help) {
            printUsage();
            return;
        }
        if (!options.reportMode.isEmpty()) {
            showReport(options.reportMode, options.reportLimit);
            return;
        }

        setupPlayers(options.bots, options.human);
        if (playerNames.size() < 2 || playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        display = new Display(options.quiet);
        GameLogic game = new GameLogic(playerNames, options.seed);
        LocalDateTime startedAt = LocalDateTime.now();
        List<RoundResult> rounds = new ArrayList<RoundResult>();

        for (int round = 1; round <= options.maxRounds && !game.isGameOver(options.targetScore); round++) {
            display.showGameHeader(round);
            rounds.add(playRound(game, round));
        }

        if (game.isGameOver(options.targetScore)) {
            display.showTargetWinner(game.getLeadingPlayerName(), game.getHighestScore(), options.targetScore);
        }
        display.showFinalScores(game.getPlayerNames(), game.getAllScores());
        saveGameHistory(startedAt, LocalDateTime.now(), rounds, game);
    }

    static RoundResult playRound(GameLogic game, int roundNumber) {
        game.initializeRound();

        for (int turn = 0; turn < TURN_LIMIT; turn++) {
            int player = game.getCurrentPlayerIndex();
            String name = game.getCurrentPlayerName();
            ArrayList<String> hand = game.getCurrentHand();

            display.showUpCard(game.getUpCard(), game.getCalledColor());
            display.showHand(name, hand);

            int chosen = humanPlayers.get(player).booleanValue()
                    ? askHuman(hand, game.getUpCard(), game.getCalledColor())
                    : chooseBotCard(hand, game.getUpCard(), game.getCalledColor());

            if (chosen < 0) {
                String drawn = game.drawForCurrentPlayer();
                display.showDraw(name, drawn);
                if (Rules.isLegal(drawn, game.getUpCard(), game.getCalledColor())
                        && shouldPlayDrawnCard(player, drawn)) {
                    chosen = hand.size() - 1;
                }
            }

            if (chosen < 0) {
                game.next();
                continue;
            }

            String played;
            try {
                played = game.playCard(chosen);
            } catch (IllegalArgumentException exception) {
                System.out.println(exception.getMessage());
                continue;
            }
            display.showPlay(name, played);

            String chosenColor = null;
            if (Card.isWild(played)) {
                chosenColor = humanPlayers.get(player).booleanValue() ? askColor() : chooseBotColor(hand);
                display.showCallsColor(name, chosenColor);
            }

            handleUno(game, player);
            game.applyCardEffect(chosenColor);

            if (game.isRoundOver()) {
                int winner = game.winnerIndex();
                int points = game.calculateRoundScore();
                game.awardRoundScore();
                display.showWin(playerNames.get(winner), points);
                return new RoundResult(roundNumber, playerNames.get(winner), points);
            }
        }

        LOGGER.warning("Round stopped at the turn safety limit.");
        display.showSafetyLimit();
        return new RoundResult(roundNumber, game.getLeadingPlayerName(), 0);
    }

    private static boolean shouldPlayDrawnCard(int player, String drawn) {
        if (!humanPlayers.get(player).booleanValue()) return true;
        System.out.print("Play drawn card " + drawn + "? y/n: ");
        String answer = scanner.nextLine().trim();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    private static void handleUno(GameLogic game, int player) {
        if (!game.hasUno(player)) return;

        boolean called = true;
        if (humanPlayers.get(player).booleanValue()) {
            System.out.print("Call UNO? y/n: ");
            String answer = scanner.nextLine().trim();
            called = answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")
                    || answer.equalsIgnoreCase("uno");
        }

        if (called) {
            game.callUno(player);
            display.showUno(playerNames.get(player));
        } else {
            game.applyMissedUnoPenalty(player);
            display.showMissedUnoPenalty(playerNames.get(player));
        }
    }

    static void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
        }
        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
        }
    }

    static int chooseBotCard(ArrayList<String> hand, String upCard, String calledColor) {
        String[] ranks = {"WILD_DRAW_FOUR", "DRAW_TWO", "SKIP", "REVERSE", "NUMBER", "WILD"};
        for (String rank : ranks) {
            for (int i = 0; i < hand.size(); i++) {
                if (Card.rank(hand.get(i)).equals(rank)
                        && Rules.isLegal(hand.get(i), upCard, calledColor)) return i;
            }
        }
        return -1;
    }

    static String chooseBotColor(ArrayList<String> hand) {
        String[] colors = {"R", "Y", "G", "B"};
        int[] counts = new int[colors.length];
        for (String card : hand) {
            for (int i = 0; i < colors.length; i++) {
                if (Card.color(card).equals(colors[i])) counts[i]++;
            }
        }
        int best = 0;
        for (int i = 1; i < counts.length; i++) {
            if (counts[i] > counts[best]) best = i;
        }
        return colors[best];
    }

    static int askHuman(ArrayList<String> hand, String upCard, String calledColor) {
        while (true) {
            System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("DRAW")) return -1;
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) {
                    if (Rules.isLegal(hand.get(index), upCard, calledColor)) return index;
                    System.out.println("That card is not legal.");
                    continue;
                }
            } catch (NumberFormatException ignored) {
                // The same input may be a card code.
            }
            boolean found = false;
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    found = true;
                    if (Rules.isLegal(hand.get(i), upCard, calledColor)) return i;
                    System.out.println("That card is not legal.");
                    break;
                }
            }
            if (!found) System.out.println("Card not found.");
        }
    }

    static String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String color = scanner.nextLine().trim().toUpperCase();
            if (color.equals("R") || color.equals("Y") || color.equals("G") || color.equals("B")) return color;
            System.out.println("Bad color.");
        }
    }

    private static void saveGameHistory(LocalDateTime startedAt, LocalDateTime completedAt,
                                        List<RoundResult> rounds, GameLogic game) {
        GameResult result = new GameResult(startedAt, completedAt, playerNames, rounds,
                finalScoreMap(game), game.getLeadingPlayerName());
        try (GameHistoryRepository repository = new JpaGameHistoryRepository()) {
            repository.save(result);
        }
    }

    private static Map<String, Integer> finalScoreMap(GameLogic game) {
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < playerNames.size(); i++) {
            result.put(playerNames.get(i), Integer.valueOf(game.getScore(i)));
        }
        return result;
    }


    private static void showReport(String mode, int limit) {
        try (GameHistoryRepository repository = new JpaGameHistoryRepository()) {
            if (mode.equals("history")) {
                System.out.println("Recent games:");
                for (GameSummary game : repository.recentGames(limit)) {
                    System.out.println("Game " + game.getGameId() + " completed=" + game.getCompletedAt()
                            + " rounds=" + game.getRoundsPlayed() + " winner=" + game.getWinnerName());
                }
            } else if (mode.equals("win-counts")) {
                System.out.println("Player win counts:");
                for (PlayerWinCount count : repository.playerWinCounts()) {
                    System.out.println(count.getPlayerName() + ": " + count.getWinCount());
                }
            } else {
                System.out.println("Highest scores:");
                for (HighScore score : repository.highestScores(limit)) {
                    System.out.println(score.getPlayerName() + ": " + score.getScore()
                            + " completed=" + score.getCompletedAt());
                }
            }
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar app.jar [--human] [--bots N] [--games N]");
        System.out.println("       [--target N] [--quiet] [--seed N]");
        System.out.println("Reports: --history [N] | --win-counts | --high-scores [N]");
    }

    private static final class Options {
        private int bots = 3;
        private int maxRounds = 20;
        private int targetScore = GameLogic.DEFAULT_TARGET_SCORE;
        private long seed = System.currentTimeMillis();
        private boolean human;
        private boolean quiet;
        private boolean help;
        private String reportMode = "";
        private int reportLimit = 10;

        private static Options parse(String[] args) {
            Options options = new Options();
            for (int i = 0; i < args.length; i++) {
                String argument = args[i];
                if (argument.equals("--human")) options.human = true;
                else if (argument.equals("--quiet")) options.quiet = true;
                else if (argument.equals("--help")) options.help = true;
                else if (argument.equals("--bots")) options.bots = positiveInt(value(args, ++i, argument), argument);
                else if (argument.equals("--games")) options.maxRounds = positiveInt(value(args, ++i, argument), argument);
                else if (argument.equals("--target")) options.targetScore = positiveInt(value(args, ++i, argument), argument);
                else if (argument.equals("--seed")) options.seed = longValue(value(args, ++i, argument), argument);
                else if (argument.equals("--history") || argument.equals("--high-scores")) {
                    options.reportMode = argument.substring(2);
                    if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                        options.reportLimit = positiveInt(args[++i], argument);
                    }
                } else if (argument.equals("--win-counts")) options.reportMode = "win-counts";
                else throw new IllegalArgumentException("Unknown option: " + argument);
            }
            return options;
        }

        private static String value(String[] args, int index, String option) {
            if (index >= args.length) throw new IllegalArgumentException("Missing value for " + option);
            return args[index];
        }

        private static int positiveInt(String value, String option) {
            try {
                int number = Integer.parseInt(value);
                if (number < 1) throw new NumberFormatException();
                return number;
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(option + " requires a positive whole number.");
            }
        }

        private static long longValue(String value, String option) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(option + " requires a whole number.");
            }
        }
    }
}
