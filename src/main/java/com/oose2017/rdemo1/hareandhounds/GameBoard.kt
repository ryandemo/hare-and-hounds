package com.oose2017.rdemo1.hareandhounds

import java.util.*
import kotlin.collections.HashMap

data class GameBoard(val id: UUID, val players: List<Piece>, val state: GameState, val positions: List<PiecePosition>)