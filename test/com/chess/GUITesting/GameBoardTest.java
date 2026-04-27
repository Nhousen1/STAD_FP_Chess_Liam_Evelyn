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

    private void makeMove(GridPane grid, int fromCol, int fromRow, int toCol, int toRow) {
        clickOn(getSquare(grid,fromCol, fromRow));
        sleep(200);
        clickOn(getSquare(grid, toCol, toRow));
        sleep(300);
    }

    @Test
    public void boardStatusShowsCheck() {
        // Load a position right before check
        startGameWithFen("r1bqkb1r/pppp1ppp/2n2n2/4p2Q/2B1P3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 1");
        sleep(500);

        GridPane grid = lookup("#boardGrid").query();
        makeMove(grid, 2, 4, 5, 1); // Creates check situation
        sleep(500);

        Label statusLabel = lookup("#statusTextLabel").query();
        assertNotNull(statusLabel);
        assertEquals("CHECK by white player", statusLabel.getText());
    }

    // ====================================================================
    // Checkmate Board State
    // ====================================================================

    @Test
    public void boardStatusShowsCheckmate() {
        // Load a position right before checkmate
        startGameWithFen("r1bqkb1r/pppp1ppp/2n2n2/4p2Q/2B1P3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 1");
        sleep(500);

        GridPane grid = lookup("#boardGrid").query();
        makeMove(grid, 7, 3, 5, 1); // Creates checkmate situation
        sleep(500);

        Label statusLabel = lookup("#statusTextLabel").query();
        assertNotNull(statusLabel);
        assertEquals("CHECKMATE by white player!", statusLabel.getText());
    }

    // ====================================================================
    // Stalemate Board Status
    // ====================================================================

    @Test
    public void boardStatusShowsStalemate() {
        // Load position right before stalemate
        startGameWithFen("7k/8/4Q3/8/4K3/8/8/8");
        sleep(500);

        GridPane grid = lookup("#boardGrid").query();
        makeMove(grid,  4, 2, 5, 1); // Creates checkmate situation
        sleep(500);

        Label statusLabel = lookup("#statusTextLabel").query();
        assertNotNull(statusLabel);
        assertEquals("STALEMATE!", statusLabel.getText());
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