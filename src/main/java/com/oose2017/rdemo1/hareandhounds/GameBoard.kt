package com.oose2017.rdemo1.hareandhounds

import java.util.*
import kotlin.collections.HashMap

/** Base `Exception` for attempted moves. */
open class MoveException: Exception()

/** Exception to be thrown when a player attempts to move their piece when it is not their turn. */
class IncorrectTurnException(override val message: String? = "INCORRECT_TURN"): MoveException()

/** Exception to be thrown when a player attempts to make an illegal move. */
class IllegalMoveException(override val message: String? = "ILLEGAL_MOVE"): MoveException()

/** Minimum x value of the game board. */
private val MIN_X = 0
/** Maximum x value of the game board. */
private val MAX_X = 4
/** Minimum y value of the game board. */
private val MIN_Y = 0
/** Maximum y value of the game board. */
private val MAX_Y = 2

/** The set of `Position`s from which a piece can only move to three other `Position`s. */
private val MOVABLE_THREE_DIRECTIONS = setOf<Position>(
        Position(0, 1),
        Position(2, 0),
        Position(2, 2),
        Position(4, 1)
)

/** The set of `Position`s from which a piece cannot move diagonally. */
private val NOT_DIAGONALLY_MOVABLE = setOf<Position>(
        Position(1, 1),
        Position(3, 1),
        Position(2, 0),
        Position(2, 2)
)

/** The set of illegal `Position`s within the bounds of the game board. */
private val ILLEGAL_POSITIONS = setOf<Position>(
        Position(MIN_X, MIN_Y),
        Position(MIN_X, MAX_Y),
        Position(MAX_X, MIN_Y),
        Position(MAX_X, MAX_Y)
)

/**
 * A 2D position.
 *
 * @property x the horizontal position.
 * @property y the vertical position.
 */
data class Position(val x: Int, val y: Int) {
    /**
     * Validates that the position is within the bounds of the game board.
     */
    fun validateOnBoard() {
        if (ILLEGAL_POSITIONS.contains(this)) {
            throw IllegalMoveException()
        }
        if (x < MIN_X || x > MAX_X || y < MIN_Y || y > MAX_Y) {
            throw IllegalMoveException()
        }
    }

    /** Validates adjacency to another position. Evaluates `adjacentTo(other: Position)` and throws on false. */
    fun validateAdjacentTo(other: Position) {
        if (!adjacentTo(other)) {
            throw IllegalMoveException()
        }
    }

    /**
     * Checks if this position is adjacent to another.
     *
     * @return True if adjacent, false if same point or not adjacent.
     */
    fun adjacentTo(other: Position): Boolean {
        if (this == other) {  // Make sure they're not the same point
            return false
        } else if (NOT_DIAGONALLY_MOVABLE.contains(this)  // If you can't move the piece diagonally, make sure the adjacency is only vertical or horizontal
                && !((Math.abs(other.x - x) == 1 && Math.abs(other.y - y) == 0)
                || (Math.abs(other.x - x) == 0 && Math.abs(other.y - y) == 1))) {
            return false
        } else if (Math.abs(other.x - x) > 1 || Math.abs(other.y - y) > 1) {  // If you can move the piece diagonally, make sure the move is only to a connected position
            return false
        }
        return true
    }

    /**
     * Checks if one position is left of another.
     *
     * @return True if left, false if right or horizontally aligned.
     */
    fun leftOf(other: Position): Boolean {
        return x < other.x
    }
}

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

    /** Updates a piece's position from one position to another. Throws if the move is invalid. */
    fun updatePosition(playerPiece: Piece, fromX: Int, fromY: Int, toX: Int, toY: Int) {

        // Throw exception if still waiting for player or a player already won
        if (illegalMoveStates().contains(state)) throw IllegalMoveException()

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

    /** Moves a hound from a position to another position. */
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
        updateOccurrences(positions)

    }

    /** Moves the hare from a position to another position. */
    private fun moveHare(from: Position, to: Position) {

        // Check that from position is a hare
        if (positions.hare != from) {
            throw IllegalMoveException()
        }

        // Update the hare's position
        positions = BoardPositions(to, positions.hounds)
        updateOccurrences(positions)

    }

    /** Updates the number of occurrences that the current board position has been in play. */
    private fun updateOccurrences(position: BoardPositions) {
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
