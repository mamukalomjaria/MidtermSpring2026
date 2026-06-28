import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameLogicFinalTest {
    @Test
    void buildsTheClassic108CardDeck() {
        ArrayList<String> deck = GameLogic.buildDeck();
        assertEquals(108, deck.size());
        for (String color : new String[]{"R", "Y", "G", "B"}) {
            assertEquals(1, count(deck, color + "0"));
            for (int number = 1; number <= 9; number++) {
                assertEquals(2, count(deck, color + number));
            }
            assertEquals(2, count(deck, color + "S"));
            assertEquals(2, count(deck, color + "R"));
            assertEquals(2, count(deck, color + "+2"));
        }
        assertEquals(4, count(deck, "W"));
        assertEquals(4, count(deck, "W4"));
    }

    @Test
    void rejectsIllegalPlaysWithoutChangingState() {
        GameLogic game = game(3);
        game.setCurrentPlayerForTest(0);
        game.clearHandForTest(0);
        game.getHand(0).add("B3");
        game.setUpCardForTest("R9");
        assertThrows(IllegalArgumentException.class, () -> game.playCard(0));
        assertEquals(1, game.getHand(0).size());
        assertEquals("R9", game.getUpCard());
    }

    @Test
    void skipAndReverseChangeTurnOrder() {
        GameLogic skip = game(3);
        skip.setCurrentPlayerForTest(0);
        skip.applySkip();
        assertEquals(2, skip.getCurrentPlayerIndex());

        GameLogic reverse = game(3);
        reverse.setCurrentPlayerForTest(0);
        reverse.applyReverse();
        assertEquals(-1, reverse.getDirection());
        assertEquals(2, reverse.getCurrentPlayerIndex());

        GameLogic twoPlayerReverse = game(2);
        twoPlayerReverse.setCurrentPlayerForTest(0);
        twoPlayerReverse.applyReverse();
        assertEquals(0, twoPlayerReverse.getCurrentPlayerIndex());
    }

    @Test
    void drawCardsAddCardsAndSkipTheAffectedPlayer() {
        GameLogic drawTwo = game(3);
        drawTwo.setCurrentPlayerForTest(0);
        int beforeTwo = drawTwo.getHand(1).size();
        drawTwo.applyDrawTwo();
        assertEquals(beforeTwo + 2, drawTwo.getHand(1).size());
        assertEquals(2, drawTwo.getCurrentPlayerIndex());

        GameLogic drawFour = game(3);
        drawFour.setCurrentPlayerForTest(0);
        drawFour.setUpCardForTest("W4");
        int beforeFour = drawFour.getHand(1).size();
        drawFour.applyCardEffect("G");
        assertEquals(beforeFour + 4, drawFour.getHand(1).size());
        assertEquals(2, drawFour.getCurrentPlayerIndex());
        assertEquals("G", drawFour.getCalledColor());
    }

    @Test
    void wildChosenColorControlsTheNextLegalPlay() {
        GameLogic game = game(3);
        game.setUpCardForTest("W");
        game.applyCardEffect("B");
        assertTrue(Rules.isLegal("B3", game.getUpCard(), game.getCalledColor()));
        assertFalse(Rules.isLegal("G3", game.getUpCard(), game.getCalledColor()));
        assertThrows(IllegalArgumentException.class, () -> game.setCalledColor("P"));
    }

    @Test
    void aDrawnLegalCardCanBePlayedImmediately() {
        GameLogic game = game(3);
        game.setCurrentPlayerForTest(0);
        game.clearHandForTest(0);
        game.getHand(0).add("B3");
        game.setUpCardForTest("R9");
        assertTrue(game.canCurrentPlayerPass());
        game.getDeck().clear();
        game.getDeck().add("R1");
        String drawn = game.drawForCurrentPlayer();
        assertTrue(Rules.isLegal(drawn, game.getUpCard(), game.getCalledColor()));
        game.playCard(game.getCurrentHand().size() - 1);
        assertEquals("R1", game.getUpCard());
    }

    @Test
    void missedUnoDrawsTwoWhileACallAvoidsThePenalty() {
        GameLogic game = game(3);
        game.clearHandForTest(0);
        game.getHand(0).add("R5");
        assertTrue(game.hasUno(0));
        assertTrue(game.missedUno(0));
        game.applyMissedUnoPenalty(0);
        assertEquals(3, game.getHand(0).size());

        game.clearHandForTest(1);
        game.getHand(1).add("G7");
        game.callUno(1);
        assertFalse(game.missedUno(1));
        assertThrows(IllegalStateException.class, () -> game.callUno(2));
    }

    @Test
    void scoresRoundAndDetectsTargetWinner() {
        GameLogic game = game(3);
        game.clearHandForTest(0);
        game.clearHandForTest(1);
        game.clearHandForTest(2);
        game.getHand(1).add("R5");
        game.getHand(1).add("GS");
        game.getHand(2).add("W4");
        assertTrue(game.isRoundOver());
        assertEquals(75, game.calculateRoundScore());
        game.awardRoundScore();
        assertEquals(75, game.getScore(0));

        game.initializeRound();
        assertEquals(75, game.getScore(0));
        game.addScore(0, 425);
        assertEquals(500, game.getScore(0));
        assertTrue(game.targetScoreReached(500));
        assertTrue(game.isGameOver(500));
        assertEquals("Player0", game.getLeadingPlayerName());
        assertThrows(IllegalArgumentException.class, () -> game.isGameOver(0));
    }

    @Test
    void refusesToScoreAnUnfinishedRound() {
        GameLogic game = game(3);
        assertThrows(IllegalStateException.class, game::calculateRoundScore);
        assertThrows(IllegalStateException.class, game::awardRoundScore);
    }

    @Test
    void eachRoundStartsOnANumberCard() {
        for (long seed = 1; seed <= 25; seed++) {
            ArrayList<String> players = players(3);
            GameLogic game = new GameLogic(players, seed);
            game.initializeRound();
            assertEquals("NUMBER", Card.rank(game.getUpCard()));
        }
    }

    private static GameLogic game(int playerCount) {
        GameLogic game = new GameLogic(players(playerCount), 3L);
        game.initializeRound();
        return game;
    }

    private static ArrayList<String> players(int count) {
        ArrayList<String> players = new ArrayList<String>();
        for (int i = 0; i < count; i++) players.add("Player" + i);
        return players;
    }

    private static int count(ArrayList<String> deck, String card) {
        int total = 0;
        for (String each : deck) if (each.equals(card)) total++;
        return total;
    }
}