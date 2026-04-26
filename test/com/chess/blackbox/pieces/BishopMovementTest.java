package com.chess.blackbox.pieces;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.moves.Move;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Black-box tests for bishop movement using EP, BA, and EG.
 *
 * Spec (FIDE Laws 3.4): bishop moves along diagonals, blocked by the first piece
 * in each direction. Own pieces block (no capture); enemy pieces can be captured.
 */
public class BishopMovementTest extends ChessTestBase {

    // Bishop on e4 (col=4, row=4), open board — kings at h8/a1 to avoid all bishop diagonals
    private static final String FEN_BISHOP_E4 =
            "7k/8/8/8/4B3/8/8/K7 w - - 0 1";

    // Bishop at a1 corner (col=0, row=7)
    private static final String FEN_BISHOP_A1 =
            "k7/8/8/8/8/8/8/B6K w - - 0 1";

    // Bishop at h8 corner (col=7, row=0)
    private static final String FEN_BISHOP_H8 =
            "k6B/8/8/8/8/8/8/7K w - - 0 1";

    // Bishop on e4 with own pawn on c2 (blocks one diagonal)
    private static final String FEN_BISHOP_E4_BLOCKED =
            "k7/8/8/8/4B3/8/2P5/7K w - - 0 1";

    // Bishop on e4 with enemy pawn on c2 (capturable, then blocked)
    private static final String FEN_BISHOP_E4_ENEMY =
            "k7/8/8/8/4B3/8/2p5/7K w - - 0 1";

    // Bishop at d1 starting square — surrounded by pawns on rank 2
    private static final String FEN_BISHOP_D1_BLOCKED =
            "4k3/8/8/8/8/8/2PPP3/3B4 w - - 0 1";

    // -------------------------------------------------------------------------
    // Equivalence Partitioning
    // -------------------------------------------------------------------------

    /**
     * [EP] Bishop on e4, open board: maximum 13 diagonal moves.
     * Partition: center square, no obstructions.
     * Goal: all four diagonals are traversable to their board edges.
     */
    @Test
    public void testBishopInCenterOpenBoardHasThirteenMoves() {
        Board board = createBoard(FEN_BISHOP_E4);
        List<Move> moves = getMovesForPiece(board, 4, 4); // e4
        assertEquals("Bishop on e4 open board should have 13 moves", 13, moves.size());
    }

    /**
     * [EP] Bishop with own piece blocking one diagonal: move count reduced.
     * Partition: friendly piece on one diagonal — blocked squares excluded.
     */
    @Test
    public void testBishopBlockedByOwnPieceReducesMoves() {
        Board board = createBoard(FEN_BISHOP_E4_BLOCKED);
        List<Move> movesBlocked = getMovesForPiece(board, 4, 4);
        // Own pawn on c2 blocks the bottom-left diagonal; bishop can't reach c2 or b1
        assertTrue("Blocked bishop should have fewer than 13 moves", movesBlocked.size() < 13);
        // Own pawn square (c2 = col=2, row=6) must NOT be in move list
        assertFalse("Bishop cannot capture own pawn on c2",
                hasMoveTo(movesBlocked, 2, 6));
    }

    /**
     * [EP] Bishop with enemy piece on diagonal: can capture it but not pass.
     * Partition: enemy piece — capture move present; squares beyond excluded.
     */
    @Test
    public void testBishopCapturesEnemyPawnButNotPassIt() {
        Board board = createBoard(FEN_BISHOP_E4_ENEMY);
        List<Move> moves = getMovesForPiece(board, 4, 4);
        // Enemy pawn on c2 (col=2, row=6) — capture should be present
        assertTrue("Bishop should be able to capture enemy pawn on c2",
                hasMoveTo(moves, 2, 6));
        // b1 (col=1, row=7) is behind the enemy pawn — should NOT be reachable
        assertFalse("Bishop cannot pass through enemy pawn to b1",
                hasMoveTo(moves, 1, 7));
    }

    // -------------------------------------------------------------------------
    // Boundary Analysis
    // -------------------------------------------------------------------------

    /**
     * [BA] Bishop at a1 corner (col=0, row=7): 7 moves along single diagonal.
     * Boundary: corner restricts bishop to exactly one diagonal.
     */
    @Test
    public void testBishopAtCornerA1HasSevenMoves() {
        Board board = createBoard(FEN_BISHOP_A1);
        List<Move> moves = getMovesForPiece(board, 0, 7); // a1
        assertEquals("Bishop at corner a1 should have 7 moves", 7, moves.size());
    }

    /**
     * [BA] Bishop at h8 corner (col=7, row=0): 7 moves along single diagonal.
     * Boundary: opposite corner, symmetric to a1.
     */
    @Test
    public void testBishopAtCornerH8HasSevenMoves() {
        Board board = createBoard(FEN_BISHOP_H8);
        List<Move> moves = getMovesForPiece(board, 7, 0); // h8
        assertEquals("Bishop at corner h8 should have 7 moves", 7, moves.size());
    }

    // -------------------------------------------------------------------------
    // Error Guessing
    // -------------------------------------------------------------------------

    /**
     * [EG] Bishop at d1 starting square, all adjacent diagonals blocked by own pawns:
     * 0 moves expected.
     * Fault class: starting-position bishops trapped by pawn structure — verifies
     * zero-move case is handled without exception.
     */
    @Test
    public void testBishopFullyBlockedByOwnPawnsHasZeroMoves() {
        Board board = createBoard(FEN_BISHOP_D1_BLOCKED);
        List<Move> moves = getMovesForPiece(board, 3, 7); // d1
        assertEquals("Bishop surrounded by own pawns should have 0 moves", 0, moves.size());
    }

    /**
     * [EG] Bishop moves never land on squares outside the 8×8 board.
     * Fault class: off-by-one error in diagonal traversal generating invalid coords.
     * Goal: all move targets have col and row in [0,7].
     */
    @Test
    public void testBishopMovesNeverLeaveBoard() {
        Board board = createBoard(FEN_BISHOP_E4);
        List<Move> moves = getMovesForPiece(board, 4, 4);
        for (Move m : moves) {
            int col = m.getField().getColumn();
            int row = m.getField().getRow();
            assertTrue("Move target col=" + col + " should be in [0,7]", col >= 0 && col <= 7);
            assertTrue("Move target row=" + row + " should be in [0,7]", row >= 0 && row <= 7);
        }
    }
}
