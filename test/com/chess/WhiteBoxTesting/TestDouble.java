package com.chess.WhiteBoxTesting;

import com.chess.application.GameController;
import com.chess.model.Setting;
import com.chess.root.Game;
import org.mockito.Mockito;

/**
 * Headless test double for {@link Game} and its {@link GameController}.
 *
 * GameController is a final class so it cannot be subclassed directly.
 * We use Mockito inline mocking (mockito-inline dependency) to create a
 * stub that absorbs all UI callbacks without throwing.
 *
 * Usage:
 * <pre>
 *   Setting s = ChessBaseTest.settingFromFen(ChessBaseTest.FEN_START);
 *   TestDouble game = new TestDouble(s);
 *   Board board = game.getBoard();
 * </pre>
 */
public class TestDouble extends Game {

    public TestDouble(Setting settings) {
        super(Mockito.mock(GameController.class), settings);
    }

    /** Build a headless game from a FEN string. */
    public static TestDouble fromFen(String fen) {
        return new TestDouble(ChessBaseTest.settingFromFen(fen));
    }

    /** Build a headless game with the standard starting position. */
    public static TestDouble defaultGame() {
        return new TestDouble(ChessBaseTest.defaultSetting());
    }
}
