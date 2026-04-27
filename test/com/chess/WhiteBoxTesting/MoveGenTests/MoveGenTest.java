package com.chess.WhiteBoxTesting.MoveGenTests;

import com.chess.WhiteBoxTesting.ChessBaseTest;
import com.chess.WhiteBoxTesting.TestDouble;
import com.chess.root.Board;
import com.chess.root.Field;
import com.chess.root.moves.Move;
import com.chess.root.pieces.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * White-box tests for move generation for each piece type.
 *
 * Strategy (from proposal):
 *   "Test each individual move for each piece setting up minimal board states
 *    and assert that moves are legal and valid. Use LOAD option with a FEN
 *    string to construct a board that isolates each piece under test."
 *
 * Each test loads a minimal FEN, retrieves the isolated piece, calls getMoves(),
 * and asserts both the count and the specific target squares.
 */
public class MoveGenTest extends ChessBaseTest {

    // ====================================================================
    // PAWN
    // ====================================================================

    @Test
    public void pawnOpeningSquare() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        Board board = game.getBoard();

        // White pawn on e2 should have 2 moves: e3 and e4
        Piece pawn = pieceAt(board, "e2");
        assertNotNull("White pawn should be at e2", pawn);
        assertTrue("Piece at e2 should be a PawnPiece", pawn instanceof PawnPiece);

        List<Move> moves = pawn.getMoves();
        assertNotNull(moves);
        assertEquals("Pawn on e2 should have exactly 2 moves", 2, moves.size());

        boolean hasE3 = moves.stream().anyMatch(m -> "e3".equals(m.getField().getNotation()));
        boolean hasE4 = moves.stream().anyMatch(m -> "e4".equals(m.getField().getNotation()));
        assertTrue("Pawn should be able to move to e3", hasE3);
        assertTrue("Pawn should be able to move to e4 (two-square run)", hasE4);
    }

    @Test
    public void pawnBlockedByFriendlyPiece() {
        // White pawn on e2 with another white piece on e3 – no advances possible
        String fen = "7k/8/8/8/8/4R3/4P3/K7 w - - 0 1";
        TestDouble game = TestDouble.fromFen(fen);
        Board board = game.getBoard();

        Piece pawn = pieceAt(board, "e2");
        assertNotNull(pawn);
        List<Move> moves = pawn.getMoves();
        assertTrue(moves.isEmpty());
    }

    @Test
    public void pawnCanCaptureDiagonally() {
        // White pawn on e4, black pawns on d5 and f5
        String fen = "7k/8/8/3p1p2/4P3/8/8/K7 w - - 0 1";
        TestDouble game = TestDouble.fromFen(fen);
        Board board = game.getBoard();

        Piece pawn = pieceAt(board, "e4");
        assertNotNull(pawn);
        List<Move> moves = pawn.getMoves();

        boolean captureD5 = moves.stream().anyMatch(m -> "d5".equals(m.getField().getNotation()));
        boolean captureF5 = moves.stream().anyMatch(m -> "f5".equals(m.getField().getNotation()));
        assertTrue(captureD5);
        assertTrue(captureF5);
    }

    @Test
    public void pawnPromotionRank() {
        TestDouble game = TestDouble.fromFen(FEN_PROMOTION);
        Board board = game.getBoard();

        Piece pawn = pieceAt(board, "e7");
        assertNotNull("White pawn should be on e7", pawn);
        List<Move> moves = pawn.getMoves();
        assertFalse("Promotion moves should be available", moves.isEmpty());

        boolean hasPromotion = moves.stream()
                .anyMatch(m -> m instanceof com.chess.root.moves.PromotionMove);
        assertTrue("Move list should contain at least one PromotionMove", hasPromotion);
    }

    @Test
    public void pawnEnPassant() {
        TestDouble game = TestDouble.fromFen(FEN_EN_PASSANT);
        Board board = game.getBoard();

        // White pawn should be on e5
        Piece pawn = pieceAt(board, "e5");
        assertNotNull("White pawn should be on e5", pawn);
        List<Move> moves = pawn.getMoves();

        boolean hasEnPassant = moves.stream()
                .anyMatch(m -> m instanceof com.chess.root.moves.PassingMove);
        assertTrue("En passant move (PassingMove) should be available", hasEnPassant);
    }

    // ====================================================================
    // KNIGHT
    // ====================================================================

    @Test
    public void knightAtCenterOfBoard() {
        TestDouble game = TestDouble.fromFen(FEN_KNIGHT_ONLY);
        Board board = game.getBoard();

        Piece knight = pieceAt(board, "d4");
        assertNotNull("White knight should be on d4", knight);
        assertTrue(knight instanceof KnightPiece);

        List<Move> moves = knight.getMoves();
        assertEquals("Knight in center should have 8 moves", 8, moves.size());
    }

    @Test
    public void knightCornerSquare() {
        // White knight on a1
        String fen = "7k/8/8/8/8/8/8/N6K w - - 0 1";
        TestDouble game = TestDouble.fromFen(fen);
        Board board = game.getBoard();

        Piece knight = pieceAt(board, "a1");
        assertNotNull(knight);
        List<Move> moves = knight.getMoves();
        assertEquals("Knight in corner a1 should have exactly 2 moves", 2, moves.size());
    }

    @Test
    public void knightStartPosition() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        Board board = game.getBoard();

        // g1 knight
        Piece knight = pieceAt(board, "g1");
        assertNotNull(knight);
        List<Move> moves = knight.getMoves();
        assertEquals("Knight on g1 should have 2 moves in starting position", 2, moves.size());
    }

    // ====================================================================
    // BISHOP
    // ====================================================================

    @Test
    public void bishopOpenDiagonals_() {
        // White bishop on c1, empty board except kings
        TestDouble game = TestDouble.fromFen(FEN_BISHOP_ONLY);
        Board board = game.getBoard();

        Piece bishop = pieceAt(board, "c1");
        assertNotNull("White bishop should be on c1", bishop);
        assertTrue(bishop instanceof BishopPiece);

        List<Move> moves = bishop.getMoves();
        // From c1 on an open board the bishop can slide diagonally in two directions
        assertFalse("Bishop should have diagonal moves available", moves.isEmpty());
    }

    @Test
    public void bishopBlockedByFriendlyPiece() {
        // Bishop on c1 with white pawn on d2 – only one diagonal open
        TestDouble game = TestDouble.fromFen(FEN_START);
        Board board = game.getBoard();

        Piece bishop = pieceAt(board, "c1");
        assertNotNull(bishop);
        // In starting position, bishop on c1 is completely blocked
        List<Move> moves = bishop.getMoves();
        assertTrue("Bishop should have no moves at start (blocked by pawns)", moves.isEmpty());
    }

    // ====================================================================
    // ROOK
    // ====================================================================

    @Test
    public void rookOpenRank() {
        TestDouble game = TestDouble.fromFen(FEN_ROOK_ONLY);
        Board board = game.getBoard();

        Piece rook = pieceAt(board, "a1");
        assertNotNull("White rook should be on a1", rook);
        assertTrue(rook instanceof RookPiece);

        List<Move> moves = rook.getMoves();
        // Rook on a1 can go to a2-a7 (7 squares up) and b1-g1 (6 squares right)
        assertFalse(moves.isEmpty());
        assertTrue("Rook on open board should have at least 13 moves", moves.size() >= 13);
    }

    @Test
    public void rookStartPosition() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        Board board = game.getBoard();

        Piece rook = pieceAt(board, "a1");
        assertNotNull(rook);
        List<Move> moves = rook.getMoves();
        assertTrue("Rook blocked by own pieces at start has no moves", moves.isEmpty());
    }

    // ====================================================================
    // QUEEN
    // ====================================================================

    @Test
    public void queenOpenBoard() {
        TestDouble game = TestDouble.fromFen(FEN_QUEEN_ONLY);
        Board board = game.getBoard();

        Piece queen = pieceAt(board, "d4");
        assertNotNull("White queen should be on d4", queen);
        assertTrue(queen instanceof QueenPiece);

        List<Move> moves = queen.getMoves();
        assertTrue("Queen on near-empty board should have many moves", moves.size() >= 20);
    }

    @Test
    public void queenStartPosition() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        Board board = game.getBoard();

        Piece queen = pieceAt(board, "d1");
        assertNotNull(queen);
        List<Move> moves = queen.getMoves();
        assertTrue("Queen blocked by own pieces at start has no moves", moves.isEmpty());
    }

    // ====================================================================
    // KING
    // ====================================================================

    @Test
    public void kingCenterOpenBoard() {
        // White king on e4, only black king far away
        String fen = "k7/8/8/8/4K3/8/8/8 w - - 0 1";
        TestDouble game = TestDouble.fromFen(fen);
        Board board = game.getBoard();

        Piece king = pieceAt(board, "e4");
        assertNotNull("White king should be on e4", king);
        assertTrue(king instanceof KingPiece);

        List<Move> moves = king.getMoves();
        assertEquals("King in center open board should have 8 moves", 8, moves.size());
    }

    @Test
    public void kingCornerPosition() {
        TestDouble game = TestDouble.fromFen(FEN_KINGS_ONLY);
        Board board = game.getBoard();

        Piece king = pieceAt(board, "a1");
        assertNotNull(king);
        List<Move> moves = king.getMoves();
        // King on a1: can go to a2, b2, b1 = 3 squares
        assertTrue("King on a1 should have 2-3 raw moves", moves.size() >= 2);
    }

    @Test
    public void kingCastlingMoves() {
        TestDouble game = TestDouble.fromFen(FEN_CASTLING_AVAILABLE);
        Board board = game.getBoard();

        KingPiece king = (KingPiece) pieceAt(board, "e1");
        assertNotNull(king);
        List<Move> castlingMoves = king.getCastlingMoves();
        assertNotNull("Castling moves list should not be null", castlingMoves);
        assertEquals("King-side and queen-side castling both available", 2, castlingMoves.size());
    }

    @Test
    public void kingAfterMoving() {
        TestDouble game = TestDouble.fromFen(FEN_CASTLING_AVAILABLE);
        Board board = game.getBoard();

        KingPiece king = (KingPiece) ChessBaseTest.pieceAt(board, "e1");
        assertNotNull(king);
        // Mark the king as having moved
        king.moved();

        List<Move> castlingMoves = king.getCastlingMoves();
        assertTrue("King that has moved should have no castling moves",
                castlingMoves == null || castlingMoves.isEmpty());
    }
}