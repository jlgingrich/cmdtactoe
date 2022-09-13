# CMD TAC TOE

## Description

A two-player version of TicTacToe that runs on the command line!

Written in two days by Liam G. in Java.

To run the project from the command line, run the following:

`java CmdTacToe <--args>...`

## Command-Line Arguments
- `--switch`: This allows Player 2 to go first. Can be used with the `--computer` flag to give the computer the opening move.
- `--suppress`: This prevents the game from rendering the numerical labels for a cleaner board.
- `--numpad`: This renders the board labels as if the user is using a standard computer numpad (7, 8, 9, 4, 5, 6, 1, 2, 3).
- `--computer`: Enables the minmax algorithm to control Player 2, playing a mathematically perfect game.
- `--imperfect`: Cripples the computer with a 20% chance of taking a random move. Enables the `--computer` flag if not already enabled.
- `--epsilon=INT`: Cripples the computer with an INT percent chance of taking a random move. Argument should be an integer value in the range (0, 100) and if not will be clamped to that range. Enables the `--computer` flag if not already enabled.
