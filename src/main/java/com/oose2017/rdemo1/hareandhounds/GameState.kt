package com.oose2017.rdemo1.hareandhounds

/** Enum describing all possible play states of the game. */
enum class GameState {
    WAITING_FOR_SECOND_PLAYER, TURN_HARE, TURN_HOUND, WIN_HARE_BY_ESCAPE, WIN_HARE_BY_STALLING, WIN_HOUND
}

/** Set of game states where a move is not allowed. */
val illegalMoveStates = setOf(
        GameState.WAITING_FOR_SECOND_PLAYER,
        GameState.WIN_HARE_BY_ESCAPE,
        GameState.WIN_HARE_BY_STALLING,
        GameState.WIN_HOUND
)

/** Enum of possible piece types in the game. */
enum class Piece {
    HARE, HOUND;

    fun asPlayerId(): Int = when(this) {
        HARE -> 0
        HOUND -> 1
    }

    fun opponent(): Piece = when(this) {
        HARE -> HOUND
        HOUND -> HARE
    }
}

/**
 * Generates a `Piece` from a playerId.
 *
 * @param id playerId for the piece: 0 is hare, 1 is hound.
 * @return a `Piece` if the playerId mapped to one.
 */
fun PieceFromPlayerId(id: Int?): Piece? = when(id) {
    0 -> Piece.HARE
    1 -> Piece.HOUND
    else -> null
}
