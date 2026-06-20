import java.time.LocalDateTime;

public class GameSummary {
    private final Long gameId;
    private final LocalDateTime completedAt;
    private final int roundsPlayed;
    private final String winnerName;

    public GameSummary(Long gameId, LocalDateTime completedAt, int roundsPlayed, String winnerName) {
        this.gameId = gameId;
        this.completedAt = completedAt;
        this.roundsPlayed = roundsPlayed;
        this.winnerName = winnerName;
    }

    public Long getGameId() {
        return gameId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public String getWinnerName() {
        return winnerName;
    }
}
