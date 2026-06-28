import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleUnoTest {

    @Test
    void validatesCardUtilities() {
        assertEquals("R", Card.color("R5"));
        assertEquals("G", Card.color("GS"));
        assertEquals("", Card.color("W"));

        assertEquals("NUMBER", Card.rank("R5"));
        assertEquals("SKIP", Card.rank("RS"));
        assertEquals("REVERSE", Card.rank("BR"));
        assertEquals("DRAW_TWO", Card.rank("G+2"));
        assertEquals("WILD", Card.rank("W"));
        assertEquals("WILD_DRAW_FOUR", Card.rank("W4"));

        assertEquals(5, Card.number("R5"));
        assertEquals(0, Card.number("B0"));
        assertEquals(-1, Card.number("RS"));

        assertTrue(Card.isWild("W"));
        assertTrue(Card.isWild("W4"));
        assertFalse(Card.isWild("R5"));
    }

    @Test
    void validatesCardPointsAndRules() {
        assertEquals(5, Card.points("R5"));
        assertEquals(9, Card.points("B9"));
        assertEquals(0, Card.points("G0"));
        assertEquals(20, Card.points("RS"));
        assertEquals(20, Card.points("GR"));
        assertEquals(20, Card.points("Y+2"));
        assertEquals(50, Card.points("W"));
        assertEquals(50, Card.points("W4"));

        assertTrue(Rules.isLegal("R5", "R9", ""));
        assertTrue(Rules.isLegal("G5", "R5", ""));
        assertTrue(Rules.isLegal("RS", "GS", ""));
        assertTrue(Rules.isLegal("W", "R5", ""));
        assertTrue(Rules.isLegal("W4", "B3", ""));
        assertTrue(Rules.isLegal("B3", "W", "B"));
        assertFalse(Rules.isLegal("G3", "W", "B"));
    }

    @Test
    void buildsARealDeckInsteadOfTestingConstants() {
        ArrayList<String> deck = GameLogic.buildDeck();
        assertEquals(108, deck.size());
        assertEquals(4, occurrences(deck, "W"));
        assertEquals(4, occurrences(deck, "W4"));
    }

    private static int occurrences(ArrayList<String> deck, String card) {
        int count = 0;
        for (String each : deck) {
            if (each.equals(card)) {
                count++;
            }
        }
        return count;
    }
}
