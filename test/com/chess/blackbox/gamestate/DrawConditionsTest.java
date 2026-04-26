package com.chess.blackbox.gamestate;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.FenParser;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Black-box tests for draw conditions using EP, BA, and EG.
 *
 * Specs tested:
 *   - FIDE 9.3: 50-move rule (100 half-moves without pawn move or capture)
 *   - FIDE 6.10: Insufficient material (K vs K, K+B vs K, K+N vs K)
 *
 * Boundary finding: the SUT uses countdown > 100 (strict). A half-move clock of
 * 50 with white to move gives countdown=100, and 100 > 100 is false, so no draw.
 * This is one half-move off from what FIDE requires (should be >= 100).
 * See testClock50WhiteTurnIsNotDrawDueToBugInSut() for the specific boundary test.
 */
public class DrawConditionsTest extends ChessTestBase {

    // Bare kings — standard insufficient material (K vs K)
    private static final String FEN_KING_VS_KING =
            "4k3/8/8/8/8/8/8/4K3 w - - 0 1";

    // K+B vs K — insufficient material
    private static final String FEN_KING_BISHOP_VS_KING =
            "4k3/8/8/8/8/8/2B5/4K3 w - - 0 1";

    // K+N vs K — insufficient material
    private static final String FEN_KING_KNIGHT_VS_KING =
            "4k3/8/8/8/8/8/2N5/4K3 w - - 0 1";

    // K+Q vs K — NOT insufficient material (queen can force mate)
    private static final String FEN_KING_QUEEN_VS_KING =
            "4k3/8/8/8/8/8/2Q5/4K3 w - - 0 1";

    // K+R vs K — NOT insufficient material
    private static final String FEN_KING_ROOK_VS_KING =
            "4k3/8/8/8/8/8/2R5/4K3 w - - 0 1";

    // K+2N vs K — theoretically cannot force mate but SUT behavior may vary
    private static final String FEN_KING_2N_VS_KING =
            "4k3/8/8/8/8/8/2N5/2N1K3 w - - 0 1";

    // K+R vs K used for 50-move tests: sufficient material (avoids immediate insufficient-material draw)
    // Black king e8, white rook a2, white king e1. Black king has 5 legal moves — no immediate game end.
    // 50-move rule: clock=49, white's turn → countdown=98 → NOT triggered
    private static final String FEN_49_HALFMOVES_WHITE =
            "4k3/8/8/8/8/8/R7/4K3 w - - 49 25";

    // 50-move rule: clock=50, white's turn → countdown=100 → 100>100 is FALSE → NOT triggered
    // Documents the off-by-one bug: FIDE says draw at 50 half-moves, SUT uses strict > 100.
    private static final String FEN_50_HALFMOVES_WHITE =
            "4k3/8/8/8/8/8/R7/4K3 w - - 50 25";

    // 50-move rule: clock=50, black's turn → countdown=101 → 101>100 → DRAW triggered
    private static final String FEN_50_HALFMOVES_BLACK =
            "4k3/8/8/8/8/8/R7/4K3 b - - 50 25";

    // 50-move rule: clock=51, white's turn → countdown=102 → DRAW triggered
    private static final String FEN_51_HALFMOVES_WHITE =
            "4k3/8/8/8/8/8/R7/4K3 w - - 51 26";

    // -------------------------------------------------------------------------
    // Equivalence Partitioning — Insufficient Material
    // -------------------------------------------------------------------------

    /**
     * [EP] King vs King: draw by insufficient material.
     * Partition: neither player has mating material.
     * Goal: SUT correctly detects K vs K as a draw.
     */
    @Test
    public void testKingVsKingIsDrawByInsufficientMaterial() {
        createBoard(FEN_KING_VS_KING);
        verifyDisplayContains("DRAW");
    }

    /**
     * [EP] King + Bishop vs King: draw by insufficient material.
     * Partition: lone bishop cannot force mate (FIDE Laws 6.9).
     */
    @Test
    public void testKingBishopVsKingIsDrawByInsufficientMaterial() {
        createBoard(FEN_KING_BISHOP_VS_KING);
        verifyDisplayContains("DRAW");
    }

    /**
     * [EP] King + Knight vs King: draw by insufficient material.
     * Partition: lone knight cannot force mate.
     */
    @Test
    public void testKingKnightVsKingIsDrawByInsufficientMaterial() {
        createBoard(FEN_KING_KNIGHT_VS_KING);
        verifyDisplayContains("DRAW");
    }

    // -------------------------------------------------------------------------
    // Error Guessing — Sufficient Material (should NOT draw)
    // -------------------------------------------------------------------------

    /**
     * [EG] King + Queen vs King: NOT a draw (queen can force mate).
     * Fault class: false-positive insufficient material detection.
     * Goal: queen's presence prevents insufficient material draw.
     */
    @Test
    public void testKingQueenVsKingIsNotInsufficientMaterial() {
        createBoard(FEN_KING_QUEEN_VS_KING);
        verifyDisplayNotContains("DRAW");
    }

    /**
     * [EG] King + Rook vs King: NOT a draw (rook can force mate).
     * Fault class: false-positive insufficient material with rook present.
     */
    @Test
    public void testKingRookVsKingIsNotInsufficientMaterial() {
        createBoard(FEN_KING_ROOK_VS_KING);
        verifyDisplayNotContains("DRAW");
    }

    /**
     * [EG] King + 2 Knights vs King: document SUT behavior (may or may not draw).
     * FIDE technically does not force draw with K+2N, but many implementations do.
     * Goal: observe the SUT's actual behavior — no assertion on the outcome.
     * This is a behavioral characterization test.
     */
    @Test
    public void testKingTwoKnightsVsKingBehavior() {
        // No assertion: just verify no exception is thrown
        try {
            createBoard(FEN_KING_2N_VS_KING);
        } catch (Exception e) {
            fail("SUT should not throw an exception for K+2N vs K: " + e);
        }
    }

    // -------------------------------------------------------------------------
    // Boundary Analysis — 50-Move Rule
    // -------------------------------------------------------------------------

    /**
     * [BA] FEN half-move clock = 49, white's turn → board countdown=98: NOT drawn.
     * Boundary: one half-move below the 50-move rule threshold.
     */
    @Test
    public void testClock49WhiteTurnIsNotDraw() {
        createBoard(FEN_49_HALFMOVES_WHITE);
        verifyDisplayNotContains("DRAW");
    }

    /**
     * [BA] FEN half-move clock = 50, white's turn → board countdown=100: NOT drawn.
     * Boundary: exactly at the 50-move threshold for white.
     *
     * EXPECTED FAULT: By FIDE rules this SHOULD be a draw. The SUT uses `countdown > 100`
     * (strict), so 100 > 100 = false → no draw triggered. This test documents the
     * off-by-one bug in the SUT's 50-move rule implementation.
     */
    @Test
    public void testClock50WhiteTurnIsNotDrawDueToBugInSut() {
        createBoard(FEN_50_HALFMOVES_WHITE);
        // SUT behavior: NOT triggered at clock=50 white's turn (potential fault)
        verifyDisplayNotContains("50 move rule");
    }

    /**
     * [BA] FEN half-move clock = 50, black's turn → board countdown=101: draw IS triggered.
     * Boundary: 101 > 100 = true → draw detected.
     * Goal: verify the draw IS triggered when the countdown tips past 100.
     */
    @Test
    public void testClock50BlackTurnIsDrawByFiftyMoveRule() {
        createBoard(FEN_50_HALFMOVES_BLACK);
        verifyDisplayContains("DRAW");
    }

    /**
     * [BA] FEN half-move clock = 51, white's turn → countdown=102: draw IS triggered.
     * Boundary: well past the threshold; both turn colors trigger draw.
     */
    @Test
    public void testClock51WhiteTurnIsDrawByFiftyMoveRule() {
        createBoard(FEN_51_HALFMOVES_WHITE);
        verifyDisplayContains("DRAW");
    }

    // -------------------------------------------------------------------------
    // FEN Round-Trip — 50-move countdown accuracy
    // -------------------------------------------------------------------------

    /**
     * [EG] FEN export of countdown value matches the loaded half-move clock.
     * Goal: FenParser.getCountdown(board) correctly divides the internal counter
     * back to the standard half-move clock for FEN export.
     * Fault class: countdown is stored/exported with different scaling than loaded.
     */
    @Test
    public void testFenCountdownRoundTrip() {
        Board board = createBoard(FEN_49_HALFMOVES_WHITE); // half-move clock = 49
        String exportedFen = FenParser.build(board);
        // The exported FEN's 5th field (index 4) should be "49"
        String[] parts = exportedFen.split(" ");
        assertTrue("Exported FEN should have at least 5 parts", parts.length >= 5);
        assertEquals("Half-move clock in exported FEN should match loaded value",
                "49", parts[4]);
    }
}
