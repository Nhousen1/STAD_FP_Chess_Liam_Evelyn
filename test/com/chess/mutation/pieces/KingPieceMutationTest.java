package com.chess.mutation.pieces;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.FenParser;
import com.chess.root.moves.Move;
import com.chess.root.pieces.KingPiece;
import com.chess.root.pieces.Piece;
import com.chess.root.pieces.RookPiece;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Mutation-targeted tests for com.chess.root.pieces.KingPiece.
 *
 * Surviving mutants targeted:
 *  - <init>(): moved flag set from position (e1/e8 checks)
 *  - checkInit(): rook detection conditions
 *  - setEndTable(): end-game table assignment conditions
 *  - getCastlingFen(): moved check and rook conditions
 *  - getCastlingMoves(): rook conditions
 *  - initializeFenCastling(): "k"/"q"/"K"/"Q" contains checks and rook.moved() calls
 *  - getMoves(): boundary math (col +/- direction)
 */
public class KingPieceMutationTest extends ChessTestBase {

    // Kings only at standard positions
    private static final String FEN_KINGS_STD =
            "4k3/8/8/8/8/8/8/4K3 w - - 0 1";

    // All 4 castling rights
    private static final String FEN_FULL_CASTLING =
            "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1";

    // Only white kingside castling
    private static final String FEN_WHITE_KSIDE_ONLY =
            "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w K - 0 1";

    // Only white queenside castling
    private static final String FEN_WHITE_QSIDE_ONLY =
            "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w Q - 0 1";

    // Only black kingside castling
    private static final String FEN_BLACK_KSIDE_ONLY =
            "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w k - 0 1";

    // Only black queenside castling
    private static final String FEN_BLACK_QSIDE_ONLY =
            "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w q - 0 1";

    // No castling rights
    private static final String FEN_NO_CASTLING =
            "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w - - 0 1";

    // White king NOT at e1 → should start as moved
    private static final String FEN_WHITE_KING_D1 =
            "4k3/8/8/8/8/8/8/3K4 w - - 0 1";

    // Black king NOT at e8 → should start as moved
    private static final String FEN_BLACK_KING_D8 =
            "3k4/8/8/8/8/8/8/4K3 w - - 0 1";

    // =========================================================================
    // KingPiece.<init>() — kills position-based moved initialization mutants
    // moved = color ? !field.getNotation().contentEquals("e8") : !field.getNotation().contentEquals("e1")
    // =========================================================================

    @Test
    public void testWhiteKingAtE1StartsNotMoved() {
        Board board = createBoard(FEN_KINGS_STD);
        KingPiece king = (KingPiece) board.getField(4, 7).getPiece();
        assertFalse("White king at e1 should start with wasMoved()==false", king.wasMoved());
    }

    @Test
    public void testWhiteKingNotAtE1StartsMoved() {
        Board board = createBoard(FEN_WHITE_KING_D1);
        KingPiece king = (KingPiece) board.getField(3, 7).getPiece();
        assertTrue("White king NOT at e1 should start with wasMoved()==true", king.wasMoved());
    }

    @Test
    public void testBlackKingAtE8StartsNotMoved() {
        Board board = createBoard(FEN_KINGS_STD);
        KingPiece king = (KingPiece) board.getField(4, 0).getPiece();
        assertFalse("Black king at e8 should start with wasMoved()==false", king.wasMoved());
    }

    @Test
    public void testBlackKingNotAtE8StartsMoved() {
        Board board = createBoard(FEN_BLACK_KING_D8);
        KingPiece king = (KingPiece) board.getField(3, 0).getPiece();
        assertTrue("Black king NOT at e8 should start with wasMoved()==true", king.wasMoved());
    }

    // =========================================================================
    // KingPiece.setEndTable() — kills conditional mutations
    // Conditions: if (end) { color ? DOWN_END : UP_END } else { color ? DOWN : UP }
    // =========================================================================

    @Test
    public void testSetEndTableChangesPositionalValue() {
        // Use a full board where gameStateCheck won't switch to endgame tables yet.
        // After setEndTable(true) AND forcing posValue update via setFieldSilently,
        // the value at e1 must change: KING_UP[7][4]=0 vs KING_UP_END[7][4]=-30.
        Board board = createBoard(FEN_FULL_CASTLING); // many pieces → not endgame
        KingPiece whiteKing = (KingPiece) board.getField(4, 7).getPiece();
        // Force midgame table first
        whiteKing.setEndTable(false);
        whiteKing.setFieldSilently(board.getField(4, 7)); // trigger updatePos with midgame table
        int valueMidgame = whiteKing.getValue();

        // Switch to endgame table and re-trigger posValue update
        whiteKing.setEndTable(true);
        whiteKing.setFieldSilently(board.getField(4, 7));
        int valueEndgame = whiteKing.getValue();

        // KING_UP[7][4]=0 vs KING_UP_END[7][4]=-30 → values must differ
        assertNotEquals("setEndTable(true) should change the king's positional value via different table",
                valueMidgame, valueEndgame);
    }

    @Test
    public void testSetEndTableFalseRestoresOriginalValue() {
        Board board = createBoard(FEN_FULL_CASTLING);
        KingPiece whiteKing = (KingPiece) board.getField(4, 7).getPiece();
        whiteKing.setEndTable(false);
        whiteKing.setFieldSilently(board.getField(4, 7));
        int originalValue = whiteKing.getValue();

        whiteKing.setEndTable(true);
        whiteKing.setFieldSilently(board.getField(4, 7));
        whiteKing.setEndTable(false);
        whiteKing.setFieldSilently(board.getField(4, 7));
        assertEquals("setEndTable(false) should restore original positional value",
                originalValue, whiteKing.getValue());
    }

    @Test
    public void testBlackKingSetEndTableChangesValue() {
        Board board = createBoard(FEN_FULL_CASTLING);
        KingPiece blackKing = (KingPiece) board.getField(4, 0).getPiece();
        blackKing.setEndTable(false);
        blackKing.setFieldSilently(board.getField(4, 0));
        int valueMidgame = blackKing.getValue();

        blackKing.setEndTable(true);
        blackKing.setFieldSilently(board.getField(4, 0));
        int valueEndgame = blackKing.getValue();

        assertNotEquals("setEndTable(true) should change black king's positional value", valueMidgame, valueEndgame);
    }

    // =========================================================================
    // KingPiece.getCastlingFen() — kills conditional mutations
    // =========================================================================

    @Test
    public void testGetCastlingFenAllRights() {
        Board board = createBoard(FEN_FULL_CASTLING);
        KingPiece whiteKing = (KingPiece) board.getField(4, 7).getPiece();
        String fen = whiteKing.getCastlingFen();
        assertTrue("White king with full castling should have 'K' in castling FEN", fen.contains("K"));
        assertTrue("White king with full castling should have 'Q' in castling FEN", fen.contains("Q"));
        assertFalse("White king castling FEN should be uppercase (not 'k')", fen.contains("k"));
    }

    @Test
    public void testGetCastlingFenKingsideOnly() {
        Board board = createBoard(FEN_WHITE_KSIDE_ONLY);
        KingPiece whiteKing = (KingPiece) board.getField(4, 7).getPiece();
        String fen = whiteKing.getCastlingFen();
        // Only "K" — queenside rook was marked moved by initializeFenCastling
        assertTrue("White king castling FEN should contain 'K' when only kingside right", fen.contains("K"));
        assertFalse("White king castling FEN must NOT contain 'Q' when queenside rook was moved", fen.contains("Q"));
    }

    @Test
    public void testGetCastlingFenQueensideOnly() {
        Board board = createBoard(FEN_WHITE_QSIDE_ONLY);
        KingPiece whiteKing = (KingPiece) board.getField(4, 7).getPiece();
        String fen = whiteKing.getCastlingFen();
        assertFalse("White king castling FEN must NOT contain 'K' when only queenside right", fen.contains("K"));
        assertTrue("White king castling FEN should contain 'Q' when only queenside right", fen.contains("Q"));
    }

    @Test
    public void testGetCastlingFenNoCastlingRights() {
        Board board = createBoard(FEN_NO_CASTLING);
        KingPiece whiteKing = (KingPiece) board.getField(4, 7).getPiece();
        String fen = whiteKing.getCastlingFen();
        // Both rooks marked moved → no rights → empty string
        assertTrue("White king castling FEN should be empty when no rights", fen.isEmpty());
    }

    @Test
    public void testGetCastlingFenBlackKingsideOnly() {
        Board board = createBoard(FEN_BLACK_KSIDE_ONLY);
        KingPiece blackKing = (KingPiece) board.getField(4, 0).getPiece();
        String fen = blackKing.getCastlingFen();
        // Should be lowercase "k" for black
        assertTrue("Black king castling FEN should contain 'k' when only kingside right", fen.contains("k"));
        assertFalse("Black king castling FEN must NOT contain 'q' when queenside rook was moved", fen.contains("q"));
    }

    @Test
    public void testGetCastlingFenBlackQueensideOnly() {
        Board board = createBoard(FEN_BLACK_QSIDE_ONLY);
        KingPiece blackKing = (KingPiece) board.getField(4, 0).getPiece();
        String fen = blackKing.getCastlingFen();
        assertFalse("Black king castling FEN must NOT contain 'k' when only queenside right", fen.contains("k"));
        assertTrue("Black king castling FEN should contain 'q' when only queenside right", fen.contains("q"));
    }

    @Test
    public void testGetCastlingFenMovedKingIsEmpty() {
        Board board = createBoard(FEN_FULL_CASTLING);
        KingPiece whiteKing = (KingPiece) board.getField(4, 7).getPiece();
        whiteKing.moved();
        String fen = whiteKing.getCastlingFen();
        assertTrue("Moved king's castling FEN must be empty string", fen.isEmpty());
    }

    // =========================================================================
    // KingPiece.initializeFenCastling() — kills conditional mutations
    // Mutants: "k"/"q"/"K"/"Q" contains check replaced with true/false;
    //          rookKingSide.moved() / rookQueenSide.moved() calls removed
    // =========================================================================

    @Test
    public void testInitFenCastlingKingsideOnlyWhiteRookQueensideMoved() {
        // FEN "K" → white queenside rook should have been marked moved during setup
        Board board = createBoard(FEN_WHITE_KSIDE_ONLY);
        // a1 rook (col=0, row=7) = white queenside rook
        RookPiece a1Rook = (RookPiece) board.getField(0, 7).getPiece();
        assertNotNull("a1 rook should exist", a1Rook);
        assertTrue("White a1 rook should be marked moved when FEN has only 'K' castling right",
                a1Rook.wasMoved());
    }

    @Test
    public void testInitFenCastlingKingsideOnlyWhiteRookKingsideNotMoved() {
        Board board = createBoard(FEN_WHITE_KSIDE_ONLY);
        // h1 rook (col=7, row=7) = white kingside rook
        RookPiece h1Rook = (RookPiece) board.getField(7, 7).getPiece();
        assertNotNull("h1 rook should exist", h1Rook);
        assertFalse("White h1 rook should NOT be marked moved when FEN has 'K' castling right",
                h1Rook.wasMoved());
    }

    @Test
    public void testInitFenCastlingQueensideOnlyWhiteRookKingsideMoved() {
        Board board = createBoard(FEN_WHITE_QSIDE_ONLY);
        RookPiece h1Rook = (RookPiece) board.getField(7, 7).getPiece();
        assertNotNull("h1 rook should exist", h1Rook);
        assertTrue("White h1 rook should be marked moved when FEN has only 'Q' castling right",
                h1Rook.wasMoved());
    }

    @Test
    public void testInitFenCastlingQueensideOnlyWhiteRookQueensideNotMoved() {
        Board board = createBoard(FEN_WHITE_QSIDE_ONLY);
        RookPiece a1Rook = (RookPiece) board.getField(0, 7).getPiece();
        assertNotNull("a1 rook should exist", a1Rook);
        assertFalse("White a1 rook should NOT be marked moved when FEN has 'Q' castling right",
                a1Rook.wasMoved());
    }

    @Test
    public void testInitFenCastlingNoneAllWhiteRooksMoved() {
        Board board = createBoard(FEN_NO_CASTLING);
        RookPiece a1Rook = (RookPiece) board.getField(0, 7).getPiece();
        RookPiece h1Rook = (RookPiece) board.getField(7, 7).getPiece();
        assertTrue("a1 rook should be marked moved when FEN has no castling rights", a1Rook.wasMoved());
        assertTrue("h1 rook should be marked moved when FEN has no castling rights", h1Rook.wasMoved());
    }

    @Test
    public void testInitFenCastlingBlackKingsideOnlyBlackRookQueensideMoved() {
        Board board = createBoard(FEN_BLACK_KSIDE_ONLY);
        // a8 rook (col=0, row=0) = black queenside rook
        RookPiece a8Rook = (RookPiece) board.getField(0, 0).getPiece();
        assertNotNull("a8 rook should exist", a8Rook);
        assertTrue("Black a8 rook should be marked moved when FEN has only 'k' castling right",
                a8Rook.wasMoved());
    }

    @Test
    public void testInitFenCastlingBlackKingsideOnlyBlackRookKingsideNotMoved() {
        Board board = createBoard(FEN_BLACK_KSIDE_ONLY);
        RookPiece h8Rook = (RookPiece) board.getField(7, 0).getPiece();
        assertNotNull("h8 rook should exist", h8Rook);
        assertFalse("Black h8 rook should NOT be marked moved when FEN has 'k' castling right",
                h8Rook.wasMoved());
    }

    @Test
    public void testInitFenCastlingBlackQueensideOnlyBlackRookKingsideMoved() {
        Board board = createBoard(FEN_BLACK_QSIDE_ONLY);
        RookPiece h8Rook = (RookPiece) board.getField(7, 0).getPiece();
        assertNotNull("h8 rook should exist", h8Rook);
        assertTrue("Black h8 rook should be marked moved when FEN has only 'q' castling right",
                h8Rook.wasMoved());
    }

    @Test
    public void testInitFenCastlingBlackQueensideOnlyBlackRookQueensideNotMoved() {
        Board board = createBoard(FEN_BLACK_QSIDE_ONLY);
        RookPiece a8Rook = (RookPiece) board.getField(0, 0).getPiece();
        assertNotNull("a8 rook should exist", a8Rook);
        assertFalse("Black a8 rook should NOT be marked moved when FEN has 'q' castling right",
                a8Rook.wasMoved());
    }

    // =========================================================================
    // KingPiece.getCastlingMoves() — kills condition mutants
    // =========================================================================

    @Test
    public void testGetCastlingMovesFullRightsHasTwoMoves() {
        Board board = createBoard(FEN_FULL_CASTLING);
        List<Move> castles = getCastlingMoves(board, 4, 7);
        assertNotNull("Full castling rights should yield castling moves", castles);
        assertEquals("With full castling rights, white king should have 2 castling moves",
                2, castles.size());
    }

    @Test
    public void testGetCastlingMovesKingsideOnlyHasOneMove() {
        Board board = createBoard(FEN_WHITE_KSIDE_ONLY);
        List<Move> castles = getCastlingMoves(board, 4, 7);
        assertNotNull("Kingside-only rights should yield castling moves", castles);
        assertEquals("With kingside-only rights, white king should have 1 castling move (O-O)",
                1, castles.size());
        assertEquals("O-O", castles.get(0).getNotation());
    }

    @Test
    public void testGetCastlingMovesQueensideOnlyHasOneMove() {
        Board board = createBoard(FEN_WHITE_QSIDE_ONLY);
        List<Move> castles = getCastlingMoves(board, 4, 7);
        assertNotNull("Queenside-only rights should yield castling moves", castles);
        assertEquals("With queenside-only rights, white king should have 1 castling move (O-O-O)",
                1, castles.size());
        assertEquals("O-O-O", castles.get(0).getNotation());
    }

    @Test
    public void testGetCastlingMovesNoCastlingRightsIsNullOrEmpty() {
        Board board = createBoard(FEN_NO_CASTLING);
        List<Move> castles = getCastlingMoves(board, 4, 7);
        // getCastlingMoves() returns null when no castling moves available
        assertTrue("No castling rights → no castling moves",
                castles == null || castles.isEmpty());
    }

    @Test
    public void testGetCastlingMovesBlackKingsideOnly() {
        Board board = createBoard(FEN_BLACK_KSIDE_ONLY);
        List<Move> castles = getCastlingMoves(board, 4, 0); // black king e8
        assertNotNull("Black kingside-only rights should yield castling moves", castles);
        assertEquals("Black king should have 1 castling move (O-O) with only 'k' rights",
                1, castles.size());
    }

    @Test
    public void testGetCastlingMovesMovedKingReturnsNullOrEmpty() {
        Board board = createBoard(FEN_FULL_CASTLING);
        KingPiece king = (KingPiece) board.getField(4, 7).getPiece();
        king.moved(); // mark king as moved
        List<Move> castles = getCastlingMoves(board, 4, 7);
        assertTrue("Moved king should have no castling moves",
                castles == null || castles.isEmpty());
    }

    // =========================================================================
    // KingPiece.getCastlingFen() round-trip via FenParser
    // =========================================================================

    @Test
    public void testCastlingFenRoundTripFullRights() {
        Board board = createBoard(FEN_FULL_CASTLING);
        String castling = FenParser.getCastling(board);
        assertTrue("Full castling: should contain 'K'", castling.contains("K"));
        assertTrue("Full castling: should contain 'Q'", castling.contains("Q"));
        assertTrue("Full castling: should contain 'k'", castling.contains("k"));
        assertTrue("Full castling: should contain 'q'", castling.contains("q"));
    }

    @Test
    public void testCastlingFenRoundTripKingsideOnly() {
        Board board = createBoard(FEN_WHITE_KSIDE_ONLY);
        String castling = FenParser.getCastling(board);
        assertEquals("Castling FEN should be 'K' for white kingside only", "K", castling);
    }

    @Test
    public void testCastlingFenRoundTripQueensideOnly() {
        Board board = createBoard(FEN_WHITE_QSIDE_ONLY);
        String castling = FenParser.getCastling(board);
        assertEquals("Castling FEN should be 'Q' for white queenside only", "Q", castling);
    }

    @Test
    public void testCastlingFenRoundTripNoCastling() {
        Board board = createBoard(FEN_NO_CASTLING);
        String castling = FenParser.getCastling(board);
        assertEquals("Castling FEN should be '-' for no castling rights", "-", castling);
    }

    // =========================================================================
    // KingPiece.getMoves() — kills boundary math mutants
    // =========================================================================

    @Test
    public void testKingAtE4HasEightMoves() {
        Board board = createBoard("7k/8/8/8/4K3/8/8/8 w - - 0 1");
        KingPiece king = (KingPiece) board.getField(4, 4).getPiece();
        List<Move> moves = king.getMoves();
        assertEquals("King at e4 (center, unobstructed) should have exactly 8 moves", 8, moves.size());
    }

    @Test
    public void testKingAtA1HasThreeMoves() {
        Board board = createBoard("7k/8/8/8/8/8/8/K7 w - - 0 1");
        KingPiece king = (KingPiece) board.getField(0, 7).getPiece();
        List<Move> moves = king.getMoves();
        assertEquals("King at a1 (corner) should have exactly 3 moves", 3, moves.size());
    }

    @Test
    public void testKingMovesDoNotIncludeOwnPieces() {
        // King at e1, rook at d1 — rook's square should not be in king's moves
        Board board = createBoard("4k3/8/8/8/8/8/8/3RK3 w - - 0 1");
        KingPiece king = (KingPiece) board.getField(4, 7).getPiece();
        List<Move> moves = king.getMoves();
        boolean goesToD1 = moves.stream().anyMatch(m -> m.getField().getColumn() == 3 && m.getField().getRow() == 7);
        assertFalse("King should not be able to move to square occupied by own rook", goesToD1);
    }

    @Test
    public void testKingCanCaptureEnemyPiece() {
        // King at e1, black pawn at d2 — king should be able to capture
        Board board = createBoard("4k3/8/8/8/8/8/3p4/4K3 w - - 0 1");
        KingPiece king = (KingPiece) board.getField(4, 7).getPiece();
        List<Move> moves = king.getMoves();
        boolean captureD2 = moves.stream().anyMatch(m ->
                m.getField().getColumn() == 3 && m.getField().getRow() == 6 && m.getVictim() != null);
        assertTrue("King should be able to capture black pawn at d2", captureD2);
    }
}
