package com.oose2017.rdemo1.hareandhounds

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

    fun asPlayerId(): String = when(this) {
        HARE -> "0"
        HOUND -> "1"
    }

    fun opponent(): Piece = when(this) {
        HARE -> HOUND
        HOUND -> HARE
    }
}

data class PiecePosition(val piece: Piece, var x: Int, var y: Int)
