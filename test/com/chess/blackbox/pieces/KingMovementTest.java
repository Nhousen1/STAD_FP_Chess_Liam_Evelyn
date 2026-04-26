package com.chess.blackbox.pieces;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.moves.Move;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Black-box tests for king movement using EP, BA, and EG.
 *
 * Spec (FIDE Laws 3.8): king moves one square in any direction.
 * piece.getMoves() returns pseudo-legal moves (no check filtering).
 * Castling is tested separately in CastlingTest.
 */
public class KingMovementTest extends ChessTestBase {

    // White king on e4 (col=4, row=4), black king far away
    private static final String FEN_KING_E4 =
            "k7/8/8/8/4K3/8/8/8 w - - 0 1";

    // White king at a1 corner (col=0, row=7)
    private static final String FEN_KING_A1 =
            "k7/8/8/8/8/8/8/K7 w - - 0 1";

    // White king at a4 edge file (col=0, row=4)
    private static final String FEN_KING_A4 =
            "k7/8/8/8/K7/8/8/8 w - - 0 1";

    // White king on e4 with own pawns blocking d3, d4, d5 (3 adjacent squares)
    private static final String FEN_KING_E4_BLOCKED =
            "k7/8/8/3P4/3PK3/3P4/8/8 w - - 0 1";

    // -------------------------------------------------------------------------
    // Equivalence Partitioning
    // -------------------------------------------------------------------------

    /**
     * [EP] King in center (e4), open board: 8 moves (one square in each direction).
     * Partition: center position, no obstructions, no check.
     */
    @Test
    public void testKingInCenterHasEightMoves() {
        Board board = createBoard(FEN_KING_E4);
        List<Move> moves = getMovesForPiece(board, 4, 4); // e4
        assertEquals("King on e4 (open board) should have 8 moves", 8, moves.size());
    }

    /**
     * [EP] King with 3 own pieces on adjacent squares: 5 moves remaining.
     * Partition: some adjacent squares blocked by friendly pieces.
     */
    @Test
    public void testKingWithOwnPiecesBlockingThreeSquaresHasFiveMoves() {
        Board board = createBoard(FEN_KING_E4_BLOCKED);
        List<Move> moves = getMovesForPiece(board, 4, 4); // e4
        assertEquals("King with 3 blocked squares should have 5 moves", 5, moves.size());
    }

    // -------------------------------------------------------------------------
    // Boundary Analysis
    // -------------------------------------------------------------------------

    /**
     * [BA] King at corner a1 (col=0, row=7): 3 moves.
     * Boundary: corner restricts king to 3 of 8 possible directions.
     */
    @Test
    public void testKingAtCornerA1HasThreeMoves() {
        Board board = createBoard(FEN_KING_A1);
        List<Move> moves = getMovesForPiece(board, 0, 7); // a1
        assertEquals("King at corner a1 should have 3 moves", 3, moves.size());
    }

    /**
     * [BA] King at edge file a4 (col=0, row=4): 5 moves.
     * Boundary: edge file — left-side squares are off-board.
     */
    @Test
    public void testKingAtEdgeFileA4HasFiveMoves() {
        Board board = createBoard(FEN_KING_A4);
        List<Move> moves = getMovesForPiece(board, 0, 4); // a4
        assertEquals("King at edge file a4 should have 5 moves", 5, moves.size());
    }

    // -------------------------------------------------------------------------
    // Error Guessing
    // -------------------------------------------------------------------------

    /**
     * [EG] King does not include its own starting square in moves (no zero-length move).
     * Fault class: identity move accidentally included.
     */
    @Test
    public void testKingDoesNotIncludeStartSquare() {
        Board board = createBoard(FEN_KING_E4);
        List<Move> moves = getMovesForPiece(board, 4, 4);
        assertFalse("King should not list its own square as a valid move",
                hasMoveTo(moves, 4, 4));
    }

    /**
     * [EG] King moves never land outside the 8×8 board.
     * Fault class: off-by-one in direction iteration.
     */
    @Test
    public void testKingMovesNeverLeaveBoard() {
        Board board = createBoard(FEN_KING_A1);
        List<Move> moves = getMovesForPiece(board, 0, 7);
        for (Move m : moves) {
            int col = m.getField().getColumn();
            int row = m.getField().getRow();
            assertTrue("col=" + col + " out of bounds", col >= 0 && col <= 7);
            assertTrue("row=" + row + " out of bounds", row >= 0 && row <= 7);
        }
    }
}
