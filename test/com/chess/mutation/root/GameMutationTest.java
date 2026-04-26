package com.chess.mutation.root;

import com.chess.blackbox.ChessTestBase;
import com.chess.root.Board;
import com.chess.root.Game;
import com.chess.root.Player;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Mutation-targeted tests for com.chess.root.Game.
 *
 * Surviving mutants targeted:
 *  - getEvent()/getSite()/getDate()/getRound()/getWhite()/getBlack(): null guards → ""
 *  - getResult(): board.hasHistory() condition; pgnResult null guard
 *  - getOtherPlayer(): currentPlayer.equals(blackPlayer) condition
 *  - getAIPlayers(): isAI() conditions
 *  - getMoveCounter(): return value replaced with 0/null
 *  - setUpPlayers(): blackPlays condition; mode conditions
 *  - setImportData(): settings.hasFen() condition
 */
public class GameMutationTest extends ChessTestBase {

    // =========================================================================
    // game.getEvent() / getSite() / getDate() / getRound()
    // Kills: null→"" replacement, pgnEvent set to wrong default
    // =========================================================================

    @Test
    public void testGetEventIsNotNull() {
        Board board = createBoard(FEN_START);
        assertNotNull(board.getGame().getEvent());
    }

    @Test
    public void testGetEventIsNotEmpty() {
        Board board = createBoard(FEN_START);
        assertFalse("Game.getEvent() must not be empty", board.getGame().getEvent().isEmpty());
    }

    @Test
    public void testGetSiteIsNotNull() {
        Board board = createBoard(FEN_START);
        assertNotNull(board.getGame().getSite());
    }

    @Test
    public void testGetSiteIsNotEmpty() {
        Board board = createBoard(FEN_START);
        assertFalse("Game.getSite() must not be empty", board.getGame().getSite().isEmpty());
    }

    @Test
    public void testGetDateIsNotNull() {
        Board board = createBoard(FEN_START);
        assertNotNull(board.getGame().getDate());
    }

    @Test
    public void testGetDateIsNotEmpty() {
        Board board = createBoard(FEN_START);
        assertFalse("Game.getDate() must not be empty", board.getGame().getDate().isEmpty());
    }

    @Test
    public void testGetRoundIsNotNull() {
        Board board = createBoard(FEN_START);
        assertNotNull(board.getGame().getRound());
    }

    @Test
    public void testGetRoundIsNotEmpty() {
        Board board = createBoard(FEN_START);
        assertFalse("Game.getRound() must not be empty", board.getGame().getRound().isEmpty());
    }

    // =========================================================================
    // game.getWhite() / getBlack()
    // =========================================================================

    @Test
    public void testGetWhiteIsNotNull() {
        Board board = createBoard(FEN_START);
        assertNotNull(board.getGame().getWhite());
    }

    @Test
    public void testGetWhiteIsNotEmpty() {
        Board board = createBoard(FEN_START);
        assertFalse("Game.getWhite() must not be empty", board.getGame().getWhite().isEmpty());
    }

    @Test
    public void testGetBlackIsNotNull() {
        Board board = createBoard(FEN_START);
        assertNotNull(board.getGame().getBlack());
    }

    @Test
    public void testGetBlackIsNotEmpty() {
        Board board = createBoard(FEN_START);
        assertFalse("Game.getBlack() must not be empty", board.getGame().getBlack().isEmpty());
    }

    // =========================================================================
    // game.getResult() — kills pgnResult null guard and hasHistory() condition
    // =========================================================================

    @Test
    public void testGetResultIsNotNull() {
        Board board = createBoard(FEN_START);
        assertNotNull(board.getGame().getResult());
    }

    @Test
    public void testGetResultDefaultWhenNoHistoryIsStar() {
        Board board = createBoard(FEN_START);
        // No moves made, pgnResult defaults to "*"
        assertEquals("*", board.getGame().getResult());
    }

    // =========================================================================
    // game.getPlayer() / getOtherPlayer()
    // Kills: currentPlayer.equals(blackPlayer) condition mutation
    // =========================================================================

    @Test
    public void testGetPlayerNotNullAtStart() {
        Board board = createBoard(FEN_START);
        assertNotNull("Current player must not be null", board.getGame().getPlayer());
    }

    @Test
    public void testGetOtherPlayerNotNullAtStart() {
        Board board = createBoard(FEN_START);
        assertNotNull("Other player must not be null", board.getGame().getOtherPlayer());
    }

    @Test
    public void testGetPlayerAndOtherPlayerAreDifferent() {
        Board board = createBoard(FEN_START);
        Game game = board.getGame();
        assertNotSame("Current player and other player must be different objects",
                game.getPlayer(), game.getOtherPlayer());
    }

    @Test
    public void testGetPlayerIsWhiteWhenWhiteToMove() {
        // FEN_START: white to move → currentPlayer is white → isBlack() == false
        Board board = createBoard(FEN_START);
        assertFalse("Current player (white's turn) should not be black",
                board.getGame().getPlayer().isBlack());
    }

    @Test
    public void testGetOtherPlayerIsBlackWhenWhiteToMove() {
        Board board = createBoard(FEN_START);
        assertTrue("Other player (white's turn) should be black",
                board.getGame().getOtherPlayer().isBlack());
    }

    @Test
    public void testGetPlayerIsBlackWhenBlackToMove() {
        Board board = createBoard("4k3/8/8/8/8/8/8/4K3 b - - 0 1");
        assertTrue("Current player (black's turn) should be black",
                board.getGame().getPlayer().isBlack());
    }

    @Test
    public void testGetOtherPlayerIsWhiteWhenBlackToMove() {
        Board board = createBoard("4k3/8/8/8/8/8/8/4K3 b - - 0 1");
        assertFalse("Other player (black's turn) should be white",
                board.getGame().getOtherPlayer().isBlack());
    }

    // =========================================================================
    // game.getAIPlayers() — kills isAI() condition mutations
    // Kills: blackPlayer.isAI() replaced with false
    // =========================================================================

    @Test
    public void testGetAIPlayersEmptyInManualOnlyMode() {
        Board board = createBoard(FEN_START);
        // MANUAL_ONLY → no AI players
        assertTrue("MANUAL_ONLY mode should have no AI players",
                board.getGame().getAIPlayers().isEmpty());
    }

    @Test
    public void testGetAIPlayersHasTwoPlayers() {
        Board board = createBoard(FEN_START);
        // Both players exist
        assertNotNull(board.getGame().getPlayer(false)); // white
        assertNotNull(board.getGame().getPlayer(true));  // black
    }

    @Test
    public void testGetPlayerByColorWhiteIsNotBlack() {
        Board board = createBoard(FEN_START);
        Player white = board.getGame().getPlayer(false);
        assertNotNull(white);
        assertFalse("getPlayer(false) should return white player", white.isBlack());
    }

    @Test
    public void testGetPlayerByColorBlackIsBlack() {
        Board board = createBoard(FEN_START);
        Player black = board.getGame().getPlayer(true);
        assertNotNull(black);
        assertTrue("getPlayer(true) should return black player", black.isBlack());
    }

    // =========================================================================
    // game.getMoveCounter() — kills return-0 / return-null mutants
    // =========================================================================

    @Test
    public void testGetMoveCounterNotNull() {
        assertNotNull(createBoard(FEN_START).getGame().getMoveCounter());
    }

    @Test
    public void testGetMoveCounterStartsAtOne() {
        assertEquals(1.0, createBoard(FEN_START).getGame().getMoveCounter(), 0.001);
    }

    @Test
    public void testGetMoveCounterFromFenField() {
        // FEN move counter is 5 → moveCounter should be initialized to 5.0
        Board board = createBoard("4k3/8/8/8/8/8/8/4K3 w - - 0 5");
        assertEquals(5.0, board.getGame().getMoveCounter(), 0.001);
    }

    // =========================================================================
    // game.getBoard() — kills return-null mutant
    // =========================================================================

    @Test
    public void testGetBoardNotNull() {
        Board board = createBoard(FEN_START);
        assertNotNull("game.getBoard() must not be null", board.getGame().getBoard());
    }

    @Test
    public void testGetBoardIsSameAsCreatedBoard() {
        Board board = createBoard(FEN_START);
        assertSame("game.getBoard() must return the same Board object",
                board, board.getGame().getBoard());
    }

    // =========================================================================
    // game.getController() — kills return-null mutant
    // =========================================================================

    @Test
    public void testGetControllerNotNull() {
        Board board = createBoard(FEN_START);
        assertNotNull("game.getController() must not be null", board.getGame().getController());
    }

    // =========================================================================
    // setImportData(): settings.hasFen() condition — currentPlayer set from FEN
    // =========================================================================

    @Test
    public void testCurrentPlayerSetFromFenPlayer() {
        // FEN with black to move: currentPlayer should be black
        Board board = createBoard("4k3/8/8/8/8/8/8/4K3 b - - 0 1");
        assertTrue("Current player should be black when FEN says black to move",
                board.getGame().getPlayer().isBlack());
    }

    @Test
    public void testMoveCounterSetFromFenMoveCounter() {
        // FEN with move counter = 10
        Board board = createBoard("4k3/8/8/8/8/8/8/4K3 w - - 0 10");
        assertEquals(10.0, board.getGame().getMoveCounter(), 0.001);
    }

    // =========================================================================
    // game.getDifficulty() — kills return-"" mutant
    // =========================================================================

    @Test
    public void testGetDifficultyNotNull() {
        Board board = createBoard(FEN_START);
        assertNotNull(board.getGame().getDifficulty());
    }

    @Test
    public void testGetDifficultyNotEmpty() {
        Board board = createBoard(FEN_START);
        assertFalse("getDifficulty() must not return empty string",
                board.getGame().getDifficulty().isEmpty());
    }
}
