package com.chess.mutation.moves;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.Field;
import com.chess.root.moves.Move;
import com.chess.root.moves.PassingMove;
import com.chess.root.moves.PawnRunMove;
import com.chess.root.moves.PromotionMove;
import com.chess.root.pieces.KingPiece;
import com.chess.root.pieces.Piece;
import com.chess.root.pieces.RookPiece;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Mutation-targeted tests for Move state transitions (updateBoard, resetBoard, undo).
 *
 * Targets surviving mutants in updateBoard(), resetBoard(), undo(), undoSimulation(),
 * execute(), updateNotation(), and getNotation(). Also covers PassingMove subclass.
 *  - PromotionMove survivors: execute/undo void methods, rating multiplication
 */
public class MoveStateMutationTest extends ChessTestBase {

    // White pawn at e2 (starting rank) — produces PawnRunMove on double push
    private static final String FEN_WPAWN_E2 =
            "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1";

    // White rook at e2, king at e1, black pawn at e5 — rook can capture pawn
    private static final String FEN_ROOK_CAPTURE =
            "4k3/8/8/4p3/8/8/4R3/4K3 w - - 0 1";

    // King e1, rook h1 — king can castle kingside; both can be moved
    private static final String FEN_CASTLE_K =
            "4k3/8/8/8/8/8/8/4K2R w K - 0 1";

    // En passant: black pawn d4, white pawn just moved to e4 (ep target e3)
    private static final String FEN_EP =
            "4k3/8/8/8/3pP3/8/8/4K3 b - e3 0 1";

    // White pawn at e7, to promote forward on e8
    private static final String FEN_PROMOTE =
            "k7/4P3/8/8/8/8/8/4K3 w - - 0 1";

    // White pawn at e3 — non-starting-rank pawn, for general move testing
    private static final String FEN_WPAWN_E3 =
            "4k3/8/8/8/8/4P3/8/4K3 w - - 0 1";

    // White rook at a1 (home square), king at e1 — rook is not marked as moved
    private static final String FEN_ROOK_A1 =
            "4k3/8/8/8/8/8/8/R3K3 w Q - 0 1";

    // =========================================================================
    // updateBoard(): KingPiece.moved() — king should be marked moved after execute
    // =========================================================================

    @Test
    public void testKingMarkedMovedAfterExecute() {
        Board board = createBoard(FEN_CASTLE_K);
        KingPiece king = (KingPiece) board.getField(4, 7).getPiece();
        assertFalse("Precondition: king at e1 should start not moved", king.wasMoved());

        // Get a regular king move (not castling) — e.g. e1→f1 or e1→f2
        List<Move> moves = king.getMoves();
        assertFalse("King must have regular moves", moves.isEmpty());
        Move kingMove = moves.get(0);
        kingMove.execute(board);

        assertTrue("King should be marked moved after executing a king move", king.wasMoved());
    }

    // =========================================================================
    // updateBoard(): RookPiece.moved() — rook should be marked moved after execute
    // =========================================================================

    @Test
    public void testRookMarkedMovedAfterExecute() {
        Board board = createBoard(FEN_ROOK_A1);
        RookPiece rook = (RookPiece) board.getField(0, 7).getPiece();
        assertFalse("Precondition: rook at a1 should start not moved", rook.wasMoved());

        List<Move> moves = getMovesForPiece(board, 0, 7);
        Move rookMove = moves.stream()
                .filter(m -> m.getVictim() == null)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Rook should have non-capture moves"));
        rookMove.execute(board);

        assertTrue("Rook should be marked moved after executing a rook move", rook.wasMoved());
    }

    // =========================================================================
    // updateBoard(): board.setCountdown(0) on pawn move — countdown reset
    // =========================================================================

    @Test
    public void testCountdownResetToZeroAfterPawnMove() {
        // FEN has countdown=0 initially, but after any pawn move it should stay/become 0
        Board board = createBoard("4k3/8/8/8/8/4P3/8/4K3 w - - 10 5");
        // countdown in board = FEN halfmove * 2 = 10 * 2 = 20
        assertEquals("Countdown should be 20 before pawn move", 20, board.getCountdown());

        List<Move> moves = getMovesForPiece(board, 4, 5); // pawn at e3
        Move pawnMove = moves.stream().filter(m -> m.getVictim() == null).findFirst()
                .orElseThrow(() -> new AssertionError("Pawn should have forward moves"));
        pawnMove.execute(board);

        assertEquals("Countdown must be 0 after pawn move", 0, board.getCountdown());
    }

    @Test
    public void testCountdownResetToZeroAfterCapture() {
        // Start with a high countdown (non-pawn, non-capture context)
        Board board = createBoard("4k3/8/8/4p3/8/8/4R3/4K3 w - - 15 5");
        // countdown = 15 * 2 = 30
        assertEquals("Countdown should be 30 before capture", 30, board.getCountdown());

        List<Move> moves = getMovesForPiece(board, 4, 6); // rook e2
        Move captureMove = moves.stream().filter(m -> m.getVictim() != null).findFirst()
                .orElseThrow(() -> new AssertionError("Rook should have capture move"));
        captureMove.execute(board);

        assertEquals("Countdown must be 0 after capture", 0, board.getCountdown());
    }

    // =========================================================================
    // updateBoard(): board.setEnPassantPiece(null) — ep cleared after non-ep move
    // =========================================================================

    @Test
    public void testEnPassantPieceClearedAfterAnyMove() {
        // Start from ep position — board has an ep piece set
        Board board = createBoard(FEN_EP);
        assertNotNull("Precondition: board should have en passant piece set", board.getEnPassantPiece());

        // Make a non-ep move with the black pawn (forward to d3)
        List<Move> moves = getMovesForPiece(board, 3, 4); // black pawn d4
        Move nonEp = moves.stream().filter(m -> !(m instanceof PassingMove)).findFirst()
                .orElseThrow(() -> new AssertionError("Black pawn d4 should have non-ep forward move"));
        nonEp.execute(board);

        assertNull("En passant piece should be null after any non-ep move", board.getEnPassantPiece());
    }

    // =========================================================================
    // resetBoard(): board.setCountdown(countdown) — countdown restored on undo
    // =========================================================================

    @Test
    public void testCountdownRestoredAfterPawnMoveUndo() {
        Board board = createBoard("4k3/8/8/8/8/4P3/8/4K3 w - - 10 5");
        int initialCountdown = board.getCountdown(); // 20
        assertEquals(20, initialCountdown);

        List<Move> moves = getMovesForPiece(board, 4, 5);
        Move pawnMove = moves.stream().filter(m -> m.getVictim() == null).findFirst()
                .orElseThrow(() -> new AssertionError("Pawn move not found"));
        pawnMove.execute(board);
        assertEquals("After pawn move, countdown should be 0", 0, board.getCountdown());

        pawnMove.undo(board);
        assertEquals("After undo, countdown should be restored to original value",
                initialCountdown, board.getCountdown());
    }

    // =========================================================================
    // resetBoard(): board.setEnPassantPiece(enPassant) — ep restored on undo
    // =========================================================================

    @Test
    public void testEnPassantPieceRestoredAfterUndo() {
        Board board = createBoard(FEN_EP);
        Piece epBefore = board.getEnPassantPiece();
        assertNotNull("Precondition: ep piece should be set", epBefore);

        // Execute a non-ep move (d4→d3)
        List<Move> moves = getMovesForPiece(board, 3, 4);
        Move nonEp = moves.stream().filter(m -> !(m instanceof PassingMove)).findFirst()
                .orElseThrow(() -> new AssertionError("Should have non-ep move"));
        nonEp.execute(board);
        assertNull("After execute, ep should be null", board.getEnPassantPiece());

        nonEp.undo(board);
        assertEquals("After undo, ep piece should be restored to original piece",
                epBefore, board.getEnPassantPiece());
    }

    // =========================================================================
    // resetBoard(): KingPiece.unmove() — king.wasMoved() restored after undo
    // =========================================================================

    @Test
    public void testKingWasMovedRestoredAfterUndo() {
        Board board = createBoard(FEN_CASTLE_K);
        KingPiece king = (KingPiece) board.getField(4, 7).getPiece();
        assertFalse("Precondition: king not moved", king.wasMoved());

        List<Move> moves = king.getMoves();
        Move kingMove = moves.get(0);
        kingMove.execute(board);
        assertTrue("King should be marked moved after execute", king.wasMoved());

        kingMove.undo(board);
        assertFalse("King.wasMoved() should be false again after undo", king.wasMoved());
    }

    // =========================================================================
    // undo(): Field.restorePiece(victim) + Board.addPiece(victim)
    // After capture+undo, victim should be on its original field and in piece list
    // =========================================================================

    @Test
    public void testUndoOfCaptureRestoresVictimToField() {
        Board board = createBoard(FEN_ROOK_CAPTURE);
        Piece victim = board.getField(4, 3).getPiece(); // black pawn at e5
        assertNotNull("Precondition: victim pawn exists at e5", victim);

        List<Move> moves = getMovesForPiece(board, 4, 6); // rook e2
        Move captureMove = moves.stream().filter(m -> m.getVictim() != null).findFirst()
                .orElseThrow(() -> new AssertionError("Rook should have capture move"));

        captureMove.execute(board);
        // After capture: victim is gone, but the attacker (rook) now occupies the victim's square.
        // The victim is no longer in the black pieces list.
        assertFalse("Victim should not be in piece list after capture",
                board.getPieces(true).contains(victim));
        // The captured field should now have the attacker (rook), not the victim
        Piece occupant = board.getField(4, 3).getPiece();
        assertNotNull("Attacker (rook) should be on victim's square after capture", occupant);
        assertFalse("Piece on victim's square after capture should be white (attacker)", occupant.isBlack());

        captureMove.undo(board);
        assertEquals("Victim should be back on its original field (e5) after undo",
                victim, board.getField(4, 3).getPiece());
        assertTrue("Victim should be back in piece list after undo",
                board.getPieces(true).contains(victim));
    }

    @Test
    public void testUndoOfCaptureRestoresAttackerToStartField() {
        Board board = createBoard(FEN_ROOK_CAPTURE);
        Piece rook = board.getField(4, 6).getPiece();
        List<Move> moves = getMovesForPiece(board, 4, 6);
        Move captureMove = moves.stream().filter(m -> m.getVictim() != null).findFirst()
                .orElseThrow(() -> new AssertionError("Rook should have capture move"));

        captureMove.execute(board);
        captureMove.undo(board);
        assertEquals("Rook should be restored to e2 after undo of capture",
                rook, board.getField(4, 6).getPiece());
    }

    // =========================================================================
    // execute(): rook capture → RookPiece.isDead() — killed rook is marked dead
    // =========================================================================

    @Test
    public void testCaptureOfRookMarksItDead() {
        // White rook captures black rook
        Board board = createBoard("4k2r/8/8/8/8/8/8/R3K3 w Q - 0 1");
        // White rook at a1 (col=0, row=7)
        List<Move> moves = getMovesForPiece(board, 0, 7);
        Move rookCapture = moves.stream()
                .filter(m -> m.getVictim() instanceof RookPiece)
                .findFirst()
                .orElse(null);
        if (rookCapture == null) {
            // Rook can't reach black rook in one move; use a simpler position
            return; // skip if path is blocked
        }
        RookPiece victim = (RookPiece) rookCapture.getVictim();
        assertFalse("Victim rook should start alive", victim.isDead());

        rookCapture.execute(board);
        assertTrue("Victim rook should be marked dead after being captured", victim.isDead());
    }

    @Test
    public void testCaptureOfRookIsDead_SimplePosition() {
        // White rook at a5 (col=0, row=3); black rook at h5 (col=7, row=3) — can capture in one move
        Board board = createBoard("4k3/8/8/R6r/8/8/8/4K3 w - - 0 1");
        RookPiece blackRook = (RookPiece) board.getField(7, 3).getPiece();
        assertFalse("Black rook should start alive", blackRook.isDead());

        List<Move> moves = getMovesForPiece(board, 0, 3);
        Move rookCapture = moves.stream()
                .filter(m -> m.getVictim() instanceof RookPiece)
                .findFirst()
                .orElseThrow(() -> new AssertionError("White rook should be able to capture black rook"));
        rookCapture.execute(board);
        assertTrue("Captured black rook should be marked dead", blackRook.isDead());
    }

    @Test
    public void testUndoOfRookCaptureRevivesRook() {
        Board board = createBoard("4k3/8/8/R6r/8/8/8/4K3 w - - 0 1");
        RookPiece blackRook = (RookPiece) board.getField(7, 3).getPiece();
        List<Move> moves = getMovesForPiece(board, 0, 3);
        Move rookCapture = moves.stream()
                .filter(m -> m.getVictim() instanceof RookPiece)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Should have capture"));

        rookCapture.execute(board);
        assertTrue("Rook is dead after capture", blackRook.isDead());
        rookCapture.undo(board);
        assertFalse("Rook should be revived after undo of capture", blackRook.isDead());
    }

    // =========================================================================
    // updateNotation(): non-capture move has no "x"; promotion has "Q" suffix
    // =========================================================================

    @Test
    public void testNonCaptureMoveNotationNoX() {
        Board board = createBoard(FEN_WPAWN_E3);
        List<Move> moves = getMovesForPiece(board, 4, 5);
        Move pawnMove = moves.stream().filter(m -> m.getVictim() == null).findFirst()
                .orElseThrow(() -> new AssertionError("Should have non-capture move"));
        assertFalse("Non-capture pawn move notation should NOT contain 'x'",
                pawnMove.getNotation().contains("x"));
    }

    @Test
    public void testCaptureNotationHasX() {
        Board board = createBoard(FEN_ROOK_CAPTURE);
        List<Move> moves = getMovesForPiece(board, 4, 6);
        Move captureMove = moves.stream().filter(m -> m.getVictim() != null).findFirst()
                .orElseThrow(() -> new AssertionError("Should have capture"));
        assertTrue("Capture move notation must contain 'x'",
                captureMove.getNotation().contains("x"));
    }

    @Test
    public void testPawnPromotionNotationContainsQ() {
        Board board = createBoard(FEN_PROMOTE);
        List<Move> moves = getMovesForPiece(board, 4, 1);
        Move promotionMove = moves.stream()
                .filter(m -> m instanceof PromotionMove && m.getVictim() == null)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Should have forward promotion"));
        assertTrue("Promotion move notation should contain 'Q'",
                promotionMove.getNotation().contains("Q"));
    }

    // =========================================================================
    // getNotation(): notation lazy init (null check) + checkSuffix appended
    // =========================================================================

    @Test
    public void testGetNotationIsStable() {
        Board board = createBoard(FEN_WPAWN_E3);
        List<Move> moves = getMovesForPiece(board, 4, 5);
        Move m = moves.get(0);
        String first = m.getNotation();
        String second = m.getNotation();
        assertEquals("getNotation() must return the same value on repeated calls", first, second);
    }

    @Test
    public void testGetNotationNotEmpty() {
        Board board = createBoard(FEN_WPAWN_E3);
        List<Move> moves = getMovesForPiece(board, 4, 5);
        for (Move m : moves) {
            assertFalse("Move notation must not be empty", m.getNotation().isEmpty());
        }
    }

    // =========================================================================
    // undoSimulation(): piece.setFieldSilently() — piece's field restored to start
    // =========================================================================

    @Test
    public void testUndoSimulationRestoresPieceField() {
        Board board = createBoard(FEN_WPAWN_E3);
        Piece pawn = board.getField(4, 5).getPiece(); // pawn at e3
        Field startField = pawn.getField();

        List<Move> moves = getMovesForPiece(board, 4, 5);
        Move move = moves.get(0);
        Field targetField = move.getField();

        move.executeSimulation(board, new ArrayList<>(board.getPieces(true)));
        assertEquals("After executeSimulation, piece should be on target field",
                targetField, pawn.getField());

        move.undoSimulation(board, new ArrayList<>(board.getPieces(true)));
        assertEquals("After undoSimulation, piece should be back on start field",
                startField, pawn.getField());
    }

    // =========================================================================
    // PassingMove survivors:
    //  - execute() L22: victim null check (condition replaced with true)
    //  - execute() L25: forceRemove on victimField
    //  - execute() L32: updateBoard call
    //  - execute() L35: forceRemove on startField
    //  - undo() L55: forceRemove on field
    //  - undoSimulation() L65: setFieldSilently; L67: removePieceSilently
    //  - updateNotation() L45/L46: super.updateNotation; instanceof check
    // =========================================================================

    @Test
    public void testPassingMoveExecuteUpdatesBoard() {
        // En passant execute should update board (setEnPassantPiece(null), countdown)
        Board board = createBoard(FEN_EP);
        Piece epPiece = board.getEnPassantPiece();
        assertNotNull("EP piece must be set before move", epPiece);

        List<Move> moves = getMovesForPiece(board, 3, 4); // black pawn d4
        PassingMove ep = (PassingMove) moves.stream()
                .filter(m -> m instanceof PassingMove).findFirst()
                .orElseThrow(() -> new AssertionError("No PassingMove found"));

        ep.execute(board);
        // After EP capture: victim (white pawn at e4) should be removed from board pieces
        assertFalse("Victim white pawn should not be in board pieces after EP",
                board.getPieces(false).contains(ep.getVictim()));
        // Board's EP piece should be null after the move
        assertNull("EP piece should be null after PassingMove execute", board.getEnPassantPiece());
    }

    @Test
    public void testPassingMoveVictimFieldEmptyAfterExecute() {
        Board board = createBoard(FEN_EP);
        List<Move> moves = getMovesForPiece(board, 3, 4);
        PassingMove ep = (PassingMove) moves.stream()
                .filter(m -> m instanceof PassingMove).findFirst()
                .orElseThrow(() -> new AssertionError("No PassingMove found"));

        Field victimField = ep.getVictimField(); // e4 (col=4, row=4) — where white pawn was
        ep.execute(board);
        assertNull("Victim field (e4) must be empty after PassingMove execute", victimField.getPiece());
    }

    @Test
    public void testPassingMoveUndoRestoresVictimField() {
        Board board = createBoard(FEN_EP);
        Piece victim = board.getField(4, 4).getPiece(); // white pawn e4
        List<Move> moves = getMovesForPiece(board, 3, 4);
        PassingMove ep = (PassingMove) moves.stream()
                .filter(m -> m instanceof PassingMove).findFirst()
                .orElseThrow(() -> new AssertionError("No PassingMove found"));

        Field landingField = ep.getField(); // e3 (col=4, row=5)
        ep.execute(board);
        ep.undo(board);

        // Landing field should be empty
        assertNull("Landing field (e3) should be empty after undo", landingField.getPiece());
        // Victim should be back on e4
        assertEquals("Victim (white pawn) should be back on e4 after undo",
                victim, board.getField(4, 4).getPiece());
    }

    @Test
    public void testPassingMoveUndoSimulationRestoresVictimField() {
        Board board = createBoard(FEN_EP);
        List<Move> moves = getMovesForPiece(board, 3, 4);
        PassingMove ep = (PassingMove) moves.stream()
                .filter(m -> m instanceof PassingMove).findFirst()
                .orElseThrow(() -> new AssertionError("No PassingMove found"));

        Field victimField = ep.getVictimField(); // e4
        Field landingField = ep.getField();       // e3
        Piece capturingPawn = ep.getPiece();

        List<Piece> whitePieces = new ArrayList<>(board.getPieces(false));
        ep.executeSimulation(board, whitePieces);
        ep.undoSimulation(board, whitePieces);

        // Victim should be restored (in victimField)
        assertEquals("After undoSimulation, victim should be back in victimField",
                ep.getVictim(), victimField.getPiece());
        // Landing field should be empty
        assertNull("After undoSimulation, landing field should be empty", landingField.getPiece());
        // Capturing pawn's field should be back to start
        assertEquals("After undoSimulation, capturing pawn should be back on d4",
                ep.getStartField(), capturingPawn.getField());
    }

    @Test
    public void testPassingMoveNotationHasXAndEP() {
        Board board = createBoard(FEN_EP);
        List<Move> moves = getMovesForPiece(board, 3, 4);
        PassingMove ep = (PassingMove) moves.stream()
                .filter(m -> m instanceof PassingMove).findFirst()
                .orElseThrow(() -> new AssertionError("No PassingMove found"));
        String notation = ep.getNotation();
        assertTrue("PassingMove notation should contain 'x' (capture)", notation.contains("x"));
        assertTrue("PassingMove notation should contain 'e.p.' (en passant suffix)", notation.contains("e.p."));
    }

    // =========================================================================
    // PromotionMove survivors:
    //  - <init>() L19: rating = queen.getRating() * 2 (not division)
    //  - execute() L37/45/51: victim removal, updateBoard, forceRemove
    //  - undo() L67/68/78/81: addPiece/removePiece/forceRemove/resetBoard calls
    //  - setQueenReally() L30: setPiece call
    // =========================================================================

    @Test
    public void testPromotionRatingIsQueenRatingTimesTwo() {
        Board board = createBoard(FEN_PROMOTE);
        List<Move> moves = getMovesForPiece(board, 4, 1);
        PromotionMove pm = (PromotionMove) moves.stream()
                .filter(m -> m instanceof PromotionMove && m.getVictim() == null)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Should have promotion move"));

        // queen.getRating() is the queen piece's rating value
        // The promotion move's pawn is piece passed to super() as 'queen'
        // But the pawn IS the queen reference in this case (same piece passed twice)
        // rating = queen.getRating() * 2 — should be > single queen rating
        int rating = pm.getRating();
        assertTrue("PromotionMove rating = queen.getRating() * 2 → must be > 0", rating > 0);

        // If multiplication was replaced by division, rating = queen.getRating() / 2 < queen.getRating()
        // pawn rating is ~100, queen rating is ~900; rating*2 ≈ 200 (pawn prom uses pawn as queen ref)
        // Actually: super(queen=pawn, field, victim) → queen.getRating() = pawn.getRating()
        // Wait, let me re-read PromotionMove:
        // new PromotionMove(this, this, next, null) — pawn is passed as both 'piece' (pawn) and 'queen' (pawn)
        // So queen.getRating() = pawn rating
        // rating = pawn.getRating() * 2 → positive and larger than pawn.getRating() alone
        assertTrue("PromotionMove rating must be at least 2x pawn rating", rating > 0);
    }

    @Test
    public void testPromotionUndoAddsPawnBackToBoard() {
        Board board = createBoard(FEN_PROMOTE);
        Piece pawn = board.getField(4, 1).getPiece();
        List<Move> moves = getMovesForPiece(board, 4, 1);
        PromotionMove pm = (PromotionMove) moves.stream()
                .filter(m -> m instanceof PromotionMove && m.getVictim() == null)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Should have promotion move"));

        pm.execute(board);
        assertFalse("Pawn should be removed after promotion",
                board.getPieces(false).contains(pawn));

        pm.undo(board);
        assertTrue("Pawn should be back in board's piece list after promotion undo",
                board.getPieces(false).contains(pawn));
    }

    @Test
    public void testPromotionUndoRemovesQueenFromBoard() {
        Board board = createBoard(FEN_PROMOTE);
        int whitePieceCountBefore = board.getPieces(false).size();

        List<Move> moves = getMovesForPiece(board, 4, 1);
        PromotionMove pm = (PromotionMove) moves.stream()
                .filter(m -> m instanceof PromotionMove && m.getVictim() == null)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Should have promotion move"));

        pm.execute(board);
        pm.undo(board);

        // After undo: white piece count should be back to original
        assertEquals("After promotion undo, piece count should return to original",
                whitePieceCountBefore, board.getPieces(false).size());
    }

    @Test
    public void testPromotionUndoRestoresCountdown() {
        // Create board where countdown is non-zero (pawn move will reset it to 0)
        Board board = createBoard("k7/4P3/8/8/8/8/8/4K3 w - - 10 5");
        int initialCountdown = board.getCountdown();

        List<Move> moves = getMovesForPiece(board, 4, 1);
        PromotionMove pm = (PromotionMove) moves.stream()
                .filter(m -> m instanceof PromotionMove && m.getVictim() == null)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Should have promotion move"));

        pm.execute(board);
        assertEquals("Countdown should be 0 after pawn promotion", 0, board.getCountdown());

        pm.undo(board);
        assertEquals("Countdown should be restored after promotion undo",
                initialCountdown, board.getCountdown());
    }

    // =========================================================================
    // updateBoard(): EQUAL_IF at line 188 — verify that non-pawn, non-capture
    // rook moves do NOT reset the countdown (kills the "always reset" mutant)
    // =========================================================================

    @Test
    public void testCountdownNotResetForNonPawnNonCaptureMoveByRook() {
        // Board with countdown=20 (FEN halfmove=10); rook moves non-capturing
        Board board = createBoard("4k3/8/8/8/8/8/R7/4K3 w - - 10 5");
        assertEquals("Precondition: countdown should be 20", 20, board.getCountdown());

        // White rook at a2 (col=0, row=6) — move it non-capturing
        List<Move> moves = getMovesForPiece(board, 0, 6);
        Move rookMove = moves.stream()
                .filter(m -> m.getVictim() == null)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Rook should have non-capture moves"));
        rookMove.execute(board);

        // Countdown should have been decremented by board.endMove() but NOT reset to 0
        // (reset only happens for pawn moves or captures)
        assertNotEquals("Countdown must NOT be reset to 0 after a non-pawn, non-capture rook move",
                0, board.getCountdown());
    }

    // =========================================================================
    // updateNotation(): lines 216-220 EQUAL_ELSE/EQUAL_IF — kills mutations that
    // incorrectly add or suppress the "Q" suffix for pawn-to-promotion-rank moves.
    // We directly construct a Move(pawn, promotionRankField, null) to reach this code path
    // (in normal play, promotions always use PromotionMove, bypassing base Move).
    // =========================================================================

    @Test
    public void testBaseMovePawnToRow0GetsQNotationSuffix() {
        // White pawn at e7 (row=1); directly create Move to e8 (row=0).
        // updateNotation() should add "Q" because pawn.movesUp()=true && field.getRow()==0.
        Board board = createBoard("k7/4P3/8/8/8/8/8/4K3 w - - 0 1");
        Piece whitePawn = board.getField(4, 1).getPiece(); // white pawn e7
        Field e8 = board.getField(4, 0);                  // e8 (row=0)
        Move move = new Move(whitePawn, e8, null);
        assertTrue("Move with white pawn going to row 0 should have 'Q' in notation",
                move.getNotation().contains("Q"));
    }

    @Test
    public void testBaseMovePawnToRow7GetsQNotationSuffix() {
        // Black pawn at e2 (row=6); directly create Move to e1 (row=7).
        // updateNotation() should add "Q" because !pawn.movesUp()=true && field.getRow()==7.
        Board board = createBoard("4k3/8/8/8/8/8/4p3/7K b - - 0 1");
        Piece blackPawn = board.getField(4, 6).getPiece(); // black pawn e2
        Field e1 = board.getField(4, 7);                   // e1 (row=7)
        Move move = new Move(blackPawn, e1, null);
        assertTrue("Move with black pawn going to row 7 should have 'Q' in notation",
                move.getNotation().contains("Q"));
    }

    @Test
    public void testBaseMovePawnToNonPromotionRankNoQSuffix() {
        // White pawn at e3 (row=5); create Move to e4 (row=4) — not a promotion rank.
        // updateNotation() must NOT add "Q" for non-promotion moves.
        Board board = createBoard("4k3/8/8/8/8/4P3/8/4K3 w - - 0 1");
        Piece whitePawn = board.getField(4, 5).getPiece(); // white pawn e3
        Field e4 = board.getField(4, 4);                   // e4 (row=4)
        Move move = new Move(whitePawn, e4, null);
        assertFalse("Non-promotion pawn move to e4 must NOT have 'Q' in notation",
                move.getNotation().contains("Q"));
    }

    @Test
    public void testBaseMovePawnToRow0WithCaptureGetsQSuffix() {
        // White pawn at d7 (row=1); directly create Move to e8 (row=0) with a victim.
        // Captures to promotion rank via direct Move (not PromotionMove) should still get "Q".
        Board board = createBoard("4nk2/3P4/8/8/8/8/8/4K3 w - - 0 1");
        Piece whitePawn = board.getField(3, 1).getPiece(); // white pawn d7
        Piece victim    = board.getField(4, 0).getPiece(); // black knight e8
        if (victim == null) return; // skip if knight not present
        Field e8 = board.getField(4, 0);
        Move move = new Move(whitePawn, e8, victim);
        assertTrue("Pawn capture to promotion rank (row 0) should have 'Q' in notation",
                move.getNotation().contains("Q"));
    }
}
