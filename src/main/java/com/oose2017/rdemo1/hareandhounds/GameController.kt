package com.oose2017.rdemo1.hareandhounds

import java.util.*

data class GamePlayerInfo(val gameId: String, val playerId: String, val pieceType: String)
data class PlayerInfo(val playerId: String)
data class StateInfo(val state: GameState)

//class GameException(override var message:String): Exception()
class NotFoundException: Exception()
class GameFullException: Exception()

class GameController(val gameDAO: GameDAO) {

    fun createGame(piece: Piece): GamePlayerInfo {
        val uuid = UUID.randomUUID()
        val pieces = listOf<Piece>(piece)
        val positions = listOf<PiecePosition>(
                PiecePosition(Piece.HOUND, 0, 1),
                PiecePosition(Piece.HOUND, 1, 0),
                PiecePosition(Piece.HOUND, 1, 2),
                PiecePosition(Piece.HARE, 4, 1)
        )
        val game = GameBoard(uuid, pieces, GameState.WAITING_FOR_SECOND_PLAYER, positions)

        gameDAO.insert(game)
        return GamePlayerInfo(uuid.toString(), pieces[0].asPlayerId(), pieces[0].toString())
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
        return GamePlayerInfo(game.id.toString(), players[1].asPlayerId(), players[1].toString())
    }

    fun updateGame(id: UUID, playerId: Int, fromX: Int, fromY: Int, toX: Int, toY: Int): PlayerInfo {
        val oldGameBoard = gameDAO.findById(id) ?: throw NotFoundException()

        // put move piece logic here

        return PlayerInfo(playerId.toString())
    }

    fun getGameBoard(id: UUID): List<PiecePosition> {
        val game = gameDAO.findById(id) ?: throw NotFoundException()
        return game.positions
    }

    fun getGameState(id: UUID): StateInfo {
        val game = gameDAO.findById(id) ?: throw NotFoundException()
        return StateInfo(game.state)
    }

}