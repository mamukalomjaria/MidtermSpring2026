import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceTest {

    @Test
    void savesGameHistoryAndRunsReports() {
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
            assertEquals(2, recentGames.size());
            assertEquals("Bot2", recentGames.get(0).getWinnerName());
            assertEquals(2, recentGames.get(0).getRoundsPlayed());

            List<PlayerWinCount> winCounts = repository.playerWinCounts();
            assertEquals(2, winCounts.size());
            assertTrue(containsWinCount(winCounts, "Bot1", 1));
            assertTrue(containsWinCount(winCounts, "Bot2", 1));

            List<HighScore> highScores = repository.highestScores(3);
            assertEquals(3, highScores.size());
            assertEquals("Bot2", highScores.get(0).getPlayerName());
            assertEquals(77, highScores.get(0).getScore());
        }
    }

    private static GameResult sampleGame(LocalDateTime completedAt,
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

    private static boolean containsWinCount(List<PlayerWinCount> winCounts, String playerName, long expected) {
        for (PlayerWinCount winCount : winCounts) {
            if (winCount.getPlayerName().equals(playerName) && winCount.getWinCount() == expected) {
                return true;
            }
        }
        return false;
    }
}
