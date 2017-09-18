package com.oose2017.rdemo1.hareandhounds

import java.util.*
import kotlin.collections.HashMap

open class MoveException: Exception()
class IncorrectTurnException(override val message: String? = "INCORRECT_TURN"): MoveException()
class IllegalMoveException(override val message: String? = "ILLEGAL_MOVE"): MoveException()

private val MIN_X = 0
private val MAX_X = 4
private val MIN_Y = 0
private val MAX_Y = 2
private val MOVABLE_THREE_DIRECTIONS = setOf<Position>(
        Position(0, 1),
        Position(2, 0),
        Position(2, 2),
        Position(4, 1)
)
private val NOT_DIAGONALLY_MOVABLE = setOf<Position>(
        Position(1, 1),
        Position(3, 1),
        Position(2, 0),
        Position(2, 2)
)
private val ILLEGAL_POSITIONS = setOf<Position>(
        Position(MIN_X, MIN_Y),
        Position(MIN_X, MAX_Y),
        Position(MAX_X, MIN_Y),
        Position(MAX_X, MAX_Y)
)

data class Position(val x: Int, val y: Int) {
    fun validateOnBoard() {
        if (ILLEGAL_POSITIONS.contains(this)) {
            throw IllegalMoveException()
        }
        if (x < MIN_X || x > MAX_X || y < MIN_Y || y > MAX_Y) {
            throw IllegalMoveException()
        }
    }

    fun validateAdjacentTo(other: Position) {
        if (!adjacentTo(other)) {
            throw IllegalMoveException()
        }
    }

    fun adjacentTo(other: Position): Boolean {
        if (NOT_DIAGONALLY_MOVABLE.contains(this)
                && !((Math.abs(other.x - x) == 1 && Math.abs(other.y - y) == 0)
                || (Math.abs(other.x - x) == 0 && Math.abs(other.y - y) == 1))) {
            return false
        } else if (Math.abs(other.x - x) > 1 || Math.abs(other.y - y) > 1) {
            return false
        }
        return true
    }

    fun leftOf(other: Position): Boolean {
        return x < other.x
    }
}

data class GameBoard(val id: UUID, var players: List<Piece>, var state: GameState, var positions: MutableList<BoardPosition>) {

    private var stallOccurred = false
    private var boardPositionOccurrences = HashMap<BoardPosition, Int>()

    init {
        // Record the initial board position
        boardPositionOccurrences[positions[0]] = 1
        println(boardPositionOccurrences)
    }

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
        if (positions.last().occupiedAt(to)) {
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

    private fun moveHound(from: Position, to: Position) {

        // Check that hound isn't moving backwards
        if (to.x < from.x) {
            throw IllegalMoveException()
        }

        // Check that from position is a hound
        var hounds = positions.last().hounds.toMutableSet()
        val houndExistsAtFrom = hounds.removeIf { it == from }
        if (!houndExistsAtFrom) {
            throw IllegalMoveException()
        }

        hounds.add(Position(to.x, to.y))
        positions.add(BoardPosition(positions.last().hare, hounds))

        updateOccurrences(positions.last())

    }

    private fun moveHare(from: Position, to: Position) {

        // Check that from position is a hare
        if (positions.last().hare != from) {
            throw IllegalMoveException()
        }

        // Update the hare's position
        positions.add(BoardPosition(to, positions.last().hounds))
        updateOccurrences(positions.last())

    }

    private fun updateOccurrences(position: BoardPosition) {
        val occurrences = boardPositionOccurrences[position]?.plus(1) ?: 1
        boardPositionOccurrences[position] = occurrences
        println(boardPositionOccurrences)

        stallOccurred = occurrences >= 3
    }

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

    private fun checkHoundWin(): Boolean {
        val hare = positions.last().hare

        if (MOVABLE_THREE_DIRECTIONS.contains(hare)) {
            positions.last().hounds.forEach {
                if (!it.adjacentTo(hare)) {
                    return false
                }
            }
            return true
        }
        return false
    }

    private fun checkHareWinEscape(): Boolean {
        val hare = positions.last().hare
        for (hound in positions.last().hounds) {
            if (hound.leftOf(hare)) {
                return false
            }
        }
        return true
    }

}
