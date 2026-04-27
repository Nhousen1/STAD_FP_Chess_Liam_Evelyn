package com.chess.WhiteBoxTesting.FENPGN;

import com.chess.WhiteBoxTesting.ChessBaseTest;
import com.chess.WhiteBoxTesting.TestDouble;
import com.chess.model.Setting;
import com.chess.root.PgnParser;
import com.chess.root.moves.Move;
import com.chess.root.moves.PromotionMove;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

    /**
     * White-box tests for PgnParser.
     *
     * Strategy (from proposal):
     *   "Test PGN parser in a similar way [to FEN] – load a PGN string, replay
     *    moves, and verify the resulting board state."
     *
     * Tests are split into:
     *   1. Static utility helpers (parseMoves, parseMove, metadata tags).
     *   2. Export helpers (getMoves, getFullPgn)
     */
    public class PGNParserTest extends ChessBaseTest {

        // ====================================================================
        // parseMoves – Branch Coverage
        // ====================================================================

        // Valid move list where every token starts with an alpha char,
        // move numbers start with a digit and are filtered out
        // Should accept all five piece/pawn tokens and reject move-num tokens
        @Test
        public void parseMovesValidMoveList() {
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

        // Empty move list produces zero-len token to execute str.length() > 0
        // condition
        // Should return empty list
        @Test
        public void parseMovesEmptyString() {
            List<String> moves = PgnParser.parseMoves("");
            assertNotNull(moves);
            assertTrue("Empty input should yield no move tokens", moves.isEmpty());
        }

        // Move list with comments to execute strip (replaceAll) logic
        // Should strip comments from the move list and return the correct tokens
        @Test
        public void parseMovesWithComments() {
            String moveText = "1. e4 {best move} e5 2. Nf3";
            List<String> moves = PgnParser.parseMoves(moveText);
            // comment block should be removed; three actual moves remain
            assertEquals(3, moves.size());
        }

        // Move list includes result token
        // Should filter out the token
        @Test
        public void parseMovesSkipsResultToken() {
            String moveText = "1. e4 e5 1:0";
            List<String> moves = PgnParser.parseMoves(moveText);
            // result "1:0" starts with a digit followed by ':', should not be included
            for (String m : moves) {
                assertFalse("Result token should not appear in move list",
                        m.contains(":"));
            }
        }

        // Newline in input
        // Should split tokens correctly by executing replaceAll("\n", "\n ")
        @Test
        public void parseMovesMultiline() {
            String moveText = "1. e4 e5\n2. Nf3 Nc6";
            List<String> moves = PgnParser.parseMoves(moveText);
            assertEquals(4, moves.size());
            assertEquals("Nf3", moves.get(2));
        }

        // ====================================================================
        // parseMove – Branch Coverage
        // ====================================================================

        // Execute length 2 branch with pawn piece
        // e4 is a known legal move from the start position, so it is guaranteed
        // to be in legalMoves
        // Should return correct move matching the token
        @Test
        public void parseMovePawnMove() {
            TestDouble game = TestDouble.fromFen(FEN_START);
            // Get all valid moves for white in the starting position
            List<Move> legalMoves = game.getBoard()
                    .getPieces(false) // white pieces
                    .stream()
                    .reduce(new java.util.ArrayList<>(),
                            (acc, p) -> { acc.addAll(p.getMoves()); return acc; },
                            (a, b) -> { a.addAll(b); return a; });

            Move resolved = PgnParser.parseMove("e4", legalMoves);
            assertNotNull("Pawn push e4 resolvable from start position", resolved);
            assertEquals("e4", resolved.getField().getNotation());
        }

        // Execute length 3 branch with knight piece
        // Should return correct move matching the token
        @Test
        public void parseMoveKnightMove() {
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

        // Empty list to execute moves == null situation
        // Should return null
        @Test
        public void parseMoveNullList() {
            Move result = PgnParser.parseMove("e4", null);
            assertNull("Null move list should return null", result);
        }

        // No move in list matches the token
        // Should return null
        @Test
        public void parseMoveInvalidToken() {
            TestDouble game = TestDouble.fromFen(FEN_START);
            List<Move> legalMoves = game.getBoard()
                    .getPieces(false)
                    .stream()
                    .reduce(new java.util.ArrayList<>(),
                            (acc, p) -> { acc.addAll(p.getMoves()); return acc; },
                            (a, b) -> { a.addAll(b); return a; });
            Move result = PgnParser.parseMove("z9", legalMoves);
            assertNull("Invalid token should return null", result);
        }

        // Promotion move executes s.contains("=") branch
        // Should confirm branch executes to return PromotionMove instance
        @Test
        public void parseMovePromotion() {
            TestDouble game = TestDouble.fromFen(FEN_PROMOTION);
            List<Move> legalMoves = game.getBoard()
                    .getPieces(false)
                    .stream()
                    .reduce(new java.util.ArrayList<>(),
                            (acc, p) -> { acc.addAll(p.getMoves()); return acc; },
                            (a, b) -> { a.addAll(b); return a; });

            Move resolved = PgnParser.parseMove("e8=Q", legalMoves);
            assertNotNull("Promotion move must resolve", resolved);
            assertTrue(resolved instanceof PromotionMove);
        }

        // Includes '+' and '#' which are stripped before branches are executed
        // Should strip '+' and '#' to allow for valid branch matching/execution
        @Test
        public void parseMoveStripSymbols() {
            TestDouble game = TestDouble.fromFen(FEN_START);
            List<Move> legalMoves = game.getBoard()
                    .getPieces(false)
                    .stream()
                    .reduce(new java.util.ArrayList<>(),
                            (acc, p) -> { acc.addAll(p.getMoves()); return acc; },
                            (a, b) -> { a.addAll(b); return a; });

            Move resolvedCheck = PgnParser.parseMove("e4+", legalMoves);
            assertNotNull("'+' must be stripped before matching", resolvedCheck);
            assertEquals("e4", resolvedCheck.getField().getNotation());

            Move resolvedCheckmate = PgnParser.parseMove("Nf3#", legalMoves);
            assertNotNull("'#' must be stripped before matching", resolvedCheckmate);
            assertEquals("f3", resolvedCheckmate.getField().getNotation());
        }

        // ====================================================================
        // Metadata tag helpers - Branch Coverage
        // ====================================================================

        // Meta array contains an [Event] tag
        // Should execute the tag-found branch and return the correct value
        @Test
        public void getEventWithEventTag() {
            String pgn = "[Event \"TestEvent\"]\n[Site \"Testland\"]\n\n1. e4 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            assertEquals("TestEvent", PgnParser.getEvent(s));
        }

        // Should return "Casual waste of time", the default
        @Test
        public void getEventWithoutEventTag() {
            Setting s = ChessBaseTest.defaultSetting();
            String result = PgnParser.getEvent(s);
            // Default is "Casual waste of time"
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        // Meta array contains a [Site] tag
        // Should execute the tag-found branch and return the correct value
        @Test
        public void getSiteWithSiteTag() {
            String pgn = "[Event \"E\"]\n[Site \"TestCity\"]\n\n1. e4 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            assertEquals("TestCity", PgnParser.getSite(s));
        }

        // Should return "Your cave, SWITZERLAND", the default
        @Test
        public void getSiteWithoutSiteTag() {
            Setting s = ChessBaseTest.defaultSetting();
            assertEquals("Your cave, SWITZERLAND", PgnParser.getSite(s));
        }

        // Meta array contains a [Date] tag
        // Should execute the tag-found branch and return the correct value
        @Test
        public void getDateWithDateTag() {
            String pgn = "[Event \"E\"]\n[Date \"2024.01.01\"]\n\n1. e4 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            assertEquals("2024.01.01", PgnParser.getDate(s));
        }

        // No [Date] tag in meta
        // Should return the current date for the system using the correct format
        @Test
        public void getDateWithoutDateTag() {
            Setting s = ChessBaseTest.defaultSetting();
            String date = PgnParser.getDate(s);
            // Should match yyyy.MM.dd pattern
            assertTrue("Date should match yyyy.MM.dd format",
                    date.matches("\\d{4}\\.\\d{2}\\.\\d{2}"));
        }

        // Meta array contains a [Round] tag
        // Should execute the tag-found branch and return the correct value
        @Test
        public void getRoundWithRoundTag() {
            String pgn = "[Round \"3\"]\n\n1. e4 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            assertEquals("3", PgnParser.getRound(s));
        }

        // No [Round] tag in meta
        // Should return null
        @Test
        public void getRoundWithoutRoundTag() {
            Setting s = ChessBaseTest.defaultSetting();
            assertNull(PgnParser.getRound(s));
        }

        // Meta array contains a [Result] tag
        // Should execute the tag-found branch and return the correct value
        @Test
        public void getResultWithResultTag_returnsResult() {
            String pgn = "[Result \"1:0\"]\n\n1. e4 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            assertEquals("1:0", PgnParser.getResult(s));
        }

        // No [Result] tag in meta
        // Should return "*", the default
        @Test
        public void getResultWithoutResultTag() {
            Setting s = ChessBaseTest.defaultSetting();
            assertEquals("*", PgnParser.getResult(s));
        }

        // Meta array contains a [White] tag
        // Should execute the tag-found branch and return the correct value
        @Test
        public void getWhiteWithWhiteTag() {
            String pgn = "[White \"Magnus\"]\n\n1. e4 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            assertEquals("Magnus", PgnParser.getWhite(s));
        }

        // No [White] tag in meta
        // Should return null
        @Test
        public void getWhiteWithoutWhiteTag() {
            Setting s = ChessBaseTest.defaultSetting();
            assertNull(PgnParser.getWhite(s));
        }

        // Meta array contains a [Black] tag
        // Should execute the tag-found branch and return the correct value
        @Test
        public void getBlackWithBlackTag() {
            String pgn = "[Black \"Kasparov\"]\n\n1. e4 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            assertEquals("Kasparov", PgnParser.getBlack(s));
        }

        // No [Black] tag in meta
        // Should return null
        @Test
        public void getBlackWithoutBlackTag() {
            Setting s = ChessBaseTest.defaultSetting();
            assertNull(PgnParser.getBlack(s));
        }

        // ====================================================================
        // Export helpers - Branch Coverage
        // ====================================================================

        // Execute a round trip pgn process (load in PGN, export PGN, verify export is valid).
        // Executes getMoves internally
        @Test
        public void pgnFullRoundTrip() {
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

        // History is null/empty
        // Should produce a blank move section
        @Test
        public void getFullPgnNoMoves() {
            TestDouble game = TestDouble.fromFen(FEN_START);
            String pgn = PgnParser.getFullPgn(game);
            assertNotNull(pgn);
            // Move section should be empty (no history yet)
            String moveSection = pgn.split("\n\n",-1)[1];
            assertTrue(moveSection.isEmpty());
        }

        // 12+ moves to execute i%12 condition
        // Should break lines for every 12 moves
        @Test
        public void getFullPgnLongGame() {
            // Use a PGN with 13+ half-moves so the i%12==0 branch fires
            String pgn = "[Event \"T\"]\n[Result \"*\"]\n\n"
                    + "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 *";
            Setting s = ChessBaseTest.defaultSetting();
            s.addPgn(pgn);
            TestDouble game = new TestDouble(s);
            String exported = PgnParser.getFullPgn(game);
            assertTrue("Long game PGN should contain a newline in move section",
                    exported.split("\n\n")[1].contains("\n"));
        }
    }
