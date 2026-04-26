package com.chess.blackbox.pieces;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.moves.Move;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Black-box tests for queen movement using EP, BA, and EG.
 *
 * Spec (FIDE Laws 3.5): queen combines rook and bishop movement (rank, file, diagonal).
 * Maximum reach from center of an empty board is 27 squares.
 */
public class QueenMovementTest extends ChessTestBase {

    // Queen on e4 (col=4, row=4) — kings at h8/a1 avoid all queen lines (rank4, e-file, diagonals)
    private static final String FEN_QUEEN_E4 =
            "7k/8/8/8/4Q3/8/8/K7 w - - 0 1";

    // Queen at a1 corner (col=0, row=7) — kings at g8/h2 avoid queen's rank, file, diagonal
    private static final String FEN_QUEEN_A1 =
            "6k1/8/8/8/8/8/7K/Q7 w - - 0 1";

    // Queen on e4 with own pawns blocking 4 directions
    private static final String FEN_QUEEN_BLOCKED =
            "k7/8/8/3PPP2/3PQP2/3PPP2/8/7K w - - 0 1";

    // Queen on e4 with one enemy pawn diagonally reachable
    private static final String FEN_QUEEN_E4_CAPTURE =
            "k7/8/8/8/4Q3/8/2p5/7K w - - 0 1";

    // -------------------------------------------------------------------------
    // Equivalence Partitioning
    // -------------------------------------------------------------------------

    /**
     * [EP] Queen on e4, open board: exactly 27 moves (max possible).
     * Partition: center square, no obstructions.
     * Goal: rook + bishop reach combined correctly.
     */
    @Test
    public void testQueenInCenterOpenBoardHasTwentySevenMoves() {
        Board board = createBoard(FEN_QUEEN_E4);
        List<Move> moves = getMovesForPiece(board, 4, 4); // e4
        assertEquals("Queen on e4 open board should have 27 moves", 27, moves.size());
    }

    /**
     * [EP] Queen blocked by own pieces in all 8 directions: 0 moves.
     * Partition: fully blocked position.
     */
    @Test
    public void testQueenSurroundedByOwnPiecesHasZeroMoves() {
        Board board = createBoard(FEN_QUEEN_BLOCKED);
        List<Move> moves = getMovesForPiece(board, 4, 4); // e4
        assertEquals("Queen surrounded by own pieces should have 0 moves", 0, moves.size());
    }

    /**
     * [EP] Queen captures enemy piece on diagonal: capture move present.
     * Partition: enemy piece within diagonal reach.
     */
    @Test
    public void testQueenCapturesEnemyPawnOnDiagonal() {
        Board board = createBoard(FEN_QUEEN_E4_CAPTURE);
        List<Move> moves = getMovesForPiece(board, 4, 4); // e4
        // Enemy pawn on c2 (col=2, row=6)
        assertTrue("Queen should be able to capture enemy pawn on c2",
                hasMoveTo(moves, 2, 6));
    }

    // -------------------------------------------------------------------------
    // Boundary Analysis
    // -------------------------------------------------------------------------

    /**
     * [BA] Queen at corner a1 (col=0, row=7): 21 moves (7 diagonal + 14 straight).
     * Boundary: corner — only one diagonal plus two straight lines.
     */
    @Test
    public void testQueenAtCornerA1HasTwentyOneMoves() {
        Board board = createBoard(FEN_QUEEN_A1);
        List<Move> moves = getMovesForPiece(board, 0, 7); // a1
        assertEquals("Queen at corner a1 should have 21 moves", 21, moves.size());
    }

    // -------------------------------------------------------------------------
    // Error Guessing
    // -------------------------------------------------------------------------

    /**
     * [EG] Queen moves never land outside the 8×8 board.
     * Fault class: off-by-one in any of the 8 directional traversals.
     */
    @Test
    public void testQueenMovesNeverLeaveBoard() {
        Board board = createBoard(FEN_QUEEN_E4);
        List<Move> moves = getMovesForPiece(board, 4, 4);
        for (Move m : moves) {
            int col = m.getField().getColumn();
            int row = m.getField().getRow();
            assertTrue("col=" + col + " out of bounds", col >= 0 && col <= 7);
            assertTrue("row=" + row + " out of bounds", row >= 0 && row <= 7);
        }
    }

    /**
     * [EG] Queen does not move to its own square (no zero-length move).
     * Fault class: identity move included accidentally.
     */
    @Test
    public void testQueenDoesNotIncludeStartSquare() {
        Board board = createBoard(FEN_QUEEN_E4);
        List<Move> moves = getMovesForPiece(board, 4, 4);
        assertFalse("Queen should not include its own starting square in moves",
                hasMoveTo(moves, 4, 4));
    }
}
