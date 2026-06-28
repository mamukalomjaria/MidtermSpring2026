public class Rules {

    /**
     * Returns true if card can legally be played on top of upCard
     * given the currently called color (empty string if none).
     */
    public static boolean isLegal(String card, String upCard, String calledColor) {
        if (Card.isWild(card)) return true;

        String cardColor = Card.color(card);
        String cardRank  = Card.rank(card);
        String upRank    = Card.rank(upCard);

        if (!calledColor.isEmpty() && cardColor.equals(calledColor)) return true;
        if (cardColor.equals(Card.color(upCard))) return true;
        if (cardRank.equals(upRank) && !cardRank.equals("NUMBER")) return true;
        if (cardRank.equals("NUMBER") && upRank.equals("NUMBER")
                && Card.number(card) == Card.number(upCard)) return true;

        return false;
    }
}