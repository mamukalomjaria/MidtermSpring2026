import java.util.List;

public interface GameHistoryRepository extends AutoCloseable {
    void save(GameResult result);

    List<GameSummary> recentGames(int limit);

    List<PlayerWinCount> playerWinCounts();

    List<HighScore> highestScores(int limit);

    void close();
}
