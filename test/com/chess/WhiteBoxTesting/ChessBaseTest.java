package com.chess.WhiteBoxTesting;

import com.chess.model.Difficulty;
import com.chess.model.Mode;
import com.chess.model.PieceValues;
import com.chess.model.Setting;
import com.chess.root.Board;
import com.chess.root.FenParser;
import com.chess.root.Field;
import com.chess.root.Game;
import com.chess.root.moves.Move;
import com.chess.root.pieces.*;
import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import org.junit.BeforeClass;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Base class providing shared utilities for chess unit tests.
 *
 * Pattern for every test:
 *   1. Build a {@link Setting} with a FEN string (or defaults).
 *   2. Create a {@link TestDouble} wrapping it.
 *   3. Access {@code game.getBoard()} and exercise the API under test.
 */
public abstract class ChessBaseTest {

    private static volatile boolean jfxStarted = false;

    @BeforeClass
    public static void initJavaFx() throws InterruptedException {
        if (jfxStarted) return;
        synchronized (ChessBaseTest.class) {
            if (jfxStarted) return;
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(latch::countDown);
            } catch (IllegalStateException e) {
                latch.countDown(); // already started
            }
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX platform did not start within 10 seconds");
            }
            jfxStarted = true;
        }
    }

    // ------------------------------------------------------------------ FEN strings

    /** Standard starting position. */
    public static final String FEN_START =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    /** Only kings left – used for stalemate / draw tests. */
    public static final String FEN_KINGS_ONLY =
            "8/8/8/8/8/8/8/K6k w - - 0 1";

    /** White is in checkmate (Scholar's mate). */
    public static final String FEN_CHECKMATE_WHITE =
            "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3";

    /** Stalemate: it's black's turn but all moves put king in check. */
    public static final String FEN_STALEMATE =
            "5bnr/4p1pq/4Qpkr/7p/7P/2N5/PPPPPP2/R1BQKBNR b KQ - 0 10";

    /** En-passant available: white pawn on e5 can capture black pawn on d5. */
    public static final String FEN_EN_PASSANT =
            "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3";

    /** White pawn on e7 – promotion on next move. */
    public static final String FEN_PROMOTION =
            "8/4P3/8/8/8/8/8/k6K w - - 0 1";

    /** Castling available for both sides. */
    public static final String FEN_CASTLING_AVAILABLE =
            "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1";

    /** Isolated white knight on d4. */
    public static final String FEN_KNIGHT_ONLY =
            "8/8/8/8/3N4/8/8/k6K w - - 0 1";

    /** Isolated white bishop on c1. */
    public static final String FEN_BISHOP_ONLY =
            "8/8/8/8/8/8/8/k1B4K w - - 0 1";

    /** Isolated white rook on a1. */
    public static final String FEN_ROOK_ONLY =
            "8/8/8/8/8/8/8/R6K w - - 0 1";

    /** Isolated white queen on d4. */
    public static final String FEN_QUEEN_ONLY =
            "8/8/8/8/3Q4/8/8/k6K w - - 0 1";

    // ------------------------------------------------------------------ Factory helpers

    /**
     * Create a minimal {@link Setting} for a two-manual-player game
     * from the given FEN string.
     */
    public static Setting settingFromFen(String fen) {
        Setting s = new Setting(true, Mode.MANUAL_ONLY,
                PieceValues.MEDIUM, Difficulty.MEDIUM);
        s.setTimeout(false); // avoid draw-by-timeout noise in tests
        s.setGrid(new GridPane());
        if (fen != null && !fen.isEmpty()) {
            String[] parts = fen.split(" ");
            s.setFenBoard(FenParser.parseBoard(parts[0]));
            s.setCompleteFen(parts);
        }
        return s;
    }

    /**
     * Create a default {@link Setting} for the standard starting position.
     */
    public static Setting defaultSetting() {
        Setting s = new Setting(true, Mode.MANUAL_ONLY,
                PieceValues.MEDIUM, Difficulty.MEDIUM);
        s.setGrid(new GridPane());
        return s;
    }

    // ------------------------------------------------------------------ Board accessors

    /** Find a piece by its FEN character on the given board (case-sensitive). */
    public static Piece findPiece(Board board, String fenChar) {
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                Field f = board.getField(col, row);
                if (f.getPiece() != null && fenChar.equals(f.getPiece().getFen())) {
                    return f.getPiece();
                }
            }
        }
        return null;
    }

    /** Return the piece at algebraic notation square (e.g. "e2"). */
    public static Piece pieceAt(Board board, String notation) {
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                Field f = board.getField(col, row);
                if (notation.equals(f.getNotation())) {
                    return f.getPiece();
                }
            }
        }
        return null;
    }

    /** Return the field at algebraic notation square (e.g. "e4"). */
    public static Field fieldAt(Board board, String notation) {
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                Field f = board.getField(col, row);
                if (notation.equals(f.getNotation())) {
                    return f;
                }
            }
        }
        return null;
    }

    /** Return the first move that targets the given field notation. */
    public static Move findMove(List<Move> moves, String targetNotation) {
        for (Move m : moves) {
            if (targetNotation.equals(m.getField().getNotation())) {
                return m;
            }
        }
        return null;
    }

    /** Return the first move whose piece sits on startNotation and targets targetNotation. */
    public static Move findMove(List<Move> moves, String startNotation, String targetNotation) {
        for (Move m : moves) {
            if (startNotation.equals(m.getStartField().getNotation())
                    && targetNotation.equals(m.getField().getNotation())) {
                return m;
            }
        }
        return null;
    }
}