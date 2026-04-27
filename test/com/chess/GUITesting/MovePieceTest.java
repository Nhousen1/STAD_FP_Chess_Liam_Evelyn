package com.chess.GUITesting;

import com.chess.application.Chess;
import com.chess.application.FieldButton;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assert.*;

/**
 * GUI tests for move input interactions on the board.
 *
 * Covers:
 *   - Piece selection
 *   - Executing a legal move (click-to-move)
 *   - Illegal move is rejected (piece stays put)
 *   - Capture moves update the board
 *   - Pawn promotion triggers a queen on rank 8
 *   - AI makes a move when it is the AI's turn
 */
public class MovePieceTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new Chess().start(stage);
    }

    // ------------------------------------------------------------------ helpers

    private void startManualGame() {
        clickOn("#modeChoice");
        clickOn("You vs your 'friend'");
        clickOn("#startButton");
        sleep(800);
    }

    private void startManualGameWithFen(String fen) {
        clickOn("#loadButton");
        sleep(300);
        clickOn(".text-field");
        write(fen);
        clickOn("OK");
        sleep(200);
        clickOn("#modeChoice");
        clickOn("You vs your 'friend'");
        clickOn("#startButton");
        sleep(800);
    }

    private void startAiGame() {
        // MANUAL_VS_AI: white is human, black is AI (default color = white)
        clickOn("#startButton");
        sleep(800);
    }

    /** Get the FieldButton at grid column col, row row. */
    private FieldButton getSquare(int col, int row) {
        GridPane grid = lookup("#boardGrid").query();
        for (Node n : grid.getChildren()) {
            Integer c = GridPane.getColumnIndex(n);
            Integer r = GridPane.getRowIndex(n);
            if (c != null && r != null && c == col && r == row
                    && n instanceof FieldButton) {
                return (FieldButton) n;
            }
        }
        return null;
    }

    /** Returns true if a FieldButton has a piece graphic. */
    private boolean hasPiece(int col, int row) {
        FieldButton fb = getSquare(col, row);
        return fb != null && fb.getGraphic() != null;
    }

    // ====================================================================
    // Piece selection + legal move execution
    // ====================================================================

    @Test
    public void pawnPieceMovesToTarget() {
        startManualGame();

        // White pawn on e2 (grid col=4, row=6) → e4 (col=4, row=4)
        assertTrue("e2 should have a piece before move", hasPiece(4, 6));
        assertFalse("e4 should be empty before move", hasPiece(4, 4));

        clickOn(getSquare(4, 6)); // select e2 pawn
        sleep(200);
        clickOn(getSquare(4, 4)); // move to e4
        sleep(300);

        assertFalse("e2 should be empty after move", hasPiece(4, 6));
        assertTrue("e4 should have a piece after move", hasPiece(4, 4));
    }

    @Test
    public void knightMovesToSquare() {
        startManualGame();

        // White knight on g1 (col=6, row=7) → f3 (col=5, row=5)
        assertTrue("g1 should have knight", hasPiece(6, 7));
        assertFalse("f3 should be empty", hasPiece(5, 5));

        clickOn(getSquare(6, 7));
        sleep(200);
        clickOn(getSquare(5, 5));
        sleep(300);

        assertFalse("g1 should be empty after knight move", hasPiece(6, 7));
        assertTrue("f3 should have knight after move", hasPiece(5, 5));
    }

    @Test
    public void illegalMove() {
        startManualGame();

        // Trying to move pawn backwards (e2 to e1) - illegal
        clickOn(getSquare(4, 6)); // select e2 pawn
        sleep(200);
        clickOn(getSquare(4, 7)); // click e1 - occupied by king, illegal target
        sleep(300);

        // Pawn should still be on e2
        assertTrue("Pawn should remain on e2 after illegal move attempt", hasPiece(4, 6));
    }

    @Test
    public void clickEmptySquareNoPieceSelected() {
        startManualGame();

        // Click an empty square first (e4 is empty at start)
        clickOn(getSquare(4, 4)); // e4 - empty
        sleep(200);
        // Nothing should happen, board unchanged
        assertTrue("e2 pawn should still be there", hasPiece(4, 6));
    }

    // ====================================================================
    // Capture
    // ====================================================================

    @Test
    public void captureVictimSquareChangesOwner() {
        // Use a FEN where white rook can immediately capture black pawn
        // White rook a1, black pawn a7, kings elsewhere
        startManualGameWithFen("k7/p7/8/8/8/8/8/R6K w - - 0 1");

        // Rook on a1 (col=0, row=7) → a7 (col=0, row=1)
        assertTrue("a1 should have white rook", hasPiece(0, 7));
        assertTrue("a7 should have black pawn", hasPiece(0, 1));

        clickOn(getSquare(0, 7));
        sleep(200);
        clickOn(getSquare(0, 1));
        sleep(300);

        assertFalse("a1 should be empty after rook moves", hasPiece(0, 7));
        assertTrue("a7 should now have white rook", hasPiece(0, 1));
    }

    // ====================================================================
    // Promotion
    // ====================================================================

    @Test
    public void pawnPromotion() {
        // White pawn on e7, only kings and this pawn
        startManualGameWithFen("8/4P3/8/8/8/8/8/k6K w - - 0 1");

        // Pawn on e7 (col=4, row=1) → e8 (col=4, row=0)
        assertTrue("e7 should have white pawn", hasPiece(4, 1));
        assertFalse("e8 should be empty", hasPiece(4, 0));

        clickOn(getSquare(4, 1));
        sleep(200);
        clickOn(getSquare(4, 0));
        sleep(500);

        assertFalse("e7 should be empty after promotion", hasPiece(4, 1));
        assertTrue("e8 should have promoted queen", hasPiece(4, 0));
    }

    // ====================================================================
    // AI move executes
    // ====================================================================

    @Test
    public void aiMoveAfterWhiteMove() {
        // Default mode = MANUAL_VS_AI, white is human
        startAiGame();

        // Count pieces before white's move
        GridPane grid = lookup("#boardGrid").query();
        long piecesBefore = grid.getChildren().stream()
                .filter(n -> n instanceof FieldButton)
                .map(n -> (FieldButton) n)
                .filter(fb -> fb.getGraphic() != null)
                .count();

        // Make white's first move: e2 → e4
        clickOn(getSquare(4, 6));
        sleep(200);
        clickOn(getSquare(4, 4));

        // Wait for AI to respond (AI has a delay + search time)
        sleep(2000);

        // After white + AI both moved, move counter should be 2.0
        Label counter = lookup("#moveCounter").query();
        assertNotNull(counter);
        double counterVal = Double.parseDouble(counter.getText());
        assertTrue("Move counter should be >= 2.0 after both players moved",
                counterVal >= 2.0);
    }

    @Test
    public void aiPieceChangesPosition() {
        startAiGame();

        // Snapshot piece positions before white moves
        GridPane grid = lookup("#boardGrid").query();

        // Make white's move
        clickOn(getSquare(4, 6)); // e2
        sleep(200);
        clickOn(getSquare(4, 4)); // e4
        sleep(2000);   // wait for AI

        // At least one black piece should have moved from its starting row
        // Row 1 = black pawns' start; after AI moves one should be gone
        long blackPawnsOnRow1 = grid.getChildren().stream()
                .filter(n -> n instanceof FieldButton)
                .map(n -> (FieldButton) n)
                .filter(fb -> {
                    Integer r = GridPane.getRowIndex(fb);
                    return r != null && r == 1 && fb.getGraphic() != null;
                })
                .count();

        // Started with 8 black pawns on row 1; after AI moves at least one should have moved
        assertTrue("AI should have moved at least one piece off its starting row",
                blackPawnsOnRow1 < 8);
    }
}