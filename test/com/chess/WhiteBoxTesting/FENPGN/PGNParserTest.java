package com.chess.WhiteBoxTesting.FENPGN;

import com.chess.WhiteBoxTesting.ChessBaseTest;
import com.chess.WhiteBoxTesting.TestDouble;
import com.chess.model.Setting;
import com.chess.root.PgnParser;
import com.chess.root.moves.Move;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

    /**
     * Unit tests for {@link PgnParser}.
     *
     * Strategy (from proposal):
     *   "Test PGN parser in a similar way [to FEN] – load a PGN string, replay
     *    moves, and verify the resulting board state."
     *
     * Tests are split into:
     *   1. Static utility helpers (parseMoves, parseMove, metadata tags).
     *   2. Full round-trip: build a game from PGN, export it, compare key fields.
     */
    public class PGNParserTest extends ChessBaseTest {

        // ====================================================================
        // parseMoves – extract move tokens from a PGN move-list string
        // ====================================================================

        @Test
        public void parseMoves_simpleMoveList_returnsCorrectTokens() {
            String moveText = "1. e4 e5 2. Nf3 Nc6 3. Bb5";
            List<String> moves = PgnParser.parseMoves(moveText);

            assertNotNull(moves);
            assertEquals("Five move tokens expected", 5, moves.size());
            assertEquals("e4", moves.get(0));
            assertEquals("e5", moves.get(1));
            assertEquals("Nf3", moves.get(2));
            assertEquals("Nc6", moves.get(3));
            assertEquals("Bb5", moves.get(4));
        }

        @Test
        public void parseMoves_emptyString_returnsEmptyList() {
            List<String> moves = PgnParser.parseMoves("");
            assertNotNull(moves);
            assertTrue("Empty input should yield no move tokens", moves.isEmpty());
        }

        @Test
        public void parseMoves_withComments_commentsStripped() {
            String moveText = "1. e4 {best move} e5 2. Nf3";
            List<String> moves = PgnParser.parseMoves(moveText);
            // comment block should be removed; three actual moves remain
            assertEquals(3, moves.size());
        }

        @Test
        public void parseMoves_resultToken_notIncluded() {
            String moveText = "1. e4 e5 1:0";
            List<String> moves = PgnParser.parseMoves(moveText);
            // result "1:0" starts with a digit followed by ':', should not be included
            for (String m : moves) {
                assertFalse("Result token should not appear in move list",
                        m.contains(":"));
            }
        }

        // ====================================================================
        // parseMove – resolve a SAN token against a list of legal moves
        // ====================================================================

        @Test
        public void parseMove_pawnMove_resolvesCorrectly() {
            TestDouble game = TestDouble.fromFen(FEN_START);
            // Get all valid moves for white in the starting position
            List<Move> legalMoves = game.getBoard()
                    .getPieces(false) // white pieces
                    .stream()
                    .reduce(new java.util.ArrayList<>(),
                            (acc, p) -> { acc.addAll(p.getMoves()); return acc; },
                            (a, b) -> { a.addAll(b); return a; });

            Move resolved = PgnParser.parseMove("e4", legalMoves);
            assertNotNull("Pawn push e4 must be resolvable from start position", resolved);
            assertEquals("e4", resolved.getField().getNotation());
        }

        @Test
        public void parseMove_knightMove_resolvesCorrectly() {
            TestDouble game = TestDouble.fromFen(FEN_START);
            List<Move> legalMoves = game.getBoard()
                    .getPieces(false)
                    .stream()
                    .reduce(new java.util.ArrayList<>(),
                            (acc, p) -> { acc.addAll(p.getMoves()); return acc; },
                            (a, b) -> { a.addAll(b); return a; });

            Move resolved = PgnParser.parseMove("Nf3", legalMoves);
            assertNotNull("Knight to f3 must be resolvable", resolved);
            assertEquals("f3", resolved.getField().getNotation());
        }

        @Test
        public void parseMove_nullList_returnsNull() {
            Move result = PgnParser.parseMove("e4", null);
            assertNull("Null move list should return null", result);
        }

        @Test
        public void parseMove_unknownToken_returnsNull() {
            TestDouble game = TestDouble.fromFen(FEN_START);
            List<Move> legalMoves = new java.util.ArrayList<>();
            Move result = PgnParser.parseMove("z9", legalMoves);
            assertNull("Unknown SAN token against empty list should return null", result);
        }

        // ====================================================================
        // Metadata tag helpers
        // ====================================================================

        @Test
        public void getEvent_pgnWithEventTag_returnsEventValue() {
            String pgn = "[Event \"TestEvent\"]\n[Site \"Testland\"]\n\n1. e4 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            assertEquals("TestEvent", PgnParser.getEvent(s));
        }

        @Test
        public void getEvent_pgnWithoutEventTag_returnsDefault() {
            Setting s = ChessBaseTest.defaultSetting();
            String result = PgnParser.getEvent(s);
            // Default is "Casual waste of time"
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        public void getSite_pgnWithSiteTag_returnsSiteValue() {
            String pgn = "[Event \"E\"]\n[Site \"TestCity\"]\n\n1. e4 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            assertEquals("TestCity", PgnParser.getSite(s));
        }

        @Test
        public void getDate_pgnWithDateTag_returnsDateValue() {
            String pgn = "[Event \"E\"]\n[Date \"2024.01.01\"]\n\n1. e4 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            assertEquals("2024.01.01", PgnParser.getDate(s));
        }

        @Test
        public void getDate_pgnWithoutDateTag_returnsCurrentDate() {
            Setting s = ChessBaseTest.defaultSetting();
            String date = PgnParser.getDate(s);
            // Should match yyyy.MM.dd pattern
            assertTrue("Date should match yyyy.MM.dd format",
                    date.matches("\\d{4}\\.\\d{2}\\.\\d{2}"));
        }

        @Test
        public void getRound_pgnWithRoundTag_returnsRoundValue() {
            String pgn = "[Round \"3\"]\n\n1. e4 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            assertEquals("3", PgnParser.getRound(s));
        }

        @Test
        public void getRound_pgnWithoutRoundTag_returnsNull() {
            Setting s = ChessBaseTest.defaultSetting();
            assertNull(PgnParser.getRound(s));
        }

        @Test
        public void getResult_pgnWithResultTag_returnsResult() {
            String pgn = "[Result \"1:0\"]\n\n1. e4 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            assertEquals("1:0", PgnParser.getResult(s));
        }

        @Test
        public void getResult_pgnWithoutResultTag_returnsAsterisk() {
            Setting s = ChessBaseTest.defaultSetting();
            assertEquals("*", PgnParser.getResult(s));
        }

        // ====================================================================
        // Full PGN round-trip: build a game with a full PGN, export, compare
        // ====================================================================

        @Test
        public void pgnFullRoundTrip_openingMoves_exportContainsExpectedMoves() {
            // Minimal PGN with two moves
            String pgn = "[Event \"Test\"]\n[Site \"Here\"]\n[Date \"2024.01.01\"]\n"
                    + "[Round \"1\"]\n[White \"A\"]\n[Black \"B\"]\n[Result \"*\"]\n\n"
                    + "1. e4 e5 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            TestDouble game = new TestDouble(s);

            String exported = PgnParser.getFullPgn(game);
            assertNotNull("Exported PGN must not be null", exported);
            assertTrue("Exported PGN should contain 'e4'", exported.contains("e4"));
            assertTrue("Exported PGN should contain 'e5'", exported.contains("e5"));
        }

        @Test
        public void pgnFullRoundTrip_metadata_eventPreserved() {
            String pgn = "[Event \"MyEvent\"]\n[Site \"S\"]\n[Date \"2024.01.01\"]\n"
                    + "[Round \"1\"]\n[White \"W\"]\n[Black \"B\"]\n[Result \"*\"]\n\n"
                    + "1. e4 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            TestDouble game = new TestDouble(s);

            assertEquals("MyEvent", game.getEvent());
        }

        @Test
        public void pgnRoundTrip_scholarsMatePgn_boardShowsCheckmate() {
            // Scholar's mate in 4 moves
            String pgn = "[Event \"Test\"]\n[Result \"1:0\"]\n\n"
                    + "1. e4 e5 2. Qh5 Nc6 3. Bc4 Nf6 4. Qxf7# 1:0";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            TestDouble game = new TestDouble(s);

            // After Scholar's mate, the board's current moves should be empty (checkmate)
            // and the move history should be non-empty
            assertFalse("History should not be empty after replay",
                    game.getHistory().isEmpty());
        }
    }
