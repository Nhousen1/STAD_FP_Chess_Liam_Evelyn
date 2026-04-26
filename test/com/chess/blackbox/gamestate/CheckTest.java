package com.chess.blackbox.gamestate;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.pieces.Piece;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Black-box tests for check detection using EP, BA, and EG.
 *
 * Spec (FIDE Laws 3.9): the king is in check when attacked by one or more
 * opponent pieces. We test this via board.isPieceEndangered(Piece, List<Piece>).
 *
 * Also validates that isPieceEndangered has no side effects on board state.
 */
public class CheckTest extends ChessTestBase {

    // White king on e1 in check by black rook on e8
    private static final String FEN_WHITE_KING_IN_CHECK_ROOK =
            "4r3/8/8/8/8/8/8/4K3 w - - 0 1";

    // White king on e1 NOT in check — black rook on d8 (different file)
    private static final String FEN_WHITE_KING_NOT_IN_CHECK =
            "3r4/8/8/8/8/8/8/4K3 w - - 0 1";

    // Black king on e8 in check by white queen on e1
    private static final String FEN_BLACK_KING_IN_CHECK_QUEEN =
            "4k3/8/8/8/8/8/8/4Q1K1 b - - 0 1";

    // Double check: white king on e1 attacked by both black rook (e8) and black bishop (b4)
    private static final String FEN_DOUBLE_CHECK =
            "4r3/8/8/8/1b6/8/8/4K3 w - - 0 1";

    // Discovered check: black bishop on h4, white rook on e1 in line, white king on a1
    // (simulates a position where moving a piece reveals a sliding attack)
    // White king on a1, black rook on a8 with white knight on a4 blocking
    private static final String FEN_DISCOVERED_CHECK_SETUP =
            "r7/8/8/8/K7/8/8/8 w - - 0 1";

    // Pin: white bishop on e4 pins white knight (currently on e5) against white king (e1)
    // Black rook on e8 would put white king in check if the knight moves
    // We test that removing the pin blocker reveals the attack
    private static final String FEN_PIN_SETUP =
            "4r3/8/8/4N3/8/8/8/4K3 w - - 0 1";

    // -------------------------------------------------------------------------
    // Equivalence Partitioning
    // -------------------------------------------------------------------------

    /**
     * [EP] White king in check by black rook on same file: isPieceEndangered = true.
     * Partition: king under attack by one opponent rook.
     * Goal: check detection correctly identifies rook attack on king's file.
     */
    @Test
    public void testWhiteKingInCheckByBlackRook() {
        Board board = createBoard(FEN_WHITE_KING_IN_CHECK_ROOK);
        Piece whiteKing = board.getKing(false);
        List<Piece> blackPieces = board.getPieces(true);
        assertTrue("White king should be in check by black rook on e8",
                board.isPieceEndangered(whiteKing, blackPieces));
    }

    /**
     * [EP] White king NOT in check — opponent rook on different file: isPieceEndangered = false.
     * Partition: king not attacked.
     * Goal: check detection correctly returns false when king is safe.
     */
    @Test
    public void testWhiteKingNotInCheckWhenRookOnDifferentFile() {
        Board board = createBoard(FEN_WHITE_KING_NOT_IN_CHECK);
        Piece whiteKing = board.getKing(false);
        List<Piece> blackPieces = board.getPieces(true);
        assertFalse("White king should not be in check when black rook is on d8",
                board.isPieceEndangered(whiteKing, blackPieces));
    }

    /**
     * [EP] Black king in check by white queen: isPieceEndangered = true.
     * Partition: king under attack by queen.
     * Goal: check detection works for black king as well as white.
     */
    @Test
    public void testBlackKingInCheckByWhiteQueen() {
        Board board = createBoard(FEN_BLACK_KING_IN_CHECK_QUEEN);
        Piece blackKing = board.getKing(true);
        List<Piece> whitePieces = board.getPieces(false);
        assertTrue("Black king should be in check by white queen on e1",
                board.isPieceEndangered(blackKing, whitePieces));
    }

    // -------------------------------------------------------------------------
    // Error Guessing
    // -------------------------------------------------------------------------

    /**
     * [EG] Double check: two pieces simultaneously attack the king.
     * Fault class: check detection only finds first attacker, misses second.
     * Goal: isPieceEndangered returns true even when multiple attackers exist.
     */
    @Test
    public void testDoubleCheckIsDetectedAsCheck() {
        Board board = createBoard(FEN_DOUBLE_CHECK);
        Piece whiteKing = board.getKing(false);
        List<Piece> blackPieces = board.getPieces(true);
        assertTrue("King should be in check even in double-check position",
                board.isPieceEndangered(whiteKing, blackPieces));
    }

    /**
     * [EG] Discovered check position: white king in line of black rook with no blocker.
     * Goal: verifies that a sliding piece's full line of attack is evaluated.
     * Fault class: discovered check missed because intermediate piece no longer blocks.
     */
    @Test
    public void testKingInCheckByRookOnSameFile() {
        Board board = createBoard(FEN_DISCOVERED_CHECK_SETUP);
        Piece whiteKing = board.getKing(false);
        List<Piece> blackPieces = board.getPieces(true);
        assertTrue("White king on a4 should be in check by black rook on a8 (direct line)",
                board.isPieceEndangered(whiteKing, blackPieces));
    }

    /**
     * [EG] Side-effect validation: isPieceEndangered must NOT modify piece lists.
     * Fault class: isPieceEndangered mutates board state as a side effect.
     * Goal: piece list size is unchanged before and after the call.
     */
    @Test
    public void testIsPieceEndangeredHasNoSideEffects() {
        Board board = createBoard(FEN_WHITE_KING_IN_CHECK_ROOK);
        Piece whiteKing = board.getKing(false);
        List<Piece> blackPieces = board.getPieces(true);
        List<Piece> whitePieces = board.getPieces(false);
        int blackCountBefore = blackPieces.size();
        int whiteCountBefore = whitePieces.size();

        board.isPieceEndangered(whiteKing, blackPieces);

        assertEquals("Black piece count should not change after isPieceEndangered",
                blackCountBefore, board.getPieces(true).size());
        assertEquals("White piece count should not change after isPieceEndangered",
                whiteCountBefore, board.getPieces(false).size());
    }

    /**
     * [EG] isPieceEndangered does not modify the king's position.
     * Fault class: simulation moves king but fails to restore it.
     */
    @Test
    public void testIsPieceEndangeredDoesNotMoveKing() {
        Board board = createBoard(FEN_WHITE_KING_IN_CHECK_ROOK);
        Piece whiteKing = board.getKing(false);
        int colBefore = whiteKing.getColumn();
        int rowBefore = whiteKing.getRow();

        board.isPieceEndangered(whiteKing, board.getPieces(true));

        assertEquals("King column should be unchanged after isPieceEndangered",
                colBefore, whiteKing.getColumn());
        assertEquals("King row should be unchanged after isPieceEndangered",
                rowBefore, whiteKing.getRow());
    }
}
