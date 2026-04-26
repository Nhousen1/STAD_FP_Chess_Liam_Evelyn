package com.chess.blackbox.parser;

import com.chess.root.FenParser;
import com.chess.root.pieces.Piece;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Black-box tests for FenParser using EP, BA, and EG.
 *
 * FEN format: "<board> <active> <castling> <ep> <halfmove> <fullmove>"
 * Board section: ranks 8→1 separated by '/', uppercase=white, lowercase=black, digits=empty.
 * No JavaFX needed — all tested methods are static.
 */
public class FenParserTest {

    // -------------------------------------------------------------------------
    // parseBoard — Equivalence Partitioning
    // -------------------------------------------------------------------------

    /**
     * [EP] Starting-position FEN: black rook at a8 → board[col=0][row=0] = "r".
     * Partition: well-formed FEN with all 32 pieces.
     * Goal: verify column/row mapping for the top-left corner of the board.
     */
    @Test
    public void testParseBoardBlackRookAtA8() {
        String[][] board = FenParser.parseBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertEquals("r", board[0][0]);
    }

    /**
     * [EP] Starting-position FEN: black king at e8 → board[col=4][row=0] = "k".
     * Partition: well-formed FEN, piece in the middle of back rank.
     */
    @Test
    public void testParseBoardBlackKingAtE8() {
        String[][] board = FenParser.parseBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertEquals("k", board[4][0]);
    }

    /**
     * [EP] Starting-position FEN: white king at e1 → board[col=4][row=7] = "K".
     * Partition: well-formed FEN, white piece on last rank.
     */
    @Test
    public void testParseBoardWhiteKingAtE1() {
        String[][] board = FenParser.parseBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertEquals("K", board[4][7]);
    }

    /**
     * [EP] Fully empty rank encoded as "8" — all eight cells must be null.
     * Partition: valid FEN with entire rank empty.
     * Goal: verify that numeric gap fills cells with null, not "8".
     */
    @Test
    public void testParseBoardEmptyRankIsAllNull() {
        String[][] board = FenParser.parseBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        for (int col = 0; col < 8; col++) {
            assertNull("Rank 5 col=" + col + " should be null", board[col][4]);
        }
    }

    /**
     * [EP] Mixed rank "3Bp4": 3 empty, white Bishop, black pawn, 4 empty.
     * Partition: rank with interleaved gaps and pieces.
     * Goal: numeric gap + piece code parsing in correct sequence.
     */
    @Test
    public void testParseBoardMixedRankWithGapsAndPieces() {
        String[][] board = FenParser.parseBoard("8/8/8/8/3Bp4/8/8/8");
        assertNull("col 0 should be empty",       board[0][4]);
        assertNull("col 2 should be empty",       board[2][4]);
        assertEquals("col 3 should be Bishop",    "B", board[3][4]);
        assertEquals("col 4 should be black pawn","p", board[4][4]);
        assertNull("col 5 should be empty",       board[5][4]);
    }

    /**
     * [EP] All uppercase piece codes in back rank: R, N, B, Q, K, B, N, R.
     * Partition: white pieces only.
     */
    @Test
    public void testParseBoardWhiteBackRankPieceCodes() {
        String[][] board = FenParser.parseBoard("8/8/8/8/8/8/8/RNBQKBNR");
        assertEquals("R", board[0][7]);
        assertEquals("N", board[1][7]);
        assertEquals("B", board[2][7]);
        assertEquals("Q", board[3][7]);
        assertEquals("K", board[4][7]);
    }

    // -------------------------------------------------------------------------
    // parseBoard — Boundary Analysis
    // -------------------------------------------------------------------------

    /**
     * [BA] Numeric digit > 8 in a rank: parser should not throw an exception.
     * Boundary: 8 is the maximum legal gap value; 9 overflows a rank.
     * Goal: verify the exception-swallowing pattern doesn't propagate crashes.
     */
    @Test
    public void testParseBoardNumericOverflowNoException() {
        try {
            String[][] board = FenParser.parseBoard("9/8/8/8/8/8/8/8");
            assertNotNull("Board array should be returned even for invalid input", board);
        } catch (Exception e) {
            fail("parseBoard must not propagate exceptions: " + e);
        }
    }

    /**
     * [BA] Exactly eight ranks separated by seven slashes is the valid boundary.
     * Goal: confirm the array is 8×8.
     */
    @Test
    public void testParseBoardExactlyEightRanksReturnsEightByEight() {
        String[][] board = FenParser.parseBoard("8/8/8/8/8/8/8/8");
        assertNotNull(board);
        assertEquals(8, board.length);
        assertEquals(8, board[0].length);
    }

    // -------------------------------------------------------------------------
    // parseBoard — Error Guessing
    // -------------------------------------------------------------------------

    /**
     * [EG] null input: must return null or an empty structure without NPE.
     * Fault class: unguarded null-pointer on caller-supplied string.
     */
    @Test
    public void testParseBoardNullInputNoNPE() {
        try {
            FenParser.parseBoard(null);
        } catch (NullPointerException e) {
            fail("parseBoard should not throw NPE on null input");
        }
    }

    /**
     * [EG] Empty string: must not throw.
     * Fault class: unchecked empty-string edge case.
     */
    @Test
    public void testParseBoardEmptyStringNoException() {
        try {
            FenParser.parseBoard("");
        } catch (Exception e) {
            fail("parseBoard should not throw on empty string: " + e);
        }
    }

    /**
     * [EG] Unknown piece character 'z' in rank: must not throw.
     * Fault class: invalid piece code may be stored verbatim or silently skipped.
     * Goal: verify no uncaught exception; actual stored value is an implementation detail.
     */
    @Test
    public void testParseBoardUnknownPieceCharacterNoException() {
        try {
            String[][] board = FenParser.parseBoard("z7/8/8/8/8/8/8/8");
            assertNotNull(board);
        } catch (Exception e) {
            fail("parseBoard should not throw on unknown piece character: " + e);
        }
    }

    /**
     * [EG] Only slashes, no rank content: must not crash.
     * Fault class: short/malformed strings.
     */
    @Test
    public void testParseBoardOnlySlashesNoException() {
        try {
            FenParser.parseBoard("////");
        } catch (Exception e) {
            fail("parseBoard should not throw on slash-only input: " + e);
        }
    }

    // -------------------------------------------------------------------------
    // parseInteger — Boundary Analysis
    // -------------------------------------------------------------------------

    /**
     * [BA] "50" → 50: standard half-move clock boundary (50-move rule threshold).
     */
    @Test
    public void testParseIntegerFifty() {
        assertEquals(50, FenParser.parseInteger("50"));
    }

    /**
     * [BA] "0" → 0: minimum valid clock value.
     */
    @Test
    public void testParseIntegerZero() {
        assertEquals(0, FenParser.parseInteger("0"));
    }

    /**
     * [BA] Empty string "" → 0 (default fallback, no exception).
     * Goal: ensure graceful degradation.
     */
    @Test
    public void testParseIntegerEmptyReturnsZero() {
        assertEquals(0, FenParser.parseInteger(""));
    }

    /**
     * [BA] null → 0 (default fallback, no exception).
     */
    @Test
    public void testParseIntegerNullReturnsZero() {
        assertEquals(0, FenParser.parseInteger(null));
    }

    /**
     * [BA] Non-numeric "abc" → 0.
     */
    @Test
    public void testParseIntegerNonNumericReturnsZero() {
        assertEquals(0, FenParser.parseInteger("abc"));
    }

    // -------------------------------------------------------------------------
    // parseMoveCounter — Boundary Analysis
    // -------------------------------------------------------------------------

    /**
     * [BA] "1" → 1.0: minimum valid full-move counter.
     */
    @Test
    public void testParseMoveCounterMinimum() {
        assertEquals(Double.valueOf(1.0), FenParser.parseMoveCounter("1"));
    }

    /**
     * [BA] "100" → 100.0: large move counter still parsed correctly.
     */
    @Test
    public void testParseMoveCounterLargeValue() {
        assertEquals(Double.valueOf(100.0), FenParser.parseMoveCounter("100"));
    }

    // -------------------------------------------------------------------------
    // parsePassing — Error Guessing
    // -------------------------------------------------------------------------

    /**
     * [EG] "-" (no en passant in FEN): must return null.
     * Partition: en passant unavailable, standard FEN dash field.
     */
    @Test
    public void testParsePassingDashReturnsNull() {
        List<Piece> empty = new LinkedList<>();
        Piece result = FenParser.parsePassing("-", empty, empty);
        assertNull("parsePassing(\"-\") should return null", result);
    }

    /**
     * [EG] Invalid en passant square "e4" (rank digit 4, not 3 or 6):
     * parsePassing must silently return null without crashing.
     * Fault: only rank 3 (white) and rank 6 (black) are handled; other ranks fall through.
     * This tests a suspected SUT limitation — any rank outside {3,6} is unhandled.
     */
    @Test
    public void testParsePassingInvalidRankSilentlyReturnsNull() {
        List<Piece> empty = new LinkedList<>();
        Piece result = FenParser.parsePassing("e4", empty, empty);
        assertNull("Invalid en passant rank should return null, not crash", result);
    }

    /**
     * [EG] null passing string: must not throw NPE.
     * Fault class: unguarded null check in parsePassing.
     */
    @Test
    public void testParsePassingNullNoException() {
        try {
            List<Piece> empty = new LinkedList<>();
            Piece result = FenParser.parsePassing(null, empty, empty);
            assertNull(result);
        } catch (NullPointerException e) {
            fail("parsePassing should not throw NPE on null input");
        }
    }

    /**
     * [EG] null piece lists: must not throw NPE when pawn lists are null.
     * Fault class: method iterates piece lists; null list causes NPE.
     */
    @Test
    public void testParsePassingNullPieceListsNoException() {
        try {
            FenParser.parsePassing("e3", null, null);
        } catch (NullPointerException e) {
            fail("parsePassing should not throw NPE with null piece lists");
        }
    }
}
