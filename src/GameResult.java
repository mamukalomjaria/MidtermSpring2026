import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameResult {
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final List<String> playerNames;
    private final List<RoundResult> rounds;
    private final Map<String, Integer> finalScores;
    private final String winnerName;

    public GameResult(LocalDateTime startedAt,
                      LocalDateTime completedAt,
                      List<String> playerNames,
                      List<RoundResult> rounds,
                      Map<String, Integer> finalScores,
                      String winnerName) {
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.playerNames = new ArrayList<String>(playerNames);
        this.rounds = new ArrayList<RoundResult>(rounds);
        this.finalScores = new LinkedHashMap<String, Integer>(finalScores);
        this.winnerName = winnerName;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public List<String> getPlayerNames() {
        return new ArrayList<String>(playerNames);
    }

    public List<RoundResult> getRounds() {
        return new ArrayList<RoundResult>(rounds);
    }

    public Map<String, Integer> getFinalScores() {
        return new LinkedHashMap<String, Integer>(finalScores);
    }

    public String getWinnerName() {
        return winnerName;
    }
}
