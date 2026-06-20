public class RoundResult {
    private final int roundNumber;
    private final String winnerName;
    private final int pointsScored;

    public RoundResult(int roundNumber, String winnerName, int pointsScored) {
        this.roundNumber = roundNumber;
        this.winnerName = winnerName;
        this.pointsScored = pointsScored;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public int getPointsScored() {
        return pointsScored;
    }
}
