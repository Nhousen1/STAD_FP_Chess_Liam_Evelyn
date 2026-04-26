package com.chess.WhiteBoxTesting.FENPGN;

import com.chess.WhiteBoxTesting.ChessBaseTest;
import com.chess.WhiteBoxTesting.TestDouble;
import com.chess.root.FenParser;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link FenParser}.
 *
 * Strategy (from proposal):
 *   "Test FEN parser by checking that exported FEN strings match the original
 *    strings using the LOAD option."
 *
 * All tests are headless – they exercise static parse/build helpers directly
 * without constructing a Board or Game.
 */
public class FENParserTest extends ChessBaseTest {

    // ====================================================================
    // parseBoard – converts the board segment of a FEN into a 2-D array
    // ====================================================================

    @Test
    public void parseBoard_startPosition_returnsPiecesAtCorrectCells() {
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
    public void parseBoard_emptyRows_allNulls() {
        String boardStr = "8/8/8/8/8/8/8/8";
        String[][] board = FenParser.parseBoard(boardStr);

        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                assertNull("All squares should be empty", board[col][row]);
            }
        }
    }

    @Test
    public void parseBoard_singlePiece_onlyThatCellFilled() {
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

    // ====================================================================
    // parseInteger
    // ====================================================================

    @Test
    public void parseInteger_validNumericString_returnsInt() {
        assertEquals(42, FenParser.parseInteger("42"));
    }

    @Test
    public void parseInteger_nonNumericString_returnsZero() {
        assertEquals(0, FenParser.parseInteger("abc"));
    }

    // ====================================================================
    // parseMoveCounter
    // ====================================================================

    @Test
    public void parseMoveCounter_numericString_returnsDouble() {
        Double result = FenParser.parseMoveCounter("5");
        assertEquals(5.0, result, 0.001);
    }

    // ====================================================================
    // FEN round-trip: build a board from FEN, export it, compare
    // ====================================================================

    @Test
    public void fenRoundTrip_startPosition_boardSegmentMatches() {
        String originalFen = FEN_START;
        TestDouble game = TestDouble.fromFen(originalFen);
        String exportedFen = game.getBoard().getFen();

        // The exported board segment (before first space) must match the original
        String originalBoard = originalFen.split(" ")[0];
        String exportedBoard = exportedFen.split(" ")[0];
        assertEquals("Board segment of exported FEN should match original",
                originalBoard, exportedBoard);
    }

    @Test
    public void fenRoundTrip_playerToken_whiteToMove() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        String fen = game.getBoard().getFen();
        // second token is current player
        assertEquals("w", fen.split(" ")[1]);
    }

    @Test
    public void fenRoundTrip_kingsOnlyPosition_boardMatches() {
        String fen = FEN_KINGS_ONLY;
        TestDouble game = TestDouble.fromFen(fen);
        String exported = game.getBoard().getFen();

        String originalBoard = fen.split(" ")[0];
        String exportedBoard = exported.split(" ")[0];
        assertEquals(originalBoard, exportedBoard);
    }

    @Test
    public void fenRoundTrip_promotionPosition_boardMatches() {
        TestDouble game = TestDouble.fromFen(FEN_PROMOTION);
        String exported = game.getBoard().getFen();
        String originalBoard = FEN_PROMOTION.split(" ")[0];
        String exportedBoard = exported.split(" ")[0];
        assertEquals(originalBoard, exportedBoard);
    }

    // ====================================================================
    // Castling FEN token
    // ====================================================================

    @Test
    public void fenCastling_startPosition_allCastlingAvailable() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        String castling = FenParser.getCastling(game.getBoard());
        assertTrue("KQkq or subset expected",
                castling.contains("K") && castling.contains("Q")
                        && castling.contains("k") && castling.contains("q"));
    }

    @Test
    public void fenCastling_kingsOnly_noCastling() {
        TestDouble game = TestDouble.fromFen(FEN_KINGS_ONLY);
        String castling = FenParser.getCastling(game.getBoard());
        assertEquals("No castling available", "-", castling);
    }

    // ====================================================================
    // En-passant FEN token
    // ====================================================================

    @Test
    public void fenPassing_noPassing_returnsDash() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        String passing = FenParser.getPassing(game.getBoard());
        assertEquals("-", passing);
    }

    @Test
    public void fenPassing_enPassantAvailable_returnsSquare() {
        // FEN_EN_PASSANT has pawn on e5 with black pawn on d5 having just moved
        TestDouble game = TestDouble.fromFen(FEN_EN_PASSANT);
        String passing = FenParser.getPassing(game.getBoard());
        // en-passant target square is d6
        assertFalse("En passant square should not be '-'", "-".equals(passing));
    }

    // ====================================================================
    // Countdown / move count tokens
    // ====================================================================

    @Test
    public void fenCountdown_startPosition_returnsZero() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        String countdown = FenParser.getCountdown(game.getBoard());
        assertEquals("0", countdown);
    }

    @Test
    public void fenMoveCount_startPosition_returnsOne() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        String moveCount = FenParser.getMoveCount(game.getBoard());
        assertEquals("1", moveCount);
    }

    // ====================================================================
    // getPlayer token
    // ====================================================================

    @Test
    public void getPlayer_whiteTurn_returnsW() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        assertEquals("w", FenParser.getPlayer(game.getBoard()));
    }
}
