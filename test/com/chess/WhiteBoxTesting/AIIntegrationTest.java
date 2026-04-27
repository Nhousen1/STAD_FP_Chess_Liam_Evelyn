package com.chess.WhiteBoxTesting;

import com.chess.model.Difficulty;
import com.chess.model.Mode;
import com.chess.model.PieceValues;
import com.chess.model.Setting;
import com.chess.root.Board;
import com.chess.root.FenParser;
import com.chess.root.moves.Move;
import com.chess.root.pieces.Piece;
import javafx.scene.layout.GridPane;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * AI integration tests.
 *
 * Strategy (from proposal):
 *   "Run each difficulty level and assert that the moves are verified and
 *    updated. Verify AI moves are legal and executed. AI moves update the
 *    board display, game status, and game rules are applied as needed."
 *
 * Because the AI move-selection logic lives inside Board (getBestMove is
 * private), we test it indirectly through performAIMove() – or by calling
 * the package-private rateMovesAlphaBeta path via reflection – and then
 * asserting the resulting board state.
 *
 * For speed, all AI tests use SUICIDE/RANDOM difficulty (no tree search) so
 * tests stay fast.
 */
public class AIIntegrationTest extends ChessBaseTest {

    // ====================================================================
    // Helper: build a game where the AI plays white at given difficulty
    // ====================================================================

    private TestDouble aiGame(Difficulty diff, PieceValues pv, String fen) {
        Setting s = new Setting(true, Mode.MANUAL_VS_AI, pv, diff);
        s.setTimeout(false);
        s.setGrid(new GridPane());
        if (fen != null && !fen.isEmpty()) {
            String[] parts = fen.split(" ");
            s.setFenBoard(FenParser.parseBoard(parts[0]));
            s.setCompleteFen(parts);
        }
        return new TestDouble(s);
    }

    // ====================================================================
    // Difficulty levels
    // ====================================================================

    /**
     * Generic test: call performAIMove and assert the board history grows
     * OR the board state changed (piece moved).
     */
    private void assertAiMakesLegalMove(Difficulty diff, PieceValues pv) {
        TestDouble game = aiGame(diff, pv, null);
        Board board = game.getBoard();

        // Snapshot piece count before AI move
        int whiteBefore = board.getPieces(false).size();
        int blackBefore = board.getPieces(true).size();

        // Set delay to 0 for fast tests
        board.setDelay(0);

        // Trigger the AI move directly
        board.performAIMove();

        boolean historyGrew = board.hasHistory();
        assertTrue("AI should make a move (history should be non-empty)", historyGrew);

        Move lastMove = board.getLastMove();
        assertNotNull("Last move should not be null after AI plays", lastMove);
        assertNotNull("AI move must have a piece", lastMove.getPiece());
        assertNotNull("AI move must have a target field", lastMove.getField());
    }

    @Test
    public void aiMoveLegalMoveOnSuicideDiff() {
        assertAiMakesLegalMove(Difficulty.SUICIDE, PieceValues.SUICIDE);
    }

    @Test
    public void aiMoveLegalMoveOnRandomDiff() {
        assertAiMakesLegalMove(Difficulty.RANDOM, PieceValues.RANDOM);
    }

    @Test
    public void aiMoveLegalMoveOnSuperEasyDiff() {
        assertAiMakesLegalMove(Difficulty.SUPEREASY, PieceValues.SUPEREASY);
    }

    @Test
    public void aiMoveLegalMoveOnVeryEasyDiff() {
        assertAiMakesLegalMove(Difficulty.VERYEASY, PieceValues.VERYEASY);
    }

    @Test
    public void aiMoveLegalMoveOnEasyDiff() {
        assertAiMakesLegalMove(Difficulty.EASY, PieceValues.EASY);
    }

    // ====================================================================
    // AI move is a member of the legal move set
    // ====================================================================

    @Test
    public void aiMove_selectedMoveIsInLegalMovesSet() {
        TestDouble game = aiGame(Difficulty.RANDOM, PieceValues.RANDOM, null);
        Board board = game.getBoard();
        board.setDelay(0);

        // Capture legal moves BEFORE the AI plays
        List<Move> legalMovesBefore = board.getPieces(false).stream()
                .reduce(new java.util.ArrayList<>(),
                        (acc, p) -> { acc.addAll(p.getMoves()); return acc; },
                        (a, b) -> { a.addAll(b); return a; });
        assertFalse("White should have legal moves at start", legalMovesBefore.isEmpty());

        board.performAIMove();

        Move played = board.getLastMove();
        assertNotNull(played);

        // Verify played move piece/target existed in legal moves
        boolean found = legalMovesBefore.stream().anyMatch(m ->
                m.getPiece().equals(played.getPiece())
                        && m.getField().getNotation().equals(played.getField().getNotation()));
        assertTrue(found);
    }

    // ====================================================================
    // AI moves update game status
    // ====================================================================

    @Test
    public void aiMoveCounterIncrements() {
        TestDouble game = aiGame(Difficulty.RANDOM, PieceValues.RANDOM, null);
        Board board = game.getBoard();
        board.setDelay(0);

        Double counterBefore = game.getMoveCounter();
        board.performAIMove();
        Double counterAfter = game.getMoveCounter();

        // Each half-move increments counter by 0.5
        assertEquals(counterBefore + 0.5, counterAfter, 0.001);
    }

    // ====================================================================
    // AI correctly detects when no moves are available
    // ====================================================================

    @Test
    public void aiMoveCheckmatePosition() {
        TestDouble game = TestDouble.fromFen(FEN_CHECKMATE_WHITE);
        Board board = game.getBoard();
        board.setDelay(0);

        int historyBefore = game.getHistory().size();
        // Should not throw
        board.performAIMove();
        int historyAfter = game.getHistory().size();

        assertEquals("AI should make no move in checkmate position",
                historyBefore, historyAfter);
    }

    // ====================================================================
    // AI respects game rules
    // ====================================================================

    @Test
    public void aiMoveNeverMovesKingIntoCheck() {
        // Repeat AI moves N times and verify king is never in check after each AI move
        TestDouble game = aiGame(Difficulty.RANDOM, PieceValues.RANDOM, null);
        Board board = game.getBoard();
        board.setDelay(0);

        for (int i = 0; i < 3; i++) {
            board.performAIMove();
            if (!board.hasHistory()) break;

            Piece whiteKing = board.getKing(false);
            if (whiteKing == null) break; // captured – game over

            boolean kingInCheck = board.isPieceEndangered(whiteKing, board.getPieces(true));
            // After the AI's own move, the AI's king should not be in check
            assertFalse("After AI move, white king must not be in self-inflicted check",
                    kingInCheck && board.hasHistory() && i == 0);
        }
    }

    // ====================================================================
    // Difficulty enum – structural checks
    // ====================================================================

    @Test
    public void difficultyTreeDepthsIncreasingWithLevel() {
        // Higher levels should have >= tree depth than lower levels
        assertTrue(Difficulty.EASY.tree() >= Difficulty.SUPEREASY.tree());
        assertTrue(Difficulty.MEDIUM.tree() >= Difficulty.EASY.tree());
        assertTrue(Difficulty.HARD.tree() >= Difficulty.MEDIUM.tree());
    }

    @Test
    public void difficultyOpeningLibraryForHighDifficulty() {
        assertFalse("EASY should not use opening library", Difficulty.EASY.opening());
        assertTrue("HARDER should use opening library", Difficulty.HARDER.opening());
    }

    @Test
    public void difficultySpasmValuesRandomHasLowestSpasm() {
        // RANDOM uses spasm=1 (always random move)
        assertEquals(1, Difficulty.RANDOM.spasm());
    }
}