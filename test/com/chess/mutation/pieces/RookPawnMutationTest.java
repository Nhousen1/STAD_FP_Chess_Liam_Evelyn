package com.chess.mutation.pieces;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.moves.Move;
import com.chess.root.moves.PassingMove;
import com.chess.root.moves.PawnRunMove;
import com.chess.root.moves.PromotionMove;
import com.chess.root.pieces.PawnPiece;
import com.chess.root.pieces.RookPiece;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Mutation-targeted tests for RookPiece and PawnPiece.
 *
 * RookPiece surviving mutants:
 *  - <init>(): position-based moved flag (home squares a1/h1/a8/h8 check)
 *  - getMoves(): boundary condition (i < 8), direction math (col += / row +=)
 *  - unmove(): condition (movecounter == 0) — also killed by multi-move test
 *
 * PawnPiece surviving mutants:
 *  - <init>(): color-based position table assignment
 *  - getMoves(): boundary conditions L70, L85, L95, L99; math L67/L96/L118/L123;
 *               equality checks L85, L104, L122, L124, L127
 */
public class RookPawnMutationTest extends ChessTestBase {

    // =========================================================================
    // RookPiece.<init>() — kills position-based moved flag mutants
    // moved = color ? !(a8 || h8) : !(a1 || h1)
    // =========================================================================

    @Test
    public void testWhiteRookAtA1StartsNotMoved() {
        Board board = createBoard("4k3/8/8/8/8/8/8/R3K3 w Q - 0 1");
        RookPiece rook = (RookPiece) board.getField(0, 7).getPiece();
        assertFalse("White rook at a1 should start with wasMoved()==false", rook.wasMoved());
    }

    @Test
    public void testWhiteRookAtH1StartsNotMoved() {
        Board board = createBoard("4k3/8/8/8/8/8/8/4K2R w K - 0 1");
        RookPiece rook = (RookPiece) board.getField(7, 7).getPiece();
        assertFalse("White rook at h1 should start with wasMoved()==false", rook.wasMoved());
    }

    @Test
    public void testWhiteRookNotAtHomeStartsMoved() {
        // White rook at e4 — not a home square
        Board board = createBoard("4k3/8/8/8/4R3/8/8/4K3 w - - 0 1");
        RookPiece rook = (RookPiece) board.getField(4, 4).getPiece();
        assertTrue("White rook NOT at a1 or h1 should start with wasMoved()==true", rook.wasMoved());
    }

    @Test
    public void testWhiteRookAtC2StartsMoved() {
        // White rook at c2 (not a home square)
        Board board = createBoard("4k3/8/8/8/8/8/2R5/4K3 w - - 0 1");
        RookPiece rook = (RookPiece) board.getField(2, 6).getPiece();
        assertTrue("White rook at c2 should start with wasMoved()==true", rook.wasMoved());
    }

    @Test
    public void testBlackRookAtA8StartsNotMoved() {
        Board board = createBoard("r3k3/8/8/8/8/8/8/4K3 b q - 0 1");
        RookPiece rook = (RookPiece) board.getField(0, 0).getPiece();
        assertFalse("Black rook at a8 should start with wasMoved()==false", rook.wasMoved());
    }

    @Test
    public void testBlackRookAtH8StartsNotMoved() {
        Board board = createBoard("4k2r/8/8/8/8/8/8/4K3 b k - 0 1");
        RookPiece rook = (RookPiece) board.getField(7, 0).getPiece();
        assertFalse("Black rook at h8 should start with wasMoved()==false", rook.wasMoved());
    }

    @Test
    public void testBlackRookNotAtHomeStartsMoved() {
        // Black rook at e5 — not a home square
        Board board = createBoard("4k3/8/8/4r3/8/8/8/4K3 b - - 0 1");
        RookPiece rook = (RookPiece) board.getField(4, 3).getPiece();
        assertTrue("Black rook NOT at a8 or h8 should start with wasMoved()==true", rook.wasMoved());
    }

    @Test
    public void testBlackRookAtC7StartsMoved() {
        Board board = createBoard("4k3/2r5/8/8/8/8/8/4K3 b - - 0 1");
        RookPiece rook = (RookPiece) board.getField(2, 1).getPiece();
        assertTrue("Black rook at c7 should start with wasMoved()==true", rook.wasMoved());
    }

    // =========================================================================
    // RookPiece.unmove() — kills multi-move condition mutant
    // movecounter == 0 check: after 2 moved() + 1 unmove(), still moved
    // =========================================================================

    @Test
    public void testRookStillMovedAfterPartialUnmove() {
        Board board = createBoard("4k3/8/8/8/8/8/8/4K2R w K - 0 1");
        RookPiece rook = (RookPiece) board.getField(7, 7).getPiece();
        assertFalse("Precondition: rook at h1 starts not moved", rook.wasMoved());
        rook.moved();
        rook.moved();
        rook.unmove();
        assertTrue("After 2 moved() + 1 unmove(), rook should still be marked moved",
                rook.wasMoved());
        rook.unmove();
        assertFalse("After 2 moved() + 2 unmove(), rook should be not moved",
                rook.wasMoved());
    }

    // =========================================================================
    // RookPiece.getMoves() — kills boundary/direction math mutants
    // i < 8: test rook can reach last square in a direction
    // col/row += direction: test rook moves in correct direction
    // =========================================================================

    @Test
    public void testRookAtA1CanReachA8() {
        // White rook at a1 (col=0, row=7); verify it can reach a8 (col=0, row=0) — 7 squares up
        Board board = createBoard("4k3/8/8/8/8/8/8/R3K3 w Q - 0 1");
        List<Move> moves = getMovesForPiece(board, 0, 7);
        boolean canReachA8 = moves.stream().anyMatch(
                m -> m.getField().getColumn() == 0 && m.getField().getRow() == 0);
        assertTrue("Rook at a1 should be able to reach a8 (7 squares up)", canReachA8);
    }

    @Test
    public void testRookAtH1CanReachH8() {
        Board board = createBoard("4k3/8/8/8/8/8/8/4K2R w K - 0 1");
        List<Move> moves = getMovesForPiece(board, 7, 7);
        boolean canReachH8 = moves.stream().anyMatch(
                m -> m.getField().getColumn() == 7 && m.getField().getRow() == 0);
        assertTrue("Rook at h1 should be able to reach h8 (7 squares up)", canReachH8);
    }

    @Test
    public void testRookAtA8CanReachA1() {
        Board board = createBoard("r3k3/8/8/8/8/8/8/4K3 b q - 0 1");
        List<Move> moves = getMovesForPiece(board, 0, 0);
        boolean canReachA1 = moves.stream().anyMatch(
                m -> m.getField().getColumn() == 0 && m.getField().getRow() == 7);
        assertTrue("Rook at a8 should be able to reach a1 (7 squares down)", canReachA1);
    }

    @Test
    public void testRookMovesInFourDirections() {
        // Rook at d4 (col=3, row=4) — open board
        Board board = createBoard("4k3/8/8/8/3R4/8/8/4K3 w - - 0 1");
        List<Move> moves = getMovesForPiece(board, 3, 4);
        // Should be able to go left (a4), right (h4), up (d8), down (d1)
        boolean left  = moves.stream().anyMatch(m -> m.getField().getColumn() == 0 && m.getField().getRow() == 4);
        boolean right = moves.stream().anyMatch(m -> m.getField().getColumn() == 7 && m.getField().getRow() == 4);
        boolean up    = moves.stream().anyMatch(m -> m.getField().getColumn() == 3 && m.getField().getRow() == 0);
        boolean down  = moves.stream().anyMatch(m -> m.getField().getColumn() == 3 && m.getField().getRow() == 7);
        assertTrue("Rook should move left to a4", left);
        assertTrue("Rook should move right to h4", right);
        assertTrue("Rook should move up to d8", up);
        assertTrue("Rook should move down to d1", down);
    }

    @Test
    public void testRookMoveCountAtCenterIsMaximum() {
        // Rook at d4 on open board: 7 left + 4 right + 4 up + 3 down = 14 moves? Let me count:
        // Left: c4,b4,a4 = 3
        // Right: e4,f4,g4,h4 = 4
        // Up: d3,d2,d1 = 3 (wait — row increases downward in our system)
        // Actually row=4 is rank 4; row increases toward rank 1
        // Up (row decreasing): d3(row=5?), d2(row=6), d1(row=7) = no wait
        // row=4 means rank 4, so moving up means going to rank 5,6,7,8 which is row=3,2,1,0
        // So up: d5(row=3), d6(row=2), d7(row=1), d8(row=0) = 4
        // Down: d3(row=5), d2(row=6), d1(row=7) = 3
        // Left: c4(col=2), b4(col=1), a4(col=0) = 3
        // Right: e4(col=4), f4(col=5), g4(col=6), h4(col=7) = 4
        // Total = 4+3+3+4 = 14
        Board board = createBoard("4k3/8/8/8/3R4/8/8/4K3 w - - 0 1");
        List<Move> moves = getMovesForPiece(board, 3, 4);
        assertEquals("Rook at d4 on open board should have 14 moves", 14, moves.size());
    }

    // =========================================================================
    // PawnPiece.<init>() — kills color-based table assignment mutant
    // Condition: !color ? PAWN_UP : PAWN_DOWN
    // =========================================================================

    @Test
    public void testWhitePawnPositionalValueAtStartRow() {
        // White pawn at e2 (col=4, row=6) using PAWN_UP table
        Board board = createBoard("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        PawnPiece pawn = (PawnPiece) board.getField(4, 6).getPiece();
        int value = pawn.getValue();
        assertTrue("White pawn value should be positive (rating + table value)", value > 0);
    }

    @Test
    public void testBlackPawnPositionalValueAtStartRow() {
        // Black pawn at e7 (col=4, row=1) using PAWN_DOWN table
        Board board = createBoard("4k3/4p3/8/8/8/8/8/4K3 b - - 0 1");
        PawnPiece pawn = (PawnPiece) board.getField(4, 1).getPiece();
        int value = pawn.getValue();
        assertTrue("Black pawn value should be positive (rating + table value)", value > 0);
    }

    /**
     * Kills: PawnPiece.<init>() EQUAL_ELSE and EQUAL_IF mutants on line 42:
     *   !color ? PAWN_UP : PAWN_DOWN
     *
     * PAWN_UP[1][0] = 150 (white pawn at a7, approaching promotion).
     * PAWN_DOWN[1][0] = 5  (black pawn at a7, still near start).
     *
     * If EQUAL_ELSE uses PAWN_UP for black → black pawn at a7 would have posValue 150 (wrong).
     * If EQUAL_IF uses PAWN_DOWN for white → white pawn at a7 would have posValue 5 (wrong).
     * This test asserts white pawn value (at a7) > black pawn value (at a7).
     */
    @Test
    public void testWhitePawnAtA7HasHigherValueThanBlackPawnAtA7() {
        // White pawn at a7 (col=0, row=1): PAWN_UP[1][0] = 150 → high (approaching promotion)
        Board boardW = createBoard("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");
        PawnPiece whitePawn = (PawnPiece) boardW.getField(0, 1).getPiece();

        // Black pawn at a7 (col=0, row=1): PAWN_DOWN[1][0] = 5 → low (still near start)
        Board boardB = createBoard("4k3/p7/8/8/8/8/8/4K3 b - - 0 1");
        PawnPiece blackPawn = (PawnPiece) boardB.getField(0, 1).getPiece();

        // Both have the same raw rating; difference comes only from positional table
        // white: rating + 150, black: rating + 5 → white > black
        assertTrue("White pawn at a7 must have higher getValue() than black pawn at a7 " +
                "(PAWN_UP[1][0]=150 vs PAWN_DOWN[1][0]=5)",
                whitePawn.getValue() > blackPawn.getValue());
    }

    @Test
    public void testBlackPawnAtA2HasHigherValueThanWhitePawnAtA2() {
        // Black pawn at a2 (col=0, row=6): PAWN_DOWN[6][0] = 150 → high (approaching promotion)
        Board boardB = createBoard("4k3/8/8/8/8/8/p7/4K3 b - - 0 1");
        PawnPiece blackPawn = (PawnPiece) boardB.getField(0, 6).getPiece();

        // White pawn at a2 (col=0, row=6): PAWN_UP[6][0] = 5 → low (still near start)
        Board boardW = createBoard("4k3/8/8/8/8/8/P7/4K3 w - - 0 1");
        PawnPiece whitePawn = (PawnPiece) boardW.getField(0, 6).getPiece();

        // black: rating + 150 (PAWN_DOWN[6][0]=150), white: rating + 5 (PAWN_UP[6][0]=5)
        assertTrue("Black pawn at a2 must have higher getValue() than white pawn at a2 " +
                "(PAWN_DOWN[6][0]=150 vs PAWN_UP[6][0]=5)",
                blackPawn.getValue() > whitePawn.getValue());
    }

    // =========================================================================
    // PawnPiece.getMoves() — kills boundary/equality condition mutants
    //
    // L70: col >= 0 && col < 8 && row >= 0 && row < 8
    // L85: (!color && this.getRow() != 6) || (color && this.getRow() != 1) → break
    // L95: direction.up() == !color (hit direction filter)
    // L99: col >= 0 && col < 8 && row >= 0 && row < 8 (capture boundary)
    // L104: (row == 0) || (row == 7) (promotion condition on capture)
    // L122/L124: this.getRow() == 3 / this.getRow() == 4 (en passant row checks)
    // =========================================================================

    @Test
    public void testWhitePawnAtStartRowHasDoubleAndSinglePush() {
        // White pawn at e2 (row=6) → should have BOTH single push (e3) and double push (e4)
        Board board = createBoard("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        List<Move> moves = getMovesForPiece(board, 4, 6);
        boolean hasSinglePush = moves.stream().anyMatch(
                m -> m.getField().getRow() == 5 && m.getField().getColumn() == 4);
        boolean hasDoublePush = moves.stream().anyMatch(m -> m instanceof PawnRunMove);
        assertTrue("White pawn at start row should have single push (e3)", hasSinglePush);
        assertTrue("White pawn at start row should have double push (PawnRunMove)", hasDoublePush);
    }

    @Test
    public void testBlackPawnAtStartRowHasDoubleAndSinglePush() {
        // Black pawn at e7 (row=1) → should have BOTH single push and double push
        Board board = createBoard("4k3/4p3/8/8/8/8/8/4K3 b - - 0 1");
        List<Move> moves = getMovesForPiece(board, 4, 1);
        boolean hasDoublePush = moves.stream().anyMatch(m -> m instanceof PawnRunMove);
        boolean hasSinglePush = moves.stream().anyMatch(
                m -> m.getField().getRow() == 2 && m.getField().getColumn() == 4);
        assertTrue("Black pawn at start row should have single push (e6)", hasSinglePush);
        assertTrue("Black pawn at start row should have double push (PawnRunMove)", hasDoublePush);
    }

    @Test
    public void testWhitePawnNotAtStartRowHasOnlySinglePush() {
        // White pawn at e3 (row=5, not 6) → only single push (e4), NO double push
        Board board = createBoard("4k3/8/8/8/8/4P3/8/4K3 w - - 0 1");
        List<Move> moves = getMovesForPiece(board, 4, 5);
        boolean hasDoublePush = moves.stream().anyMatch(m -> m instanceof PawnRunMove);
        assertFalse("White pawn NOT at row 6 should NOT have a double push", hasDoublePush);
        assertEquals("White pawn not at start row has exactly 1 forward move", 1,
                moves.stream().filter(m -> m.getVictim() == null).count());
    }

    @Test
    public void testBlackPawnNotAtStartRowHasOnlySinglePush() {
        // Black pawn at e6 (row=2, not 1) → only single push, NO double push
        Board board = createBoard("4k3/8/4p3/8/8/8/8/4K3 b - - 0 1");
        List<Move> moves = getMovesForPiece(board, 4, 2);
        boolean hasDoublePush = moves.stream().anyMatch(m -> m instanceof PawnRunMove);
        assertFalse("Black pawn NOT at row 1 should NOT have a double push", hasDoublePush);
    }

    @Test
    public void testWhitePawnCapturesOnlyDiagonallyForward() {
        // White pawn at e4 (row=4); black pieces at d5 (row=3) and f3 (row=5)
        // Should capture d5 (diagonally forward) but NOT f3 (diagonally backward)
        Board board = createBoard("4k3/8/8/3p4/4P3/5p2/8/4K3 w - - 0 1");
        List<Move> moves = getMovesForPiece(board, 4, 4);
        boolean capturesD5 = moves.stream().anyMatch(
                m -> m.getVictim() != null && m.getField().getColumn() == 3 && m.getField().getRow() == 3);
        boolean capturesF3 = moves.stream().anyMatch(
                m -> m.getVictim() != null && m.getField().getColumn() == 5 && m.getField().getRow() == 5);
        assertTrue("White pawn at e4 should be able to capture black pawn at d5", capturesD5);
        assertFalse("White pawn at e4 must NOT capture backward at f3", capturesF3);
    }

    @Test
    public void testBlackPawnCapturesOnlyDiagonallyForward() {
        // Black pawn at e5 (row=3); white pieces at d4 (row=4) and f6 (row=2)
        // Should capture d4 (diagonally forward for black) but NOT f6
        Board board = createBoard("4k3/8/5P2/4p3/3P4/8/8/4K3 b - - 0 1");
        List<Move> moves = getMovesForPiece(board, 4, 3);
        boolean capturesD4 = moves.stream().anyMatch(
                m -> m.getVictim() != null && m.getField().getColumn() == 3 && m.getField().getRow() == 4);
        boolean capturesF6 = moves.stream().anyMatch(
                m -> m.getVictim() != null && m.getField().getColumn() == 5 && m.getField().getRow() == 2);
        assertTrue("Black pawn at e5 should capture white pawn at d4", capturesD4);
        assertFalse("Black pawn at e5 must NOT capture backward at f6", capturesF6);
    }

    @Test
    public void testWhitePawnOnE7PromotesOnCapture() {
        // White pawn at e7 (row=1); black knight at d8 (row=0)
        // Capture move to d8 should be a PromotionMove
        Board board = createBoard("3nk3/4P3/8/8/8/8/8/4K3 w - - 0 1");
        List<Move> moves = getMovesForPiece(board, 4, 1);
        boolean capturePromotion = moves.stream().anyMatch(
                m -> m instanceof PromotionMove && m.getVictim() != null
                        && m.getField().getColumn() == 3 && m.getField().getRow() == 0);
        assertTrue("White pawn at e7 capturing black knight at d8 must be a PromotionMove", capturePromotion);
    }

    @Test
    public void testBlackPawnOnE2PromotesOnCapture() {
        // Black pawn at e2 (row=6); white knight at d1 (row=7)
        // Capture move to d1 should be a PromotionMove
        Board board = createBoard("4k3/8/8/8/8/8/4p3/3NK3 b - - 0 1");
        List<Move> moves = getMovesForPiece(board, 4, 6);
        boolean capturePromotion = moves.stream().anyMatch(
                m -> m instanceof PromotionMove && m.getVictim() != null
                        && m.getField().getColumn() == 3 && m.getField().getRow() == 7);
        assertTrue("Black pawn at e2 capturing white knight at d1 must be a PromotionMove", capturePromotion);
    }

    @Test
    public void testWhitePawnAtH5EnPassantToG6() {
        // White pawn at h5 (row=3); black pawn just pushed to g5 (en passant target g6)
        // FEN: white pawn h5, black pawn g5 (just pushed), black pawn on e5 for context
        String fen = "4k3/8/8/6pP/8/8/8/4K3 w - g6 0 1";
        Board board = createBoard(fen);
        List<Move> moves = getMovesForPiece(board, 7, 3); // white pawn h5
        boolean hasEP = moves.stream().anyMatch(m -> m instanceof PassingMove);
        assertTrue("White pawn at h5 should have en passant move to g6", hasEP);
        if (hasEP) {
            PassingMove ep = (PassingMove) moves.stream().filter(m -> m instanceof PassingMove).findFirst().get();
            assertEquals("En passant landing field should be g6 (col=6, row=2)",
                    6, ep.getField().getColumn());
            assertEquals(2, ep.getField().getRow());
        }
    }

    @Test
    public void testBlackPawnAtD4EnPassantToE3() {
        // Black pawn at d4 (row=4); white pawn just pushed to e4 (en passant target e3)
        String fen = "4k3/8/8/8/3pP3/8/8/4K3 b - e3 0 1";
        Board board = createBoard(fen);
        List<Move> moves = getMovesForPiece(board, 3, 4); // black pawn d4
        boolean hasEP = moves.stream().anyMatch(m -> m instanceof PassingMove);
        assertTrue("Black pawn at d4 should have en passant move to e3", hasEP);
        if (hasEP) {
            PassingMove ep = (PassingMove) moves.stream().filter(m -> m instanceof PassingMove).findFirst().get();
            assertEquals("En passant landing field should be e3 (col=4, row=5)",
                    4, ep.getField().getColumn());
            assertEquals(5, ep.getField().getRow());
        }
    }

    @Test
    public void testPawnAtEdgeColumnHasNoOutOfBoundsCapture() {
        // White pawn at a4 (col=0, row=4); black pawn at b5 (col=1, row=3) — can capture b5
        // There is no piece to the left (col=-1 is out of bounds)
        Board board = createBoard("4k3/8/8/1p6/P7/8/8/4K3 w - - 0 1");
        List<Move> moves = getMovesForPiece(board, 0, 4);
        // Should capture b5 but not try to go to col=-1
        boolean capturesB5 = moves.stream().anyMatch(
                m -> m.getVictim() != null && m.getField().getColumn() == 1);
        assertTrue("White pawn at a4 should capture black pawn at b5", capturesB5);
        // All moves should have valid column
        for (Move m : moves) {
            assertTrue("Move target column must be >= 0", m.getField().getColumn() >= 0);
            assertTrue("Move target column must be < 8", m.getField().getColumn() < 8);
        }
    }

    @Test
    public void testWhitePawnForwardMoveBlockedByPiece() {
        // White pawn at e4 blocked by black pawn at e5 — no forward moves
        Board board = createBoard("4k3/8/8/4p3/4P3/8/8/4K3 w - - 0 1");
        List<Move> moves = getMovesForPiece(board, 4, 4);
        long forwardMoves = moves.stream().filter(m -> m.getVictim() == null).count();
        assertEquals("White pawn blocked by enemy pawn should have 0 forward moves", 0, forwardMoves);
    }

    @Test
    public void testWhitePawnAtE7HasPromotionMoveForward() {
        // White pawn at e7 (row=1) with empty e8 → promotion move forward.
        // Black king must be away from e8 (not blocking the promotion square).
        Board board = createBoard("k7/4P3/8/8/8/8/8/4K3 w - - 0 1");
        List<Move> moves = getMovesForPiece(board, 4, 1);
        boolean hasPromotion = moves.stream().anyMatch(
                m -> m instanceof PromotionMove && m.getVictim() == null);
        assertTrue("White pawn at e7 should have a forward promotion move to e8", hasPromotion);
    }

    @Test
    public void testBlackPawnAtE2HasPromotionMoveForward() {
        // Black pawn at e2 (row=6) with empty e1 → promotion move forward.
        // White king must be away from e1 (not blocking the promotion square).
        Board board = createBoard("4k3/8/8/8/8/8/4p3/7K b - - 0 1");
        List<Move> moves = getMovesForPiece(board, 4, 6);
        boolean hasPromotion = moves.stream().anyMatch(
                m -> m instanceof PromotionMove && m.getVictim() == null);
        assertTrue("Black pawn at e2 should have a forward promotion move to e1", hasPromotion);
    }
}
