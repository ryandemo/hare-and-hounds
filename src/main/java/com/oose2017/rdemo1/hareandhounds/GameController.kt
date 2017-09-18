package com.oose2017.rdemo1.hareandhounds

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.Route

import spark.Spark.*
import java.util.*

/**
 * Describes the reason for an error.
 *
 * @property reason the error message.
 */
data class ErrorReason(val reason: String)

/**
 * Controls the game sessions through a game service.
 *
 * @property gameService the game service to manipulate games through.
 */
class GameController(val gameService: GameService) {

    private val API_CONTEXT = "/hareandhounds/api/games"
    private val logger = LoggerFactory.getLogger(GameController::class.java)

    private val gson = GsonBuilder().setPrettyPrinting().create()

    init {
        setupEndpoints()
    }

    /**
     * Renders responses as JSON.
     *
     * @param model POJO model to render into JSON.
     * @return JSON string serialization of POJO or empty JSON dictionary.
     */
    private fun render(model: Any): String {
        return if (model is Response) {
            gson.toJson(HashMap<Any, Any>())
        } else gson.toJson(model)
    }

    /**
     * Strictly deserializes JSON into POJOs conforming to the Validatable interface.
     * Necessary to validate enum creation from a string; validation function will throw if deserialization is invalid.
     */
    @Throws(JsonSyntaxException::class)
    private fun <T: Validatable> Gson.fromJsonStrict(json: String, classOfT: Class<T>): T {
        val obj = gson.fromJson<T>(json, classOfT)
        obj.validate()
        return obj
    }

    /**
     * Gets the game UUID from a request's parameters.
     *
     * @param request request from which to get the game id through its link parameters.
     * @return UUID of the game
     */
    private fun getGameIdFromRequest(request: Request): UUID {
        try {
            return UUID.fromString(request.params("gameId"))
        } catch (e: IllegalArgumentException) {
            throw InvalidGameIDException()
        }
    }

    /** Sets up endpoints. */
    private fun setupEndpoints() {

        // Post request to create a game
        post(API_CONTEXT, "application/json", { request, response ->
            try {
                if (request.body().isNullOrEmpty()) throw JsonParseException("JSON body is null")
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

        // Put request to join a game
        put(API_CONTEXT + "/:gameId", "application/json", { request, response ->
            try {
                val uuid = getGameIdFromRequest(request)
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

        // Post request to update a game
        post(API_CONTEXT + "/:gameId/turns", "application/json", { request, response ->
            try {
                val uuid = getGameIdFromRequest(request)
//                if (request.body().isNullOrEmpty()) throw JsonParseException("JSON body is null")
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
                ErrorReason(e.message ?: "ILLEGAL_MOVE")
            } catch (e: Exception) {
                logger.error(e.message)
                response.status(500)
            }
        }, this::render)

        // Get request to describe a game's board
        get(API_CONTEXT + "/:gameId/board", "application/json", { request, response ->
            try {
                val uuid = getGameIdFromRequest(request)
                gameService.getGameBoard(uuid)
            } catch (e: InvalidIDException) {
                logger.error(e.message)
                response.status(404)
            } catch (e: Exception) {
                logger.error(e.message)
                response.status(500)
            }
        }, this::render)

        // Get request to describe a game's state
        get(API_CONTEXT + "/:gameId/state", "application/json", { request, response ->
            try {
                val uuid = getGameIdFromRequest(request)
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