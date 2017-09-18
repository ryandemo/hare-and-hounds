package com.oose2017.rdemo1.hareandhounds

import com.google.gson.JsonParseException
import java.util.*

data class GamePlayerInfo(val gameId: String, val playerId: String, val pieceType: String)
data class PlayerInfo(val playerId: String)
data class PieceTypeInfo(val pieceType: Piece): Validatable {
    override fun validate() {
        if (pieceType == null) {  // Compiler says this is always false, but Gson can actually make it null
            throw JsonParseException("Invalid piece type")
        }
    }
}
data class MoveInfo(val playerId: Int, val fromX: Int, val fromY: Int, val toX: Int, val toY: Int)
data class PieceInfo(val pieceType: String, val x: Int, val y: Int)
data class StateInfo(val state: GameState)

open class InvalidIDException: Exception()
class GameFullException: Exception()
class InvalidGameIDException(override val message: String? = "INVALID_GAME_ID"): InvalidIDException()
class InvalidPlayerIDException(override val message: String? = "INVALID_PLAYER_ID"): InvalidIDException()

class GameService(val gameDAO: GameDAO) {

    fun createGame(piece: Piece): GamePlayerInfo {
        val uuid = UUID.randomUUID()
        val pieces = listOf<Piece>(piece)
        val positions = BoardPositions(
                Position(4, 1),
                setOf<Position>(
                        Position(0, 1),
                        Position(1, 0),
                        Position(1, 2)
                ))
        val game = GameBoard(uuid, pieces, GameState.WAITING_FOR_SECOND_PLAYER, positions)

        gameDAO.insert(game)
        return GamePlayerInfo(uuid.toString(), pieces[0].asPlayerId().toString(), pieces[0].toString())
    }

    fun joinGame(id: UUID): GamePlayerInfo {
        val oldGameBoard = gameDAO.findById(id) ?: throw InvalidGameIDException()
        if (oldGameBoard.players.size > 1) {
            throw GameFullException()
        }

        val players = listOf<Piece>(
                oldGameBoard.players[0],
                oldGameBoard.players[0].opponent()
        )
        val game = GameBoard(oldGameBoard.id, players, GameState.TURN_HOUND, oldGameBoard.positions)

        gameDAO.update(game)
        return GamePlayerInfo(game.id.toString(), players[1].asPlayerId().toString(), players[1].toString())
    }

    fun updateGame(id: UUID, moveInfo: MoveInfo): PlayerInfo {
        val game = gameDAO.findById(id) ?: throw InvalidGameIDException()
        val piece = PieceFromPlayerId(moveInfo.playerId) ?: throw InvalidPlayerIDException()
        if (!game.players.contains(piece)) throw InvalidPlayerIDException()  // throw if trying to move as a player who has not joined

        game.updatePosition(piece, moveInfo.fromX, moveInfo.fromY, moveInfo.toX, moveInfo.toY)
        gameDAO.update(game)  // will throw if illegal move

        return PlayerInfo(moveInfo.playerId.toString())
    }

    fun getGameBoard(id: UUID): List<PieceInfo> {
        val game = gameDAO.findById(id) ?: throw InvalidGameIDException()

        val houndInfo = game.positions.hounds.map { PieceInfo("HOUND", it.x, it.y) }

        val hare = game.positions.hare
        val hareInfo = PieceInfo("HARE", hare.x, hare.y)

        return houndInfo + hareInfo
    }

    fun getGameState(id: UUID): StateInfo {
        val game = gameDAO.findById(id) ?: throw InvalidGameIDException()
        return StateInfo(game.state)
    }

}