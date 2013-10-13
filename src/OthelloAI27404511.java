import java.util.ArrayList;

// Name: Lucas Ou-Yang   ID#: 27404511

// This othello ai placed 5th in a competition of 250 people.

// Interesting results and findings about the evaluations and algorithms. 
// -------------------------------------------------------------------------
// The four factors that I mainly wanted to test were two search algorithms and two
// evaluation functions and how well they worked together. 

// I wanted to first test the standard mini-max against alpha-beta pruning. 
// Mini-max is capable of running an entire game (with all moves under 5 
// seconds) implementing a depth of four. AB pruning can achieve this with a depth of 5-6.
// On a controlled depth of 4, AB prunning's average computation time per move is 
// 0.0624 seconds, the average computation time per move for the mini-max was 0.584 seconds. 
// AB-prunning is nearly 10x faster than its counterpart, which is a big deal.

// MiniMax CITED from Norm Jacobson's lab 2 manual, (pseudo-code).
// AB Pruning CITED from the TA's drawings on the board + it's Wikipedia page.

// Now for the more interesting results, there are two evaluation functions in this lab.
// One is cited from the lab manual, the standard black minus white or white minus black 
// pieces where every piece is worth one point. 

// For the second function, I created an 8 x 8 two-dimensional array of integers, and 
// mimicked priority values for every spot on the game board, corners were worth more,
// adjacent to corners were worth less, and the moves adjacent to the moves adjacent
// to corners were worth "ok". Also, from what I read on http://www.samsoft.org.uk/
// reversi/strategy.htm, which is a page for Othello/Reversi strategies,
// its important to stay in the center of the board, so I made the inner box worth a 
// substantial amount. To add to this I also implemented a mobility factor, if a move
// gives our AI more valid moves, it will be valued higher, if the move gives out
// opponent more valid moves, it will be valued lower. 

// Used strategy CITED from : http://www.samsoft.org.uk/reversi/strategy.htm

// To test the effectiveness of these evaluators, I first made an AI using the
// standard evaluation play versus an AI using the custom evaluation, the controlled
// variable was a depth of four for both AI's. The custom evaluator won very very 
// convincingly with a score of 40-50ish to only 10. 

// But the next test was even more unusual, I now had the AI using the custom 
// evaluator implement a depth of 3, versus the the opponent AI using the regular evaluator
// on a depth of 4. I expected the regular evaluator to win out, but despite playing
// on a lower depth level, but the custom evaluator AI won (not convincingly). I
// ran several other similar tests, and the results were similar. The evaluator, for my
// AI at least, means more than the depth.

// Knowing all of this, I have decided to use my custom evaluator + AB pruning for my AI.

public class OthelloAI27404511 implements OthelloAI {
    private boolean amIBlack;
    public static final int ROWNUMBER = 8;
    public static final int COLNUMBER = 8;
    // This is a two-dimensional array that holds "priority" values for every
    // single space on a board, so that our evaluation will be smarter and stronger,
    // the idea is that corners are more valuable, moves adjacent to corners are less
    // valuable, and moves adjacent to the moves adjacent to corners are a little valuable
    // and so on, the moves in the center box are "semi-valuable", the
    // priorities are adjusted accordingly.
    private int[][] priorityBoard = new int[8][8];

    // An inner class encapsulating Othello Moves and their evaluations, so we
    // can effectively store them together in a list, bypassing the P-queue.
    private static class EvaluationMove {
        private int evaluation;
        private OthelloMove move;

        public EvaluationMove(int evaluation, OthelloMove move) {
            this.evaluation = evaluation;
            this.move = move;
        }

        public OthelloMove getMove() {
            return move;
        }

        public int getEvaluation() {
            return evaluation;
        }
    }

    // Returns an OthelloMove object with corresponding (row, col) based off of
    // calculations for the best placement. NOTE: Rows/cols go from 0-7 not 1-8
    @Override
    public OthelloMove chooseMove(OthelloGameState state) {
        long startTime = System.currentTimeMillis();
        // This method fills out mimic'ed game board with new prioritized
        // values.
        this.fillThePriorityBoard();
        // A list of possible moves, each is evaluated and the max is chosen.
        ArrayList<EvaluationMove> listOfMoves = new ArrayList<EvaluationMove>();
        // My depth counter, it changes along with the number of possible moves.
        int theDepth;
        // A counter for the number of valid moves, this is necessary because I
        // am basing my depth counter off of the number of valid moves, the higher
        // the number of valid moves, the lower my depth will be (to stay under the
        // 5 second time limit).
        int validMoves = 0;

        for (int row = 0; row < ROWNUMBER; row++) {
            for (int col = 0; col < COLNUMBER; col++) {
                if (state.isValidMove(row, col)) {
                    validMoves++;
                }
            }
        }

        // Decision statements choosing our depth based off of the number of
        // valid moves, the more valid moves, the lower the depth.
        if (validMoves >= 1 && validMoves < 5) {
            theDepth = 6;
        } else if (validMoves >= 5 && validMoves < 7) {
            theDepth = 5;
        } else {
            theDepth = 4;
        }

        System.out.println("I am now looking " + theDepth
                + " amount of moves into the future!");
        // Sets the color of the player.
        if (!state.isBlackTurn()) {
            amIBlack = false;
        } else {
            amIBlack = true;
        }

        for (int row = 0; row < ROWNUMBER; row++) {
            for (int col = 0; col < COLNUMBER; col++) {
                if (state.isValidMove(row, col)) {
                    OthelloMove currentMove = new OthelloMove(row, col);
                    OthelloGameState clone = state.clone();
                    clone.makeMove(row, col);
                    // If the move causes the opponent to forfeit his turn,
                    // execute
                    // it under any circumstance.
                    if ((amIBlack && clone.isBlackTurn())
                            || (!amIBlack && !clone.isBlackTurn())) {
                        return new OthelloMove(row, col);
                    }

                    // If the move is a corner move, it is prioritized over
                    // regular moves.
                    else if ((row == 0 && col == 0) || (row == 0 && col == 7)
                            || (row == 7 && col == 0) || (row == 7 && col == 7)) {
                        return new OthelloMove(row, col);
                    }
                    listOfMoves.add(new EvaluationMove(alphabeta(clone,
                            theDepth, Integer.MIN_VALUE, Integer.MAX_VALUE),
                            currentMove));
                }
            }
        }

        // This wont cause errors because there will always be at least one
        // element in the listOfMoves ArrayList.
        EvaluationMove max = listOfMoves.get(0);
        for (int i = 1; i < listOfMoves.size(); i++) {
            if (listOfMoves.get(i).getEvaluation() > max.getEvaluation()) {
                max = listOfMoves.get(i);
            }
        }
        long endTime = System.currentTimeMillis();
        double total = (endTime - startTime) * 0.001;
        System.out.println(" \r\n" + "I have spent " + total
                + " seconds making this move!");
        return max.getMove();
    }

    // *************************Alpha-Beta Pruning****************************
    // The alpha-beta pruning algorithm, alpha is our best move and beta is the
    // Opponents best move. CITED FROM the TA's white-board drawings
    // (Stellyanos) and
    // Alpha-Beta Prunning's Wikipedia Page.
    public int alphabeta(OthelloGameState inState, int depth, int alpha,
            int beta) {
        if (depth == 0 || inState.gameIsOver()) {
            // The getEvaluation is just a method to hold the contents of my
            // evaluation technique. It's lengthy, so thats why its stored in a
            // method.
            return getEvaluation(inState);
        }

        else {
            // If it is our (maximum's) turn.
            if ((amIBlack && inState.isBlackTurn())
                    || (!amIBlack && !inState.isBlackTurn())) {
                for (int row = 0; row < ROWNUMBER; row++) {
                    for (int col = 0; col < COLNUMBER; col++) {
                        if (inState.isValidMove(row, col)) {
                            // A clone is needed to keep the game "instance"
                            // similar.
                            OthelloGameState temp = inState.clone();
                            // Make the valid move.
                            temp.makeMove(row, col);
                            // Recursively search the new game state
                            int runningMax = alphabeta(temp, depth - 1, alpha,
                                    beta);
                            if (runningMax > alpha) {
                                alpha = runningMax;
                            }
                            // If this is true, we will "prune" off the unneeded
                            // game tree branch.
                            if (alpha >= beta) {
                                return alpha;
                            }
                        }
                    }
                }
                return alpha;
            } else {
                for (int row = 0; row < ROWNUMBER; row++) {
                    for (int col = 0; col < COLNUMBER; col++) {
                        if (inState.isValidMove(row, col)) {
                            // A clone is needed to keep the game "instance" similar.
                            OthelloGameState temp = inState.clone();
                            // Make the valid move.
                            temp.makeMove(row, col);
                            // Recursively search the new game state
                            int runningMin = alphabeta(temp, depth - 1, alpha,
                                    beta);
                            if (runningMin < beta) {
                                beta = runningMin;
                            }
                            // Cut-off
                            if (alpha >= beta) {
                                return beta;
                            }
                        }
                    }
                }
                return beta;
            }
        }
    }

    // Method designed to provide us with an evaluation of an input game state.
    // This needs to be in a separate method because the evaluation is so
    // lengthy.
    public int getEvaluation(OthelloGameState inState) {
        int returnThisEvaluation = 0;
        int blackScore = 0;
        int whiteScore = 0;

        // I will not use the pre-set getWhite/getBlack score methods, I have my
        // own set from my priorityBoard.
        for (int row = 0; row < ROWNUMBER; row++) {
            for (int col = 0; col < COLNUMBER; col++) {
                if (inState.getCell(row, col).equals(OthelloCell.BLACK)) {
                    blackScore += priorityBoard[row][col];
                } else if (inState.getCell(row, col).equals(OthelloCell.WHITE)) {
                    whiteScore += priorityBoard[row][col];
                }
                // Otherwise, its marked OthelloCell.NONE, and we do nothing!
                // If its our turn, we count up the valid moves and evaluate
                // them as
                // such, they are expensive because its important to have more
                // valid
                // options.
                if (((amIBlack && inState.isBlackTurn()) || (!amIBlack && !inState
                        .isBlackTurn())) && inState.isValidMove(row, col)) {
                    returnThisEvaluation += 500;
                }
                // If its not our turn, but there are a ton of valids, subtract
                // points. We do not want our opponents to have many choices.
                else if (((amIBlack && !inState.isBlackTurn()) || (!amIBlack && inState
                        .isBlackTurn())) && inState.isValidMove(row, col)) {
                    returnThisEvaluation -= 500;
                }
            }
        }

        if (amIBlack) {
            returnThisEvaluation += (blackScore - whiteScore);
        } else {
            returnThisEvaluation += (whiteScore - blackScore);
        }

        return returnThisEvaluation;
    }

    // Just a separate method to fill up our "priorityBoard", for a definition
    // of this read above. This method just makes things more clean.
    // Strategy to maintain the center box is CITED from the strategy site,
    // http://www.samsoft.org.uk/reversi/strategy.htm
    // This method will only be executed once.
    public void fillThePriorityBoard() {
        priorityBoard[0][0] = 5000;
        priorityBoard[0][1] = -1500;
        priorityBoard[0][2] = 500;
        priorityBoard[0][3] = 400;
        priorityBoard[0][4] = 400;
        priorityBoard[0][5] = 500;
        priorityBoard[0][6] = -1500;
        priorityBoard[0][7] = 5000;
        priorityBoard[1][0] = -1500;
        priorityBoard[1][1] = -2500;
        priorityBoard[1][2] = -225;
        priorityBoard[1][3] = -250;
        priorityBoard[1][4] = -250;
        priorityBoard[1][5] = -225;
        priorityBoard[1][6] = -2500;
        priorityBoard[1][7] = -1500;
        priorityBoard[2][0] = 500;
        priorityBoard[2][1] = -225;
        priorityBoard[2][2] = 15;
        priorityBoard[2][3] = 5;
        priorityBoard[2][4] = 5;
        priorityBoard[2][5] = 15;
        priorityBoard[2][6] = -225;
        priorityBoard[2][7] = 500;
        priorityBoard[3][0] = 400;
        priorityBoard[3][1] = -250;
        priorityBoard[3][2] = 5;
        priorityBoard[3][3] = 25;
        priorityBoard[3][4] = 25;
        priorityBoard[3][5] = 5;
        priorityBoard[3][6] = -250;
        priorityBoard[3][7] = 400;
        priorityBoard[4][0] = 400;
        priorityBoard[4][1] = -250;
        priorityBoard[4][2] = 5;
        priorityBoard[4][3] = 25;
        priorityBoard[4][4] = 25;
        priorityBoard[4][5] = 5;
        priorityBoard[4][6] = -250;
        priorityBoard[4][7] = 400;
        priorityBoard[5][0] = 500;
        priorityBoard[5][1] = -225;
        priorityBoard[5][2] = 15;
        priorityBoard[5][3] = 5;
        priorityBoard[5][4] = 5;
        priorityBoard[5][5] = 15;
        priorityBoard[5][6] = -225;
        priorityBoard[5][7] = 500;
        priorityBoard[6][0] = -1500;
        priorityBoard[6][1] = -2500;
        priorityBoard[6][2] = -225;
        priorityBoard[6][3] = -250;
        priorityBoard[6][4] = -250;
        priorityBoard[6][5] = -225;
        priorityBoard[6][6] = -2500;
        priorityBoard[6][7] = -1500;
        priorityBoard[7][0] = 5000;
        priorityBoard[7][1] = -1500;
        priorityBoard[7][2] = 500;
        priorityBoard[7][3] = 400;
        priorityBoard[7][4] = 400;
        priorityBoard[7][5] = 500;
        priorityBoard[7][6] = -1500;
        priorityBoard[7][7] = 5000;
    }

    public int search(OthelloGameState s, int depth) {
        if (depth == 0 || s.gameIsOver()) {
            if (amIBlack) {
                return s.getBlackScore() - s.getWhiteScore();
            } else {
                return s.getWhiteScore() - s.getBlackScore();
            }
        }

        else {
            // If it's my turn to move.
            if ((amIBlack && s.isBlackTurn())
                    || (!amIBlack && !s.isBlackTurn())) {
                int max = Integer.MIN_VALUE;
                for (int row = 0; row < ROWNUMBER; row++) {
                    for (int col = 0; col < COLNUMBER; col++) {
                        if (s.isValidMove(row, col)) {
                            OthelloGameState temp = s.clone();
                           
                            temp.makeMove(row, col);
                            int runningMax = search(temp, depth - 1);
                            if (runningMax > max) {
                                max = runningMax;
                            }
                        }
                    }
                }
                return max;
            }

            // If its my opponents turn to move.
            else {
                int min = Integer.MAX_VALUE;
                for (int row = 0; row < ROWNUMBER; row++) {
                    for (int col = 0; col < COLNUMBER; col++) {
                        if (s.isValidMove(row, col)) {
                            
                            OthelloGameState temp = s.clone();
                            temp.makeMove(row, col);
                            int runningMin = search(temp, depth - 1);
                            if (runningMin < min) {
                                min = runningMin;
                            }
                        }
                    }
                }
                return min;
            }
        }
    }
}