import java.time.LocalDateTime;

public class HighScore {
    private final String playerName;
    private final int score;
    private final LocalDateTime completedAt;

    public HighScore(String playerName, int score, LocalDateTime completedAt) {
        this.playerName = playerName;
        this.score = score;
        this.completedAt = completedAt;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
