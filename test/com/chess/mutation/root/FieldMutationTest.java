package com.chess.mutation.root;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.Field;
import com.chess.root.pieces.Piece;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Mutation-targeted tests for com.chess.root.Field.
 *
 * Surviving mutants targeted:
 *  - getColNotation(): switch-case mutations for all 8 columns (a–h)
 *  - getRowNotation(): switch-case mutations for all 8 rows (8→1)
 *  - getNotation(): concatenation replaced with constant ""
 *  - isBlack(): replaced return with true / replaced return with false
 *  - getColumn() / getRow(): replaced return with 0
 *  - getFen(): replaced return with null when piece present
 *  - getPiece(): replaced return with null
 *  - equals(): condition mutations
 *  - hashCode(): constant mutations (3↔4, 123 changed)
 *  - setPieceSilently / removePieceSilently: assignment skipped
 */
public class FieldMutationTest extends ChessTestBase {

    // =========================================================================
    // getColNotation() — tests all 8 cases in the switch
    // =========================================================================

    @Test
    public void testColNotationAllEightColumns() {
        Board board = createBoard(FEN_START);
        String[] expected = {"a", "b", "c", "d", "e", "f", "g", "h"};
        for (int col = 0; col < 8; col++) {
            assertEquals("Column " + col + " notation",
                    expected[col], board.getField(col, 0).getColNotation());
        }
    }

    @Test
    public void testColNotationA() {
        assertEquals("a", createBoard(FEN_START).getField(0, 0).getColNotation());
    }

    @Test
    public void testColNotationB() {
        assertEquals("b", createBoard(FEN_START).getField(1, 0).getColNotation());
    }

    @Test
    public void testColNotationC() {
        assertEquals("c", createBoard(FEN_START).getField(2, 0).getColNotation());
    }

    @Test
    public void testColNotationD() {
        assertEquals("d", createBoard(FEN_START).getField(3, 0).getColNotation());
    }

    @Test
    public void testColNotationE() {
        assertEquals("e", createBoard(FEN_START).getField(4, 0).getColNotation());
    }

    @Test
    public void testColNotationF() {
        assertEquals("f", createBoard(FEN_START).getField(5, 0).getColNotation());
    }

    @Test
    public void testColNotationG() {
        assertEquals("g", createBoard(FEN_START).getField(6, 0).getColNotation());
    }

    @Test
    public void testColNotationH() {
        assertEquals("h", createBoard(FEN_START).getField(7, 0).getColNotation());
    }

    // =========================================================================
    // getRowNotation() — tests all 8 cases in the switch (row 0→8, row 7→1)
    // =========================================================================

    @Test
    public void testRowNotationAllEightRows() {
        Board board = createBoard(FEN_START);
        int[] expected = {8, 7, 6, 5, 4, 3, 2, 1};
        for (int row = 0; row < 8; row++) {
            assertEquals("Row " + row + " notation",
                    expected[row], board.getField(0, row).getRowNotation());
        }
    }

    @Test
    public void testRowNotationRank8() {
        assertEquals(8, createBoard(FEN_START).getField(0, 0).getRowNotation());
    }

    @Test
    public void testRowNotationRank7() {
        assertEquals(7, createBoard(FEN_START).getField(0, 1).getRowNotation());
    }

    @Test
    public void testRowNotationRank6() {
        assertEquals(6, createBoard(FEN_START).getField(0, 2).getRowNotation());
    }

    @Test
    public void testRowNotationRank5() {
        assertEquals(5, createBoard(FEN_START).getField(0, 3).getRowNotation());
    }

    @Test
    public void testRowNotationRank4() {
        assertEquals(4, createBoard(FEN_START).getField(0, 4).getRowNotation());
    }

    @Test
    public void testRowNotationRank3() {
        assertEquals(3, createBoard(FEN_START).getField(0, 5).getRowNotation());
    }

    @Test
    public void testRowNotationRank2() {
        assertEquals(2, createBoard(FEN_START).getField(0, 6).getRowNotation());
    }

    @Test
    public void testRowNotationRank1() {
        assertEquals(1, createBoard(FEN_START).getField(0, 7).getRowNotation());
    }

    // =========================================================================
    // getNotation() — kills "" constant replacement mutant
    // =========================================================================

    @Test
    public void testNotationA8() {
        assertEquals("a8", createBoard(FEN_START).getField(0, 0).getNotation());
    }

    @Test
    public void testNotationH1() {
        assertEquals("h1", createBoard(FEN_START).getField(7, 7).getNotation());
    }

    @Test
    public void testNotationE4() {
        assertEquals("e4", createBoard(FEN_START).getField(4, 4).getNotation());
    }

    @Test
    public void testNotationD5() {
        assertEquals("d5", createBoard(FEN_START).getField(3, 3).getNotation());
    }

    @Test
    public void testNotationA1() {
        assertEquals("a1", createBoard(FEN_START).getField(0, 7).getNotation());
    }

    @Test
    public void testNotationH8() {
        assertEquals("h8", createBoard(FEN_START).getField(7, 0).getNotation());
    }

    @Test
    public void testNotationIsNotEmpty() {
        Board board = createBoard(FEN_START);
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                assertFalse("getNotation() must not be empty at (" + col + "," + row + ")",
                        board.getField(col, row).getNotation().isEmpty());
            }
        }
    }

    // =========================================================================
    // isBlack() — kills replaced-return-with-true and replaced-return-with-false
    // =========================================================================

    @Test
    public void testA8IsWhiteSquare() {
        // In this implementation: black=false starts at (col=0,row=0).
        // isBlack = (col+row) % 2 == 1. a8: (0+0)%2=0 → isBlack=false (white square).
        assertFalse("a8 should be a white square in this board's coloring convention",
                createBoard(FEN_START).getField(0, 0).isBlack());
    }

    @Test
    public void testB8IsBlackSquare() {
        // b8: (col=1,row=0) → (1+0)%2=1 → isBlack=true (black square)
        assertTrue("b8 should be a black square in this board's coloring convention",
                createBoard(FEN_START).getField(1, 0).isBlack());
    }

    @Test
    public void testH1IsWhiteSquare() {
        assertFalse("h1 should not be a black square",
                createBoard(FEN_START).getField(7, 7).isBlack());
    }

    @Test
    public void testG1IsBlackSquare() {
        assertTrue("g1 should be a black square",
                createBoard(FEN_START).getField(6, 7).isBlack());
    }

    @Test
    public void testAdjacentFieldsHaveDifferentColor() {
        Board board = createBoard(FEN_START);
        Field a8 = board.getField(0, 0); // white square (isBlack=false)
        Field b8 = board.getField(1, 0); // black square (isBlack=true)
        assertNotEquals("Horizontally adjacent fields must have different square colors",
                a8.isBlack(), b8.isBlack());
    }

    @Test
    public void testDiagonalFieldsHaveSameColor() {
        Board board = createBoard(FEN_START);
        Field a8 = board.getField(0, 0); // white (isBlack=false)
        Field b7 = board.getField(1, 1); // (1+1)%2=0 → isBlack=false (also white)
        assertEquals("Diagonally adjacent fields must have the same color",
                a8.isBlack(), b7.isBlack());
    }

    // =========================================================================
    // getColumn() / getRow() — kills return-0 mutant
    // =========================================================================

    @Test
    public void testGetColumnForAllColumns() {
        Board board = createBoard(FEN_START);
        for (int col = 0; col < 8; col++) {
            assertEquals("getColumn() for col " + col,
                    col, board.getField(col, 0).getColumn());
        }
    }

    @Test
    public void testGetRowForAllRows() {
        Board board = createBoard(FEN_START);
        for (int row = 0; row < 8; row++) {
            assertEquals("getRow() for row " + row,
                    row, board.getField(0, row).getRow());
        }
    }

    // =========================================================================
    // getFen() — kills replaced-return-null mutant for occupied field
    // =========================================================================

    @Test
    public void testGetFenEmptyFieldReturnsNull() {
        Board board = createBoard(FEN_START);
        assertNull("e4 is empty in start position → getFen() == null",
                board.getField(4, 4).getFen());
    }

    @Test
    public void testGetFenWhiteKingE1() {
        Board board = createBoard(FEN_START);
        assertEquals("K", board.getField(4, 7).getFen());
    }

    @Test
    public void testGetFenBlackKingE8() {
        Board board = createBoard(FEN_START);
        assertEquals("k", board.getField(4, 0).getFen());
    }

    @Test
    public void testGetFenWhiteQueenD1() {
        Board board = createBoard(FEN_START);
        assertEquals("Q", board.getField(3, 7).getFen());
    }

    @Test
    public void testGetFenBlackRookA8() {
        Board board = createBoard(FEN_START);
        assertEquals("r", board.getField(0, 0).getFen());
    }

    @Test
    public void testGetFenWhitePawnIsUpperCase() {
        Board board = createBoard(FEN_START);
        String fen = board.getField(0, 6).getFen(); // a2 has white pawn
        assertNotNull(fen);
        assertEquals("P", fen);
        assertEquals(fen.toUpperCase(), fen);
    }

    @Test
    public void testGetFenBlackPawnIsLowerCase() {
        Board board = createBoard(FEN_START);
        String fen = board.getField(0, 1).getFen(); // a7 has black pawn
        assertNotNull(fen);
        assertEquals("p", fen);
        assertEquals(fen.toLowerCase(), fen);
    }

    // =========================================================================
    // getPiece() — kills replaced-return-null mutant
    // =========================================================================

    @Test
    public void testGetPieceNotNullForOccupiedField() {
        Board board = createBoard(FEN_START);
        assertNotNull("a8 has a piece in start position", board.getField(0, 0).getPiece());
    }

    @Test
    public void testGetPieceNullForEmptyField() {
        Board board = createBoard(FEN_START);
        assertNull("e4 is empty in start position", board.getField(4, 4).getPiece());
    }

    @Test
    public void testGetPieceIsCorrectType() {
        Board board = createBoard(FEN_START);
        Piece e1 = board.getField(4, 7).getPiece(); // e1 = white king
        assertNotNull(e1);
        assertFalse("White king should not be black", e1.isBlack());
    }

    // =========================================================================
    // setPieceSilently() / removePieceSilently() — kills assignment-skip mutants
    // =========================================================================

    @Test
    public void testSetPieceSilentlyUpdatesPiece() {
        Board board = createBoard(FEN_START);
        Piece rook = board.getField(0, 0).getPiece(); // black rook at a8
        Field dest = board.getField(4, 4); // empty field e4
        assertNull("e4 should start empty", dest.getPiece());
        dest.setPieceSilently(rook);
        assertSame("e4 should now hold the rook", rook, dest.getPiece());
    }

    @Test
    public void testRemovePieceSilentlyClearsPiece() {
        Board board = createBoard(FEN_START);
        Field f = board.getField(0, 0); // a8 has black rook
        assertNotNull("a8 should have a piece initially", f.getPiece());
        f.removePieceSilently();
        assertNull("a8 should be empty after removePieceSilently()", f.getPiece());
    }

    @Test
    public void testSetThenRemoveSilently() {
        Board board = createBoard(FEN_START);
        Piece piece = board.getField(4, 7).getPiece(); // white king
        Field target = board.getField(4, 4);
        target.setPieceSilently(piece);
        assertNotNull(target.getPiece());
        target.removePieceSilently();
        assertNull("Field should be empty after remove", target.getPiece());
    }

    // =========================================================================
    // equals() — kills condition mutations
    // =========================================================================

    @Test
    public void testFieldEqualsItself() {
        Board board = createBoard(FEN_START);
        Field f = board.getField(0, 0);
        assertTrue("Field must equal itself", f.equals(f));
    }

    @Test
    public void testFieldDoesNotEqualDifferentField() {
        Board board = createBoard(FEN_START);
        Field f1 = board.getField(0, 0);
        Field f2 = board.getField(1, 0);
        assertFalse("Two distinct fields must not be equal", f1.equals(f2));
    }

    @Test
    public void testFieldDoesNotEqualNull() {
        Board board = createBoard(FEN_START);
        assertFalse(board.getField(0, 0).equals(null));
    }

    // =========================================================================
    // hashCode() — kills constant mutations (3↔4, +123 removed/changed)
    // =========================================================================

    @Test
    public void testBlackFieldHashCodeFormula() {
        Board board = createBoard(FEN_START);
        // b8: (col=1,row=0) → isBlack=true → hash uses 3
        Field f = board.getField(1, 0);
        assertTrue("b8 should be a black square in this coloring", f.isBlack());
        int expected = 3 + f.toString().hashCode() + 123;
        assertEquals("Black field hashCode must follow formula", expected, f.hashCode());
    }

    @Test
    public void testWhiteFieldHashCodeFormula() {
        Board board = createBoard(FEN_START);
        // a8: (col=0,row=0) → isBlack=false → hash uses 4
        Field f = board.getField(0, 0);
        assertFalse("a8 should be a white square in this coloring", f.isBlack());
        int expected = 4 + f.toString().hashCode() + 123;
        assertEquals("White field hashCode must follow formula", expected, f.hashCode());
    }

    @Test
    public void testBlackAndWhiteAdjacentFieldsHaveDifferentHashCodes() {
        Board board = createBoard(FEN_START);
        // a8 (col=0,row=0): isBlack=false (white square)
        // b8 (col=1,row=0): isBlack=true (black square)
        Field white = board.getField(0, 0);
        Field black = board.getField(1, 0);
        assertNotEquals("Adjacent fields with different colors must have different hashes",
                black.hashCode(), white.hashCode());
    }

    @Test
    public void testHashCodeIsStable() {
        Board board = createBoard(FEN_START);
        Field f = board.getField(3, 3);
        assertEquals("hashCode must be stable", f.hashCode(), f.hashCode());
    }

    // =========================================================================
    // toString() — kills replaced-return-with-"" mutant
    // =========================================================================

    @Test
    public void testFieldToStringNotEmpty() {
        Board board = createBoard(FEN_START);
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                assertFalse("toString() must not be empty at (" + col + "," + row + ")",
                        board.getField(col, row).toString().isEmpty());
            }
        }
    }
}
