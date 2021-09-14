CMD TAC TOE

===========
DESCRIPTION
===========

A two-player version of TicTacToe that runs on the command line!

Written in two days by Liam G. in Java

To run the project from the command line, go to the dist folder and
type the following:

java -jar "CmdTacToe.jar" <--args>...

======================
COMMAND-LINE ARGUMENTS
======================

--switch: This allows Player 2 to go first.

--suppress: This prevents the game from rendering the numerical labels.

--numpad: This renders the board labels as if the user is using a standard computer numpad (7, 8, 9, 4, 5, 6, 1, 2, 3).

--computer: Enables the minmax algorithm to control Player 2, playing a perfect game.

--imperfect: Cripples the minmax algorithm with a 20% chance of taking a random move.

--epsilon=CHANCE: Cripples the minmax algorithm with a CHANCE * 100% chance of taking a random move. CHANCE should be a double value and in the range (0, 100].