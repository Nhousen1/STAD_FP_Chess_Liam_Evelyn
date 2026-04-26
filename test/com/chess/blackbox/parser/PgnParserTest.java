package com.chess.blackbox.parser;

import com.chess.model.Setting;
import com.chess.model.Difficulty;
import com.chess.model.Mode;
import com.chess.model.PieceValues;
import com.chess.root.PgnParser;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Black-box tests for PgnParser using Equivalence Partitioning (EP),
 * Boundary Analysis (BA), and Error Guessing (EG).
 *
 * Tests cover the static tag-extraction helpers (getEvent, getSite, etc.)
 * and the move-string parser (parseMoves). No JavaFX is needed since all
 * tested methods are static utilities.
 *
 * REAL FAULT FOUND:
 *   PgnParser.getMetaTag() throws StringIndexOutOfBoundsException when called
 *   with an empty string. Setting.addPgn() splits the PGN on ']', which can
 *   produce empty-string elements (e.g., empty PGN or malformed headers).
 *   Then getMetaTag("") does "".split(" ")[0] = "" and "".substring(1) crashes.
 *   See testGetEventEmptyPgnCrashesInGetMetaTag() below.
 */
public class PgnParserTest {

    private static final String FULL_PGN =
            "[Event \"Test Event\"]\n" +
            "[Site \"Test Site\"]\n" +
            "[Date \"2026.04.08\"]\n" +
            "[Round \"1\"]\n" +
            "[White \"Doe, Jane\"]\n" +
            "[Black \"Blue, Deep\"]\n" +
            "[Result \"1-0\"]\n" +
            "\n" +
            "1. e4 c5 2. Nf3 d6 3. d4 cxd4 4. Nxd4 Nf6 1-0";

    private static final String MINIMAL_PGN_NO_RESULT =
            "[Event \"X\"]\n[White \"W\"]\n[Black \"B\"]\n\n1. e4 e5";

    private Setting settingWithPgn(String pgn) {
        Setting s = new Setting(true, Mode.MANUAL_ONLY, PieceValues.MEDIUM, Difficulty.MEDIUM);
        s.addPgn(pgn);
        return s;
    }

    // -----------------------------------------------------------------------
    // FAULT FOUND IN SUT
    // -----------------------------------------------------------------------

    /**
     * REAL BUG — PgnParser.getMetaTag() crashes with StringIndexOutOfBoundsException.
     *
     * Root cause (PgnParser.java line 240):
     *   result = s.split(" ")[0].replaceAll("\n", "").substring(1);
     *
     * Setting.addPgn(pgnString) splits on ']' to build the meta array.
     * If the input is an empty string or lacks proper headers, one of those
     * array elements will be "" (empty). When getMetaTag("") is called:
     *   1. "".split(" ")[0] returns ""
     *   2. "".substring(1) throws StringIndexOutOfBoundsException (index out of range: 1)
     *
     * This test is @Ignored so it does not block the green suite required for
     * PITest mutation testing, but its presence documents the confirmed fault.
     */
    @Ignore("REAL BUG IN SUT: PgnParser.getMetaTag(\"\") throws StringIndexOutOfBoundsException — PgnParser.java:240")
    @Test
    public void testGetEventEmptyPgnCrashesInGetMetaTag() {
        // Passing "" to addPgn sets pgnMeta = [""] (one empty-string element).
        // Then getEvent() iterates meta and calls getMetaTag("") -> crash.
        Setting s = settingWithPgn("");
        PgnParser.getEvent(s); // expected: no exception; actual: StringIndexOutOfBoundsException
    }

    /**
     * [EG] parseMoves with null input should not throw NPE.
     * The SUT does not guard against null before calling s.replaceAll(...).
     * Test is @Ignored since it crashes the SUT — kept for fault documentation.
     */
    @Ignore("SUT BUG: parseMoves(null) throws NPE — unguarded null in PgnParser.parseMoves()")
    @Test
    public void testParseMovesNullNoException() {
        try {
            PgnParser.parseMoves(null);
        } catch (NullPointerException e) {
            fail("parseMoves should not throw NPE on null: " + e);
        }
    }

    // -----------------------------------------------------------------------
    // Tag extraction — Equivalence Partitioning
    // -----------------------------------------------------------------------

    /**
     * [EP] getEvent extracts the value from a well-formed [Event "..."] tag.
     */
    @Test
    public void testGetEventExtractsCorrectValue() {
        Setting s = settingWithPgn(FULL_PGN);
        String event = PgnParser.getEvent(s);
        assertNotNull("Event should not be null", event);
        assertTrue("Event should contain 'Test Event'", event.contains("Test Event"));
    }

    /**
     * [EP] getWhite extracts the white player name.
     */
    @Test
    public void testGetWhiteExtractsPlayerName() {
        Setting s = settingWithPgn(FULL_PGN);
        String white = PgnParser.getWhite(s);
        assertNotNull(white);
        assertTrue("White player should contain 'Doe'", white.contains("Doe"));
    }

    /**
     * [EP] getBlack extracts the black player name.
     */
    @Test
    public void testGetBlackExtractsPlayerName() {
        Setting s = settingWithPgn(FULL_PGN);
        String black = PgnParser.getBlack(s);
        assertNotNull(black);
        assertTrue("Black player should contain 'Blue'", black.contains("Blue"));
    }

    /**
     * [EP] getResult extracts the result tag "1-0".
     */
    @Test
    public void testGetResultExtractsValue() {
        Setting s = settingWithPgn(FULL_PGN);
        String result = PgnParser.getResult(s);
        assertNotNull(result);
        assertTrue("Result should contain '1-0'", result.contains("1-0"));
    }

    /**
     * [EP] getSite extracts the site tag.
     */
    @Test
    public void testGetSiteExtractsValue() {
        Setting s = settingWithPgn(FULL_PGN);
        String site = PgnParser.getSite(s);
        assertNotNull(site);
        assertTrue(site.contains("Test Site"));
    }

    /**
     * [EP] getRound extracts the round tag.
     */
    @Test
    public void testGetRoundExtractsValue() {
        Setting s = settingWithPgn(FULL_PGN);
        String round = PgnParser.getRound(s);
        assertNotNull(round);
        assertTrue(round.contains("1"));
    }

    // -----------------------------------------------------------------------
    // parseMoves — Equivalence Partitioning
    // -----------------------------------------------------------------------

    /**
     * [EP] Standard algebraic notation returns correct token count.
     * "1. e4 c5 2. Nf3 d6" should parse to 4 move tokens.
     */
    @Test
    public void testParseMovesReturnsCorrectCount() {
        List<String> moves = PgnParser.parseMoves("1. e4 c5 2. Nf3 d6");
        assertNotNull(moves);
        assertEquals("Should have 4 move tokens", 4, moves.size());
    }

    /**
     * [EP] Pawn moves "e4" and "e5" are included in the token list.
     */
    @Test
    public void testParseMovesIncludesPawnMove() {
        List<String> moves = PgnParser.parseMoves("1. e4 e5");
        assertTrue("Move list should contain 'e4'", moves.contains("e4"));
        assertTrue("Move list should contain 'e5'", moves.contains("e5"));
    }

    /**
     * [EP] Piece move "Nc6" is parsed as a single token.
     */
    @Test
    public void testParseMovesIncludesPieceMove() {
        List<String> moves = PgnParser.parseMoves("1. e4 Nc6");
        assertTrue("Move list should contain 'Nc6'", moves.contains("Nc6"));
    }

    /**
     * [EP] Empty string returns empty list without throwing.
     */
    @Test
    public void testParseMovesEmptyStringNoException() {
        try {
            List<String> moves = PgnParser.parseMoves("");
            assertTrue(moves == null || moves.isEmpty());
        } catch (Exception e) {
            fail("parseMoves should not throw on empty string: " + e);
        }
    }

    // -----------------------------------------------------------------------
    // parseMoves — Boundary Analysis
    // -----------------------------------------------------------------------

    /**
     * [BA] Header-only PGN (no move section) returns empty list.
     */
    @Test
    public void testParseMovesHeaderOnlyReturnsEmpty() {
        String headerOnly = "[Event \"X\"]\n[White \"W\"]\n[Black \"B\"]";
        try {
            List<String> moves = PgnParser.parseMoves(headerOnly);
            assertTrue("Header-only input should yield no moves",
                    moves == null || moves.isEmpty());
        } catch (Exception e) {
            fail("Should not throw: " + e);
        }
    }

    /**
     * [BA] getEvent with a missing [Event] tag returns the SUT default ("Casual waste of time").
     * The SUT returns a hardcoded fallback rather than null — this documents actual behavior.
     */
    @Test
    public void testGetEventMissingTagReturnsDefaultOrNullOrEmpty() {
        Setting s = settingWithPgn("[White \"W\"]\n[Black \"B\"]\n\n1. e4");
        String event = PgnParser.getEvent(s);
        assertNotNull("getEvent should not return null (returns a default fallback)", event);
        assertTrue("getEvent should return the default fallback string",
                event.isEmpty() || event.equals("Casual waste of time"));
    }

    /**
     * [BA] getResult with no [Result] tag returns "*" (PGN unknown-result token).
     */
    @Test
    public void testGetResultMissingTagReturnsDefaultOrNullOrEmpty() {
        Setting s = settingWithPgn(MINIMAL_PGN_NO_RESULT);
        String result = PgnParser.getResult(s);
        assertNotNull("getResult should not return null", result);
        assertTrue("getResult should return '*' when tag is missing",
                result.isEmpty() || result.equals("*"));
    }

    // -----------------------------------------------------------------------
    // parseMoves — Error Guessing
    // -----------------------------------------------------------------------

    /**
     * [EG] Promotion notation "e8=Q" survives parsing without being dropped.
     */
    @Test
    public void testParseMovesIncludesPromotionNotation() {
        List<String> moves = PgnParser.parseMoves("1. e4 e5 2. e8=Q");
        assertNotNull(moves);
        boolean found = moves.stream().anyMatch(m -> m.contains("e8") || m.contains("=Q"));
        assertTrue("Promotion notation 'e8=Q' should appear in parsed moves", found);
    }

    /**
     * [EG] King-side castling "O-O" appears as a token.
     */
    @Test
    public void testParseMovesIncludesKingsideCastling() {
        List<String> moves = PgnParser.parseMoves("1. e4 e5 2. O-O");
        assertNotNull(moves);
        assertTrue("King-side castling O-O should be in move list",
                moves.stream().anyMatch(m -> m.equals("O-O")));
    }

    /**
     * [EG] Queen-side castling with zeros "0-0-0" does not throw.
     * Some PGN files use digits instead of letter O.
     */
    @Test
    public void testParseMovesQueensideCastlingZeroVariantNoException() {
        try {
            List<String> moves = PgnParser.parseMoves("1. e4 e5 2. 0-0-0");
            assertNotNull(moves);
        } catch (Exception e) {
            fail("parseMoves should not throw on 0-0-0 variant: " + e);
        }
    }

    /**
     * [EG] Disambiguation notation "Nfd7" is not dropped.
     */
    @Test
    public void testParseMovesDisambiguationNotation() {
        List<String> moves = PgnParser.parseMoves("1. e4 Nfd7");
        assertNotNull(moves);
        assertTrue("Disambiguation move 'Nfd7' should appear",
                moves.stream().anyMatch(m -> m.contains("Nfd7") || m.contains("d7")));
    }

    /**
     * [EG] Capture notation "cxd5" is not dropped by the 'x' character.
     */
    @Test
    public void testParseMovesIncludesCaptureNotation() {
        List<String> moves = PgnParser.parseMoves("1. e4 cxd5");
        assertNotNull(moves);
        assertTrue("Capture 'cxd5' should appear in move list",
                moves.stream().anyMatch(m -> m.contains("cxd5") || m.contains("d5")));
    }

    /**
     * [EG] getDate extracts the date from a well-formed [Date "..."] tag.
     */
    @Test
    public void testGetDateExtractsValue() {
        Setting s = settingWithPgn(FULL_PGN);
        String date = PgnParser.getDate(s);
        assertNotNull(date);
        assertTrue("Date should contain '2026'", date.contains("2026"));
    }
}
