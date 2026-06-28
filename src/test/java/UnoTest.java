import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnoTest {
    @Test
    void validatesColorNumberActionWildAndCalledColorRules() {
        assertTrue(Rules.isLegal("R2", "R9", ""));
        assertTrue(Rules.isLegal("G9", "R9", ""));
        assertTrue(Rules.isLegal("RS", "GS", ""));
        assertTrue(Rules.isLegal("BR", "GR", ""));
        assertTrue(Rules.isLegal("R+2", "G+2", ""));
        assertTrue(Rules.isLegal("W", "R5", ""));
        assertTrue(Rules.isLegal("W4", "GR", ""));
        assertTrue(Rules.isLegal("B3", "W", "B"));
        assertFalse(Rules.isLegal("B3", "R9", ""));
        assertFalse(Rules.isLegal("RS", "GR", ""));
        assertFalse(Rules.isLegal("G3", "W", "B"));
    }

    @Test
    void extractsCardPropertiesAndPoints() {
        assertEquals("R", Card.color("R5"));
        assertEquals("", Card.color("W"));
        assertEquals("SKIP", Card.rank("RS"));
        assertEquals("REVERSE", Card.rank("YR"));
        assertEquals("DRAW_TWO", Card.rank("G+2"));
        assertEquals("WILD_DRAW_FOUR", Card.rank("W4"));
        assertEquals(5, Card.number("R5"));
        assertEquals(20, Card.points("Y+2"));
        assertEquals(50, Card.points("W4"));
    }

    @Test
    void botChoosesPlayableCardsAndColors() {
        ArrayList<String> hand = new ArrayList<String>();
        hand.add("R9");
        hand.add("R+2");
        hand.add("W");
        assertEquals(1, Main.chooseBotCard(hand, "R5", ""));
        ArrayList<String> colors = new ArrayList<String>();
        colors.add("B1");
        colors.add("B2");
        colors.add("R3");
        assertEquals("B", Main.chooseBotColor(colors));
    }

    @Test
    void drawRecyclesDiscardWithoutInventingCards() {
        GameLogic game = game();
        game.getDeck().clear();
        game.getDiscard().clear();
        game.getDiscard().add("G3");
        game.getDiscard().add("B7");
        String first = game.draw();
        String second = game.draw();
        assertTrue(first.equals("G3") || first.equals("B7"));
        assertTrue(second.equals("G3") || second.equals("B7"));
        assertFalse(first.equals(second));
    }

    private static GameLogic game() {
        ArrayList<String> players = new ArrayList<String>();
        players.add("A");
        players.add("B");
        GameLogic game = new GameLogic(players, 1L);
        game.initializeRound();
        return game;
    }
}