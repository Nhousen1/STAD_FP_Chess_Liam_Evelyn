package com.chess.blackbox.pieces;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.moves.Move;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Black-box tests for knight movement using EP, BA, and EG.
 *
 * Spec (FIDE Laws 3.6): knight moves in an L-shape and can jump over other pieces.
 * piece.getMoves() returns pseudo-legal moves without check filtering.
 */
public class KnightMovementTest extends ChessTestBase {

    // Knight on e4 (col=4, row=4), surrounded by empty squares. Both kings on far corners.
    private static final String FEN_KNIGHT_E4 =
            "4k3/8/8/8/4N3/8/8/4K3 w - - 0 1";

    // Knight on a1 corner (col=0, row=7)
    private static final String FEN_KNIGHT_A1 =
            "4k3/8/8/8/8/8/8/N3K3 w - - 0 1";

    // Knight on a4 edge file (col=0, row=4)
    private static final String FEN_KNIGHT_A4 =
            "4k3/8/8/8/N7/8/8/4K3 w - - 0 1";

    // Knight on b1, one square from corner (col=1, row=7)
    private static final String FEN_KNIGHT_B1 =
            "4k3/8/8/8/8/8/8/1N2K3 w - - 0 1";

    // Knight on h4 (col=7, row=4) — far edge file
    private static final String FEN_KNIGHT_H4 =
            "4k3/8/8/8/7N/8/8/4K3 w - - 0 1";

    // Knight on e4 with an enemy pawn on f6 (reachable square)
    private static final String FEN_KNIGHT_E4_CAPTURE =
            "4k3/8/5p2/8/4N3/8/8/4K3 w - - 0 1";

    // Knight on e4 with own pawns blocking 2 of its 8 squares
    private static final String FEN_KNIGHT_E4_BLOCKED =
            "4k3/8/3P1P2/8/4N3/8/8/4K3 w - - 0 1";

    // -------------------------------------------------------------------------
    // Equivalence Partitioning
    // -------------------------------------------------------------------------

    /**
     * [EP] Knight in center (e4) on open board: exactly 8 legal moves.
     * Partition: piece in center, no blocking pieces.
     * Goal: verify the L-shape generates all 8 target squares.
     */
    @Test
    public void testKnightInCenterHasEightMoves() {
        Board board = createBoard(FEN_KNIGHT_E4);
        List<Move> moves = getMovesForPiece(board, 4, 4); // e4
        assertEquals("Knight on e4 (open board) should have 8 moves", 8, moves.size());
    }

    /**
     * [EP] Knight can capture an enemy piece on a reachable square.
     * Partition: capture move available.
     * Goal: verify the capture square (f6 = col=5, row=2) is in the move list.
     */
    @Test
    public void testKnightCaptureEnemyPawn() {
        Board board = createBoard(FEN_KNIGHT_E4_CAPTURE);
        List<Move> moves = getMovesForPiece(board, 4, 4); // e4
        assertTrue("Knight should be able to capture on f6 (col=5, row=2)",
                hasMoveTo(moves, 5, 2));
    }

    /**
     * [EP] Knight with 2 own pieces on 2 target squares: 6 moves remain.
     * Partition: friendly pieces blocking some destinations.
     * Goal: own pieces are not captured; those squares excluded.
     */
    @Test
    public void testKnightBlockedByOwnPiecesReducesMoves() {
        Board board = createBoard(FEN_KNIGHT_E4_BLOCKED);
        List<Move> moves = getMovesForPiece(board, 4, 4); // e4
        assertEquals("Knight with 2 blocked squares should have 6 moves", 6, moves.size());
    }

    // -------------------------------------------------------------------------
    // Boundary Analysis
    // -------------------------------------------------------------------------

    /**
     * [BA] Knight at corner a1 (col=0, row=7): exactly 2 legal moves.
     * Boundary: minimum number of moves for a knight at any corner.
     * Goal: board boundary correctly limits L-shape reach.
     */
    @Test
    public void testKnightAtCornerA1HasTwoMoves() {
        Board board = createBoard(FEN_KNIGHT_A1);
        List<Move> moves = getMovesForPiece(board, 0, 7); // a1
        assertEquals("Knight at corner a1 should have exactly 2 moves", 2, moves.size());
    }

    /**
     * [BA] Knight at edge file a, mid-rank a4 (col=0, row=4): 4 legal moves.
     * Boundary: edge file — half the L-shapes fall off the left edge.
     */
    @Test
    public void testKnightAtEdgeFileA4HasFourMoves() {
        Board board = createBoard(FEN_KNIGHT_A4);
        List<Move> moves = getMovesForPiece(board, 0, 4); // a4
        assertEquals("Knight at a4 (edge file) should have 4 moves", 4, moves.size());
    }

    /**
     * [BA] Knight at b1 (col=1, row=7), one square from corner: 3 legal moves.
     * Boundary: near-corner position where 5 of 8 L-shapes are off-board.
     */
    @Test
    public void testKnightNearCornerB1HasThreeMoves() {
        Board board = createBoard(FEN_KNIGHT_B1);
        List<Move> moves = getMovesForPiece(board, 1, 7); // b1
        assertEquals("Knight at b1 should have 3 moves", 3, moves.size());
    }

    // -------------------------------------------------------------------------
    // Error Guessing
    // -------------------------------------------------------------------------

    /**
     * [EG] Knight at edge file h, mid-rank h4 (col=7, row=4): 4 legal moves.
     * Fault class: right-edge off-board calculation (symmetric to left-edge).
     */
    @Test
    public void testKnightAtEdgeFileH4HasFourMoves() {
        Board board = createBoard(FEN_KNIGHT_H4);
        List<Move> moves = getMovesForPiece(board, 7, 4); // h4
        assertEquals("Knight at h4 (right edge file) should have 4 moves", 4, moves.size());
    }
}
