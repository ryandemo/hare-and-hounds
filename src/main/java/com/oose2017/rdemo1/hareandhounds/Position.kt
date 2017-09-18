package com.oose2017.rdemo1.hareandhounds

/** Minimum x value of the game board. */
private val MIN_X = 0
/** Maximum x value of the game board. */
private val MAX_X = 4
/** Minimum y value of the game board. */
private val MIN_Y = 0
/** Maximum y value of the game board. */
private val MAX_Y = 2

/** The set of `Position`s from which a piece can only move to three other `Position`s. */
val MOVABLE_THREE_DIRECTIONS = setOf<Position>(
        Position(0, 1),
        Position(2, 0),
        Position(2, 2),
        Position(4, 1)
)

/** The set of `Position`s from which a piece cannot move diagonally. */
val NOT_DIAGONALLY_MOVABLE = setOf<Position>(
        Position(1, 1),
        Position(3, 1),
        Position(2, 0),
        Position(2, 2)
)

/** The set of illegal `Position`s within the bounds of the game board. */
val ILLEGAL_POSITIONS = setOf<Position>(
        Position(MIN_X, MIN_Y),
        Position(MIN_X, MAX_Y),
        Position(MAX_X, MIN_Y),
        Position(MAX_X, MAX_Y)
)

/**
 * Encapsulates the hare's position and a set of hound positions.
 *
 * @property hare position of the hare.
 * @property hounds set of positions of the hounds.
 */
data class BoardPositions(val hare: Position, val hounds: Set<Position>) {
    fun allPieces(): List<Position> = hounds.toList() + hare
    fun occupiedAt(position: Position): Boolean = (hare == position || hounds.contains(position))
}

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