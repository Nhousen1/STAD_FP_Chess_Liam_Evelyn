package com.chess.blackbox.pieces;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.moves.Move;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Black-box tests for rook movement using EP, BA, and EG.
 *
 * Spec (FIDE Laws 3.3): rook moves along ranks and files, blocked by the first
 * piece in each direction. Own pieces block; enemy pieces can be captured.
 */
public class RookMovementTest extends ChessTestBase {

    // Rook on e4 (col=4, row=4), open board
    private static final String FEN_ROOK_E4 =
            "k7/8/8/8/4R3/8/8/7K w - - 0 1";

    // Rook at a1 corner (col=0, row=7) — white king on h2 (not rank1) so it doesn't block
    private static final String FEN_ROOK_A1 =
            "k7/8/8/8/8/8/7K/R7 w - - 0 1";

    // Rook at a4 (col=0, row=4) — edge file, mid-rank
    private static final String FEN_ROOK_A4 =
            "k7/8/8/8/R7/8/8/7K w - - 0 1";

    // Rook on e4 with own pawn on e6 (blocks upward)
    private static final String FEN_ROOK_E4_BLOCKED_UP =
            "k7/8/4P3/8/4R3/8/8/7K w - - 0 1";

    // Rook on e4 with enemy pawn on e6 (capturable, then stopped)
    private static final String FEN_ROOK_E4_ENEMY_UP =
            "k7/8/4p3/8/4R3/8/8/7K w - - 0 1";

    // Rook on e4 completely surrounded by own pieces on all 4 directions
    private static final String FEN_ROOK_SURROUNDED =
            "k7/8/8/4P3/3PRP2/4P3/8/7K w - - 0 1";

    // -------------------------------------------------------------------------
    // Equivalence Partitioning
    // -------------------------------------------------------------------------

    /**
     * [EP] Rook on e4, open board: exactly 14 moves (7 per axis).
     * Partition: center square, no obstructions.
     */
    @Test
    public void testRookInCenterOpenBoardHasFourteenMoves() {
        Board board = createBoard(FEN_ROOK_E4);
        List<Move> moves = getMovesForPiece(board, 4, 4); // e4
        assertEquals("Rook on e4 open board should have 14 moves", 14, moves.size());
    }

    /**
     * [EP] Rook blocked by own piece on same file: move count decreases.
     * Partition: own piece on the same file — blocked squares excluded.
     */
    @Test
    public void testRookBlockedByOwnPieceReducesMoves() {
        Board board = createBoard(FEN_ROOK_E4_BLOCKED_UP);
        List<Move> moves = getMovesForPiece(board, 4, 4);
        assertTrue("Blocked rook should have fewer than 14 moves", moves.size() < 14);
        // Own pawn on e6 (col=4, row=2) must NOT be in list
        assertFalse("Rook cannot capture own pawn on e6", hasMoveTo(moves, 4, 2));
    }

    /**
     * [EP] Rook captures enemy piece on same file: capture present, squares beyond excluded.
     * Partition: enemy piece on same file — capturable but can't pass.
     */
    @Test
    public void testRookCapturesEnemyPawnButNotPassIt() {
        Board board = createBoard(FEN_ROOK_E4_ENEMY_UP);
        List<Move> moves = getMovesForPiece(board, 4, 4);
        // Enemy on e6 (col=4, row=2) — capture should be in list
        assertTrue("Rook should be able to capture enemy pawn on e6",
                hasMoveTo(moves, 4, 2));
        // e7 (col=4, row=1) is behind enemy — should NOT be reachable
        assertFalse("Rook cannot pass through enemy pawn to e7",
                hasMoveTo(moves, 4, 1));
    }

    // -------------------------------------------------------------------------
    // Boundary Analysis
    // -------------------------------------------------------------------------

    /**
     * [BA] Rook at a1 corner (col=0, row=7): 14 moves (7 right + 7 up).
     * Boundary: corner position — two axes truncated on one side each.
     */
    @Test
    public void testRookAtCornerA1HasFourteenMoves() {
        Board board = createBoard(FEN_ROOK_A1);
        List<Move> moves = getMovesForPiece(board, 0, 7); // a1
        assertEquals("Rook at corner a1 should have 14 moves", 14, moves.size());
    }

    /**
     * [BA] Rook at edge file a4 (col=0, row=4): 14 moves.
     * Boundary: edge file — one axis starts from the board edge.
     */
    @Test
    public void testRookAtEdgeFileA4HasFourteenMoves() {
        Board board = createBoard(FEN_ROOK_A4);
        List<Move> moves = getMovesForPiece(board, 0, 4); // a4
        assertEquals("Rook at edge file a4 should have 14 moves", 14, moves.size());
    }

    // -------------------------------------------------------------------------
    // Error Guessing
    // -------------------------------------------------------------------------

    /**
     * [EG] Rook completely surrounded by own pieces on all 4 sides: 0 moves.
     * Fault class: zero-move case must not throw; all directions blocked.
     */
    @Test
    public void testRookSurroundedByOwnPiecesHasZeroMoves() {
        Board board = createBoard(FEN_ROOK_SURROUNDED);
        List<Move> moves = getMovesForPiece(board, 4, 4); // e4
        assertEquals("Rook surrounded by own pieces should have 0 moves", 0, moves.size());
    }

    /**
     * [EG] Rook move targets never fall outside the 8×8 board.
     * Fault class: off-by-one in rank/file traversal loop.
     */
    @Test
    public void testRookMovesNeverLeaveBoard() {
        Board board = createBoard(FEN_ROOK_E4);
        List<Move> moves = getMovesForPiece(board, 4, 4);
        for (Move m : moves) {
            int col = m.getField().getColumn();
            int row = m.getField().getRow();
            assertTrue("col=" + col + " out of bounds", col >= 0 && col <= 7);
            assertTrue("row=" + row + " out of bounds", row >= 0 && row <= 7);
        }
    }
}
