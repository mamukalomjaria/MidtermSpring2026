import java.util.ArrayList;

public class UnoTest {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        testColorMatching();
        testNumberMatching();
        testActionTypeMatching();
        testWildAlwaysLegal();
        testWildDrawFourAlwaysLegal();
        testCalledColorOverride();
        testIllegalMismatch();
        testSkipRank();
        testReverseRank();
        testDrawTwoRank();
        testPointsNumberCard();
        testPointsActionCard();
        testPointsWildCard();
        testColorExtraction();
        testNumberExtraction();
        testBotChoosesDrawTwoFirst();
        testBotChoosesSkipBeforeNumber();
        testBotChoosesWildLast();
        testBotColorChoosesmost();
        testEdgeCaseWildOnWild();

        System.out.println("\n=== UnoTest Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // --- Legality Tests ---

    static void testColorMatching() {
        // Same color should always be legal
        assertTrue("R2 is legal on R9 (same color)", Main.isLegal("R2", "R9", ""));
        assertTrue("YS is legal on Y3 (same color)", Main.isLegal("YS", "Y3", ""));
        assertFalse("B3 is NOT legal on R9 (different color, no call)", Main.isLegal("B3", "R9", ""));
    }

    static void testNumberMatching() {
        // Same number, different color should be legal
        assertTrue("G5 is legal on R5 (same number)", Main.isLegal("G5", "R5", ""));
        assertTrue("B0 is legal on R0 (same number 0)", Main.isLegal("B0", "R0", ""));
        assertFalse("G3 is NOT legal on R5 (different number)", Main.isLegal("G3", "R5", ""));
    }

    static void testActionTypeMatching() {
        // Skip on skip, reverse on reverse, draw two on draw two
        assertTrue("RS is legal on GS (skip on skip)", Main.isLegal("RS", "GS", ""));
        assertTrue("BR is legal on GR (reverse on reverse)", Main.isLegal("BR", "GR", ""));
        assertTrue("R+2 is legal on G+2 (draw two on draw two)", Main.isLegal("R+2", "G+2", ""));
        assertFalse("RS is NOT legal on GR (skip on reverse)", Main.isLegal("RS", "GR", ""));
    }

    static void testWildAlwaysLegal() {
        assertTrue("W is legal on R5", Main.isLegal("W", "R5", ""));
        assertTrue("W is legal on GS", Main.isLegal("W", "GS", ""));
        assertTrue("W is legal on W4", Main.isLegal("W", "W4", ""));
    }

    static void testWildDrawFourAlwaysLegal() {
        assertTrue("W4 is legal on B3", Main.isLegal("W4", "B3", ""));
        assertTrue("W4 is legal on GR", Main.isLegal("W4", "GR", ""));
    }

    static void testCalledColorOverride() {
        // After a wild, the called color overrides the up card's color
        assertTrue("B3 is legal on W when B is called", Main.isLegal("B3", "W", "B"));
        assertFalse("G3 is NOT legal on W when B is called", Main.isLegal("G3", "W", "B"));
        assertTrue("R5 is legal on W4 when R is called", Main.isLegal("R5", "W4", "R"));
    }

    static void testIllegalMismatch() {
        assertFalse("B3 is not legal on R9 (no match)", Main.isLegal("B3", "R9", ""));
        assertFalse("YR is not legal on R5 (action vs number, color mismatch)", Main.isLegal("YR", "R5", ""));
    }

    // --- Rank Tests ---

    static void testSkipRank() {
        assertEquals("RS rank is SKIP", "SKIP", Main.rank("RS"));
        assertEquals("BS rank is SKIP", "SKIP", Main.rank("BS"));
    }

    static void testReverseRank() {
        assertEquals("RR rank is REVERSE", "REVERSE", Main.rank("RR"));
        assertEquals("YR rank is REVERSE", "REVERSE", Main.rank("YR"));
    }

    static void testDrawTwoRank() {
        assertEquals("R+2 rank is DRAW_TWO", "DRAW_TWO", Main.rank("R+2"));
        assertEquals("G+2 rank is DRAW_TWO", "DRAW_TWO", Main.rank("G+2"));
    }

    // --- Points Tests ---

    static void testPointsNumberCard() {
        assertEquals("R5 is worth 5 points", 5, Main.points("R5"));
        assertEquals("B9 is worth 9 points", 9, Main.points("B9"));
        assertEquals("G0 is worth 0 points", 0, Main.points("G0"));
    }

    static void testPointsActionCard() {
        assertEquals("RS (skip) is worth 20 points", 20, Main.points("RS"));
        assertEquals("GR (reverse) is worth 20 points", 20, Main.points("GR"));
        assertEquals("Y+2 (draw two) is worth 20 points", 20, Main.points("Y+2"));
    }

    static void testPointsWildCard() {
        assertEquals("W is worth 50 points", 50, Main.points("W"));
        assertEquals("W4 is worth 50 points", 50, Main.points("W4"));
    }

    // --- Color / Number extraction ---

    static void testColorExtraction() {
        assertEquals("color of R5 is R", "R", Main.color("R5"));
        assertEquals("color of GS is G", "G", Main.color("GS"));
        assertEquals("color of W is empty", "", Main.color("W"));
    }

    static void testNumberExtraction() {
        assertEquals("number of R5 is 5", 5, Main.number("R5"));
        assertEquals("number of B0 is 0", 0, Main.number("B0"));
    }

    // --- Bot behavior ---

    static void testBotChoosesDrawTwoFirst() {
        // Bot prefers draw two over other legal cards
        Main.upCard = "R5";
        Main.calledColor = "";
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R9");       // legal number match
        hand.add("R+2");      // legal draw two - should be chosen first
        hand.add("W");
        int chosen = Main.chooseBotCard(hand);
        assertEquals("Bot picks draw two (index 1) before other cards", 1, chosen);
    }

    static void testBotChoosesSkipBeforeNumber() {
        Main.upCard = "R5";
        Main.calledColor = "";
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R9");       // legal number
        hand.add("RS");       // legal skip - should be chosen before number
        int chosen = Main.chooseBotCard(hand);
        assertEquals("Bot picks skip (index 1) before number card", 1, chosen);
    }

    static void testBotChoosesWildLast() {
        // Wild is only played when no other legal card exists
        Main.upCard = "R5";
        Main.calledColor = "";
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3");       // not legal
        hand.add("W");        // wild - only option
        int chosen = Main.chooseBotCard(hand);
        assertEquals("Bot plays wild when no other option", 1, chosen);
    }

    static void testBotColorChoosesmost() {
        // Bot picks color it has the most of
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B1");
        hand.add("B2");
        hand.add("R3");
        String color = Main.chooseBotColor(hand);
        assertEquals("Bot calls B (has 2 blue, 1 red)", "B", color);
    }

    // --- Edge case ---

    static void testEdgeCaseWildOnWild() {
        // Wild is always legal even on top of another wild
        assertTrue("W is legal on W", Main.isLegal("W", "W", ""));
        assertTrue("W4 is legal on W4", Main.isLegal("W4", "W4", ""));
    }

    // --- Assertion helpers ---

    static void assertTrue(String label, boolean condition) {
        if (condition) {
            System.out.println("  PASS: " + label);
            passed++;
        } else {
            System.out.println("  FAIL: " + label);
            failed++;
        }
    }

    static void assertFalse(String label, boolean condition) {
        assertTrue(label, !condition);
    }

    static void assertEquals(String label, String expected, String actual) {
        if (expected.equals(actual)) {
            System.out.println("  PASS: " + label);
            passed++;
        } else {
            System.out.println("  FAIL: " + label + " (expected=" + expected + " actual=" + actual + ")");
            failed++;
        }
    }

    static void assertEquals(String label, int expected, int actual) {
        if (expected == actual) {
            System.out.println("  PASS: " + label);
            passed++;
        } else {
            System.out.println("  FAIL: " + label + " (expected=" + expected + " actual=" + actual + ")");
            failed++;
        }
    }
}