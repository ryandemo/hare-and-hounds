package com.oose2017.rdemo1.hareandhounds

import org.slf4j.LoggerFactory

import spark.Spark.*

object Bootstrap {
    val IP_ADDRESS = "localhost"
    val PORT = 8080

    private val logger = LoggerFactory.getLogger(Bootstrap::class.java!!)

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {

        // Specify the IP address and Port at which the server should be run
        ipAddress(IP_ADDRESS)
        port(PORT)

        //Specify the sub-directory from which to serve static resources (like html and css)
        staticFileLocation("/public")

        // Create a new game data access object
        val gameDAO = GameDAO()

        // Inject gameDAO into GameService
        val gameService = GameService(gameDAO)

        // Start a GameController using gameService
        GameController(gameService)

    }
}
