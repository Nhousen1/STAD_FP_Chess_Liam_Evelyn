package com.chess.mutation.moves;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.Field;
import com.chess.root.moves.CastlingMove;
import com.chess.root.moves.Move;
import com.chess.root.moves.PassingMove;
import com.chess.root.moves.PawnRunMove;
import com.chess.root.moves.PromotionMove;
import com.chess.root.pieces.Piece;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Mutation-killing tests for com.chess.root.moves.
 *
 * These tests target specific surviving and NO_COVERAGE mutants identified by
 * PITest after the initial black-box test run. Each test is labeled with the
 * mutant class it aims to kill.
 *
 * Strategy:
 *   - Call execute() / undo() / executeSimulation() on move objects
 *   - Assert board field state, piece location, and board metadata (en passant)
 *   - Assert ratings and notation strings for SURVIVED constructor/method mutants
 */
public class
MoveExecutionTest extends ChessTestBase {

    // White pawn on e3 (not starting rank) — produces a single regular Move
    private static final String FEN_WPAWN_E3 =
            "4k3/8/8/8/8/4P3/8/4K3 w - - 0 1";

    // White pawn on e2 (starting rank) — produces a PawnRunMove (double push)
    private static final String FEN_WPAWN_E2 =
            "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1";

    // White rook e2, black pawn e5 — rook has a capture move
    private static final String FEN_ROOK_CAPTURE =
            "4k3/8/8/4p3/8/8/4R3/4K3 w - - 0 1";

    // King-side castling rights: king e1, rook h1
    private static final String FEN_KINGSIDE_CASTLE =
            "4k3/8/8/8/8/8/8/4K2R w K - 0 1";

    // Queen-side castling rights: king e1, rook a1
    private static final String FEN_QUEENSIDE_CASTLE =
            "4k3/8/8/8/8/8/8/R3K3 w Q - 0 1";

    // En passant: white pawn just pushed to c4; black pawn d4 can capture to c3
    private static final String FEN_EN_PASSANT =
            "4k3/8/8/8/2Pp4/8/8/4K3 b - c3 0 1";

    // White pawn e7, black king a8 — forward promotion to e8
    private static final String FEN_PROMOTION =
            "k7/4P3/8/8/8/8/8/4K3 w - - 0 1";

    // White pawn e7, black knight f8 — promotion with capture to f8
    private static final String FEN_PROMOTION_CAPTURE =
            "k4n2/4P3/8/8/8/8/8/4K3 w - - 0 1";

    // =========================================================================
    // CastlingMove — getters (NO_COVERAGE: lines 69, 73, 77)
    // =========================================================================

    /**
     * Targets NO_COVERAGE mutants: getRook()→null, getRookField()→null,
     * getRookStartField()→null (CastlingMove lines 69, 73, 77).
     */
    @Test
    public void testCastlingMoveGettersReturnNonNull() {
        Board board = createBoard(FEN_KINGSIDE_CASTLE);
        List<Move> castles = getCastlingMoves(board, 4, 7); // white king e1
        assertFalse("Should have at least one castling move", castles.isEmpty());
        CastlingMove move = (CastlingMove) castles.get(0);
        assertNotNull("getRook() should return the rook piece", move.getRook());
        assertNotNull("getRookField() should return the rook destination field", move.getRookField());
        assertNotNull("getRookStartField() should return the rook origin field", move.getRookStartField());
    }

    /**
     * Targets SURVIVED: CastlingMove.updateRating() call removed in constructor (line 24),
     * and addition replaced by subtraction in updateRating (line 65).
     * Both mutants are killed by asserting the rating is positive.
     */
    @Test
    public void testCastlingMoveHasPositiveRating() {
        Board board = createBoard(FEN_KINGSIDE_CASTLE);
        List<Move> castles = getCastlingMoves(board, 4, 7);
        assertFalse(castles.isEmpty());
        CastlingMove move = (CastlingMove) castles.get(0);
        assertTrue("Castling move rating should be positive (king rating is added, not subtracted)",
                move.getRating() > 0);
    }

    // =========================================================================
    // CastlingMove.execute() — NO_COVERAGE lines 29-33
    // =========================================================================

    /**
     * Targets NO_COVERAGE mutants in CastlingMove.execute():
     * rook.removePiece, rook.forceRemove, rookField.setRookPiece, rook.moved, super.execute.
     * After king-side castling: king on g1 (col=6, row=7), rook on f1 (col=5, row=7).
     */
    @Test
    public void testKingSideCastlingExecuteMovesKingAndRook() {
        Board board = createBoard(FEN_KINGSIDE_CASTLE);
        List<Move> castles = getCastlingMoves(board, 4, 7);
        CastlingMove move = (CastlingMove) castles.stream()
                .filter(m -> m.getNotation().equals("O-O"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("O-O not found"));

        move.execute(board);

        assertNotNull("King should be on g1 after O-O",      board.getField(6, 7).getPiece());
        assertNotNull("Rook should be on f1 after O-O",      board.getField(5, 7).getPiece());
        assertNull("e1 should be empty after O-O",            board.getField(4, 7).getPiece());
        assertNull("h1 should be empty after O-O",            board.getField(7, 7).getPiece());
    }

    /**
     * Targets NO_COVERAGE mutants in CastlingMove.execute() for queen-side.
     * After queen-side castling: king on c1 (col=2, row=7), rook on d1 (col=3, row=7).
     */
    @Test
    public void testQueenSideCastlingExecuteMovesKingAndRook() {
        Board board = createBoard(FEN_QUEENSIDE_CASTLE);
        List<Move> castles = getCastlingMoves(board, 4, 7);
        CastlingMove move = (CastlingMove) castles.stream()
                .filter(m -> m.getNotation().equals("O-O-O"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("O-O-O not found"));

        move.execute(board);

        assertNotNull("King should be on c1 after O-O-O",    board.getField(2, 7).getPiece());
        assertNotNull("Rook should be on d1 after O-O-O",    board.getField(3, 7).getPiece());
        assertNull("e1 should be empty after O-O-O",          board.getField(4, 7).getPiece());
        assertNull("a1 should be empty after O-O-O",          board.getField(0, 7).getPiece());
    }

    // =========================================================================
    // CastlingMove.undo() — NO_COVERAGE lines 38-42
    // =========================================================================

    /**
     * Targets NO_COVERAGE mutants in CastlingMove.undo():
     * rookField.removePiece, rookField.forceRemove, rookStartField.setRookPiece,
     * rook.unmove, super.undo.
     * After execute then undo, king and rook should be restored to original squares.
     */
    @Test
    public void testCastlingUndoRestoresBothPiecesToOriginalSquares() {
        Board board = createBoard(FEN_KINGSIDE_CASTLE);
        List<Move> castles = getCastlingMoves(board, 4, 7);
        CastlingMove move = (CastlingMove) castles.stream()
                .filter(m -> m.getNotation().equals("O-O"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("O-O not found"));

        move.execute(board);
        move.undo(board);

        assertNotNull("King should be back on e1 after undo", board.getField(4, 7).getPiece());
        assertNotNull("Rook should be back on h1 after undo", board.getField(7, 7).getPiece());
        assertNull("g1 should be empty after undo",           board.getField(6, 7).getPiece());
        assertNull("f1 should be empty after undo",           board.getField(5, 7).getPiece());
    }

    // =========================================================================
    // Move.execute() — basic and capture, NO_COVERAGE
    // =========================================================================

    /**
     * Targets NO_COVERAGE mutants in Move.execute() — startField.removePiece,
     * field.setPiece, startField.forceRemove.
     * After executing a pawn advance from e3 to e4, the pawn should be on e4 and e3 empty.
     */
    @Test
    public void testBasicMoveExecuteMovesPieceToTargetField() {
        Board board = createBoard(FEN_WPAWN_E3);
        // White pawn on e3: col=4, row=5
        Piece pawn = board.getField(4, 5).getPiece();
        List<Move> moves = getMovesForPiece(board, 4, 5);
        assertFalse("Pawn on e3 should have moves", moves.isEmpty());
        Move move = moves.get(0); // e3→e4

        move.execute(board);

        assertEquals("Pawn should be on e4 (col=4, row=4) after move",
                pawn, board.getField(4, 4).getPiece());
        assertNull("e3 should be empty after move", board.getField(4, 5).getPiece());
    }

    /**
     * Targets NO_COVERAGE mutants in Move.execute() with capture:
     * victimField.removePiece, board.removePiece(victim), RookPiece.kill.
     * After rook captures black pawn at e5, the pawn should be removed from the board.
     */
    @Test
    public void testCaptureExecuteRemovesVictimFromBoard() {
        Board board = createBoard(FEN_ROOK_CAPTURE);
        // White rook on e2 (col=4, row=6); black pawn on e5 (col=4, row=3)
        List<Move> moves = getMovesForPiece(board, 4, 6);
        Move captureMove = moves.stream()
                .filter(m -> m.getVictim() != null)
                .findFirst()
                .orElse(null);
        assertNotNull("Rook at e2 should have a capture move toward e5", captureMove);

        Piece victim = captureMove.getVictim();
        List<Piece> blackBefore = board.getPieces(true);
        assertTrue("Victim pawn should be on board before capture", blackBefore.contains(victim));

        captureMove.execute(board);

        assertFalse("Victim pawn should be removed from board after capture",
                board.getPieces(true).contains(victim));
    }

    /**
     * Targets NO_COVERAGE in Move.execute() with capture — field placement after capture.
     * The rook should occupy the captured pawn's square after execute.
     */
    @Test
    public void testCaptureExecutePlacesAttackerOnVictimSquare() {
        Board board = createBoard(FEN_ROOK_CAPTURE);
        List<Move> moves = getMovesForPiece(board, 4, 6);
        Move captureMove = moves.stream()
                .filter(m -> m.getVictim() != null)
                .findFirst()
                .orElse(null);
        assertNotNull(captureMove);
        Piece rook = board.getField(4, 6).getPiece();
        Field targetField = captureMove.getField();

        captureMove.execute(board);

        assertEquals("Rook should occupy the target (captured) square",
                rook, targetField.getPiece());
    }

    // =========================================================================
    // SURVIVED: Move constructor — updateNotation() call removed (line 49)
    // =========================================================================

    /**
     * Targets SURVIVED mutant: removed call to Move::updateNotation in constructor (line 49).
     * updateNotation() sets hitOrMove="x" when victim != null.
     * If the call is removed, the notation will not contain "x" for capture moves.
     */
    @Test
    public void testCaptureMoveNotationContainsX() {
        Board board = createBoard(FEN_ROOK_CAPTURE);
        List<Move> moves = getMovesForPiece(board, 4, 6);
        Move captureMove = moves.stream()
                .filter(m -> m.getVictim() != null)
                .findFirst()
                .orElse(null);
        assertNotNull(captureMove);
        assertTrue("Capture move notation must contain 'x' (set by updateNotation in constructor)",
                captureMove.getNotation().contains("x"));
    }

    // =========================================================================
    // Move.undo() — NO_COVERAGE (no-capture branch, lines 162-163)
    // =========================================================================

    /**
     * Targets NO_COVERAGE mutants in Move.undo() no-victim branch:
     * field.removePiece, field.forceRemove.
     * After execute then undo of a non-capture move, pawn should be back on e3 and e4 empty.
     */
    @Test
    public void testBasicMoveUndoRestoresPieceToStartField() {
        Board board = createBoard(FEN_WPAWN_E3);
        Piece pawn = board.getField(4, 5).getPiece();
        List<Move> moves = getMovesForPiece(board, 4, 5);
        Move move = moves.get(0);

        move.execute(board);
        move.undo(board);

        assertEquals("Pawn should be back on e3 after undo", pawn, board.getField(4, 5).getPiece());
        assertNull("e4 should be empty after undo of non-capture", board.getField(4, 4).getPiece());
    }

    // =========================================================================
    // SURVIVED: Move.executeSimulation() — victim null check, removePieceSilently,
    //           board.setEnPassantPiece(null), piece.setFieldSilently(field)
    // =========================================================================

    /**
     * Targets SURVIVED mutants in Move.executeSimulation():
     * - if (victim != null) conditional
     * - victimField.removePieceSilently()
     *
     * For a regular capture, victimField == field (same square), so removePieceSilently()
     * is immediately overwritten by field.setPieceSilently(attacker). The mutant is only
     * observable when victimField != field — the en passant case.
     * Black pawn lands on c3 (field) but captures the white pawn on c4 (victimField).
     * After executeSimulation: c4 must be null (removed by removePieceSilently),
     * while c3 has the capturing pawn (set by setPieceSilently).
     */
    @Test
    public void testExecuteSimulationEnPassantEmptiesVictimFieldSeparateFromLanding() {
        Board board = createBoard(FEN_EN_PASSANT);
        List<Move> moves = getMovesForPiece(board, 3, 4); // black pawn d4
        PassingMove ep = (PassingMove) moves.stream()
                .filter(m -> m instanceof PassingMove)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No PassingMove found"));

        Field victimField = ep.getVictimField(); // c4 (col=2, row=4) — white pawn's square
        Field landingField = ep.getField();      // c3 (col=2, row=5) — where black pawn lands
        assertNotEquals("victimField and landingField must differ (en passant rule)",
                victimField, landingField);

        ep.executeSimulation(board, new ArrayList<>(board.getPieces(false)));

        assertNull("Victim field c4 should be empty after en passant executeSimulation",
                victimField.getPiece());
        assertNotNull("Landing field c3 should have the capturing pawn",
                landingField.getPiece());
    }

    /**
     * Targets SURVIVED mutant: board.setEnPassantPiece(null) in executeSimulation (line 128).
     * Use FEN with an active ep piece, then simulate a regular (non-ep) move — ep should become null.
     */
    @Test
    public void testExecuteSimulationClearsEnPassantPiece() {
        Board board = createBoard(FEN_EN_PASSANT);
        assertNotNull("EP piece should be set before simulation", board.getEnPassantPiece());

        // Black pawn at d4 (col=3, row=4): get its non-ep forward move (d4→d3)
        List<Move> moves = getMovesForPiece(board, 3, 4);
        Move nonEpMove = moves.stream()
                .filter(m -> !(m instanceof PassingMove))
                .findFirst()
                .orElse(null);
        assertNotNull("Black pawn d4 should have a forward non-ep move", nonEpMove);

        nonEpMove.executeSimulation(board, new ArrayList<>(board.getPieces(false)));

        assertNull("En passant piece should be null after executeSimulation of regular move",
                board.getEnPassantPiece());
    }

    /**
     * Targets SURVIVED mutant: piece.setFieldSilently(field) in executeSimulation (line 130).
     * After executeSimulation, piece.getField() must equal the target field.
     */
    @Test
    public void testExecuteSimulationUpdatesPieceFieldReference() {
        Board board = createBoard(FEN_WPAWN_E3);
        Piece pawn = board.getField(4, 5).getPiece();
        List<Move> moves = getMovesForPiece(board, 4, 5);
        Move move = moves.get(0); // e3→e4
        Field targetField = move.getField();

        move.executeSimulation(board, new ArrayList<>(board.getPieces(true)));

        assertEquals("After executeSimulation, piece.getField() should be the target field",
                targetField, pawn.getField());
    }

    // =========================================================================
    // PawnRunMove — execute and executeSimulation set en passant piece (SURVIVED)
    // =========================================================================

    /**
     * Targets SURVIVED mutant: PawnRunMove.updateBoard — board.setEnPassantPiece(piece) (line 17).
     * After executing a double pawn push, board.getEnPassantPiece() must be the pawn itself.
     */
    @Test
    public void testPawnRunMoveExecuteSetsEnPassantPiece() {
        Board board = createBoard(FEN_WPAWN_E2);
        Piece pawn = board.getField(4, 6).getPiece(); // white pawn on e2
        List<Move> moves = getMovesForPiece(board, 4, 6);
        Move doublePush = moves.stream()
                .filter(m -> m instanceof PawnRunMove)
                .findFirst()
                .orElse(null);
        assertNotNull("Double pawn push from e2 should be a PawnRunMove", doublePush);

        doublePush.execute(board);

        assertEquals("After double push execute, board.getEnPassantPiece() should be the pawn",
                pawn, board.getEnPassantPiece());
    }

    /**
     * Targets SURVIVED mutant: PawnRunMove.executeSimulation — board.setEnPassantPiece(piece) (line 23).
     * After executeSimulation of a double push, board.getEnPassantPiece() must be the pawn.
     */
    @Test
    public void testPawnRunMoveExecuteSimulationSetsEnPassantPiece() {
        Board board = createBoard(FEN_WPAWN_E2);
        Piece pawn = board.getField(4, 6).getPiece();
        List<Move> moves = getMovesForPiece(board, 4, 6);
        Move doublePush = moves.stream()
                .filter(m -> m instanceof PawnRunMove)
                .findFirst()
                .orElse(null);
        assertNotNull(doublePush);

        doublePush.executeSimulation(board, new ArrayList<>(board.getPieces(true)));

        assertEquals("After executeSimulation of double push, ep piece should be the pawn",
                pawn, board.getEnPassantPiece());
    }

    // =========================================================================
    // PassingMove.execute() — NO_COVERAGE; notation SURVIVED
    // =========================================================================

    /**
     * Targets NO_COVERAGE mutants in PassingMove.execute():
     * victimField.removePiece, board.removePiece(victim), victimField.forceRemove, field.setPiece.
     * After en passant: capturing pawn lands on c3, white pawn removed from c4, d4 empty.
     */
    @Test
    public void testPassingMoveExecuteMovesCapturingPawnAndRemovesVictim() {
        Board board = createBoard(FEN_EN_PASSANT);
        Piece capturingPawn = board.getField(3, 4).getPiece(); // black pawn d4
        List<Move> moves = getMovesForPiece(board, 3, 4);
        PassingMove ep = (PassingMove) moves.stream()
                .filter(m -> m instanceof PassingMove)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No PassingMove found"));

        Field victimField = ep.getVictimField(); // c4 (col=2, row=4) — white pawn's square
        Piece victim = ep.getVictim();

        ep.execute(board);

        // Capturing pawn should be on c3 (col=2, row=5)
        assertEquals("Capturing pawn should land on c3 after en passant",
                capturingPawn, board.getField(2, 5).getPiece());
        // c4 (victim's original square) should be empty
        assertNull("c4 (victim field) should be empty after en passant", victimField.getPiece());
        // d4 (start square) should be empty
        assertNull("d4 (start field) should be empty after en passant", board.getField(3, 4).getPiece());
        // Victim should be removed from board piece list
        assertFalse("Victim pawn should be removed from board piece list",
                board.getPieces(false).contains(victim));
    }

    /**
     * Targets SURVIVED mutant: PassingMove.updateNotation — if (this instanceof PassingMove)
     * check (line 46). If removed, " e.p." is never appended to notation.
     */
    @Test
    public void testPassingMoveNotationContainsEnPassantSuffix() {
        Board board = createBoard(FEN_EN_PASSANT);
        List<Move> moves = getMovesForPiece(board, 3, 4);
        PassingMove ep = (PassingMove) moves.stream()
                .filter(m -> m instanceof PassingMove)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No PassingMove found"));

        assertTrue("PassingMove notation should contain 'e.p.' suffix",
                ep.getNotation().contains("e.p."));
    }

    /**
     * Targets NO_COVERAGE mutants in PassingMove.undo():
     * super.undo, field.removePiece, field.forceRemove.
     * After execute then undo: start field has pawn, landing field and victim field restored.
     */
    @Test
    public void testPassingMoveUndoRestoresBoard() {
        Board board = createBoard(FEN_EN_PASSANT);
        Piece capturingPawn = board.getField(3, 4).getPiece();
        Piece victim = board.getField(2, 4).getPiece(); // white pawn c4
        List<Move> moves = getMovesForPiece(board, 3, 4);
        PassingMove ep = (PassingMove) moves.stream()
                .filter(m -> m instanceof PassingMove)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No PassingMove found"));

        ep.execute(board);
        ep.undo(board);

        // d4 should have the capturing pawn back
        assertEquals("Black pawn should be back on d4 after undo",
                capturingPawn, board.getField(3, 4).getPiece());
        // c3 (landing square) should be empty
        assertNull("c3 (landing field) should be empty after undo", board.getField(2, 5).getPiece());
    }

    // =========================================================================
    // PromotionMove — rating SURVIVED; execute/undo NO_COVERAGE
    // =========================================================================

    /**
     * Targets SURVIVED mutant: PromotionMove constructor — rating = queen.getRating() * 2 (line 19).
     * If the expression is mutated (e.g., division, subtraction), the rating changes.
     * Assert that the rating is positive.
     */
    @Test
    public void testPromotionMoveHasPositiveRating() {
        Board board = createBoard(FEN_PROMOTION);
        List<Move> moves = getMovesForPiece(board, 4, 1); // white pawn e7
        PromotionMove promotion = (PromotionMove) moves.stream()
                .filter(m -> m instanceof PromotionMove)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No PromotionMove found"));

        assertTrue("PromotionMove rating should be positive (queen.getRating() * 2)",
                promotion.getRating() > 0);
    }

    /**
     * Targets NO_COVERAGE mutants in PromotionMove.execute():
     * board.removePiece(pawn), startField.removePiece, startField.forceRemove,
     * queen creation and placement, field.setPiece(queen).
     * After promotion: e8 has a piece (queen), pawn removed from board's piece list.
     */
    @Test
    public void testPromotionMoveExecutePlacesQueenAndRemovesPawn() {
        Board board = createBoard(FEN_PROMOTION);
        Piece pawn = board.getField(4, 1).getPiece(); // white pawn e7
        List<Move> moves = getMovesForPiece(board, 4, 1);
        PromotionMove promotion = (PromotionMove) moves.stream()
                .filter(m -> m instanceof PromotionMove && m.getField().getColumn() == 4)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No forward PromotionMove found"));

        promotion.execute(board);

        // e8 (col=4, row=0) should have a piece (the queen)
        assertNotNull("e8 should have a piece (promoted queen) after promotion",
                board.getField(4, 0).getPiece());
        // Pawn should be removed from board's white piece list
        assertFalse("Original pawn should be removed from board after promotion",
                board.getPieces(false).contains(pawn));
        // e7 should be empty
        assertNull("e7 should be empty after promotion", board.getField(4, 1).getPiece());
    }

    /**
     * Targets NO_COVERAGE mutants in PromotionMove.execute() with capture:
     * victimField.removePiece, board.removePiece(victim), victim kill path.
     * After promotion-capture: f8 has a piece; victim knight removed from board.
     */
    @Test
    public void testPromotionWithCaptureExecuteRemovesVictimAndPlacesQueen() {
        Board board = createBoard(FEN_PROMOTION_CAPTURE);
        List<Move> moves = getMovesForPiece(board, 4, 1); // white pawn e7
        PromotionMove capturePromotion = (PromotionMove) moves.stream()
                .filter(m -> m instanceof PromotionMove && m.getVictim() != null)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No capture PromotionMove found"));

        Piece victim = capturePromotion.getVictim();
        capturePromotion.execute(board);

        // f8 (col=5, row=0) should have the promoted queen
        assertNotNull("f8 should have a piece (promoted queen) after promotion-capture",
                board.getField(5, 0).getPiece());
        // Victim knight should be removed from board
        assertFalse("Victim knight should be removed from board after promotion-capture",
                board.getPieces(true).contains(victim));
    }

    /**
     * Targets NO_COVERAGE mutants in PromotionMove.undo():
     * startField.restorePiece(pawn), board.addPiece(pawn), board.removePiece(queen),
     * field.removePiece, field.forceRemove, resetBoard.
     * After execute then undo: pawn back on e7, e8 empty.
     */
    @Test
    public void testPromotionMoveUndoRestoresPawnAndClearsPromotionSquare() {
        Board board = createBoard(FEN_PROMOTION);
        Piece pawn = board.getField(4, 1).getPiece(); // white pawn e7
        List<Move> moves = getMovesForPiece(board, 4, 1);
        PromotionMove promotion = (PromotionMove) moves.stream()
                .filter(m -> m instanceof PromotionMove && m.getField().getColumn() == 4)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No forward PromotionMove found"));

        promotion.execute(board);
        promotion.undo(board);

        // Pawn should be restored to e7
        assertEquals("Pawn should be back on e7 after promotion undo",
                pawn, board.getField(4, 1).getPiece());
        // e8 should be empty
        assertNull("e8 should be empty after promotion undo",
                board.getField(4, 0).getPiece());
    }
}
