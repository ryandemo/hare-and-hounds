package com.oose2017.rdemo1.hareandhounds

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class GameDAO {

    private var games = ConcurrentHashMap<UUID, GameBoard>()

    fun insert(game: GameBoard) {
        games[game.id] = game
    }

    fun update(game: GameBoard) {
        games[game.id] = game
    }

    fun findById(id: UUID): GameBoard? {
        return games[id]
    }

}