package com.oose2017.rdemo1.hareandhounds

import com.google.gson.JsonParseException
import java.util.*

// Request/response data classes for easy JSON serialization/deserialization

/**
 * Information about the game, the player, and the piece type.
 *
 * @property gameId uuid of the game as a string.
 * @property playerId id of the player as a string.
 * @property pieceType type of piece as a string.
 */
data class GamePlayerInfo(val gameId: String, val playerId: String, val pieceType: String)

/**
 * Information about a player.
 * @property playerId id of the player as a string.
 */
data class PlayerInfo(val playerId: String)

/**
 * Information about the piece type.
 *
 * @property pieceType type of the piece as a string.
 */
data class PieceTypeInfo(val pieceType: Piece): Validatable {
    override fun validate() {
        if (pieceType == null) {  // Compiler says this is always false, but Gson can actually make it null
            throw JsonParseException("Invalid piece type")
        }
    }
}

/** Information about an attempted move.
 *
 * @property playerId id of player to move.
 * @param fromX x coordinate of player's piece to move.
 * @param fromY y coordinate of player's piece to move.
 * @param toX x coordinate to move player's piece to.
 * @param toY y coordinate to move player's piece to.
 */
data class MoveInfo(val playerId: Int, val fromX: Int, val fromY: Int, val toX: Int, val toY: Int)

/**
 * Information about a piece and its position.
 *
 * @property pieceType type of the piece as a string.
 * @property x x coordinate of the piece
 * @property y y coordinate of the piece
 */
data class PieceInfo(val pieceType: String, val x: Int, val y: Int)

/**
 * Information about the state of a game.
 *
 * @property state game state as a string.
 */
data class StateInfo(val state: GameState)


// Exceptions

/** Base exception for invalid id exceptions. */
open class InvalidIDException: Exception()

/** Exception to be thrown when the game is full. */
class GameFullException: Exception()

/** Exception to be thrown when gameId is invalid. */
class InvalidGameIDException(override val message: String? = "INVALID_GAME_ID"): InvalidIDException()

/** Exception to be thrown when playerId is invalid. */
class InvalidPlayerIDException(override val message: String? = "INVALID_PLAYER_ID"): InvalidIDException()


// Game service

/**
 * Manages game creation, joining, updating, and getting.
 *
 * @property gameDAO data access object to find games from and persist games to.
 */
class GameService(val gameDAO: GameDAO) {

    /**
     * Creates a new game.
     *
     * @param piece the piece that the game creator chose.
     * @return information about the game and player.
     */
    fun createGame(piece: Piece): GamePlayerInfo {

        // Create a UUID for the game
        val uuid = UUID.randomUUID()

        // Initialize the list of pieces to player 1's piece
        val pieces = listOf<Piece>(piece)

        // Initialize starting board positions
        val positions = BoardPositions(
                Position(4, 1),
                setOf<Position>(
                        Position(0, 1),
                        Position(1, 0),
                        Position(1, 2)
                ))

        // Create the game board
        val game = GameBoard(uuid, pieces, GameState.WAITING_FOR_SECOND_PLAYER, positions)

        // Save the game
        gameDAO.insert(game)
        return GamePlayerInfo(uuid.toString(), pieces[0].asPlayerId().toString(), pieces[0].toString())

    }

    /**
     * Joins an existing game.
     *
     * @param id uuid of existing game to join.
     * @return information about the game and player.
     */
    fun joinGame(id: UUID): GamePlayerInfo {

        // Find the existing game
        var game = gameDAO.findById(id) ?: throw InvalidGameIDException()

        // Throw game full exception if game is already full.
        if (game.players.size > 1) {
            throw GameFullException()
        }

        // Create the opponent player
        val players = listOf<Piece>(
                game.players[0],
                game.players[0].opponent()
        )

        // Update the game
        game.players = players
        game.state = GameState.TURN_HOUND

        // Save the game
        gameDAO.update(game)

        return GamePlayerInfo(game.id.toString(), players[1].asPlayerId().toString(), players[1].toString())
    }

    /**
     * Updates an existing game with a move.
     *
     * @param id uuid of existing game to update.
     * @param moveInfo requested move parameters to be validated and persisted.
     * @return information about the player that just moved.
     */
    fun updateGame(id: UUID, moveInfo: MoveInfo): PlayerInfo {

        // Find the existing game
        val game = gameDAO.findById(id) ?: throw InvalidGameIDException()

        // Find the piece type from the playerId
        val piece = PieceFromPlayerId(moveInfo.playerId) ?: throw InvalidPlayerIDException()

        // Throw if trying to move as a player who has not joined
        if (!game.players.contains(piece)) throw InvalidPlayerIDException()

        // Attempt the move, will throw if illegal
        game.updatePosition(piece, moveInfo.fromX, moveInfo.fromY, moveInfo.toX, moveInfo.toY)

        // Save the game
        gameDAO.update(game)

        return PlayerInfo(moveInfo.playerId.toString())
    }

    /**
     * Gets the game board of an existing game.
     *
     * @param id uuid of existing game to describe game board of.
     * @return list of piece information (pieces' type and position).
     */
    fun getGameBoard(id: UUID): List<PieceInfo> {

        // Find the existing game
        val game = gameDAO.findById(id) ?: throw InvalidGameIDException()

        // Create a piece info object for each hound
        val houndInfo = game.positions.hounds.map { PieceInfo("HOUND", it.x, it.y) }

        // Create a piece info object for the hare
        val hare = game.positions.hare
        val hareInfo = PieceInfo("HARE", hare.x, hare.y)

        // Return the list of all piece info
        return houndInfo + hareInfo
    }

    /**
     * Gets the game state of an existing game.
     *
     * @param id uuid of existing game to describe state of.
     * @return state information.
     */
    fun getGameState(id: UUID): StateInfo {

        // Find the existing game
        val game = gameDAO.findById(id) ?: throw InvalidGameIDException()

        // Return the state
        return StateInfo(game.state)

    }

}