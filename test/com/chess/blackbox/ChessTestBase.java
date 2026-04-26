package com.chess.blackbox;

import com.chess.application.GameController;
import com.chess.model.Difficulty;
import com.chess.model.Mode;
import com.chess.model.PieceValues;
import com.chess.model.Setting;
import com.chess.root.Board;
import com.chess.root.FenParser;
import com.chess.root.Game;
import com.chess.root.moves.Move;
import com.chess.root.pieces.KingPiece;
import com.chess.root.pieces.Piece;
import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Shared base class for all black-box tests that need a live Board/Game.
 *
 * JavaFX must be initialized before any Board can be constructed (Board creates
 * FieldButton UI elements). We initialize it once headlessly using Monocle, which
 * is configured via the surefire argLine in pom.xml:
 *   -Dglass.platform=Monocle -Dmonocle.platform=Headless -Dprism.order=sw
 *
 * GameController is mocked so its void callbacks (setDisplay, etc.) become no-ops
 * instead of crashing. Mode.MANUAL_ONLY prevents AI threads from starting.
 */
public abstract class ChessTestBase {

    private static volatile boolean jfxStarted = false;

    /** Standard starting position FEN. */
    protected static final String FEN_START =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    // Exposed so subclasses can call Mockito.verify(mockController, ...)
    protected GameController mockController;

    /** Starts the JavaFX toolkit headlessly once per test run. */
    @BeforeClass
    public static void initJavaFx() throws InterruptedException {
        if (jfxStarted) return;
        synchronized (ChessTestBase.class) {
            if (jfxStarted) return;
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(latch::countDown);
            } catch (IllegalStateException e) {
                // Already started (multiple test classes sharing the same JVM)
                latch.countDown();
            }
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX platform did not start within 10 seconds");
            }
            jfxStarted = true;
        }
    }

    /**
     * Builds a Board from a FEN string. Uses a mock GameController and
     * Mode.MANUAL_ONLY so no AI threads are spawned.
     */
    protected Board createBoard(String fen) {
        mockController = Mockito.mock(GameController.class);
        Setting setting = buildSetting(fen);
        Game game = new Game(mockController, setting);
        return game.getBoard();
    }

    /** Creates a Board from the standard starting position. */
    protected Board createStartingBoard() {
        return createBoard(FEN_START);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Setting buildSetting(String fen) {
        Setting s = new Setting(true, Mode.MANUAL_ONLY, PieceValues.MEDIUM, Difficulty.MEDIUM);
        s.setGrid(new GridPane());
        String[] parts = fen.split(" ");
        s.setFenBoard(FenParser.parseBoard(parts[0]));
        s.setCompleteFen(parts);
        return s;
    }

    // -----------------------------------------------------------------------
    // Test utility methods
    // -----------------------------------------------------------------------

    /** Returns pseudo-legal moves for the piece on the given field (col 0-7, row 0-7). */
    protected List<Move> getMovesForPiece(Board board, int col, int row) {
        Piece p = board.getField(col, row).getPiece();
        if (p == null) throw new IllegalArgumentException("No piece at (" + col + "," + row + ")");
        return p.getMoves();
    }

    /** Returns castling moves for the king at the given field. */
    protected List<Move> getCastlingMoves(Board board, int col, int row) {
        Piece p = board.getField(col, row).getPiece();
        if (!(p instanceof KingPiece)) throw new IllegalArgumentException("No king at (" + col + "," + row + ")");
        List<Move> castles = ((KingPiece) p).getCastlingMoves();
        return castles != null ? castles : Collections.emptyList();
    }

    /** Returns how many moves in the list target the given field. */
    protected long countMovesTo(List<Move> moves, int col, int row) {
        return moves.stream()
                .filter(m -> m.getField().getColumn() == col && m.getField().getRow() == row)
                .count();
    }

    /** Returns true if any move in the list targets the given field. */
    protected boolean hasMoveTo(List<Move> moves, int col, int row) {
        return countMovesTo(moves, col, row) > 0;
    }

    /**
     * Asserts that the mock controller's setDisplay was called with a string
     * containing the given keyword (e.g. "CHECKMATE", "DRAW", "no playable situation").
     */
    protected void verifyDisplayContains(String keyword) {
        Mockito.verify(mockController, Mockito.atLeastOnce())
               .setDisplay(Mockito.argThat(s -> s != null && s.contains(keyword)));
    }

    /** Asserts that setDisplay was NOT called with the given keyword. */
    protected void verifyDisplayNotContains(String keyword) {
        Mockito.verify(mockController, Mockito.never())
               .setDisplay(Mockito.argThat(s -> s != null && s.contains(keyword)));
    }
}
