package com.oose2017.rdemo1.hareandhounds

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.Response
import spark.Route

import spark.Spark.*
import java.util.*

data class ErrorReason(val reason: String)

class GameController(val gameService: GameService) {

    private val API_CONTEXT = "/hareandhounds/api/games"
    private val logger = LoggerFactory.getLogger(GameController::class.java)

    private val gson = GsonBuilder().setPrettyPrinting().create()

    init {
        setupEndpoints()
    }

    private fun render(model: Any): String {
        return if (model is Response) {
            gson.toJson(HashMap<Any, Any>())
        } else gson.toJson(model)
    }

    @Throws(JsonSyntaxException::class)
    private fun <T: Validatable> Gson.fromJsonStrict(json: String, classOfT: Class<T>): T {
        val obj = gson.fromJson<T>(json, classOfT)
        obj.validate()
        return obj
    }

    private fun setupEndpoints() {

        post(API_CONTEXT, "application/json", { request, response ->
            try {
                val pieceTypeInfo = gson.fromJsonStrict(request.body(), PieceTypeInfo::class.java)
                response.status(201)
                gameService.createGame(pieceTypeInfo.pieceType)
            } catch (e: JsonParseException) {
                logger.error(e.message)
                response.status(400)
            } catch (e: JsonSyntaxException) {
                logger.error(e.message)
                response.status(400)
            } catch (e: Exception) {
                logger.error(e.message)
                response.status(500)
            }
        }, this::render)

        put(API_CONTEXT + "/:gameId", "application/json", { request, response ->
            try {
                val uuid = UUID.fromString(request.params("gameId")) ?: throw InvalidGameIDException()
                gameService.joinGame(uuid)
            } catch (e: InvalidIDException) {
                logger.error(e.message)
                response.status(404)
            } catch (e: GameFullException) {
                logger.error(e.message)
                response.status(410)
            } catch (e: Exception) {
                logger.error(e.message)
                response.status(500)
            }
        }, this::render)

        post(API_CONTEXT + "/:gameId/turns", "application/json", { request, response ->
            try {
                val uuid = UUID.fromString(request.params("gameId")) ?: throw InvalidGameIDException()
                val moveInfo = gson.fromJson(request.body(), MoveInfo::class.java)
                gameService.updateGame(uuid, moveInfo)
            } catch (e: JsonParseException) {
                logger.error(e.message)
                response.status(400)
            } catch (e: JsonSyntaxException) {
                logger.error(e.message)
                response.status(400)
            } catch (e: InvalidIDException) {
                logger.error(e.message)
                response.status(404)
                ErrorReason(e.message ?: "INVALID_ID")
            } catch (e: MoveException) {
                logger.error(e.message)
                response.status(422)
                ErrorReason(e.message ?: "INVALID_ID")
            } catch (e: Exception) {
                logger.error(e.message)
                response.status(500)
            }
        }, this::render)

        get(API_CONTEXT + "/:gameId/board", "application/json", { request, response ->
            try {
                val uuid = UUID.fromString(request.params("gameId")) ?: throw InvalidGameIDException()
                gameService.getGameBoard(uuid)
            } catch (e: InvalidIDException) {
                logger.error(e.message)
                response.status(404)
            } catch (e: Exception) {
                logger.error(e.message)
                response.status(500)
            }
        }, this::render)

        get(API_CONTEXT + "/:gameId/state", "application/json", { request, response ->
            try {
                val uuid = UUID.fromString(request.params("gameId")) ?: throw InvalidGameIDException()
                gameService.getGameState(uuid)
            } catch (e: InvalidIDException) {
                logger.error(e.message)
                response.status(404)
            } catch (e: Exception) {
                logger.error(e.message)
                response.status(500)
            }
        }, this::render)

    }

}