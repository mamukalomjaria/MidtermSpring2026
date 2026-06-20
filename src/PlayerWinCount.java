public class PlayerWinCount {
    private final String playerName;
    private final long winCount;

    public PlayerWinCount(String playerName, long winCount) {
        this.playerName = playerName;
        this.winCount = winCount;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getWinCount() {
        return winCount;
    }
}
