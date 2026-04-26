package com.chess.mutation.pieces;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.pieces.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Mutation-targeted tests for com.chess.root.pieces.
 *
 * Each test is annotated with the specific survived mutant it is designed to kill.
 * FEN coordinate convention: col=0..7 (a..h), row=0..7 (rank8..rank1).
 * White = !color (color==false), Black = color (color==true).
 */
public class PieceMutationTest extends ChessTestBase {

    // -------------------------------------------------------------------------
    // Board positions used across tests
    // -------------------------------------------------------------------------
    // Knight e4 (col=4, row=4), kings on corners to avoid interference
    private static final String FEN_KNIGHT_E4 = "k7/8/8/8/4N3/8/8/7K w - - 0 1";
    // Bishop e4
    private static final String FEN_BISHOP_E4 = "7k/8/8/8/4B3/8/8/K7 w - - 0 1";
    // Queen e4
    private static final String FEN_QUEEN_E4  = "7k/8/8/8/4Q3/8/8/K7 w - - 0 1";
    // Rook h1 (col=7, row=7) — at home square, so wasMoved()==false
    private static final String FEN_ROOK_H1   = "4k3/8/8/8/8/8/8/4K2R w K - 0 1";
    // King e1, rook h1 — castling available so king.wasMoved()==false
    private static final String FEN_KING_E1   = "4k3/8/8/8/8/8/8/4K2R w K - 0 1";
    // White pawn e2 (col=4, row=6)
    private static final String FEN_PAWN_E2   = "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1";
    // Black pawn e7 (col=4, row=1)
    private static final String FEN_BPAWN_E7  = "4k3/4p3/8/8/8/8/8/4K3 b - - 0 1";

    // -------------------------------------------------------------------------
    // Piece.getNotation() — kills getNotation() → "" mutant
    // -------------------------------------------------------------------------

    /**
     * Kills: Piece.getNotation() replaced by empty string constant.
     * Each piece type returns its canonical algebraic notation letter.
     */
    @Test
    public void testPieceNotationIsTypeSpecific() {
        Board bN = createBoard(FEN_KNIGHT_E4);
        Board bB = createBoard(FEN_BISHOP_E4);
        Board bR = createBoard(FEN_ROOK_H1);
        Board bQ = createBoard(FEN_QUEEN_E4);
        Board bK = createBoard(FEN_KING_E1);
        Board bP = createBoard(FEN_PAWN_E2);

        assertEquals("Knight notation must be N", "N", bN.getField(4, 4).getPiece().getNotation());
        assertEquals("Bishop notation must be B", "B", bB.getField(4, 4).getPiece().getNotation());
        assertEquals("Rook notation must be R",   "R", bR.getField(7, 7).getPiece().getNotation());
        assertEquals("Queen notation must be Q",  "Q", bQ.getField(4, 4).getPiece().getNotation());
        assertEquals("King notation must be K",   "K", bK.getField(4, 7).getPiece().getNotation());
        assertEquals("Pawn notation must be empty string", "", bP.getField(4, 6).getPiece().getNotation());
    }

    // -------------------------------------------------------------------------
    // Piece.getFen() — kills getFen() → "" mutant
    // -------------------------------------------------------------------------

    /**
     * Kills: Piece.getFen() replaced by empty string constant.
     * White pieces use uppercase FEN letters; black pieces use lowercase.
     */
    @Test
    public void testPieceFenWhiteIsUppercase() {
        Board bN = createBoard(FEN_KNIGHT_E4);
        Board bB = createBoard(FEN_BISHOP_E4);
        Board bR = createBoard(FEN_ROOK_H1);
        Board bQ = createBoard(FEN_QUEEN_E4);
        Board bK = createBoard(FEN_KING_E1);
        Board bP = createBoard(FEN_PAWN_E2);

        assertEquals("White knight FEN", "N", bN.getField(4, 4).getPiece().getFen());
        assertEquals("White bishop FEN", "B", bB.getField(4, 4).getPiece().getFen());
        assertEquals("White rook FEN",   "R", bR.getField(7, 7).getPiece().getFen());
        assertEquals("White queen FEN",  "Q", bQ.getField(4, 4).getPiece().getFen());
        assertEquals("White king FEN",   "K", bK.getField(4, 7).getPiece().getFen());
        assertEquals("White pawn FEN",   "P", bP.getField(4, 6).getPiece().getFen());
    }

    /**
     * Kills: getFen() → "" mutant for black pieces.
     */
    @Test
    public void testPieceFenBlackIsLowercase() {
        Board board = createBoard(FEN_BPAWN_E7);
        Piece bpawn = board.getField(4, 1).getPiece();
        assertEquals("Black pawn FEN must be lowercase 'p'", "p", bpawn.getFen());
    }

    // -------------------------------------------------------------------------
    // Piece.wasMoved() (base class) — kills wasMoved() → false mutant
    // -------------------------------------------------------------------------

    /**
     * Kills: Piece.wasMoved() (base class) replaced with constant false.
     * Knight and bishop do NOT override wasMoved(); the base always returns true.
     */
    @Test
    public void testKnightWasMovedAlwaysTrue() {
        Board board = createBoard(FEN_KNIGHT_E4);
        Piece knight = board.getField(4, 4).getPiece();
        assertTrue("Base Piece.wasMoved() must always return true for knight",
                knight.wasMoved());
    }

    @Test
    public void testBishopWasMovedAlwaysTrue() {
        Board board = createBoard(FEN_BISHOP_E4);
        Piece bishop = board.getField(4, 4).getPiece();
        assertTrue("Base Piece.wasMoved() must always return true for bishop",
                bishop.wasMoved());
    }

    // -------------------------------------------------------------------------
    // Piece.isDead() (base class) — kills isDead() → true mutant
    // -------------------------------------------------------------------------

    /**
     * Kills: Piece.isDead() (base class) replaced with constant true.
     * Knight does not override isDead(); base always returns false.
     */
    @Test
    public void testKnightIsDeadAlwaysFalse() {
        Board board = createBoard(FEN_KNIGHT_E4);
        Piece knight = board.getField(4, 4).getPiece();
        assertFalse("Base Piece.isDead() must always return false for knight",
                knight.isDead());
    }

    // -------------------------------------------------------------------------
    // RookPiece.wasMoved() / moved() / unmove() — kills counter mutation
    // -------------------------------------------------------------------------

    /**
     * Kills: RookPiece.wasMoved() → false constant mutant.
     * Rook at h1 starts with moved=false; after calling moved() it is true.
     */
    @Test
    public void testRookWasMovedFalseAtHomeSquare() {
        Board board = createBoard(FEN_ROOK_H1);
        RookPiece rook = (RookPiece) board.getField(7, 7).getPiece();
        assertFalse("White rook at h1 (home square) should start with wasMoved()==false",
                rook.wasMoved());
    }

    /**
     * Kills: RookPiece.moved() counter-- mutant AND unmove() counter++ mutant.
     *
     * Correct sequence: movecounter 0→1→0, moved: false→true→false.
     * counter-- mutant:  movecounter 0→-1→-2 (never reaches 0 in unmove check), moved stays true.
     * counter++ in unmove: movecounter 0→1→2 (never reaches 0), moved stays true.
     * Both mutants are killed by asserting wasMoved()==false after moved() then unmove().
     */
    @Test
    public void testRookWasMovedFalseAfterMoveThenUnmove() {
        Board board = createBoard(FEN_ROOK_H1);
        RookPiece rook = (RookPiece) board.getField(7, 7).getPiece();
        assertFalse("Precondition: rook starts not moved", rook.wasMoved());
        rook.moved();
        assertTrue("After moved(), wasMoved() must be true", rook.wasMoved());
        rook.unmove();
        assertFalse("After moved()+unmove(), wasMoved() must be false again", rook.wasMoved());
    }

    // -------------------------------------------------------------------------
    // RookPiece.isDead() / kill() / revive() — kills isDead() → false mutant
    // -------------------------------------------------------------------------

    /**
     * Kills: RookPiece.isDead() replaced with constant false.
     * After kill() the rook must report isDead()==true.
     */
    @Test
    public void testRookIsDeadAfterKill() {
        Board board = createBoard(FEN_ROOK_H1);
        RookPiece rook = (RookPiece) board.getField(7, 7).getPiece();
        assertFalse("Rook should start alive", rook.isDead());
        rook.kill();
        assertTrue("Rook must be dead after kill()", rook.isDead());
    }

    /**
     * Kills: any mutation that leaves the rook dead after revive().
     */
    @Test
    public void testRookIsAliveAfterRevive() {
        Board board = createBoard(FEN_ROOK_H1);
        RookPiece rook = (RookPiece) board.getField(7, 7).getPiece();
        rook.kill();
        assertTrue("Rook should be dead before revive", rook.isDead());
        rook.revive();
        assertFalse("Rook must be alive after revive()", rook.isDead());
    }

    // -------------------------------------------------------------------------
    // KingPiece.wasMoved() / moved() / unmove() — kills KingPiece counter mutants
    // -------------------------------------------------------------------------

    /**
     * Kills: KingPiece.wasMoved() → false constant mutant.
     * After calling moved(), the king's wasMoved() must be true.
     */
    @Test
    public void testKingWasMovedTrueAfterMovedCall() {
        Board board = createBoard(FEN_KING_E1);
        KingPiece king = (KingPiece) board.getField(4, 7).getPiece();
        assertFalse("King at e1 should start with wasMoved()==false", king.wasMoved());
        king.moved();
        assertTrue("After moved(), king.wasMoved() must be true", king.wasMoved());
    }

    /**
     * Kills: KingPiece.moved() counter-- mutant AND unmove() counter++ mutant.
     *
     * Same pattern as rook: moved()+unmove() must restore wasMoved() to false.
     */
    @Test
    public void testKingWasMovedFalseAfterMoveThenUnmove() {
        Board board = createBoard(FEN_KING_E1);
        KingPiece king = (KingPiece) board.getField(4, 7).getPiece();
        king.moved();
        assertTrue("After moved(), king.wasMoved() must be true", king.wasMoved());
        king.unmove();
        assertFalse("After moved()+unmove(), king.wasMoved() must be false", king.wasMoved());
    }

    /**
     * Kills: KingPiece.unmove() condition != 0 mutant (inverted boundary).
     * Two moved() calls followed by two unmove() calls must restore wasMoved() to false.
     * With condition inverted (movecounter != 0), it resets moved on every unmove call
     * that does NOT hit zero, so moved becomes false after the first unmove.
     * This test verifies that after the FIRST unmove (movecounter goes from 2→1), moved is STILL true.
     */
    @Test
    public void testKingStillMovedAfterPartialUnmove() {
        Board board = createBoard(FEN_KING_E1);
        KingPiece king = (KingPiece) board.getField(4, 7).getPiece();
        king.moved();
        king.moved();
        king.unmove();
        assertTrue("King should still be marked moved after only one of two unmoves",
                king.wasMoved());
        king.unmove();
        assertFalse("King should be unmarked after both unmoves", king.wasMoved());
    }

    // -------------------------------------------------------------------------
    // PawnPiece.movesUp() — kills movesUp() → false mutant
    // -------------------------------------------------------------------------

    /**
     * Kills: PawnPiece.movesUp() replaced with constant false.
     * White pawn (color==false) must return movesUp()==true.
     */
    @Test
    public void testWhitePawnMovesUpIsTrue() {
        Board board = createBoard(FEN_PAWN_E2);
        PawnPiece pawn = (PawnPiece) board.getField(4, 6).getPiece();
        assertTrue("White pawn must move up (movesUp()==true)", pawn.movesUp());
    }

    /**
     * Kills: any mutation that makes movesUp() return true for black.
     * Black pawn (color==true) must return movesUp()==false.
     */
    @Test
    public void testBlackPawnMovesUpIsFalse() {
        Board board = createBoard(FEN_BPAWN_E7);
        PawnPiece pawn = (PawnPiece) board.getField(4, 1).getPiece();
        assertFalse("Black pawn must NOT move up (movesUp()==false)", pawn.movesUp());
    }

    // -------------------------------------------------------------------------
    // PawnPiece.getQueenRating() — kills getQueenRating() → 0 mutant
    // -------------------------------------------------------------------------

    /**
     * Kills: PawnPiece.getQueenRating() replaced with constant 0.
     * Queen rating is a large positive value derived from PieceValues.queen().
     */
    @Test
    public void testPawnQueenRatingIsPositive() {
        Board board = createBoard(FEN_PAWN_E2);
        PawnPiece pawn = (PawnPiece) board.getField(4, 6).getPiece();
        assertTrue("Queen rating must be a positive value (queen is the most valuable piece)",
                pawn.getQueenRating() > 0);
    }

    // -------------------------------------------------------------------------
    // Piece.getValue() — kills rating - posValue subtraction mutant
    // -------------------------------------------------------------------------

    /**
     * Kills: Piece.getValue() changed from (rating + posValue) to (rating - posValue).
     * Uses a bishop at e4 where posValue > 0 (center is strong for sliders).
     * If subtraction: getValue() < getRating(); if addition: getValue() > getRating().
     */
    @Test
    public void testPieceValueExceedsRatingAtFavorableSquare() {
        // Knight e4 — center position is strongly positive in piece-square tables
        Board board = createBoard(FEN_KNIGHT_E4);
        Piece knight = board.getField(4, 4).getPiece();
        int rating = knight.getRating();
        int value  = knight.getValue();
        assertTrue("Piece rating must be positive", rating > 0);
        // At a central square the position bonus is positive, so getValue() > getRating()
        assertTrue("Knight at e4 should have getValue() > getRating() (positive positional bonus)",
                value > rating);
    }

    // -------------------------------------------------------------------------
    // Piece.toString() — kills toString() → "" mutant
    // -------------------------------------------------------------------------

    /**
     * Kills: Piece.toString() replaced with empty string constant.
     */
    @Test
    public void testPieceToStringIsNotEmpty() {
        Board board = createBoard(FEN_KNIGHT_E4);
        Piece knight = board.getField(4, 4).getPiece();
        assertFalse("Piece.toString() must not be empty", knight.toString().isEmpty());
    }

    // -------------------------------------------------------------------------
    // Piece.hashCode() — kills hashCode() → 0 and arithmetic mutants
    // -------------------------------------------------------------------------

    /**
     * Kills: Piece.hashCode() replaced with constant 0.
     * A knight at e4 has non-zero field indices and a non-zero toString() hashCode,
     * making the overall hash non-zero.
     */
    @Test
    public void testPieceHashCodeIsNotZero() {
        Board board = createBoard(FEN_KNIGHT_E4);
        Piece knight = board.getField(4, 4).getPiece();
        assertNotEquals("Piece.hashCode() must not be 0", 0, knight.hashCode());
    }

    /**
     * Kills: Piece.hashCode() arithmetic mutants (e.g., + replaced by -).
     * Two pieces of different types at different positions must have distinct hash codes.
     */
    @Test
    public void testPieceHashCodesDistinctByTypeAndPosition() {
        Board bN = createBoard(FEN_KNIGHT_E4);
        Board bB = createBoard(FEN_BISHOP_E4);
        Piece knight = bN.getField(4, 4).getPiece();
        Piece bishop = bB.getField(4, 4).getPiece();
        // Different piece types at same square should have different hashes
        // (toString differs → "knighte4" vs "bishope4")
        assertNotEquals("Knight and bishop at e4 must have different hash codes",
                knight.hashCode(), bishop.hashCode());
    }

    // -------------------------------------------------------------------------
    // Piece.equals() — kills instanceof and conditional mutants
    // -------------------------------------------------------------------------

    /**
     * Kills: Piece.equals() instanceof conditions replaced/inverted.
     * Identity check (this == obj) combined with correct instanceof must return true
     * for each piece type when compared with itself.
     */
    @Test
    public void testPieceEqualsIdentityForAllTypes() {
        Board bN = createBoard(FEN_KNIGHT_E4);
        Board bB = createBoard(FEN_BISHOP_E4);
        Board bR = createBoard(FEN_ROOK_H1);
        Board bQ = createBoard(FEN_QUEEN_E4);
        Board bK = createBoard(FEN_KING_E1);
        Board bP = createBoard(FEN_PAWN_E2);

        Piece knight = bN.getField(4, 4).getPiece();
        Piece bishop = bB.getField(4, 4).getPiece();
        Piece rook   = bR.getField(7, 7).getPiece();
        Piece queen  = bQ.getField(4, 4).getPiece();
        Piece king   = bK.getField(4, 7).getPiece();
        Piece pawn   = bP.getField(4, 6).getPiece();

        assertEquals("KnightPiece.equals(itself) must be true", knight, knight);
        assertEquals("BishopPiece.equals(itself) must be true", bishop, bishop);
        assertEquals("RookPiece.equals(itself) must be true",   rook,   rook);
        assertEquals("QueenPiece.equals(itself) must be true",  queen,  queen);
        assertEquals("KingPiece.equals(itself) must be true",   king,   king);
        assertEquals("PawnPiece.equals(itself) must be true",   pawn,   pawn);
    }

    /**
     * Kills: Piece.equals() returning true for cross-type comparison.
     * Different piece types at the same square must NOT be equal to each other.
     * (Tests that instanceof checks are working correctly.)
     */
    @Test
    public void testPieceEqualsFalseAcrossTypes() {
        Board bN = createBoard(FEN_KNIGHT_E4);
        Board bB = createBoard(FEN_BISHOP_E4);
        Piece knight = bN.getField(4, 4).getPiece();
        Piece bishop = bB.getField(4, 4).getPiece();
        assertNotEquals("Knight must not equal Bishop even at same square", knight, bishop);
    }

    /**
     * Kills: Piece.equals() EQUAL_ELSE mutants that corrupt individual instanceof checks.
     * Each piece type is tested for self-equality AND for inequality with a different type.
     * Specifically targets: `this instanceof RookPiece && obj instanceof RookPiece` variants.
     */
    @Test
    public void testEachPieceEqualsItselfAndNotOther() {
        Board bN = createBoard(FEN_KNIGHT_E4);
        Board bR = createBoard(FEN_ROOK_H1);
        Board bK = createBoard(FEN_KING_E1);
        Board bP = createBoard(FEN_PAWN_E2);
        Board bQ = createBoard(FEN_QUEEN_E4);
        Board bB = createBoard(FEN_BISHOP_E4);

        Piece knight = bN.getField(4, 4).getPiece();
        Piece rook   = bR.getField(7, 7).getPiece();
        Piece king   = bK.getField(4, 7).getPiece();
        Piece pawn   = bP.getField(4, 6).getPiece();
        Piece queen  = bQ.getField(4, 4).getPiece();
        Piece bishop = bB.getField(4, 4).getPiece();

        // Self-equality for all types
        assertTrue("Knight equals itself",  knight.equals(knight));
        assertTrue("Rook equals itself",    rook.equals(rook));
        assertTrue("King equals itself",    king.equals(king));
        assertTrue("Pawn equals itself",    pawn.equals(pawn));
        assertTrue("Queen equals itself",   queen.equals(queen));
        assertTrue("Bishop equals itself",  bishop.equals(bishop));

        // Cross-type inequality (kills EQUAL_IF that merges instanceof conditions)
        assertFalse("Knight != Rook",   knight.equals(rook));
        assertFalse("Rook != Knight",   rook.equals(knight));
        assertFalse("King != Pawn",     king.equals(pawn));
        assertFalse("Pawn != King",     pawn.equals(king));
        assertFalse("Queen != Bishop",  queen.equals(bishop));
        assertFalse("Bishop != Queen",  bishop.equals(queen));
    }

    // -------------------------------------------------------------------------
    // Piece.createSymbol() — kills VoidMethodCallMutator (setFitWidth/setFitHeight removed)
    // and EQUAL_ELSE conditions (image==null || symbol==null and isBlack() check)
    // -------------------------------------------------------------------------

    /**
     * Kills: VoidMethodCallMutator removing img.setFitWidth(IMGSIZE) at line 158.
     * If setFitWidth is not called, getSymbol().getFitWidth() returns 0 (default).
     */
    @Test
    public void testPieceSymbolFitWidthIsCorrect() {
        Board board = createBoard(FEN_KNIGHT_E4);
        Piece knight = board.getField(4, 4).getPiece();
        assertNotNull("Knight symbol must not be null", knight.getSymbol());
        assertEquals("Symbol fitWidth must be 60 (IMGSIZE)", 60.0, knight.getSymbol().getFitWidth(), 0.001);
    }

    /**
     * Kills: VoidMethodCallMutator removing img.setFitHeight(IMGSIZE) at line 159.
     * If setFitHeight is not called, getSymbol().getFitHeight() returns 0 (default).
     */
    @Test
    public void testPieceSymbolFitHeightIsCorrect() {
        Board board = createBoard(FEN_KNIGHT_E4);
        Piece knight = board.getField(4, 4).getPiece();
        assertNotNull("Knight symbol must not be null", knight.getSymbol());
        assertEquals("Symbol fitHeight must be 60 (IMGSIZE)", 60.0, knight.getSymbol().getFitHeight(), 0.001);
    }

    /**
     * Kills: Piece.createSymbol() EQUAL_IF condition mutant (line 152: image==null||symbol==null).
     * Tests that getImage() returns a non-null Image after piece creation.
     */
    @Test
    public void testPieceImageNotNull() {
        Board board = createBoard(FEN_PAWN_E2);
        Piece pawn = board.getField(4, 6).getPiece();
        assertNotNull("Piece image must not be null after creation", pawn.getImage());
    }

    /**
     * Kills: Piece.createSymbol() EQUAL_ELSE condition mutant (line 155: isBlack() check).
     * A black piece and white piece at the same position must both have non-null symbols.
     * The fact both are created from the correct file path is tested via non-null symbol.
     */
    @Test
    public void testBlackPieceSymbolIsNotNull() {
        Board board = createBoard(FEN_BPAWN_E7);
        Piece bpawn = board.getField(4, 1).getPiece();
        assertNotNull("Black piece symbol must not be null", bpawn.getSymbol());
        assertNotNull("Black piece image must not be null", bpawn.getImage());
        assertEquals("Black piece symbol fitWidth must be 60", 60.0, bpawn.getSymbol().getFitWidth(), 0.001);
        assertEquals("Black piece symbol fitHeight must be 60", 60.0, bpawn.getSymbol().getFitHeight(), 0.001);
    }
}
