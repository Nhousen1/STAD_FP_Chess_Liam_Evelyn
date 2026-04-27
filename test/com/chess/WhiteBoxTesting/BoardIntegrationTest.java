package com.chess.WhiteBoxTesting;

import com.chess.root.Board;
import com.chess.root.moves.Move;
import com.chess.root.pieces.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for Board-Piece interactions.
 *
 * Strategy (from proposal):
 *   "Execute a sequence of moves that will expect different board states.
 *    Assert that move execution process is valid from beginning to end for
 *    each possible state including checkmate, stalemate, and en passant
 *    lifecycle, timeout states."
 *
 * Tests use Board.validateBoard() return value (true = game ended) and
 * piece presence / absence to verify board state after each move sequence.
 */
public class BoardIntegrationTest extends ChessBaseTest {

    // ====================================================================
    // Basic move execution
    // ====================================================================

    @Test
    public void executePawnAdvanceChangesBoard() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        Board board = game.getBoard();

        Piece pawn = pieceAt(board, "e2");
        assertNotNull(pawn);

        List<Move> moves = pawn.getMoves();
        Move e4move = findMove(moves, "e4");
        assertNotNull("e4 move should exist", e4move);

        pawn.getField().removePiece(false);
        e4move.execute(board);

        Piece afterMove = pieceAt(board, "e4");
        assertNotNull("Pawn should now be on e4", afterMove);
        assertTrue("Piece on e4 should be a pawn", afterMove instanceof PawnPiece);
        assertNull("e2 should be empty after move", pieceAt(board, "e2"));
    }

    @Test
    public void executeCaptureRemovesVictimFromBoard() {
        // White rook can capture a black pawn
        String fen = "k7/p7/8/8/8/8/8/R6K w - - 0 1";
        TestDouble game = TestDouble.fromFen(fen);
        Board board = game.getBoard();

        Piece rook = pieceAt(board, "a1");
        assertNotNull(rook);

        List<Move> moves = rook.getMoves();
        Move capture = findMove(moves, "a7");
        assertNotNull("Rook should be able to reach a7", capture);

        rook.getField().removePiece(false);
        capture.execute(board);

        assertNotNull("Rook should be on a7", pieceAt(board, "a7"));
        assertTrue(pieceAt(board, "a7") instanceof RookPiece);

        assertFalse("Captured black pawn should not be in black pieces list",
                board.getPieces(true).stream()
                        .anyMatch(p -> p instanceof PawnPiece
                                && "a7".equals(p.getField().getNotation())));
    }

    // ====================================================================
    // Undo move
    // ====================================================================

    @Test
    public void undoMoveAfterPawnAdvance() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        Board board = game.getBoard();

        Piece pawn = pieceAt(board, "e2");
        assertNotNull(pawn);
        List<Move> moves = pawn.getMoves();
        Move e4 = findMove(moves, "e4");
        assertNotNull(e4);

        pawn.getField().removePiece(false);
        e4.execute(board);

        // Now undo
        board.undoMove(e4);

        assertNotNull("Pawn should be back on e2 after undo", pieceAt(board, "e2"));
        assertNull("e4 should be empty after undo", pieceAt(board, "e4"));
    }

    // ====================================================================
    // Castling lifecycle
    // ====================================================================

    @Test
    public void castlingKingSide() {
        TestDouble game = TestDouble.fromFen(FEN_CASTLING_AVAILABLE);
        Board board = game.getBoard();

        KingPiece king = (KingPiece) pieceAt(board, "e1");
        assertNotNull(king);
        List<Move> castlingMoves = king.getCastlingMoves();
        assertNotNull(castlingMoves);

        Move kingSide = castlingMoves.stream()
                .filter(m -> m.getNotation().contains("O-O") && !m.getNotation().contains("O-O-O"))
                .findFirst().orElse(null);
        assertNotNull("King-side castling move must exist", kingSide);

        king.getField().removePiece(false);
        kingSide.execute(board);

        assertNotNull("King should be on g1 after king-side castling", pieceAt(board, "g1"));
        assertTrue(pieceAt(board, "g1") instanceof KingPiece);
        assertNotNull("Rook should be on f1 after king-side castling", pieceAt(board, "f1"));
        assertTrue(pieceAt(board, "f1") instanceof RookPiece);
    }

    // ====================================================================
    // En passant lifecycle
    // ====================================================================

    @Test
    public void enPassantPassedPawnRemovedAfterCapture() {
        TestDouble game = TestDouble.fromFen(FEN_EN_PASSANT);
        Board board = game.getBoard();

        Piece whitePawn = pieceAt(board, "e5");
        assertNotNull(whitePawn);
        List<Move> moves = whitePawn.getMoves();

        // Find the en-passant move
        Move epMove = moves.stream()
                .filter(m -> m instanceof com.chess.root.moves.PassingMove)
                .findFirst().orElse(null);
        assertNotNull("En passant move should exist", epMove);

        whitePawn.getField().removePiece(false);
        epMove.execute(board);

        assertNull("Black pawn on d5 should be captured via en passant", pieceAt(board, "d5"));
    }

    // ====================================================================
    // Check detection
    // ====================================================================

    @Test
    public void kingInCheck() {
        String fen = "4k3/8/8/7Q/8/8/8/K7 b - - 0 1";
        TestDouble game = TestDouble.fromFen(fen);
        Board board = game.getBoard();

        // validateBoard will compute legal moves for black
        board.validateBoard();

        // Black king must have moves that escape check
        List<Move> currentMoves = board.getPieces(true).stream()
                .reduce(new java.util.ArrayList<>(),
                        (acc, p) -> { acc.addAll(p.getMoves()); return acc; },
                        (a, b) -> { a.addAll(b); return a; });
        // At minimum the king must be able to flee
        assertFalse("Black should have at least one legal move to escape check",
                currentMoves.isEmpty());
    }

    // ====================================================================
    // Checkmate
    // ====================================================================

    @Test
    public void checkmateNoLegalMovesForLoser() {
        TestDouble game = TestDouble.fromFen(FEN_CHECKMATE_WHITE);
        Board board = game.getBoard();

        // validateBoard should detect the game is over
        boolean ended = board.validateBoard();
        // The board should have no current moves for white
        assertTrue("validateBoard should return true for checkmate position", ended);
    }

    // ====================================================================
    // Stalemate
    // ====================================================================

    @Test
    public void stalematePosition() {
        TestDouble game = TestDouble.fromFen(FEN_STALEMATE);
        Board board = game.getBoard();

        boolean ended = board.validateBoard();
        // In stalemate the game should be considered ended
        assertTrue("validateBoard should return true for stalemate position", ended);
    }

    // ====================================================================
    // Pawn promotion – board reflects new queen
    // ====================================================================

    @Test
    public void whitePawnBecomesQueen() {
        TestDouble game = TestDouble.fromFen(FEN_PROMOTION);
        Board board = game.getBoard();

        Piece pawn = pieceAt(board, "e7");
        assertNotNull(pawn);
        List<Move> moves = pawn.getMoves();

        Move promo = moves.stream()
                .filter(m -> m instanceof com.chess.root.moves.PromotionMove)
                .findFirst().orElse(null);
        assertNotNull(promo);

        pawn.getField().removePiece(false);
        promo.execute(board);

        Piece promoted = pieceAt(board, "e8");
        assertNotNull("Promoted piece should be on e8", promoted);
        assertTrue("Promoted piece should be a queen", promoted instanceof QueenPiece);
    }

    // ====================================================================
    // Fifty-move / timeout draw rule
    // ====================================================================

    @Test
    public void fiftyMoveRuleEndsGame() {
        // Must use a setting where timeout is ENABLED
        com.chess.model.Setting s = new com.chess.model.Setting(
                true, com.chess.model.Mode.MANUAL_ONLY,
                com.chess.model.PieceValues.SUPERSUPERHARD,
                com.chess.model.Difficulty.SUPERSUPERHARD);
        s.setGrid(new javafx.scene.layout.GridPane());
        String[] parts = FEN_KINGS_ONLY.split(" ");
        s.setFenBoard(com.chess.root.FenParser.parseBoard(parts[0]));
        s.setCompleteFen(parts);

        TestDouble game = new TestDouble(s);
        Board board = game.getBoard();

        board.setCountdown(102);
        boolean ended = board.validateBoard();
        assertTrue(ended);
    }

    // ====================================================================
    // Piece counts after capture
    // ====================================================================

    @Test
    public void pieceCountDecreasesAfterCapture() {
        String fen = "r6k/8/8/8/8/8/8/R6K w - - 0 1";
        TestDouble game = TestDouble.fromFen(fen);
        Board board = game.getBoard();

        int initialBlackCount = board.getPieces(true).size();

        Piece rook = pieceAt(board, "a1");
        assertNotNull(rook);
        List<Move> moves = rook.getMoves();
        Move capture = findMove(moves, "a8");
        assertNotNull(capture);

        rook.getField().removePiece(false);
        capture.execute(board);

        assertEquals("Black piece count should decrease by 1 after capture",
                initialBlackCount - 1, board.getPieces(true).size());
    }

    // ====================================================================
    // Board.getKing helper
    // ====================================================================

    @Test
    public void getKingReturnsCorrectKing() {
        TestDouble game = TestDouble.fromFen(FEN_START);
        Board board = game.getBoard();

        Piece whiteKing = board.getKing(false);
        Piece blackKing = board.getKing(true);

        assertNotNull(whiteKing);
        assertNotNull(blackKing);
        assertTrue(whiteKing instanceof KingPiece);
        assertTrue(blackKing instanceof KingPiece);
        assertFalse("White king should not be black", whiteKing.isBlack());
        assertTrue("Black king should be black", blackKing.isBlack());
    }

    // ====================================================================
    // Board.isPieceEndangered
    // ====================================================================

    @Test
    public void kingUnderAttackReturnsTrue() {
        String fen = "4k3/8/8/8/8/8/8/4QK2 b - - 0 1";
        TestDouble game = TestDouble.fromFen(fen);
        Board board = game.getBoard();

        Piece blackKing = board.getKing(true);
        assertNotNull(blackKing);

        boolean endangered = board.isPieceEndangered(blackKing, board.getPieces(false));
        assertTrue(endangered);
    }

    @Test
    public void safeKingReturnsFalse() {
        TestDouble game = TestDouble.fromFen(FEN_KINGS_ONLY);
        Board board = game.getBoard();

        Piece whiteKing = board.getKing(false);
        boolean endangered = board.isPieceEndangered(whiteKing, board.getPieces(true));
        assertFalse(endangered);
    }
}
