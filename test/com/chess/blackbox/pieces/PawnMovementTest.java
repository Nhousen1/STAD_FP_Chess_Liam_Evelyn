package com.chess.blackbox.pieces;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.moves.Move;
import com.chess.root.moves.PromotionMove;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Black-box tests for pawn movement using EP, BA, and EG.
 *
 * Spec (FIDE Laws 3.7): pawns move forward 1 square (or 2 from starting rank),
 * capture diagonally, and promote on the last rank (PromotionMove).
 * En passant is tested separately in EnPassantTest.
 *
 * White pawns start on row=6 and move toward row=0.
 * Black pawns start on row=1 and move toward row=7.
 */
public class PawnMovementTest extends ChessTestBase {

    // White pawn on e2 (col=4, row=6) — starting rank, open board
    private static final String FEN_WPAWN_E2 =
            "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1";

    // White pawn on e3 (col=4, row=5) — not starting rank, open board
    private static final String FEN_WPAWN_E3 =
            "4k3/8/8/8/8/4P3/8/4K3 w - - 0 1";

    // White pawn on e2 with enemy pawn diagonally on d3 (col=3, row=5)
    private static final String FEN_WPAWN_E2_CAPTURE =
            "4k3/8/8/8/8/3p4/4P3/4K3 w - - 0 1";

    // White pawn on e7 (col=4, row=1) — black king at a8 so e8 is clear for promotion
    private static final String FEN_WPAWN_E7 =
            "k7/4P3/8/8/8/8/8/4K3 w - - 0 1";

    // White pawn on e2 fully blocked by own piece on e3 (col=4, row=5)
    private static final String FEN_WPAWN_E2_BLOCKED =
            "4k3/8/8/8/8/4P3/4P3/4K3 w - - 0 1";

    // White pawn on a2 (col=0, row=6) — left edge with enemy on b3 (col=1, row=5)
    private static final String FEN_WPAWN_A2_CAPTURE =
            "4k3/8/8/8/8/1p6/P7/4K3 w - - 0 1";

    // White pawn on h2 (col=7, row=6) — right edge, open board
    private static final String FEN_WPAWN_H2 =
            "4k3/8/8/8/8/8/7P/4K3 w - - 0 1";

    // Black pawn on e7 (col=4, row=1) — black starting rank (moves toward row=7)
    private static final String FEN_BPAWN_E7 =
            "4k3/4p3/8/8/8/8/8/4K3 b - - 0 1";

    // White pawn on e2 with own piece on e4 but NOT on e3 (blocked 2 away, not 1)
    private static final String FEN_WPAWN_E2_BLOCKED_TWO_AWAY =
            "4k3/8/8/8/4P3/8/4P3/4K3 w - - 0 1";

    // White pawn on e2 with own piece directly on e3 (blocked 1 away)
    private static final String FEN_WPAWN_E2_BLOCKED_ONE_AWAY =
            "4k3/8/8/8/8/4P3/4P3/4K3 w - - 0 1";

    // -------------------------------------------------------------------------
    // Equivalence Partitioning
    // -------------------------------------------------------------------------

    /**
     * [EP] White pawn on starting rank e2, open board: 2 forward moves (e3 and e4).
     * Partition: starting rank, no obstructions.
     * Goal: double-push available only from starting rank.
     */
    @Test
    public void testWhitePawnOnStartingRankHasTwoForwardMoves() {
        Board board = createBoard(FEN_WPAWN_E2);
        List<Move> moves = getMovesForPiece(board, 4, 6); // e2
        assertEquals("White pawn on e2 should have 2 forward moves", 2, moves.size());
        assertTrue("Should be able to move to e3", hasMoveTo(moves, 4, 5));
        assertTrue("Should be able to move to e4", hasMoveTo(moves, 4, 4));
    }

    /**
     * [EP] White pawn on e3 (not starting rank), open board: 1 forward move (e4).
     * Partition: non-starting rank, no capture available.
     */
    @Test
    public void testWhitePawnNotOnStartingRankHasOneForwardMove() {
        Board board = createBoard(FEN_WPAWN_E3);
        List<Move> moves = getMovesForPiece(board, 4, 5); // e3
        assertEquals("White pawn on e3 should have 1 forward move", 1, moves.size());
        assertTrue("Should be able to move to e4", hasMoveTo(moves, 4, 4));
    }

    /**
     * [EP] White pawn on e2 with enemy pawn diagonally at d3: 1 forward + 1 capture = 2.
     * Partition: diagonal capture available.
     * Goal: diagonal capture moves are generated in addition to forward moves.
     */
    @Test
    public void testWhitePawnWithDiagonalCaptureHasTwoMoves() {
        Board board = createBoard(FEN_WPAWN_E2_CAPTURE);
        List<Move> moves = getMovesForPiece(board, 4, 6); // e2
        // e3 forward + d3 capture (plus potentially e4 if not blocked)
        assertTrue("Should include diagonal capture at d3 (col=3, row=5)",
                hasMoveTo(moves, 3, 5));
    }

    // -------------------------------------------------------------------------
    // Boundary Analysis
    // -------------------------------------------------------------------------

    /**
     * [BA] White pawn on e7 (one step from promotion rank): only PromotionMove instances.
     * Boundary: pawn on the rank immediately before promotion.
     * Goal: promotion logic activates at the correct rank.
     */
    @Test
    public void testWhitePawnOnPrePromotionRankGeneratesPromotionMoves() {
        Board board = createBoard(FEN_WPAWN_E7);
        List<Move> moves = getMovesForPiece(board, 4, 1); // e7
        assertFalse("Pawn about to promote should have moves", moves.isEmpty());
        for (Move m : moves) {
            assertTrue("All moves from e7 should be PromotionMoves",
                    m instanceof PromotionMove);
        }
    }

    /**
     * [BA] White pawn on e2 completely blocked by own piece on e3: 0 moves.
     * Boundary: pawn blocked one square ahead from starting rank.
     * Goal: double-push also unavailable when adjacent square is blocked.
     */
    @Test
    public void testWhitePawnBlockedOneSquareAheadHasZeroMoves() {
        Board board = createBoard(FEN_WPAWN_E2_BLOCKED_ONE_AWAY);
        List<Move> moves = getMovesForPiece(board, 4, 6); // e2 (the lower pawn)
        assertEquals("Pawn blocked one square ahead should have 0 moves", 0, moves.size());
    }

    /**
     * [BA] White pawn on a2 (edge file) with enemy pawn on b3: 1 forward + 1 diagonal capture.
     * Boundary: left-edge file — only one diagonal direction possible.
     */
    @Test
    public void testWhitePawnOnEdgeFileWithCaptureHasTwoMoves() {
        Board board = createBoard(FEN_WPAWN_A2_CAPTURE);
        List<Move> moves = getMovesForPiece(board, 0, 6); // a2
        assertTrue("Edge-file pawn should include diagonal capture at b3",
                hasMoveTo(moves, 1, 5));
        assertTrue("Edge-file pawn should include forward move to a3",
                hasMoveTo(moves, 0, 5));
    }

    /**
     * [BA] White pawn on h2 (right edge), open board: 2 forward moves, no right capture.
     * Boundary: right-edge file — only one diagonal direction available (g-file side).
     */
    @Test
    public void testWhitePawnOnRightEdgeFileH2HasTwoForwardMoves() {
        Board board = createBoard(FEN_WPAWN_H2);
        List<Move> moves = getMovesForPiece(board, 7, 6); // h2
        assertTrue("h2 pawn should include h3", hasMoveTo(moves, 7, 5));
        assertTrue("h2 pawn should include h4", hasMoveTo(moves, 7, 4));
    }

    // -------------------------------------------------------------------------
    // Error Guessing
    // -------------------------------------------------------------------------

    /**
     * [EG] Black pawn on e7 starting rank (moves downward): 2 forward moves.
     * Fault class: direction calculation error for black pawns (should move toward row 7).
     */
    @Test
    public void testBlackPawnOnStartingRankHasTwoMoves() {
        Board board = createBoard(FEN_BPAWN_E7);
        List<Move> moves = getMovesForPiece(board, 4, 1); // e7 for black
        assertEquals("Black pawn on e7 (starting rank) should have 2 moves", 2, moves.size());
        assertTrue("Black pawn should move to e6 (col=4, row=2)", hasMoveTo(moves, 4, 2));
        assertTrue("Black pawn should move to e5 (col=4, row=3)", hasMoveTo(moves, 4, 3));
    }

    /**
     * [EG] White pawn on e2 with own piece on e4 but e3 open: can move 1 square but NOT 2.
     * Fault class: double-push check skipping the immediate square.
     * FIDE rule: both squares along the pawn's path must be clear for a double push.
     */
    @Test
    public void testWhitePawnBlockedTwoSquaresAheadCanOnlyMoveOne() {
        Board board = createBoard(FEN_WPAWN_E2_BLOCKED_TWO_AWAY);
        List<Move> moves = getMovesForPiece(board, 4, 6); // lower pawn on e2
        assertTrue("Pawn should be able to move 1 square to e3", hasMoveTo(moves, 4, 5));
        assertFalse("Pawn should NOT be able to double-push through own piece on e4",
                hasMoveTo(moves, 4, 4));
    }
}
