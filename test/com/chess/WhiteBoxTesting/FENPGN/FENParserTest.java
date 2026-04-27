package com.chess.WhiteBoxTesting.FENPGN;

import com.chess.WhiteBoxTesting.ChessBaseTest;
import com.chess.WhiteBoxTesting.TestDouble;
import com.chess.root.FenParser;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

/**
 * White-box tests for FenParser.
 *
 * Strategy (from proposal):
 *   "Test FEN parser by checking that exported FEN strings match the original
 *    strings using the LOAD option."
 *
 */
public class FENParserTest extends ChessBaseTest {

    // ====================================================================
    // parseBoard – Branch Coverage
    // ====================================================================

    @Test
    public void parseBoardStartPosition() {
        String boardStr = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        String[][] board = FenParser.parseBoard(boardStr);

        assertNotNull("Board array must not be null", board);
        // Black rook at a8 → col 0, row 0
        assertEquals("r", board[0][0]);
        // White rook at a1 → col 0, row 7
        assertEquals("R", board[0][7]);
        // Empty square at e4 → col 4, row 4
        assertNull("Empty square should be null", board[4][4]);
    }

    @Test
    public void parseBoardEmptyRows() {
        String boardStr = "8/8/8/8/8/8/8/8";
        String[][] board = FenParser.parseBoard(boardStr);

        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                assertNull("All squares should be empty", board[col][row]);
            }
        }
    }

    @Test
    public void parseBoardSinglePiece() {
        // White king on e1 only
        String boardStr = "8/8/8/8/8/8/8/4K3";
        String[][] board = FenParser.parseBoard(boardStr);

        assertEquals("White king at e1 (col=4, row=7)", "K", board[4][7]);

        // all others null
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                if (col == 4 && row == 7) continue;
                assertNull("All other squares should be empty", board[col][row]);
            }
        }
    }

    @Test
    public void parseBoardSingleEmptyCell() {
        String[][] board = FenParser.parseBoard("1r6/8/8/8/8/8/8/8");
        assertNull(board[0][0]);
        assertEquals("r", board[1][0]);
    }

    @Test
    public void parseBoardColOverflow() {
        try {
            String[][] board = FenParser.parseBoard("RNBQKBNRP/8/8/8/8/8/8/8");
            assertNotNull(board);
        } catch (Exception e) {
            fail("col>=8; should prevent ArrayIndexOutOfBounds: " + e);
        }
    }

    @Test
    public void parseBoardRowOverflow() {
        try {
            String[][] board = FenParser.parseBoard("8/8/8/8/8/8/8/8/r7");
            assertNotNull(board);
        } catch (Exception e) {
            fail("row>=8; should prevent ArrayIndexOutOfBounds: " + e);
        }
    }

    // ====================================================================
    // parseInteger
    // ====================================================================

    @Test
    public void parseIntValidNumericString() {
        assertEquals(42, FenParser.parseInteger("42"));
    }

    @Test
    public void parseIntNonNumericString() {
        assertEquals(0, FenParser.parseInteger("abc"));
    }

    // ====================================================================
    // parseMoveCounter
    // ====================================================================

    @Test
    public void parseMoveCounterNumericString() {
        Double result = FenParser.parseMoveCounter("5");
        assertEquals(5.0, result, 0.001);
    }

    @Test
    public void parseMoveCounterInvalidString() {
        assertEquals(Double.valueOf(0.0), FenParser.parseMoveCounter("abc"));
    }

    // ====================================================================
    // FEN round-trip: build a board from FEN, export it, compare
    // ====================================================================

    @Test
    public void fenRoundTripStartPosition() {
        String originalFen = FEN_START;
        TestDouble game = TestDouble.fromFen(originalFen);
        String exportedFen = game.getBoard().getFen();

        // The exported board segment must match the original
        String originalBoard = originalFen.split(" ")[0];
        String exportedBoard = exportedFen.split(" ")[0];
        assertEquals("Board segment of exported FEN should match original",
                originalBoard, exportedBoard);
    }

    @Test
    public void fenRoundTripPlayerTokenWhiteToMove() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        String fen = game.getBoard().getFen();
        // second token is current player
        assertEquals("w", fen.split(" ")[1]);
    }

    @Test
    public void fenRoundTripKingsOnlyPosition() {
        String fen = FEN_KINGS_ONLY;
        TestDouble game = TestDouble.fromFen(fen);
        String exported = game.getBoard().getFen();

        String originalBoard = fen.split(" ")[0];
        String exportedBoard = exported.split(" ")[0];
        assertEquals(originalBoard, exportedBoard);
    }

    @Test
    public void fenRoundTripPromotionPosition() {
        TestDouble game = TestDouble.fromFen(FEN_PROMOTION);
        String exported = game.getBoard().getFen();
        String originalBoard = FEN_PROMOTION.split(" ")[0];
        String exportedBoard = exported.split(" ")[0];
        assertEquals(originalBoard, exportedBoard);
    }

    @Test
    public void getBoardMixedRankWithLeadingGap() {
        TestDouble game = TestDouble.fromFen("3r4/8/8/8/8/8/8/4K3 w - - 0 1");
        String fen = game.getBoard().getFen();
        assertTrue(fen.startsWith("3r"));
    }

    @Test
    public void getBoardRankEndingWithEmptySquares() {
        TestDouble game = TestDouble.fromFen("r7/8/8/8/8/8/8/4K3 w - - 0 1");
        String fen = game.getBoard().getFen();
        assertTrue(fen.startsWith("r7"));
    }

    @Test
    public void getPlayerWhiteTurnReturnsW() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        assertEquals("w", FenParser.getPlayer(game.getBoard()));
    }

    // ====================================================================
    // Castling FEN token
    // ====================================================================

    @Test
    public void fenCastlingStartPosition() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        String castling = FenParser.getCastling(game.getBoard());
        assertTrue(castling.contains("K") && castling.contains("Q")
                        && castling.contains("k") && castling.contains("q"));
    }

    @Test
    public void fenCastlingKingsOnly() {
        TestDouble game = TestDouble.fromFen(FEN_KINGS_ONLY);
        String castling = FenParser.getCastling(game.getBoard());
        assertEquals("No castling available", "-", castling);
    }

    @Test
    public void parseCastlingNull() {
        TestDouble game = TestDouble.fromFen(FEN_KINGS_ONLY);
        try {
            FenParser.parseCastling(null, game.getBoard());
        } catch (Exception e) {
            fail("null cas should skip body: " + e);
        }
    }

    @Test
    public void parseCastlingDefault() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        try {
            FenParser.parseCastling("KQkq", game.getBoard());
        } catch (Exception e) {
            fail("KQkq should skip body: " + e);
        }
    }

    @Test
    public void parseCastlingPartialCastling() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        try {
            FenParser.parseCastling("Kq", game.getBoard());
        } catch (Exception e) {
            fail("Should not throw on partial castling string: " + e);
        }
    }

    // ====================================================================
    // En-passant FEN token
    // ====================================================================

    @Test
    public void fenPassingNoPassing() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        String passing = FenParser.getPassing(game.getBoard());
        assertEquals("-", passing);
    }

    @Test
    public void fenPassingEnPassantAvailable() {
        TestDouble game = TestDouble.fromFen(FEN_EN_PASSANT);
        String passing = FenParser.getPassing(game.getBoard());
        // en-passant target square is d6
        assertFalse("En passant square should not be '-'", "-".equals(passing));
    }

    @Test
    public void fenPassingRow3Branch() {
        assertNull(FenParser.parsePassing("e3", new LinkedList<>(), new LinkedList<>()));
    }

    @Test
    public void parsePassingRow6Branch() {
        assertNull(FenParser.parsePassing("e6", new LinkedList<>(), new LinkedList<>()));
    }

    @Test
    public void parsePassingInvalidRow() {
        assertNull(FenParser.parsePassing("e4", new LinkedList<>(), new LinkedList<>()));
    }

    @Test
    public void getPassingWhitePawn() {
        TestDouble game = TestDouble.fromFen(FEN_EN_PASSANT);
        String passing = FenParser.getPassing(game.getBoard());
        assertFalse("-".equals(passing));
    }

    // ====================================================================
    // Countdown / move count tokens
    // ====================================================================

    @Test
    public void fenCountdownStartPositionReturnsZero() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        String countdown = FenParser.getCountdown(game.getBoard());
        assertEquals("0", countdown);
    }

    @Test
    public void fenMoveCountStartPositionReturnsOne() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        String moveCount = FenParser.getMoveCount(game.getBoard());
        assertEquals("1", moveCount);
    }
}
