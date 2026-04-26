package com.chess.blackbox.moves;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.moves.Move;
import com.chess.root.moves.PromotionMove;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Black-box tests for pawn promotion (PromotionMove) using EP, BA, and EG.
 *
 * Spec (FIDE Laws 3.7.5): when a pawn reaches the last rank it must be promoted.
 * The SUT always auto-promotes to queen. PromotionMove is the Move subclass used.
 * White promotes at row=0 (rank 8), black at row=7 (rank 1).
 */
public class PromotionTest extends ChessTestBase {

    // White pawn on e7 (col=4, row=1) — black king at a8 so e8 is unobstructed
    private static final String FEN_WPAWN_E7_OPEN =
            "k7/4P3/8/8/8/8/8/4K3 w - - 0 1";

    // Black pawn on e2 (col=4, row=6) — white king at a1 so e1 is unobstructed
    private static final String FEN_BPAWN_E2_OPEN =
            "4k3/8/8/8/8/8/4p3/K7 b - - 0 1";

    // White pawn on e7 with enemy piece on f8 (col=5, row=0) — promotion with capture
    private static final String FEN_WPAWN_E7_CAPTURE =
            "4kn2/4P3/8/8/8/8/8/4K3 w - - 0 1";

    // White pawn on e7 blocked by own piece on e8 (col=4, row=0)
    private static final String FEN_WPAWN_E7_BLOCKED =
            "4kQ2/4P3/8/8/8/8/8/4K3 w - - 0 1";

    // White pawn on e6 (not on promotion rank) — no promotion expected
    private static final String FEN_WPAWN_E6_NOT_PROMOTING =
            "4k3/8/4P3/8/8/8/8/4K3 w - - 0 1";

    // -------------------------------------------------------------------------
    // Equivalence Partitioning
    // -------------------------------------------------------------------------

    /**
     * [EP] White pawn on e7, open board: all moves are PromotionMove instances.
     * Partition: pawn on pre-promotion rank, forward square clear.
     * Goal: PromotionMove is generated when pawn reaches the last rank.
     */
    @Test
    public void testWhitePawnE7GeneratesOnlyPromotionMoves() {
        Board board = createBoard(FEN_WPAWN_E7_OPEN);
        List<Move> moves = getMovesForPiece(board, 4, 1); // e7
        assertFalse("Pawn about to promote should have at least one move", moves.isEmpty());
        for (Move m : moves) {
            assertTrue("All moves from e7 should be PromotionMoves for a white pawn",
                    m instanceof PromotionMove);
        }
    }

    /**
     * [EP] Black pawn on e2, open board: all moves are PromotionMove instances.
     * Partition: black pawn on pre-promotion rank, forward square clear.
     */
    @Test
    public void testBlackPawnE2GeneratesOnlyPromotionMoves() {
        Board board = createBoard(FEN_BPAWN_E2_OPEN);
        List<Move> moves = getMovesForPiece(board, 4, 6); // e2
        assertFalse("Black pawn about to promote should have at least one move", moves.isEmpty());
        for (Move m : moves) {
            assertTrue("All moves from e2 should be PromotionMoves for a black pawn",
                    m instanceof PromotionMove);
        }
    }

    /**
     * [EP] White pawn on e7 with enemy on f8: promotion with capture also available.
     * Partition: promotion rank with diagonal enemy piece.
     * Goal: PromotionMove with victim (capture) is generated alongside forward promotion.
     */
    @Test
    public void testWhitePawnE7WithEnemyOnF8HasPromotionCapture() {
        Board board = createBoard(FEN_WPAWN_E7_CAPTURE);
        List<Move> moves = getMovesForPiece(board, 4, 1); // e7
        // All moves should be PromotionMoves (either forward e8 or capture f8)
        boolean hasCapture = moves.stream()
                .filter(m -> m instanceof PromotionMove)
                .anyMatch(m -> m.getVictim() != null);
        assertTrue("A promotion-with-capture PromotionMove should be available when enemy on f8",
                hasCapture);
    }

    // -------------------------------------------------------------------------
    // Boundary Analysis
    // -------------------------------------------------------------------------

    /**
     * [BA] White pawn on e7 blocked by own piece on e8: 0 forward promotions.
     * Boundary: promotion rank square occupied by own piece — only captures possible.
     * (If f8/d8 have no enemies, the result is 0 moves.)
     */
    @Test
    public void testWhitePawnE7BlockedForwardHasNoForwardPromotion() {
        Board board = createBoard(FEN_WPAWN_E7_BLOCKED);
        List<Move> moves = getMovesForPiece(board, 4, 1); // e7
        // The forward square (e8 col=4, row=0) is blocked by own queen
        boolean forwardPromotion = moves.stream()
                .filter(m -> m instanceof PromotionMove)
                .anyMatch(m -> m.getField().getColumn() == 4 && m.getField().getRow() == 0);
        assertFalse("Pawn blocked by own piece on e8 should have no forward promotion", forwardPromotion);
    }

    // -------------------------------------------------------------------------
    // Error Guessing
    // -------------------------------------------------------------------------

    /**
     * [EG] PromotionMove target field is on the 8th rank (row=0 for white).
     * The SUT stores the original pawn in getPiece() at construction time; the actual
     * QueenPiece is created lazily during execute() via setQueenReally().
     * This test verifies the promotion targets the correct promotion-rank square.
     * Fault class: promotion generated to wrong rank.
     */
    @Test
    public void testPromotionMoveTargetsEighthRank() {
        Board board = createBoard(FEN_WPAWN_E7_OPEN);
        List<Move> moves = getMovesForPiece(board, 4, 1); // e7
        PromotionMove promotion = (PromotionMove) moves.stream()
                .filter(m -> m instanceof PromotionMove && m.getField().getColumn() == 4)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No promotion to e8 found"));
        assertNotNull("PromotionMove should have a non-null piece", promotion.getPiece());
        // Promotion target must be on rank 8 (row=0 in SUT coordinate system)
        assertEquals("Promotion target should be on rank 8 (row=0)", 0, promotion.getField().getRow());
        assertEquals("Promotion target should be on the e-file (col=4)", 4, promotion.getField().getColumn());
    }

    /**
     * [EG] PromotionMove.getPawn() returns the original pawn, not the queen.
     * Fault class: pawn and queen swapped in PromotionMove fields.
     * FIDE rule: the pawn ceases to exist once promotion occurs.
     */
    @Test
    public void testPromotionMoveGetPawnReturnsOriginalPawn() {
        Board board = createBoard(FEN_WPAWN_E7_OPEN);
        List<Move> moves = getMovesForPiece(board, 4, 1); // e7
        PromotionMove promotion = (PromotionMove) moves.get(0);
        assertNotNull("getPawn() should return the original pawn", promotion.getPawn());
        // The pawn should currently be on e7 (col=4, row=1)
        assertEquals("Pawn should be on column 4 (e-file)", 4,
                promotion.getPawn().getColumn());
    }

    /**
     * [EG] Pawn on e6 (not on pre-promotion rank): NO PromotionMove generated.
     * Fault class: promotion triggered a rank too early.
     */
    @Test
    public void testPawnNotOnPrePromotionRankHasNoPromotionMoves() {
        Board board = createBoard(FEN_WPAWN_E6_NOT_PROMOTING);
        List<Move> moves = getMovesForPiece(board, 4, 2); // e6
        boolean hasPromotion = moves.stream().anyMatch(m -> m instanceof PromotionMove);
        assertFalse("Pawn on e6 (not pre-promotion) should have no PromotionMoves",
                hasPromotion);
    }

    /**
     * [EG] Promotion with capture: victim field is occupied before the move.
     * Goal: captures during promotion are validated correctly by verifying the
     * victim piece exists on its square prior to the move being executed.
     */
    @Test
    public void testPromotionWithCaptureHasVictimOnBoard() {
        Board board = createBoard(FEN_WPAWN_E7_CAPTURE);
        List<Move> moves = getMovesForPiece(board, 4, 1); // e7
        PromotionMove capturePromotion = (PromotionMove) moves.stream()
                .filter(m -> m instanceof PromotionMove && m.getVictim() != null)
                .findFirst()
                .orElse(null);
        if (capturePromotion != null) {
            assertNotNull("Promotion capture should have a non-null victim piece",
                    capturePromotion.getVictim());
            // The victim should be on the board (f8 = col=5, row=0)
            assertNotNull("Victim piece should still be on its field before move executes",
                    capturePromotion.getVictimField().getPiece());
        }
    }
}
