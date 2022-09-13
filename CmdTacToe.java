package cmdtactoe;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmdTacToe {

    static int N = 3; // The height and width of the board
    // Recommended to keep this 3 or below while using the computer-controlled P2, as calculation time for minimax solutions expands polynomially

    static HashMap<Boolean, String> playersMark = new HashMap<>();
    static HashMap<Boolean, String> playersNumber = new HashMap<>();

    static { // Eash conversion from curent player to that player's mark
        playersMark.put(true, "X"); // P1's mark
        playersMark.put(false, "O"); // P2's mark
    }

    static { // Easy conversion from curent player to that player's number
        playersNumber.put(true, "1"); // P1's number
        playersNumber.put(false, "2"); // P2's number
    }

    static Scanner keyboard = new Scanner(System.in); // Gets keyboard inputs
    static Random rng = new Random(); // Handles the rng needed in imperfect mode

    static boolean computer = false; // Whether P2 is computer-controlled
    static boolean imperfect = false; // Whether or not the computer-controlled P2 plays perfectly
    static double epsilon = 0.20; // The chance of the computer-controlled P2 taking a randomly determined (non-optimal) move 
    static boolean suppress = false; // Whether to suppress the space labels
    static boolean numpad = false; // Whether to refer to cells with a layout corresponding to a standard numpad

    static boolean currentPlayer = true; // True if the current player is P1, false if P2
    static String[] gameBoard = new String[N * N]; // The NxN board

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < N * N; i++) {
            gameBoard[i] = String.valueOf(i + 1); // Shifted +1 so that '0' never prints
        }

        // One function that takes in String[] args and sets a bunch of variables
        
        // Process command-line arguments
        if (!patternInArray(args, "^(--switch)$").isEmpty()) { // Allows P2 to go first
            currentPlayer = false;
        }
        if (!patternInArray(args, "^(--suppress)$").isEmpty()) { // Removes number labels from screen
            suppress = true;
        }
        if (!patternInArray(args, "^(--numpad)$").isEmpty()) { // Activates numpad mode
            numpad = true;
        }
        if (!patternInArray(args, "^(--computer)$").isEmpty()) { // Sets the game to single-player
            computer = true;
        }
        if (!patternInArray(args, "^(--imperfect)$").isEmpty()) { // Activates imperfect mode with default epsilon
            imperfect = true;
            computer = true; // Implied, though fine if redundant
        }
        String epsilonString = patternInArray(args, "^--epsilon=(-?[0-9]+)$");
        if (!epsilonString.isEmpty()) { // Activates imperfect mode with epsilon override
            imperfect = true; // Implied, though fine if redundant
            computer = true; // Implied, though fine if redundant
            try {
                epsilon = Integer.parseInt(epsilonString); // The larger epsilon is, the more random and less perfect the computer is
                epsilon = Math.max(0, Math.min(100, epsilon)) / (double) 100; // Clamp the received integer to between 0-100 and shift to percentage
            } catch (Exception e) {
                // If conversion fails, silently use the default epsilon value
            }
        }

        printWipe();
        print("CMD TAC TOE\nWritten in Java by Liam G.\n");
        prompt("Press enter to continue");
        waitForEnter();
        printWipe();
        takeTurn();
    }

    // Method Definitions
    public static String patternInArray(String[] arr, String pattern) {
        Pattern p = Pattern.compile(pattern);
        for (String s : arr) {
            Matcher m = p.matcher(s);
            if (m.find()) {
                return m.group(1);
            }
        }
        return "";
    }
    
    public static void takeTurn() throws InterruptedException {
        /**
         * Tail-recursive function that acts as game loop, calling itself until
         * the program terminates
         */

        if (!currentPlayer && computer) { // If its P2's turn and the computer is on
            goComputer();
        } else {
            goPlayer();
        }

        if (doesPlayerWin(gameBoard, currentPlayer)) {
            printBoard();
            print("Player " + playersNumber.get(currentPlayer) + " won!\n");
            System.exit(0);
        }
        if (isBoardFull(gameBoard)) {
            printBoard();
            print("Player " + playersNumber.get(currentPlayer) + " ended the game in a draw!\n");
            System.exit(0);
        }
        currentPlayer = !currentPlayer; // Switch to other player
        takeTurn(); // Repeat
    }

    public static void goPlayer() {
        if (numpad) {
            goPlayerNumpad();
        } else {
            goPlayerStandard();
        }
    }    
    public static void goPlayerStandard() {
        /**
         * Prompts the player for their input and writes to the board
         */
        int index = -1;
        do {
            printBoard();
            prompt("Player " + playersNumber.get(currentPlayer) + ", which space would you like to mark with an '" + playersMark.get(currentPlayer) + "'?");
            try {
                String input = keyboard.nextLine(); // Needs to implement nextLine to prevent infinite loops
                index = (int) (Byte.parseByte(input) - 1); // Adjustment for the +1 shift from indexes
            } catch (Exception e) {
                index = -1;
            }
            printWipe();
        } while ((index < 0 || index > (N * N - 1)) || playersMark.containsValue(gameBoard[index]));
        gameBoard[index] = playersMark.get(currentPlayer);
    }
    
    public static void goPlayerNumpad() {
        /**
         * Prompts the player for their input and writes to the board
         */
        int index = -1;
        do {
            printBoardNumpad();
            prompt("Player " + playersNumber.get(currentPlayer) + ", which space would you like to mark with an '" + playersMark.get(currentPlayer) + "'?");
            try {
                String input = keyboard.nextLine(); // Needs to implement nextLine to prevent infinite loops
                index = (int) (Byte.parseByte(input) - 1); // Adjustment for the +1 shift from indexes
            } catch (Exception e) {
                index = -1;
            }
            printWipe();
        } while ((index < 0 || index > (N * N - 1)) || playersMark.containsValue(gameBoard[index]));
        gameBoard[index] = playersMark.get(currentPlayer);
    }

    public static void goComputer() throws InterruptedException {
        /**
         * Generates the AI's choice and writes to the board, asking the player
         * to confirm. Player is hard-coded as false since P1 is never AI
         */
        int index = -1;
        if (rng.nextDouble() < epsilon) { // Random chance of imperfections on imperfect mode
            List<Integer> possibilities = availableMoves(gameBoard);
            index = possibilities.get(rng.nextInt(possibilities.size()));
        } else {
            int optimalScore = -1000;
            for (int i : availableMoves(gameBoard)) {
                String[] nextBoard = gameBoard.clone();
                nextBoard[i] = playersMark.get(false);
                int score = minimax(nextBoard, 0, true);
                if (score > optimalScore) {
                    optimalScore = score;
                    index = i;
                }
            }
        }
        gameBoard[index] = playersMark.get(false);
        printBoard();
        print("The Computer placed an '" + playersMark.get(false) + "' mark on space " + (index + 1) + "\n");
        prompt("Press enter to continue");
        waitForEnter();
        printWipe();
    }

    public static <T> void print(T obj) {
        /**
         * Simple alias for System.out.print
         */
        System.out.print(obj);
    }

    public static <T> void prompt(T obj) {
        /**
         * Simple alias for System.out.print that wraps it in '<>'
         */
        System.out.print("<");
        System.out.print(obj);
        System.out.println(">");
    }

    public static void printBoard() {
        if (numpad) {
            printBoardNumpad();
        } else {
            printBoardStandard();
        }
    }
    
    public static void printBoardStandard() {
        /**
         * Prints the board as a formatted 2d matrix with labels in the
         * unoccupied spots of the board
         */
        String s = "╔" + repeatString("═══╤", N - 1) + "═══╗\n";
        for (int j = 0; j < N; j++) {
            s += "║";
            for (int i = 0; i < N; i++) {
                String temp = gameBoard[N * j + i];
                if (tryCastToInt(temp) && suppress) {
                    temp = " ";
                }
                s += padTo(temp, 3);
                if (i < N - 1) {
                    s += "│";
                } else {
                    s += "║";
                }
            }
            s += " \n";
            if (j < N - 1) {
                s += "╟" + repeatString("───┼", N - 1) + "───╢\n";
            }
        }
        s += "╚" + repeatString("═══╧", N - 1) + "═══╝\n";
        print(s);
    }

    public static void printBoardNumpad() {
        /**
         * Prints the board as a formatted 2d matrix with labels in the
         * unoccupied spots of the board in a numpad style
         */
        String s = "╔" + repeatString("═══╤", N - 1) + "═══╗\n";
        for (int j = N - 1; j >= 0; j--) {
            s += "║";
            for (int i = 0; i < N; i++) {
                String temp = gameBoard[N * j + i];
                if (tryCastToInt(temp) && suppress) {
                    temp = " ";
                }
                s += padTo(temp, 3);
                if (i < N - 1) {
                    s += "│";
                } else {
                    s += "║";
                }
            }
            s += " \n";
            if (j > 0) {
                s += "╟" + repeatString("───┼", N - 1) + "───╢\n";
            }
        }
        s += "╚" + repeatString("═══╧", N - 1) + "═══╝\n";
        print(s);
    }
    
    public static String repeatString(String s, int rep) {
        /**
         * Returns the given string 's' 'n' times
         */
        String s_out = s;
        for (int i = 0; i < rep - 1; i++) {
            s_out += s;
        }
        return s_out;
    }

    public static void printWipe() {
        /**
         * From https://www.delftstack.com/howto/java/java-clear-console/
         * Tries to wipe the console of all previous text
         */
        try {
            String operatingSystem = System.getProperty("os.name"); //Check the current operating system

            if (operatingSystem.contains("Windows")) {
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "cls");
                Process startProcess = pb.inheritIO().start();
                startProcess.waitFor();
            } else {
                ProcessBuilder pb = new ProcessBuilder("clear");
                Process startProcess = pb.inheritIO().start();

                startProcess.waitFor();
            }
        } catch (Exception e) {
            print(e);
        }
    }

    public static void waitForEnter() {
        /**
         * Waits until the user presses 'Enter'
         */
        String input;
        do {
            input = keyboard.nextLine();
        } while (!input.isEmpty());
    }

    public static boolean isBoardFull(String[] board) {
        /**
         * Tests if the board has no available places
         */
        boolean full = true;
        for (String c : board) {
            if (tryCastToInt(c)) {
                full = false;
            }
        }
        return full;
    }

    public static boolean doesPlayerWin(String[] board, boolean testPlayer) {
        /**
         * Test if the player given has won
         */
        boolean won = false;

        // Test each row for a win
        for (int j = 0; j < N; j++) {
            boolean completeRow = true;
            for (int i = 0; i < N; i++) {
                if (board[N * j + i] != playersMark.get(testPlayer)) {
                    completeRow = false;
                }
            }
            if (completeRow) {
                won = true;
            }
        }

        // Test each column
        for (int i = 0; i < N; i++) {
            boolean completeCol = true;
            for (int j = 0; j < N; j++) {
                if (board[N * j + i] != playersMark.get(testPlayer)) {
                    completeCol = false;
                }
            }
            if (completeCol) {
                won = true;
            }
        }

        // Test main diagonal
        boolean completeDiag = true;
        for (int j = 0; j < N; j++) {
            if (board[(N + 1) * j] != playersMark.get(testPlayer)) {
                completeDiag = false;
            }
        }
        if (completeDiag) {
            won = true;
        }

        // Test opposite diagonal
        boolean completeInvDiag = true;
        for (int j = 1; j < N + 1; j++) {
            if (board[(N - 1) * j] != playersMark.get(testPlayer)) {
                completeInvDiag = false;
            }
        }
        if (completeInvDiag) {
            won = true;
        }
        return won;
    }

    public static boolean tryCastToInt(String test) {
        boolean result = false;
        try {
            Integer.parseInt(test);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static List<Integer> availableMoves(String[] board) {
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < (N * N); i++) {
            if (tryCastToInt(board[i])) {
                moves.add(i);
            }
        }
        return moves;
    }

    public static boolean isGameOver(String[] board) {
        return (isBoardFull(board) || doesPlayerWin(board, true) || doesPlayerWin(board, false));
    }

    public static String padTo(String input, int n) {
        return repeatString(" ", n - input.length()) + input;
    }

    public static int minimax(String[] board, int depth, boolean currentPlayer) {
        // Captures the leaves
        if (isGameOver(board)) {
            if (doesPlayerWin(board, false)) {
                return 10 - depth; // A player loss is good for the computer
            } else if (doesPlayerWin(board, true)) {
                return depth - 10; // A player win is bad for the computer
            } else {
                return -1; // Draws are bad, but better than losses
            }
        }

        // Recursively explores the game tree from the current board
        if (currentPlayer) { // Now minimizing
            int minimum = Integer.MAX_VALUE;
            for (int i : availableMoves(board)) { // Test each next move
                String[] testBoard = board.clone(); // Create deep copy
                testBoard[i] = playersMark.get(currentPlayer);
                int score = minimax(testBoard, depth + 1, false);
                minimum = Math.min(score, minimum);
            }
            return minimum;
        } else { // Now maximizing
            int maximum = Integer.MIN_VALUE;
            for (int i : availableMoves(board)) { // Test each next move
                String[] testBoard = board.clone(); // Create deep copy
                testBoard[i] = playersMark.get(currentPlayer);
                int score = minimax(testBoard, depth + 1, true);
                maximum = Math.max(score, maximum);
            }
            return maximum;
        }
    }
}
