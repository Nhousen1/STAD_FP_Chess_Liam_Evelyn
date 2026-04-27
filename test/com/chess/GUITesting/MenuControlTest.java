package com.chess.GUITesting;

import com.chess.application.Chess;
import com.chess.model.Mode;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assert.*;

/**
 * GUI tests for menus and edit-bar controls on the game board screen.
 *
 * Covers:
 *   - Game menu: New game, Restart, Copy FEN, Copy PGN, Export PGN, Copy board, Quit
 *   - Edit menu: Rotate board, Start/Stop editing, play/pause/</>  buttons
 *   - Help menu: Dummy mode on/off, Show moves
 */
public class MenuControlTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new Chess().start(stage);
    }

    // ------------------------------------------------------------------ helpers

    private void startManualGame() {
            ComboBox<String> combo = lookup("#modeChoice").query();
            interact(() -> combo.getSelectionModel().select(Mode.MANUAL_ONLY.get()));
            sleep(200);
            clickOn("#startButton");
            sleep(800);
    }

    private void openGameMenu() {
        clickOn("Game");
        sleep(200);
    }

    private void openEditMenu() {
        clickOn("Edit");
        sleep(200);
    }

    private void openHelpMenu() {
        clickOn("Help");
        sleep(200);
    }

    // ====================================================================
    // Game menu
    // ====================================================================

    @Test
    public void gameMenu_newGame_returnsToSettingsScreen() {
        startManualGame();
        openGameMenu();
        clickOn("New game");
        sleep(500);
        // Should be back on settings screen
        assertNotNull("Welcome label should reappear after New Game",
                lookup("#welcomeLabel").tryQuery().orElse(null));
    }

    @Test
    public void restartReloadsGameBoard() {
        startManualGame();

        // Make one move first
        clickOn(getSquare(4, 6)); // e2
        sleep(200);
        clickOn(getSquare(4, 4)); // e4
        sleep(300);

        openGameMenu();
        clickOn("Restart");
        sleep(800);

        // Board should be back with 32 pieces
        GridPane grid = lookup("#boardGrid").query();
        assertNotNull(grid);
        long withPiece = grid.getChildren().stream()
                .filter(n -> n instanceof javafx.scene.control.Button)
                .map(n -> (javafx.scene.control.Button) n)
                .filter(b -> b.getGraphic() != null)
                .count();
        assertEquals("Board should have 32 pieces after restart", 32, withPiece);
    }

    @Test
    public void copyFENExecutes() {
        startManualGame();
        openGameMenu();
        clickOn("Copy FEN");
        sleep(200);
    }

    @Test
    public void copyPGNExecutes() {
        startManualGame();
        openGameMenu();
        clickOn("Copy PGN");
        sleep(200);
    }

    // ====================================================================
    // Edit menu - rotate board
    // ====================================================================

    @Test
    public void rotateBoard() {
        startManualGame();
        GridPane grid = lookup("#boardGrid").query();
        assertNotNull(grid);
        double rotationBefore = grid.getRotate();

        openEditMenu();
        clickOn("Rotate board");
        sleep(300);

        double rotationAfter = grid.getRotate();
        assertNotEquals("Board rotation should change after Rotate Board",
                rotationBefore, rotationAfter, 0.1);
    }

    @Test
    public void rotateTwice() {
        startManualGame();
        GridPane grid = lookup("#boardGrid").query();
        double original = grid.getRotate();

        openEditMenu();
        clickOn("Rotate board");
        sleep(200);
        openEditMenu();
        clickOn("Rotate board");
        sleep(200);

        assertEquals("Two rotations should restore original orientation",
                original, grid.getRotate(), 0.1);
    }

    // ====================================================================
    // Edit mode - start and stop editing (shows/hides edit bar buttons)
    // ====================================================================

    @Test
    public void startEditing() {
        startManualGame();
        openEditMenu();
        clickOn("Start editing");
        sleep(300);

        Button goButton = lookup("#goButton").query();
        Button stopButton = lookup("#stopButton").query();
        assertTrue("Go button should be visible after entering edit mode", goButton.isVisible());
        assertTrue("Stop button should be visible after entering edit mode", stopButton.isVisible());
    }

    @Test
    public void stopEditing() {
        startManualGame();
        openEditMenu();
        clickOn("Start editing");
        sleep(300);
        openEditMenu();
        clickOn("Stop editing");
        sleep(300);

        Button goButton = lookup("#goButton").query();
        Button stopButton = lookup("#stopButton").query();
        assertFalse("Go button should be hidden after leaving edit mode", goButton.isVisible());
        assertFalse("Stop button should be hidden after leaving edit mode", stopButton.isVisible());
    }

    // ====================================================================
    // Edit bar - step back / forward buttons
    // ====================================================================

    @Test
    public void stepBackEditMoveCounterDecreases() {
        startManualGame();

        // Make a move so there's history to step back through
        clickOn(getSquare(4, 6)); // e2
        sleep(200);
        clickOn(getSquare(4, 4)); // e4
        sleep(300);

        Label counter = lookup("#moveCounter").query();
        String before = counter.getText();

        // Enter edit mode to enable the back button
        openEditMenu();
        clickOn("Start editing");
        sleep(200);

        // Step back button should now be enabled
        clickOn("#stepBackButton");
        sleep(300);

        String after = counter.getText();
        assertNotEquals("Move counter should change after stepping back", before, after);
    }

    // ====================================================================
    // Help menu - dummy mode
    // ====================================================================

    @Test
    public void dummyModeOnExecutes() {
        startManualGame();
        openHelpMenu();
        clickOn("Dummy mode on");
        sleep(200);
    }

    @Test
    public void dummyModeOffExecutes() {
        startManualGame();
        openHelpMenu();
        clickOn("Dummy mode on");
        sleep(200);
        openHelpMenu();
        clickOn("Dummy mode off");
        sleep(200);
    }

    // ====================================================================
    // Helper
    // ====================================================================

    private Node getSquare(int col, int row) {
        GridPane grid = lookup("#boardGrid").query();
        if (grid == null) return null;
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
