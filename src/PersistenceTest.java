import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PersistenceTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        savesGameHistoryAndRunsReports();

        System.out.println("\n=== PersistenceTest Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void savesGameHistoryAndRunsReports() {
        String databaseName = "uno_history_test_" + System.nanoTime();
        try (GameHistoryRepository repository =
                     new JpaGameHistoryRepository(JpaGameHistoryRepository.inMemoryTestProperties(databaseName))) {
            repository.save(sampleGame(
                    LocalDateTime.of(2026, 6, 20, 10, 0),
                    "Bot1",
                    42,
                    13,
                    0
            ));
            repository.save(sampleGame(
                    LocalDateTime.of(2026, 6, 20, 11, 0),
                    "Bot2",
                    5,
                    77,
                    8
            ));

            List<GameSummary> recentGames = repository.recentGames(2);
            assertEquals("Recent games includes two saved games", 2, recentGames.size());
            assertEquals("Most recent game is first", "Bot2", recentGames.get(0).getWinnerName());
            assertEquals("Rounds played are saved", 2, recentGames.get(0).getRoundsPlayed());

            List<PlayerWinCount> winCounts = repository.playerWinCounts();
            assertEquals("Two players have wins", 2, winCounts.size());
            assertTrue("Bot1 has one win", containsWinCount(winCounts, "Bot1", 1));
            assertTrue("Bot2 has one win", containsWinCount(winCounts, "Bot2", 1));

            List<HighScore> highScores = repository.highestScores(3);
            assertEquals("High scores include three player score rows", 3, highScores.size());
            assertEquals("Highest score belongs to Bot2", "Bot2", highScores.get(0).getPlayerName());
            assertEquals("Highest score value is saved", 77, highScores.get(0).getScore());
        }
    }

    static GameResult sampleGame(LocalDateTime completedAt,
                                 String winnerName,
                                 int bot1Score,
                                 int bot2Score,
                                 int bot3Score) {
        List<String> players = Arrays.asList("Bot1", "Bot2", "Bot3");

        List<RoundResult> rounds = new ArrayList<RoundResult>();
        rounds.add(new RoundResult(1, winnerName, 20));
        rounds.add(new RoundResult(2, winnerName, 30));

        Map<String, Integer> scores = new LinkedHashMap<String, Integer>();
        scores.put("Bot1", Integer.valueOf(bot1Score));
        scores.put("Bot2", Integer.valueOf(bot2Score));
        scores.put("Bot3", Integer.valueOf(bot3Score));

        return new GameResult(completedAt.minusMinutes(15), completedAt, players, rounds, scores, winnerName);
    }

    static boolean containsWinCount(List<PlayerWinCount> winCounts, String playerName, long expected) {
        for (PlayerWinCount winCount : winCounts) {
            if (winCount.getPlayerName().equals(playerName) && winCount.getWinCount() == expected) {
                return true;
            }
        }
        return false;
    }

    static void assertTrue(String label, boolean condition) {
        if (condition) {
            System.out.println("  PASS: " + label);
            passed++;
        } else {
            System.out.println("  FAIL: " + label);
            failed++;
        }
    }

    static void assertEquals(String label, String expected, String actual) {
        assertTrue(label + " (expected=" + expected + " actual=" + actual + ")", expected.equals(actual));
    }

    static void assertEquals(String label, int expected, int actual) {
        assertTrue(label + " (expected=" + expected + " actual=" + actual + ")", expected == actual);
    }
}
