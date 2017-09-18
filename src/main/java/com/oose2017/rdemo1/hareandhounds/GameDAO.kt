package com.oose2017.rdemo1.hareandhounds

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/** Exception to be thrown when a game with the id alrady exists. */
class GameExistsException(override val message: String?): Exception()

/** Exception to be thrown when a game is not found. */
class GameNotFoundException(override val message: String?): Exception()

/** The game data access object. Abstracts in-memory storage of concurrent game information. */
class GameDAO {

    private var games = ConcurrentHashMap<UUID, GameBoard>()

    /**
     * Inserts a new game into memory.
     *
     * @param game game to insert.
     */
    fun insert(game: GameBoard) {
        if (games[game.id] != null) throw GameExistsException("Game already exists with id: " + game.id.toString())
        games[game.id] = game
    }

    /**
     * Updates a new game in memory.
     *
     * @param game game to update.
     */
    fun update(game: GameBoard) {
        if (games[game.id] == null) throw GameNotFoundException("Could not find game with id: " + game.id.toString())
        games[game.id] = game
    }

    /**
     * Finds a game by its id.
     *
     * @param id uuid of the game.
     * @return `GameBoard` with relevant id if it exists.
     */
    fun findById(id: UUID): GameBoard? {
        return games[id]
    }

}