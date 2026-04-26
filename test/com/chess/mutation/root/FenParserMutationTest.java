package com.chess.mutation.root;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.FenParser;
import com.chess.root.pieces.Piece;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Mutation-targeted tests for com.chess.root.FenParser.
 *
 * Surviving mutants targeted:
 *  - parseBoard(): col < 8 && row < 8 boundary conditions; digit parsing
 *  - parsePassing(): row == 3 and row == 6 equality checks
 *  - getBoard(): counter != 0 condition; j == 7 condition
 *  - getPlayer(): return "b"/"w" replaced with ""
 *  - getCastling(): empty-string/"-" conditions
 *  - getPassing(): row == 3 vs row == 6 in export
 *  - getCountdown(): division by 2 changed
 *  - parseInteger(): return 0 on invalid input
 */
public class FenParserMutationTest extends ChessTestBase {

    /** FEN with black pawn on d5 (just moved there), white can en passant via d6. */
    private static final String FEN_EP_D6 =
            "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3";

    /** FEN with white pawn on e4 (just moved there), black can en passant via e3. */
    private static final String FEN_EP_E3 =
            "rnbqkbnr/pppp1ppp/8/8/3pP3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 3";

    // =========================================================================
    // parseBoard() — boundary conditions
    // =========================================================================

    @Test
    public void testParseBoardBlackRookA8() {
        // col=0 (a), row=0 (rank 8) → 'r'
        String[][] board = FenParser.parseBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertEquals("r", board[0][0]);
    }

    @Test
    public void testParseBoardBlackKingE8() {
        // col=4 (e), row=0 (rank 8) → 'k'
        String[][] board = FenParser.parseBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertEquals("k", board[4][0]);
    }

    @Test
    public void testParseBoardWhiteKingE1() {
        // col=4 (e), row=7 (rank 1) → 'K'
        String[][] board = FenParser.parseBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertEquals("K", board[4][7]);
    }

    @Test
    public void testParseBoardWhiteRookH1() {
        // col=7 (h), row=7 (rank 1) → 'R'
        String[][] board = FenParser.parseBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertEquals("R", board[7][7]);
    }

    @Test
    public void testParseBoardEmptyRanksAreNull() {
        String[][] board = FenParser.parseBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        // Ranks 3–6 (rows 2–5) are empty
        for (int col = 0; col < 8; col++) {
            assertNull("Row 2 col " + col + " should be null", board[col][2]);
            assertNull("Row 3 col " + col + " should be null", board[col][3]);
        }
    }

    @Test
    public void testParseBoardBlackPawnB7() {
        // col=1 (b), row=1 (rank 7) → 'p'
        String[][] board = FenParser.parseBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertEquals("p", board[1][1]);
    }

    @Test
    public void testParseBoardWhitePawnH2() {
        // col=7 (h), row=6 (rank 2) → 'P'
        String[][] board = FenParser.parseBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertEquals("P", board[7][6]);
    }

    @Test
    public void testParseBoardAllWhitePawnsRow6() {
        String[][] board = FenParser.parseBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        for (int col = 0; col < 8; col++) {
            assertEquals("White pawn at col " + col + " rank 2", "P", board[col][6]);
        }
    }

    @Test
    public void testParseBoardAllBlackPawnsRow1() {
        String[][] board = FenParser.parseBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        for (int col = 0; col < 8; col++) {
            assertEquals("Black pawn at col " + col + " rank 7", "p", board[col][1]);
        }
    }

    @Test
    public void testParseBoardSinglePieceInCenter() {
        // Single queen on d4: col=3, row=4 → 'Q'
        String[][] board = FenParser.parseBoard("8/8/8/8/3Q4/8/8/8");
        assertEquals("Q", board[3][4]);
        assertNull("a4 should be null", board[0][4]);
        assertNull("h4 should be null", board[7][4]);
    }

    // =========================================================================
    // parseInteger()
    // =========================================================================

    @Test
    public void testParseIntegerValidPositive() {
        assertEquals(42, FenParser.parseInteger("42"));
    }

    @Test
    public void testParseIntegerZero() {
        assertEquals(0, FenParser.parseInteger("0"));
    }

    @Test
    public void testParseIntegerLargeValue() {
        assertEquals(100, FenParser.parseInteger("100"));
    }

    @Test
    public void testParseIntegerInvalidReturnsZero() {
        assertEquals(0, FenParser.parseInteger("abc"));
    }

    // =========================================================================
    // getBoard() — kills counter/boundary condition mutants
    // =========================================================================

    @Test
    public void testGetBoardStartingPositionRoundTrip() {
        Board board = createBoard(FEN_START);
        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR",
                FenParser.getBoard(board));
    }

    @Test
    public void testGetBoardKingsOnly() {
        Board board = createBoard("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        assertEquals("4k3/8/8/8/8/8/8/4K3", FenParser.getBoard(board));
    }

    @Test
    public void testGetBoardSinglePieceRoundTrip() {
        Board board = createBoard("8/4P3/8/8/8/8/8/k6K w - - 0 1");
        assertEquals("8/4P3/8/8/8/8/8/k6K", FenParser.getBoard(board));
    }

    @Test
    public void testGetBoardTrailingEmptySquares() {
        // Rook on a1, kings elsewhere: trailing empty squares must be counted
        Board board = createBoard("4k3/8/8/8/8/8/8/R3K3 w Q - 0 1");
        String fen = FenParser.getBoard(board);
        assertTrue("Board FEN should contain rook", fen.contains("R"));
        assertTrue("Board FEN should contain empty counts", fen.contains("3") || fen.contains("4"));
    }

    @Test
    public void testGetBoardFullBoardMatchesStartFen() {
        Board board = createBoard(FEN_START);
        String[] parts = FEN_START.split(" ");
        assertEquals(parts[0], FenParser.getBoard(board));
    }

    // =========================================================================
    // getPlayer() — kills replaced-return-with-"" mutant
    // =========================================================================

    @Test
    public void testGetPlayerWhiteToMove() {
        Board board = createBoard("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        assertEquals("w", FenParser.getPlayer(board));
    }

    @Test
    public void testGetPlayerBlackToMove() {
        Board board = createBoard("4k3/8/8/8/8/8/8/4K3 b - - 0 1");
        assertEquals("b", FenParser.getPlayer(board));
    }

    @Test
    public void testGetPlayerNotEmpty() {
        Board board = createBoard(FEN_START);
        assertFalse("getPlayer() must not return empty string", FenParser.getPlayer(board).isEmpty());
    }

    @Test
    public void testGetPlayerWhiteAndBlackDiffer() {
        Board w = createBoard("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        Board b = createBoard("4k3/8/8/8/8/8/8/4K3 b - - 0 1");
        assertNotEquals(FenParser.getPlayer(w), FenParser.getPlayer(b));
    }

    // =========================================================================
    // getCastling() — kills empty-string / "-" condition mutants
    // =========================================================================

    @Test
    public void testGetCastlingNoneIsHyphen() {
        Board board = createBoard("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        assertEquals("-", FenParser.getCastling(board));
    }

    @Test
    public void testGetCastlingFullRightsNotHyphen() {
        Board board = createBoard("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        String castling = FenParser.getCastling(board);
        assertFalse("Castling string should not be empty", castling.isEmpty());
        assertNotEquals("-", castling);
    }

    @Test
    public void testGetCastlingContainsWhiteKingside() {
        Board board = createBoard("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        assertTrue("Should contain K for white kingside", FenParser.getCastling(board).contains("K"));
    }

    // =========================================================================
    // getPassing() — kills row == 3 / row == 6 condition mutants
    // =========================================================================

    @Test
    public void testGetPassingNoneIsHyphen() {
        Board board = createBoard(FEN_START);
        assertEquals("-", FenParser.getPassing(board));
    }

    @Test
    public void testGetPassingBlackPawnOnD5ReturnsD6() {
        // Black pawn just moved to d5 (row 3 internal); en passant target = d6
        Board board = createBoard(FEN_EP_D6);
        assertEquals("d6", FenParser.getPassing(board));
    }

    @Test
    public void testGetPassingWhitePawnOnE4ReturnsE3() {
        // White pawn just moved to e4 (row 4 internal); en passant target = e3
        Board board = createBoard(FEN_EP_E3);
        assertEquals("e3", FenParser.getPassing(board));
    }

    // =========================================================================
    // getCountdown() — kills division mutant (/ 2 changed)
    // =========================================================================

    @Test
    public void testGetCountdownZeroAtStart() {
        Board board = createBoard(FEN_START); // half-move clock = 0
        assertEquals("0", FenParser.getCountdown(board));
    }

    @Test
    public void testGetCountdownHalfMoveClockRoundTrip() {
        // half-move clock = 49 in FEN → countdown = 98 in board → getCountdown = "49"
        Board board = createBoard("4k3/8/8/8/8/8/R7/4K3 w - - 49 25");
        assertEquals("49", FenParser.getCountdown(board));
    }

    @Test
    public void testGetCountdownNotDoubled() {
        // Verify that getCountdown divides by 2 (not returns raw countdown value)
        Board board = createBoard("4k3/8/8/8/8/8/R7/4K3 w - - 10 5");
        assertEquals("10", FenParser.getCountdown(board));
    }

    // =========================================================================
    // parsePassing() — kills row == 3 and row == 6 equality mutations
    // =========================================================================

    @Test
    public void testParsePassingNullWhenHyphen() {
        Board board = createBoard(FEN_START);
        assertNull(FenParser.parsePassing("-", board.getPieces(true), board.getPieces(false)));
    }

    @Test
    public void testParsePassingNullWhenNull() {
        Board board = createBoard(FEN_START);
        assertNull(FenParser.parsePassing(null, board.getPieces(true), board.getPieces(false)));
    }

    @Test
    public void testParsePassingRow6FindsBlackPawn() {
        // "d6" → row=6 → look in blackPieces for pawn at d5
        Board board = createBoard(FEN_EP_D6);
        Piece pawn = FenParser.parsePassing("d6", board.getPieces(true), board.getPieces(false));
        assertNotNull("Should find a pawn for 'd6' en passant square", pawn);
        assertTrue("En passant piece for 'd6' should be black", pawn.isBlack());
    }

    @Test
    public void testParsePassingRow3FindsWhitePawn() {
        // "e3" → row=3 → look in whitePieces for pawn at e4 (row+1=4)
        Board board = createBoard(FEN_EP_E3);
        Piece pawn = FenParser.parsePassing("e3", board.getPieces(true), board.getPieces(false));
        assertNotNull("Should find a pawn for 'e3' en passant square", pawn);
        assertFalse("En passant piece for 'e3' should be white", pawn.isBlack());
    }

    @Test
    public void testParsePassingRow6CorrectColumn() {
        Board board = createBoard(FEN_EP_D6);
        Piece pawn = FenParser.parsePassing("d6", board.getPieces(true), board.getPieces(false));
        assertNotNull(pawn);
        assertEquals("d5", pawn.getField().getNotation());
    }

    @Test
    public void testParsePassingRow3CorrectColumn() {
        Board board = createBoard(FEN_EP_E3);
        Piece pawn = FenParser.parsePassing("e3", board.getPieces(true), board.getPieces(false));
        assertNotNull(pawn);
        assertEquals("e4", pawn.getField().getNotation());
    }

    // =========================================================================
    // build() — integration test for full FEN export
    // =========================================================================

    @Test
    public void testBuildStartingPositionHasAllParts() {
        Board board = createBoard(FEN_START);
        String fen = FenParser.build(board);
        String[] parts = fen.split(" ");
        assertEquals("FEN must have 6 parts", 6, parts.length);
    }

    @Test
    public void testBuildStartingPositionBoardSection() {
        Board board = createBoard(FEN_START);
        String fen = FenParser.build(board);
        assertTrue("FEN board section must be present", fen.startsWith("rnbqkbnr"));
    }

    @Test
    public void testBuildStartingPositionPlayerSection() {
        Board board = createBoard(FEN_START);
        String[] parts = FenParser.build(board).split(" ");
        assertEquals("Player section must be 'w'", "w", parts[1]);
    }

    // =========================================================================
    // parseCastling() line 62 VoidMethodCallMutator — kills removal of black king call
    // If board.getKing(true).initializeFenCastling(cas) is removed, black rooks
    // at their home squares (a8, h8) would incorrectly start as not-moved when
    // the FEN castling string indicates they should be marked moved.
    // =========================================================================

    @Test
    public void testParseCastlingKOnlyMarksBlackKingsideRookMoved() {
        // FEN "K" means only white kingside rights remain.
        // Black rooks at a8 and h8 should BOTH be marked as moved (no black rights).
        Board board = createBoard("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w K - 0 1");
        Piece blackKsideRook  = board.getField(7, 0).getPiece(); // h8
        Piece blackQsideRook  = board.getField(0, 0).getPiece(); // a8
        assertTrue("Black kingside rook (h8) must be marked moved when FEN='K'",
                blackKsideRook.wasMoved());
        assertTrue("Black queenside rook (a8) must be marked moved when FEN='K'",
                blackQsideRook.wasMoved());
    }

    @Test
    public void testParseCastlingQOnlyMarksBlackRooksMoved() {
        // FEN "Q" means only white queenside rights remain.
        // Black rooks should both be marked moved.
        Board board = createBoard("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w Q - 0 1");
        Piece blackKsideRook  = board.getField(7, 0).getPiece();
        Piece blackQsideRook  = board.getField(0, 0).getPiece();
        assertTrue("Black kingside rook must be moved when FEN='Q'", blackKsideRook.wasMoved());
        assertTrue("Black queenside rook must be moved when FEN='Q'", blackQsideRook.wasMoved());
    }

    @Test
    public void testParseCastlingKkMarksOnlyBlackQueensideRookMoved() {
        // FEN "Kk" — white kingside and black kingside remain.
        // Black queenside rook (a8) should be marked moved; black kingside (h8) should NOT be.
        Board board = createBoard("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w Kk - 0 1");
        Piece blackKsideRook  = board.getField(7, 0).getPiece(); // h8
        Piece blackQsideRook  = board.getField(0, 0).getPiece(); // a8
        assertFalse("Black kingside rook (h8) must NOT be moved when 'k' is in FEN castling",
                blackKsideRook.wasMoved());
        assertTrue("Black queenside rook (a8) must be moved when 'q' is not in FEN castling",
                blackQsideRook.wasMoved());
    }

    // =========================================================================
    // getPassingPiece() line 88 EQUAL_IF — kills instanceof PawnPiece check removal
    // If instanceof is removed, a non-pawn piece at the lookup position would be returned.
    // =========================================================================

    @Test
    public void testParsePassingReturnsNullWhenNoPawnAtExpectedPosition() {
        // Position: white queen at e4 (not a pawn), en passant target claims "e3"
        // parsePassing("e3", ...) should look for a white PAWN at e4, find none → null.
        // With mutant removing instanceof check, the queen would be returned instead.
        Board board = createBoard("4k3/8/8/8/4Q3/8/8/4K3 w - - 0 1");
        List<Piece> blackPieces = board.getPieces(true);
        List<Piece> whitePieces = board.getPieces(false);
        Piece result = FenParser.parsePassing("e3", blackPieces, whitePieces);
        assertNull("parsePassing should return null when no pawn is at e4 (only a queen)", result);
    }

    @Test
    public void testParsePassingReturnsNullForRookAtPassingPosition() {
        // White rook at d4, claimed ep target "d3" → should return null (not a pawn)
        Board board = createBoard("4k3/8/8/8/3R4/8/8/4K3 w - - 0 1");
        Piece result = FenParser.parsePassing("d3", board.getPieces(true), board.getPieces(false));
        assertNull("parsePassing should return null when piece at d4 is a rook, not a pawn", result);
    }
}
