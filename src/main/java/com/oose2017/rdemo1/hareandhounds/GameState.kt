package com.oose2017.rdemo1.hareandhounds

fun illegalMoveStates(): Set<GameState> {
    return setOf(
            GameState.WAITING_FOR_SECOND_PLAYER,
            GameState.WIN_HARE_BY_ESCAPE,
            GameState.WIN_HARE_BY_STALLING,
            GameState.WIN_HOUND
    )
}
enum class GameState {
    WAITING_FOR_SECOND_PLAYER, TURN_HARE, TURN_HOUND, WIN_HARE_BY_ESCAPE, WIN_HARE_BY_STALLING, WIN_HOUND
}

fun PieceFromPlayerId(id: Int): Piece? = when(id) {
    0 -> Piece.HARE
    1 -> Piece.HOUND
    else -> null
}

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

data class BoardPositions(val hare: Position, val hounds: Set<Position>) {
    fun allPieces(): List<Position> = hounds.toList() + hare
    fun occupiedAt(position: Position): Boolean = (hare == position || hounds.contains(position))
}