package com.oose2017.rdemo1.hareandhounds

import java.util.*
import kotlin.collections.HashMap

/** Base `Exception` for attempted moves. */
open class MoveException: Exception()

/** Exception to be thrown when a player attempts to move their piece when it is not their turn. */
class IncorrectTurnException(override val message: String? = "INCORRECT_TURN"): MoveException()

/** Exception to be thrown when a player attempts to make an illegal move. */
class IllegalMoveException(override val message: String? = "ILLEGAL_MOVE"): MoveException()

/**
 * The state of an entire game board, and its history.
 *
 * @property id the uuid for the game.
 * @property players the list of `Piece`s in the game (at most two).
 * @property state the play state of the game.
 * @property positions the hare and hounds' positions encapsulated in a `BoardPositions`.
 */
data class GameBoard(val id: UUID, var players: List<Piece>, var state: GameState, var positions: BoardPositions) {

    private var stallOccurred = false
    private var boardPositionOccurrences = HashMap<BoardPositions, Int>()

    init {
        // Record the initial board position's frequency as 1
        boardPositionOccurrences[positions] = 1
    }

    /**
     * Updates a piece's position from one position to another. Throws if the move is invalid.
     *
     * @param playerPiece piece to move.
     * @param fromX x coordinate of piece to move.
     * @param fromY y coordinate of piece to move.
     * @param toX x coordinate to move piece to.
     * @param toY y coordinate to move piece to.
     */
    fun updatePosition(playerPiece: Piece, fromX: Int, fromY: Int, toX: Int, toY: Int) {

        // Throw exception if still waiting for player or a player already won
        if (illegalMoveStates.contains(state)) throw IllegalMoveException()

        // Check for correct turn
        if (!((playerPiece == Piece.HOUND && state == GameState.TURN_HOUND)
                || (playerPiece == Piece.HARE && state == GameState.TURN_HARE))) {
            throw IncorrectTurnException()
        }

        val from = Position(fromX, fromY)
        val to = Position(toX, toY)

        // Check that the from and to positions are on the game board
        from.validateOnBoard()
        to.validateOnBoard()

        // Check that positions are only one "jump" away from each other
        from.validateAdjacentTo(to)

        // Check that a piece can't move to a position already occupied by a piece
        if (positions.occupiedAt(to)) {
            throw IllegalMoveException()
        }

        // Validate and move the piece
        when (playerPiece) {
            Piece.HOUND -> moveHound(from, to)
            Piece.HARE -> moveHare(from, to)
        }

        // Update game state and check for winning conditions
        updateGameState()

    }

    /**
     * Moves a hound from a position to another position.
     *
     * @param from position to move hound from.
     * @param to position to move hound to.
     */
    private fun moveHound(from: Position, to: Position) {

        // Check that hound isn't moving backwards
        if (to.x < from.x) {
            throw IllegalMoveException()
        }

        // Check that from position is a hound and remove `from` from positions
        var hounds = positions.hounds.toMutableSet()
        val houndExistsAtFrom = hounds.removeIf { it == from }
        if (!houndExistsAtFrom) {
            throw IllegalMoveException()
        }

        // Add the new hound position to the list of hound positions
        hounds.add(Position(to.x, to.y))

        // Set the new `BoardPosition`
        positions = BoardPositions(positions.hare, hounds)
        incrementOccurrences(positions)

    }

    /**
     * Moves the hare from a position to another position.
     *
     * @param from position to move hare from.
     * @param to position to move hare to.
     */
    private fun moveHare(from: Position, to: Position) {

        // Check that from position is a hare
        if (positions.hare != from) {
            throw IllegalMoveException()
        }

        // Update the hare's position
        positions = BoardPositions(to, positions.hounds)
        incrementOccurrences(positions)

    }

    /**
     * Increments the number of occurrences that the current board position has been in play.
     *
     * @param position position to increment occurrences for.
     */
    private fun incrementOccurrences(position: BoardPositions) {
        val occurrences = boardPositionOccurrences[position]?.plus(1) ?: 1
        boardPositionOccurrences[position] = occurrences

        stallOccurred = occurrences >= 3
    }

    /** Updates the game state after a successful update of positions. Follows [game state machine logic](http://pl.cs.jhu.edu/oose/assignments/images/Game-State-Diagram.png). */
    private fun updateGameState() {

        when (state) {
            GameState.TURN_HOUND -> {
                if (stallOccurred) {
                    state = GameState.WIN_HARE_BY_STALLING
                } else if (checkHoundWin()) {
                    state = GameState.WIN_HOUND
                } else {
                    state = GameState.TURN_HARE
                }
            }

            GameState.TURN_HARE -> {
                if (checkHareWinEscape()) {
                    state = GameState.WIN_HARE_BY_ESCAPE
                } else {
                    state = GameState.TURN_HOUND
                }
            }

            else -> {
                throw IllegalMoveException()
            }
        }

    }

    /**
     * Checks if the hounds have won by trapping the hare.
     *
     * @return True if hounds win, false if not.
     */
    private fun checkHoundWin(): Boolean {
        val hare = positions.hare

        // Hare must be cornered by the three hounds to lose, therefore hare must be in a position only adjacent to three others
        if (MOVABLE_THREE_DIRECTIONS.contains(hare)) {
            positions.hounds.forEach {
                if (!it.adjacentTo(hare)) {
                    return false
                }
            }
            return true
        }
        return false
    }

    /**
     * Checks if the hare has won by escape.
     *
     * @return True if hare escaped, false if not.
     */
    private fun checkHareWinEscape(): Boolean {
        val hare = positions.hare
        for (hound in positions.hounds) {
            if (hound.leftOf(hare)) {
                return false
            }
        }
        return true
    }

}
