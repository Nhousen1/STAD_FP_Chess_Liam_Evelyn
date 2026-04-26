package com.chess.blackbox.gamestate;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.pieces.Piece;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Black-box tests for stalemate detection using EP, BA, and EG.
 *
 * Spec (FIDE Laws 9.6): stalemate = no legal moves AND king NOT in check.
 * Key distinction from checkmate: in stalemate the king is safe but has nowhere to go.
 *
 * The SUT calls setDisplay("no playable situation") for both stalemate and checkmate
 * when a position is loaded directly from FEN. We use isPieceEndangered to
 * confirm the king is not in check, which distinguishes stalemate from checkmate.
 */
public class StalemateTest extends ChessTestBase {

    // Classic stalemate: black king h8 stalemated by white queen on g6, white king a1
    // Black to move, black king has no legal moves and is NOT in check
    private static final String FEN_CLASSIC_STALEMATE =
            "7k/8/6Q1/8/8/8/8/K7 b - - 0 1";

    // Another stalemate: black king a8, white queen on b6, white king c6
    private static final String FEN_STALEMATE_CORNER =
            "k7/8/1QK5/8/8/8/8/8 b - - 0 1";

    // NOT stalemate: black king has one escape move (d6 is safe)
    private static final String FEN_NOT_STALEMATE =
            "4k3/8/4R3/8/8/8/8/4K3 b - - 0 1";

    // Checkmate position for comparison (king IS in check) — Fool's mate
    private static final String FEN_CHECKMATE_FOR_COMPARISON =
            "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3";

    // -------------------------------------------------------------------------
    // Equivalence Partitioning
    // -------------------------------------------------------------------------

    /**
     * [EP] Classic stalemate position: controller called with "no playable situation".
     * Partition: current player has no legal moves, king not in check.
     * Goal: stalemate detected and game ends correctly.
     */
    @Test
    public void testClassicStalemateIsDetectedAsGameOver() {
        createBoard(FEN_CLASSIC_STALEMATE);
        verifyDisplayContains("no playable situation");
    }

    /**
     * [EP] Classic stalemate: king is NOT in check (distinguishes from checkmate).
     * Goal: isPieceEndangered = false confirms this is stalemate, not checkmate.
     */
    @Test
    public void testClassicStalemateKingIsNotInCheck() {
        Board board = createBoard(FEN_CLASSIC_STALEMATE);
        Piece blackKing = board.getKing(true);
        List<Piece> whitePieces = board.getPieces(false);
        assertFalse("In stalemate, the king must NOT be in check",
                board.isPieceEndangered(blackKing, whitePieces));
    }

    /**
     * [EP] Corner stalemate: another known stalemate position.
     * Partition: stalemate with king cornered at a8.
     */
    @Test
    public void testCornerStalemateIsDetected() {
        createBoard(FEN_STALEMATE_CORNER);
        verifyDisplayContains("no playable situation");
    }

    /**
     * [EP] NOT a stalemate: black king has an escape move — game continues.
     * Partition: current player has at least one legal move.
     */
    @Test
    public void testNotStalemateWhenKingHasEscapeMove() {
        createBoard(FEN_NOT_STALEMATE);
        verifyDisplayNotContains("no playable situation");
        verifyDisplayNotContains("STALEMATE");
    }

    // -------------------------------------------------------------------------
    // Error Guessing
    // -------------------------------------------------------------------------

    /**
     * [EG] Stalemate vs checkmate distinction (critical correctness test).
     * In stalemate: isPieceEndangered = false.
     * In checkmate: isPieceEndangered = true.
     * Fault class: confusing stalemate with checkmate.
     */
    @Test
    public void testStalemateAndCheckmateDistinctByCheckStatus() {
        // Stalemate: king NOT in check
        Board stalemateBoard = createBoard(FEN_CLASSIC_STALEMATE);
        Piece stalemateKing = stalemateBoard.getKing(true);
        assertFalse("Stalemate king should NOT be in check",
                stalemateBoard.isPieceEndangered(stalemateKing, stalemateBoard.getPieces(false)));

        // Checkmate: king IS in check
        Board checkmateBoard = createBoard(FEN_CHECKMATE_FOR_COMPARISON);
        Piece checkmateKing = checkmateBoard.getKing(false);
        assertTrue("Checkmate king should be in check",
                checkmateBoard.isPieceEndangered(checkmateKing, checkmateBoard.getPieces(true)));
    }

    /**
     * [EG] Corner stalemate: king at a8 is not in check.
     * Fault class: queen on b6 may appear to attack a8, but it's not a check.
     */
    @Test
    public void testCornerStalemateKingNotInCheck() {
        Board board = createBoard(FEN_STALEMATE_CORNER);
        Piece blackKing = board.getKing(true);
        assertFalse("Black king on a8 should not be in check in corner stalemate",
                board.isPieceEndangered(blackKing, board.getPieces(false)));
    }
}
