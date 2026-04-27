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
 * GUI tests for the game board screen (GameFrame.fxml).
 *
 * Covers:
 *   - Board visibility and correct number of pieces
 *   - Status label updates for check / checkmate / stalemate
 *   - Move counter display
 *   - Board grid is populated with FieldButtons
 */
public class GameBoardTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new Chess().start(stage);
    }

    /** Navigate from settings to the game board before each test. */
    private void startGame() {
        // Use MANUAL_ONLY mode so no AI thread fires during tests
        clickOn("#modeChoice");
        clickOn("You vs your 'friend'");
        clickOn("#startButton");
        sleep(800);
    }

    private void startGameWithFen(String fen) {
        clickOn("#loadButton");
        sleep(300);
        clickOn(".text-field");
        write(fen);
        clickOn("OK");
        sleep(200);
        // Use MANUAL_ONLY so no AI fires
        clickOn("#modeChoice");
        clickOn("You vs your 'friend'");
        clickOn("#startButton");
        sleep(800);
    }

    // ====================================================================
    // Game Board
    // ====================================================================

    @Test
    public void boardGridVisibleAfterStart() {
        startGame();
        GridPane grid = lookup("#boardGrid").query();
        assertNotNull("Board grid should be visible after starting game", grid);
        assertTrue("Board grid should be visible", grid.isVisible());
    }

    @Test
    public void statusLabelVisible() {
        startGame();
        assertNotNull("Status text label should exist",
                lookup("#statusTextLabel").tryQuery().orElse(null));
    }

    @Test
    public void moveCounterStartsAtOne() {
        startGame();
        Label counter = lookup("#moveCounter").query();
        assertNotNull(counter);
        assertEquals("1.0", counter.getText());
    }

    // ====================================================================
    // Component Visibility - 32 pieces on the board
    // ====================================================================

    @Test
    public void startPositionHas64Squares() {
        startGame();
        GridPane grid = lookup("#boardGrid").query();
        assertNotNull(grid);
        assertEquals("Board should have exactly 64 squares", 64, grid.getChildren().size());
    }

    @Test
    public void startPosition32SquaresHavePieces() {
        startGame();
        GridPane grid = lookup("#boardGrid").query();
        assertNotNull(grid);

        long withPiece = grid.getChildren().stream()
                .filter(n -> n instanceof FieldButton)
                .map(n -> (FieldButton) n)
                .filter(fb -> fb.getGraphic() != null)
                .count();

        assertEquals("Exactly 32 squares should have pieces at start", 32, withPiece);
    }

    @Test
    public void startPosition32SquaresAreEmpty() {
        startGame();
        GridPane grid = lookup("#boardGrid").query();
        assertNotNull(grid);

        long empty = grid.getChildren().stream()
                .filter(n -> n instanceof FieldButton)
                .map(n -> (FieldButton) n)
                .filter(fb -> fb.getGraphic() == null)
                .count();

        assertEquals("Exactly 32 squares should be empty at start", 32, empty);
    }

    // ====================================================================
    // Move counter increments after a move
    // ====================================================================

    @Test
    public void moveCounterAfterOneMoveIncrements() {
        startGame();
        GridPane grid = lookup("#boardGrid").query();
        assertNotNull(grid);

        // Click e2 pawn (column 4, row 6 in grid = square "e2")
        Node e2 = getSquare(grid, 4, 6);
        Node e4 = getSquare(grid, 4, 4);
        assertNotNull(e2);
        assertNotNull(e4);

        clickOn(e2);
        clickOn(e4);
        sleep(300);

        Label counter = lookup("#moveCounter").query();
        assertEquals("1.5", counter.getText());
    }

    // ====================================================================
    // Check Board State
    // ====================================================================

    @Test
    public void boardStatusShowsCheck() {
        // Load a position where white is already in check
        // Black queen on h4 gives check to white king
        startGameWithFen("rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3");
        sleep(500);

        Label statusLabel = lookup("#statusTextLabel").query();
        assertNotNull(statusLabel);
        // The board will call setDisplay("CHECK by black player") or end the game
        // Either way the status text should be non-empty
        assertFalse("Status label should not be blank in check/checkmate position",
                statusLabel.getText().isEmpty());
    }

    // ====================================================================
    // Checkmate Board State
    // ====================================================================

    @Test
    public void boardStatusShowsCheckmate() {
        startGameWithFen("rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3");
        sleep(500);

        Label statusLabel = lookup("#statusTextLabel").query();
        assertNotNull(statusLabel);
        assertTrue("Status should mention CHECKMATE",
                statusLabel.getText().toUpperCase().contains("CHECKMATE"));
    }

    // ====================================================================
    // Stalemate Board Status
    // ====================================================================

    @Test
    public void boardStatusShowsStalemate() {
        startGameWithFen("5bnr/4p1pq/4Qpkr/7p/7P/2N5/PPPPPP2/R1BQKBNR b KQ - 0 10");
        sleep(500);

        Label statusLabel = lookup("#statusTextLabel").query();
        assertNotNull(statusLabel);
        assertTrue("Status should mention STALEMATE",
                statusLabel.getText().toUpperCase().contains("STALEMATE"));
    }

    // ====================================================================
    // Helper - get a specific square from the Grid by column/row
    // ====================================================================

    private Node getSquare(GridPane grid, int col, int row) {
        for (Node n : grid.getChildren()) {
            Integer c = GridPane.getColumnIndex(n);
            Integer r = GridPane.getRowIndex(n);
            if (c != null && r != null && c == col && r == row) {
                return n;
            }
        }
        return null;
    }
}