package com.chess.mutation.root;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Player;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Mutation-targeted tests for com.chess.root.Player.
 *
 * Surviving mutants targeted:
 *  - isAI() → replaced return with false
 *  - isBlack() → replaced return with true / replaced return with false
 *  - toString() → replaced return with ""
 *  - equals() → condition mutations (&&→||, this==obj→false, instanceof removed)
 *  - hashCode() → constant mutations (3↔4, 123 removed, + → -)
 */
public class PlayerMutationTest extends ChessTestBase {

    // -------------------------------------------------------------------------
    // isAI() — kills "replaced return with false" mutant
    // -------------------------------------------------------------------------

    @Test
    public void testPlayerIsNotAiByDefault() {
        Player p = new Player(false);
        assertFalse("Player should not be AI by default", p.isAI());
    }

    @Test
    public void testPlayerIsAiAfterSetAi() {
        Player p = new Player(false);
        p.setAI();
        assertTrue("Player should be AI after setAI()", p.isAI());
    }

    @Test
    public void testBlackPlayerIsAiAfterSetAi() {
        Player p = new Player(true);
        p.setAI();
        assertTrue("Black player should be AI after setAI()", p.isAI());
    }

    // -------------------------------------------------------------------------
    // isBlack() — kills replaced-return-with-true and replaced-return-with-false
    // -------------------------------------------------------------------------

    @Test
    public void testWhitePlayerIsNotBlack() {
        Player p = new Player(false);
        assertFalse("White player (color=false) should not be black", p.isBlack());
    }

    @Test
    public void testBlackPlayerIsBlack() {
        Player p = new Player(true);
        assertTrue("Black player (color=true) should be black", p.isBlack());
    }

    @Test
    public void testWhiteAndBlackPlayersDifferInColor() {
        Player white = new Player(false);
        Player black = new Player(true);
        assertNotEquals("White and black players should differ on isBlack()",
                white.isBlack(), black.isBlack());
    }

    // -------------------------------------------------------------------------
    // toString() — kills replaced-return-with-"" mutant
    // -------------------------------------------------------------------------

    @Test
    public void testWhitePlayerToString() {
        Player p = new Player(false);
        assertEquals("white", p.toString());
    }

    @Test
    public void testBlackPlayerToString() {
        Player p = new Player(true);
        assertEquals("black", p.toString());
    }

    @Test
    public void testToStringIsNotEmpty() {
        assertFalse(new Player(false).toString().isEmpty());
        assertFalse(new Player(true).toString().isEmpty());
    }

    @Test
    public void testToStringDiffersForColors() {
        assertNotEquals(new Player(false).toString(), new Player(true).toString());
    }

    // -------------------------------------------------------------------------
    // equals() — kills condition mutations on this==obj, instanceof checks
    // -------------------------------------------------------------------------

    @Test
    public void testPlayerEqualsItself() {
        Player p = new Player(false);
        assertTrue("Player must equal itself", p.equals(p));
    }

    @Test
    public void testPlayerDoesNotEqualDifferentInstance() {
        Player p1 = new Player(false);
        Player p2 = new Player(false);
        assertFalse("Two separate Player objects should not be equal", p1.equals(p2));
    }

    @Test
    public void testPlayerDoesNotEqualNull() {
        Player p = new Player(false);
        assertFalse("Player.equals(null) must be false", p.equals(null));
    }

    @Test
    public void testPlayerDoesNotEqualString() {
        Player p = new Player(false);
        assertFalse("Player.equals(String) must be false", p.equals("white"));
    }

    @Test
    public void testBlackPlayerEqualsItself() {
        Player p = new Player(true);
        assertTrue("Black player must equal itself", p.equals(p));
    }

    // -------------------------------------------------------------------------
    // hashCode() — kills constant mutations (3↔4, ±123)
    // -------------------------------------------------------------------------

    @Test
    public void testWhitePlayerHashCode() {
        Player p = new Player(false);
        // hash = 4 (isBlack()==false) + "white".hashCode() + 123
        int expected = 4 + "white".hashCode() + 123;
        assertEquals("White player hashCode should follow the formula", expected, p.hashCode());
    }

    @Test
    public void testBlackPlayerHashCode() {
        Player p = new Player(true);
        // hash = 3 (isBlack()==true) + "black".hashCode() + 123
        int expected = 3 + "black".hashCode() + 123;
        assertEquals("Black player hashCode should follow the formula", expected, p.hashCode());
    }

    @Test
    public void testBlackAndWhitePlayersHaveDifferentHashCodes() {
        Player white = new Player(false);
        Player black = new Player(true);
        assertNotEquals("Black and white players must have different hash codes",
                white.hashCode(), black.hashCode());
    }

    @Test
    public void testHashCodeIsStable() {
        Player p = new Player(false);
        int h1 = p.hashCode();
        int h2 = p.hashCode();
        assertEquals("hashCode must be stable across calls", h1, h2);
    }

    // -------------------------------------------------------------------------
    // getLock() — kills getLock() → null mutant
    // -------------------------------------------------------------------------

    @Test
    public void testLockIsNotNull() {
        Player p = new Player(false);
        assertNotNull("Lock object must not be null", p.getLock());
    }

    // -------------------------------------------------------------------------
    // getThread() — kills getThread() return mutations when null by default
    // -------------------------------------------------------------------------

    @Test
    public void testGetThreadIsNullByDefault() {
        Player p = new Player(false);
        assertNull("Thread should be null before setThread()", p.getThread());
    }
}
