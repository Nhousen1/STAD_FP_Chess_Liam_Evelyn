package com.chess.blackbox.moves;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.moves.Move;
import com.chess.root.moves.PassingMove;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Black-box tests for en passant (PassingMove) using EP, BA, and EG.
 *
 * Spec (FIDE Laws 3.7.4): a pawn that advances two squares may be captured
 * by an adjacent opponent pawn on the very next move, landing behind the pawn.
 *
 * In the SUT, board.getEnPassantPiece() returns the double-pushed pawn (or null),
 * and PassingMove is the subclass generated for the capture.
 *
 * FEN ep target on rank 3 = white just double-pushed (black can capture).
 * FEN ep target on rank 6 = black just double-pushed (white can capture).
 */
public class EnPassantTest extends ChessTestBase {

    // White pawn on c4 (col=2, row=4) just double-pushed; black pawn on d4 (col=3, row=4) adjacent
    // EP target: c3 (col=2, row=5). Black to move.
    private static final String FEN_EP_AVAILABLE =
            "4k3/8/8/8/2Pp4/8/8/4K3 b - c3 0 1";

    // Same position but no en passant (dash)
    private static final String FEN_NO_EP =
            "4k3/8/8/8/2Pp4/8/8/4K3 b - - 0 1";

    // En passant with capturing pawn on LEFT edge: black pawn on a4 (col=0, row=4),
    // white pawn just pushed to b4 (col=1, row=4), ep target b3 (col=1, row=5). Black to move.
    private static final String FEN_EP_EDGE_FILE =
            "4k3/8/8/8/pP6/8/8/4K3 b - b3 0 1";

    // En passant with capturing pawn on RIGHT edge: black pawn on h4 (col=7, row=4),
    // white pawn just pushed to g4 (col=6, row=4), ep target g3 (col=6, row=5). Black to move.
    private static final String FEN_EP_RIGHT_EDGE =
            "4k3/8/8/8/6Pp/8/8/4K3 b - g3 0 1";

    // Invalid en passant square "e4" (rank digit 4, not 3 or 6) — SUT silently treats as absent
    private static final String FEN_EP_INVALID_RANK =
            "4k3/8/8/8/2Pp4/8/8/4K3 b - e4 0 1";

    // -------------------------------------------------------------------------
    // Equivalence Partitioning
    // -------------------------------------------------------------------------

    /**
     * [EP] FEN with valid en passant field "c3": getEnPassantPiece() returns non-null.
     * Partition: en passant pawn available.
     * Goal: FEN en passant field is parsed and stored correctly.
     */
    @Test
    public void testEnPassantPieceAvailableAfterDoublePush() {
        Board board = createBoard(FEN_EP_AVAILABLE);
        assertNotNull("En passant piece should be non-null when FEN has ep field",
                board.getEnPassantPiece());
    }

    /**
     * [EP] FEN with en passant "-": getEnPassantPiece() returns null.
     * Partition: en passant not available (dash field).
     */
    @Test
    public void testEnPassantPieceNullWhenFenDash() {
        Board board = createBoard(FEN_NO_EP);
        assertNull("En passant piece should be null when FEN ep field is '-'",
                board.getEnPassantPiece());
    }

    /**
     * [EP] Capturing pawn adjacent to en passant piece: PassingMove appears in getMoves().
     * Partition: valid en passant capture available to the adjacent pawn.
     * Goal: PawnPiece generates PassingMove when board.getEnPassantPiece() is set.
     *
     * Black pawn on d4 (col=3, row=4) can capture white pawn on c4 via ep to c3.
     */
    @Test
    public void testPassingMoveGeneratedForAdjacentPawn() {
        Board board = createBoard(FEN_EP_AVAILABLE);
        List<Move> moves = getMovesForPiece(board, 3, 4); // d4 black pawn
        boolean hasPassingMove = moves.stream().anyMatch(m -> m instanceof PassingMove);
        assertTrue("Adjacent pawn should have a PassingMove available", hasPassingMove);
    }

    // -------------------------------------------------------------------------
    // Boundary Analysis
    // -------------------------------------------------------------------------

    /**
     * [BA] En passant capturing pawn on left edge file a (col=0): at most 1 PassingMove.
     * The capturing pawn on a4 can only capture RIGHT (to b3), not left (off-board).
     * Boundary: left-edge capturing pawn.
     */
    @Test
    public void testEnPassantOnLeftEdgeFileHasAtMostOnePassingMove() {
        Board board = createBoard(FEN_EP_EDGE_FILE);
        assertNotNull("En passant piece should be set for b3 target", board.getEnPassantPiece());
        // Black pawn on a4 (col=0, row=4) — on the LEFT edge, can only capture right
        List<Move> moves = getMovesForPiece(board, 0, 4); // a4
        long passingMoves = moves.stream().filter(m -> m instanceof PassingMove).count();
        assertTrue("Edge-file pawn should have at most 1 en passant capture", passingMoves <= 1);
    }

    /**
     * [BA] En passant capturing pawn on right edge file h (col=7): at most 1 PassingMove.
     * Boundary: right-edge capturing pawn.
     */
    @Test
    public void testEnPassantOnRightEdgeFileHasAtMostOnePassingMove() {
        Board board = createBoard(FEN_EP_RIGHT_EDGE);
        assertNotNull("En passant piece should be set for g3 target", board.getEnPassantPiece());
        // Black pawn on h4 (col=7, row=4) — on RIGHT edge, can only capture left
        List<Move> moves = getMovesForPiece(board, 7, 4); // h4
        long passingMoves = moves.stream().filter(m -> m instanceof PassingMove).count();
        assertTrue("Right-edge pawn should have at most 1 en passant capture", passingMoves <= 1);
    }

    // -------------------------------------------------------------------------
    // Error Guessing
    // -------------------------------------------------------------------------

    /**
     * [EG] FEN en passant field on invalid rank "e4" (rank digit 4, not 3 or 6):
     * getEnPassantPiece() must return null — the SUT only handles ranks 3 and 6.
     * Fault: SUT silently ignores invalid ep ranks; this test documents that behavior.
     */
    @Test
    public void testEnPassantWithInvalidFenRankReturnsNull() {
        Board board = createBoard(FEN_EP_INVALID_RANK);
        assertNull("Invalid en passant rank in FEN should yield null getEnPassantPiece()",
                board.getEnPassantPiece());
    }

    /**
     * [EG] En passant capture: victim field differs from the landing (target) field.
     * FIDE rule: the captured pawn is removed from the square it occupies, NOT
     * the square the capturing pawn lands on.
     * Goal: validate that PassingMove records these as distinct fields.
     * Fault class: victim left on board, or victim/target fields confused.
     *
     * Black pawn on d4 captures white pawn on c4 by landing on c3.
     * Victim field = c4 (col=2, row=4); target field = c3 (col=2, row=5) — must differ.
     */
    @Test
    public void testEnPassantCaptureVictimIsOnSeparateSquareFromTarget() {
        Board board = createBoard(FEN_EP_AVAILABLE);
        List<Move> moves = getMovesForPiece(board, 3, 4); // d4 black pawn
        PassingMove passing = (PassingMove) moves.stream()
                .filter(m -> m instanceof PassingMove)
                .findFirst()
                .orElse(null);
        assertNotNull("PassingMove should be available", passing);
        // Verify that the victim square and the target square differ (fundamental en passant rule)
        assertNotEquals("En passant victim field must differ from the target (landing) field",
                passing.getVictimField(), passing.getField());
        // Verify the victim piece is on the correct field (c4 = col=2, row=4) before capture
        assertNotNull("Victim piece should be on c4 before capture",
                passing.getVictimField().getPiece());
    }
}
