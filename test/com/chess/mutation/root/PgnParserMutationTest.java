package com.chess.mutation.root;

import com.chess.blackbox.ChessTestBase;
import com.chess.model.Difficulty;
import com.chess.model.Mode;
import com.chess.model.PieceValues;
import com.chess.model.Setting;
import com.chess.root.PgnParser;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Mutation-targeted tests for com.chess.root.PgnParser.
 *
 * Surviving mutants targeted:
 *  - getEvent() / getSite() / getDate() / getResult() → replaced return with ""
 *  - getRound() / getWhite() / getBlack() → replaced null return with ""
 *  - Conditional mutations in meta loop: contentEquals("X") → false
 *  - parseMoves() → returned empty list / wrong list content
 */
public class PgnParserMutationTest extends ChessTestBase {

    /** Setting with no PGN metadata (pgnMeta == null). */
    private static Setting emptyMeta() {
        return new Setting(true, Mode.MANUAL_ONLY, PieceValues.MEDIUM, Difficulty.MEDIUM);
    }

    /** Setting with PGN metadata parsed from the given string. */
    private static Setting withPgn(String pgn) {
        Setting s = emptyMeta();
        s.addPgn(pgn);
        return s;
    }

    // -------------------------------------------------------------------------
    // getEvent() — kills return-"" mutant and contentEquals("Event") → false mutant
    // -------------------------------------------------------------------------

    @Test
    public void testGetEventDefaultNotEmpty() {
        String event = PgnParser.getEvent(emptyMeta());
        assertNotNull(event);
        assertFalse("Default event must not be empty string", event.isEmpty());
    }

    @Test
    public void testGetEventDefaultValue() {
        assertEquals("Casual waste of time", PgnParser.getEvent(emptyMeta()));
    }

    @Test
    public void testGetEventReadFromMeta() {
        String pgn = "[Event \"Grand Tournament\"][Site \"Online\"]";
        assertEquals("Grand Tournament", PgnParser.getEvent(withPgn(pgn)));
    }

    @Test
    public void testGetEventDoesNotReturnSiteValue() {
        String pgn = "[Event \"My Event\"][Site \"My Site\"]";
        assertEquals("My Event", PgnParser.getEvent(withPgn(pgn)));
    }

    // -------------------------------------------------------------------------
    // getSite() — kills return-"" mutant and contentEquals("Site") → false mutant
    // -------------------------------------------------------------------------

    @Test
    public void testGetSiteDefaultNotEmpty() {
        String site = PgnParser.getSite(emptyMeta());
        assertNotNull(site);
        assertFalse("Default site must not be empty string", site.isEmpty());
    }

    @Test
    public void testGetSiteDefaultValue() {
        assertEquals("Your cave, SWITZERLAND", PgnParser.getSite(emptyMeta()));
    }

    @Test
    public void testGetSiteReadFromMeta() {
        String pgn = "[Event \"E\"][Site \"Chess Club\"]";
        assertEquals("Chess Club", PgnParser.getSite(withPgn(pgn)));
    }

    @Test
    public void testGetSiteDoesNotReturnEventValue() {
        String pgn = "[Event \"My Event\"][Site \"My Site\"]";
        assertEquals("My Site", PgnParser.getSite(withPgn(pgn)));
    }

    // -------------------------------------------------------------------------
    // getDate() — kills return-"" mutant and contentEquals("Date") → false mutant
    // -------------------------------------------------------------------------

    @Test
    public void testGetDateDefaultNotNull() {
        assertNotNull(PgnParser.getDate(emptyMeta()));
    }

    @Test
    public void testGetDateDefaultNotEmpty() {
        assertFalse("Default date must not be empty", PgnParser.getDate(emptyMeta()).isEmpty());
    }

    @Test
    public void testGetDateReadFromMeta() {
        String pgn = "[Date \"2025.01.15\"]";
        assertEquals("2025.01.15", PgnParser.getDate(withPgn(pgn)));
    }

    // -------------------------------------------------------------------------
    // getResult() — kills return-"" mutant (default is "*")
    // -------------------------------------------------------------------------

    @Test
    public void testGetResultDefaultIsStar() {
        assertEquals("*", PgnParser.getResult(emptyMeta()));
    }

    @Test
    public void testGetResultDefaultNotEmpty() {
        assertFalse("Default result must not be empty", PgnParser.getResult(emptyMeta()).isEmpty());
    }

    @Test
    public void testGetResultReadFromMeta() {
        String pgn = "[Result \"1-0\"]";
        assertEquals("1-0", PgnParser.getResult(withPgn(pgn)));
    }

    @Test
    public void testGetResultDrawFromMeta() {
        String pgn = "[Result \"1/2-1/2\"]";
        assertEquals("1/2-1/2", PgnParser.getResult(withPgn(pgn)));
    }

    // -------------------------------------------------------------------------
    // getRound() — kills null→"" mutant; default is null (no meta)
    // -------------------------------------------------------------------------

    @Test
    public void testGetRoundDefaultIsNull() {
        assertNull("getRound() with no meta should return null", PgnParser.getRound(emptyMeta()));
    }

    @Test
    public void testGetRoundReadFromMeta() {
        String pgn = "[Round \"3\"]";
        assertEquals("3", PgnParser.getRound(withPgn(pgn)));
    }

    @Test
    public void testGetRoundDoesNotReturnEventValue() {
        String pgn = "[Event \"E\"][Round \"7\"]";
        assertEquals("7", PgnParser.getRound(withPgn(pgn)));
    }

    // -------------------------------------------------------------------------
    // getWhite() — kills null→"" mutant
    // -------------------------------------------------------------------------

    @Test
    public void testGetWhiteDefaultIsNull() {
        assertNull("getWhite() with no meta should return null", PgnParser.getWhite(emptyMeta()));
    }

    @Test
    public void testGetWhiteReadFromMeta() {
        String pgn = "[White \"Magnus Carlsen\"]";
        assertEquals("Magnus Carlsen", PgnParser.getWhite(withPgn(pgn)));
    }

    // -------------------------------------------------------------------------
    // getBlack() — kills null→"" mutant
    // -------------------------------------------------------------------------

    @Test
    public void testGetBlackDefaultIsNull() {
        assertNull("getBlack() with no meta should return null", PgnParser.getBlack(emptyMeta()));
    }

    @Test
    public void testGetBlackReadFromMeta() {
        String pgn = "[Black \"Fabiano Caruana\"]";
        assertEquals("Fabiano Caruana", PgnParser.getBlack(withPgn(pgn)));
    }

    @Test
    public void testGetBlackDoesNotReturnWhiteValue() {
        String pgn = "[White \"Player A\"][Black \"Player B\"]";
        assertEquals("Player A", PgnParser.getWhite(withPgn(pgn)));
        assertEquals("Player B", PgnParser.getBlack(withPgn(pgn)));
    }

    // -------------------------------------------------------------------------
    // parseMoves() — kills empty-list mutant and content mutations
    // -------------------------------------------------------------------------

    @Test
    public void testParseMovesNonEmpty() {
        List<String> moves = PgnParser.parseMoves("1. e4 e5 2. Nf3 Nc6");
        assertFalse("parseMoves should return non-empty list", moves.isEmpty());
    }

    @Test
    public void testParseMovesCount() {
        List<String> moves = PgnParser.parseMoves("1. e4 e5 2. Nf3 Nc6");
        assertEquals("Should parse 4 moves", 4, moves.size());
    }

    @Test
    public void testParseMovesContainsFirstMove() {
        List<String> moves = PgnParser.parseMoves("1. e4 e5 2. Nf3");
        assertTrue("parseMoves should include 'e4'", moves.contains("e4"));
    }

    @Test
    public void testParseMovesContainsAllMoves() {
        List<String> moves = PgnParser.parseMoves("1. e4 e5 2. Nf3 Nc6 3. Bc4");
        assertEquals("Should parse 5 moves", 5, moves.size());
        assertTrue(moves.contains("e4"));
        assertTrue(moves.contains("e5"));
        assertTrue(moves.contains("Nf3"));
        assertTrue(moves.contains("Nc6"));
        assertTrue(moves.contains("Bc4"));
    }

    @Test
    public void testParseMovesEmptyStringReturnsEmpty() {
        List<String> moves = PgnParser.parseMoves("   ");
        assertTrue("parseMoves on whitespace should return empty list", moves.isEmpty());
    }
}
