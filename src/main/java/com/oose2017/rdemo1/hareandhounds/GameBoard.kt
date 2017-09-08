package com.oose2017.rdemo1.hareandhounds

import java.util.*
import kotlin.collections.HashMap

class InvalidGameIDException(override val message: String? = "INVALID_GAME_ID"): Exception()
class IncorrectTurnException(override val message: String? = "INCORRECT_TURN"): Exception()
class IllegalMoveException(override val message: String? = "ILLEGAL_MOVE"): Exception()

private val MIN_X = 0
private val MAX_X = 4
private val MIN_Y = 0
private val MAX_Y = 2
private val ILLEGAL_POSITIONS = listOf<Position>(
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
        if (Math.abs(other.x - x) > 1 || Math.abs(other.y - y) > 1) {
            throw IllegalMoveException()
        }
    }
}

data class GameBoard(val id: UUID, var players: List<Piece>, var state: GameState, var positions: MutableList<BoardPosition>) {

    private var stallOccurred = false
    private var boardPositionOccurrences = HashMap<BoardPosition, Int>()

    fun updatePosition(playerPiece: Piece, fromX: Int, fromY: Int, toX: Int, toY: Int) {

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
        val houndExistsAtFrom = hounds.removeIf { it == Position(from.x, from.y) }
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

        stallOccurred = occurrences >= 3
    }

    private fun updateGameState() {

        when (state) {
            GameState.TURN_HOUND -> {
                if (checkHoundWin()) {
                    state = GameState.WIN_HOUND
                } else if (stallOccurred) {
                    state = GameState.WIN_HARE_BY_STALLING
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
        }

    }

    private fun checkHoundWin(): Boolean {

        return false
    }

    private fun checkHareWinEscape(): Boolean {

        return false
    }

}
