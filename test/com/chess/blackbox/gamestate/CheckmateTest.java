package com.chess.blackbox.gamestate;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.pieces.Piece;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Black-box tests for checkmate detection using EP, BA, and EG.
 *
 * Spec (FIDE Laws 5.1.1): checkmate = no legal moves AND king in check.
 *
 * When loading a checkmate position via FEN (no prior move played in this session),
 * the SUT calls setDisplay("no playable situation"). We also confirm the king is
 * in check using isPieceEndangered to distinguish checkmate from stalemate.
 */
public class CheckmateTest extends ChessTestBase {

    // Fool's mate (2-move) — white to move, white is in checkmate
    // 1. f3 e5  2. g4 Qh4#
    private static final String FEN_FOOLS_MATE =
            "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3";

    // Back-rank checkmate: black king a1, white rook on a8 gives check on a-file,
    // white king on c1 covers b1 and b2. Black king has no escape.
    private static final String FEN_BACK_RANK_MATE =
            "R7/8/8/8/8/8/8/k1K5 b - - 0 1";

    // Smothered mate: black king h8 checked by white knight f7; own rook g8 and pawns g7/h7 block all escapes
    // Knight on f7 attacks h8; king can't go g8 (own rook), g7 (own pawn), h7 (own pawn)
    private static final String FEN_SMOTHERED_MATE =
            "6rk/5Npp/8/8/8/8/8/6K1 b - - 0 1";

    // Not checkmate: white king on d1 (not on e-file, so NOT in check by black rook on e8).
    // White has legal escape moves (c1, c2, d2). Black king on g1.
    private static final String FEN_NEAR_CHECKMATE_NOT_MATE =
            "4r3/8/8/8/8/8/8/3K2k1 w - - 0 1";

    // -------------------------------------------------------------------------
    // Equivalence Partitioning
    // -------------------------------------------------------------------------

    /**
     * [EP] Fool's mate position: white is in checkmate.
     * Loaded via FEN (no prior move in this session) → "no playable situation".
     * Additionally: white king must be in check (isPieceEndangered = true).
     * Goal: checkmate detected on board construction from a known FEN.
     */
    @Test
    public void testFoolsMateIsDetectedAsGameOver() {
        createBoard(FEN_FOOLS_MATE);
        // Controller must have been called with either "CHECKMATE" or "no playable situation"
        boolean checkmate = false;
        try {
            verifyDisplayContains("CHECKMATE");
            checkmate = true;
        } catch (AssertionError ignored) {}
        if (!checkmate) {
            verifyDisplayContains("no playable situation");
        }
    }

    /**
     * [EP] Fool's mate: white king must be in check in that position.
     * Goal: the check flag is correctly set for the mated king.
     */
    @Test
    public void testFoolsMateKingIsInCheck() {
        Board board = createBoard(FEN_FOOLS_MATE);
        Piece whiteKing = board.getKing(false);
        List<Piece> blackPieces = board.getPieces(true);
        assertTrue("White king should be in check in Fool's mate position",
                board.isPieceEndangered(whiteKing, blackPieces));
    }

    /**
     * [EP] Back-rank checkmate: black king is mated.
     * Goal: checkmate detected via game-end callback.
     */
    @Test
    public void testBackRankMateIsDetectedAsGameOver() {
        createBoard(FEN_BACK_RANK_MATE);
        boolean detected = false;
        try {
            verifyDisplayContains("CHECKMATE");
            detected = true;
        } catch (AssertionError ignored) {}
        if (!detected) {
            verifyDisplayContains("no playable situation");
        }
    }

    /**
     * [EP] Back-rank mate: black king is in check.
     */
    @Test
    public void testBackRankMateKingIsInCheck() {
        Board board = createBoard(FEN_BACK_RANK_MATE);
        Piece blackKing = board.getKing(true);
        List<Piece> whitePieces = board.getPieces(false);
        assertTrue("Black king should be in check in back-rank mate",
                board.isPieceEndangered(blackKing, whitePieces));
    }

    // -------------------------------------------------------------------------
    // Error Guessing
    // -------------------------------------------------------------------------

    /**
     * [EG] Near-checkmate position with one escape square: game is NOT over.
     * Fault class: false positive — mated when king still has a legal escape.
     * Goal: controller NOT called with game-over message; game continues.
     */
    @Test
    public void testNearCheckmateWithEscapeIsNotMate() {
        createBoard(FEN_NEAR_CHECKMATE_NOT_MATE);
        // The king has a legal escape — verify game NOT ended with "no playable situation"
        verifyDisplayNotContains("no playable situation");
        verifyDisplayNotContains("CHECKMATE");
    }

    /**
     * [EG] Smothered mate (knight delivers checkmate, king surrounded by own pieces).
     * Fault class: knight-delivered checkmate not recognized.
     * Goal: checkmate detected even when delivered by a non-sliding piece.
     */
    @Test
    public void testSmotheredMateIsDetectedAsGameOver() {
        createBoard(FEN_SMOTHERED_MATE);
        boolean detected = false;
        try {
            verifyDisplayContains("CHECKMATE");
            detected = true;
        } catch (AssertionError ignored) {}
        if (!detected) {
            verifyDisplayContains("no playable situation");
        }
    }

    /**
     * [EG] Smothered mate: black king is in check.
     */
    @Test
    public void testSmotheredMateKingIsInCheck() {
        Board board = createBoard(FEN_SMOTHERED_MATE);
        Piece blackKing = board.getKing(true);
        List<Piece> whitePieces = board.getPieces(false);
        assertTrue("Black king should be in check in smothered mate",
                board.isPieceEndangered(blackKing, whitePieces));
    }

    /**
     * [EG] Stalemate vs checkmate: in a checkmate position, the king IS in check.
     * This cross-validates checkmate positions to distinguish from stalemate.
     * Fault class: stalemate incorrectly reported as checkmate or vice versa.
     */
    @Test
    public void testCheckmateKingAlwaysInCheck() {
        // Use Fool's mate as canonical checkmate example
        Board board = createBoard(FEN_FOOLS_MATE);
        Piece king = board.getKing(false); // white to move, white is mated
        List<Piece> opponents = board.getPieces(true);
        assertTrue("In any checkmate, the king must be in check",
                board.isPieceEndangered(king, opponents));
    }
}
