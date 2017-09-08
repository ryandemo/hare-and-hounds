package com.oose2017.rdemo1.hareandhounds

import java.util.*

data class GamePlayerInfo(val gameId: String, val playerId: String, val pieceType: String)
data class PlayerInfo(val playerId: String)
data class PieceInfo(val pieceType: String, val x: Int, val y: Int)
data class StateInfo(val state: GameState)

//class GameException(override var message:String): Exception()
class NotFoundException: Exception()
class GameFullException: Exception()
class InvalidPlayerIDException(override val message: String? = "INVALID_PLAYER_ID"): Exception()

class GameController(val gameDAO: GameDAO) {

    fun createGame(piece: Piece): GamePlayerInfo {
        val uuid = UUID.randomUUID()
        val pieces = listOf<Piece>(piece)
        val positions = BoardPosition(
                Position(4, 1),
                setOf<Position>(
                        Position(0, 1),
                        Position(1, 0),
                        Position(1, 2)
                ))
        val game = GameBoard(uuid, pieces, GameState.WAITING_FOR_SECOND_PLAYER, mutableListOf(positions))

        gameDAO.insert(game)
        return GamePlayerInfo(uuid.toString(), pieces[0].asPlayerId().toString(), pieces[0].toString())
    }

    fun joinGame(id: UUID): GamePlayerInfo {
        val oldGameBoard = gameDAO.findById(id) ?: throw NotFoundException()
        if (oldGameBoard.players.size > 1) {
            throw GameFullException()
        }

        val piece = oldGameBoard.players[0].opponent()
        val players = listOf<Piece>(
                oldGameBoard.players[0],
                oldGameBoard.players[0].opponent()
        )
        val game = GameBoard(oldGameBoard.id, players, GameState.TURN_HOUND, oldGameBoard.positions)

        gameDAO.update(game)
        return GamePlayerInfo(game.id.toString(), players[1].asPlayerId().toString(), players[1].toString())
    }

    fun updateGame(id: UUID, playerId: Int, fromX: Int, fromY: Int, toX: Int, toY: Int): PlayerInfo {
        val game = gameDAO.findById(id) ?: throw NotFoundException()
        val piece = PieceFromPlayerId(playerId) ?: throw InvalidPlayerIDException()

        game.updatePosition(piece, fromX, fromY, toX, toY)
        gameDAO.update(game)  // will throw if illegal move

        return PlayerInfo(playerId.toString())
    }

    fun getGameBoard(id: UUID): List<PieceInfo> {
        val game = gameDAO.findById(id) ?: throw NotFoundException()

        val houndInfo = game.positions.last().hounds.map { PieceInfo("HOUND", it.x, it.y) }

        val hare = game.positions.last().hare
        val hareInfo = PieceInfo("HARE", hare.x, hare.y)

        return houndInfo + hareInfo
    }

    fun getGameState(id: UUID): StateInfo {
        val game = gameDAO.findById(id) ?: throw NotFoundException()
        return StateInfo(game.state)
    }

}