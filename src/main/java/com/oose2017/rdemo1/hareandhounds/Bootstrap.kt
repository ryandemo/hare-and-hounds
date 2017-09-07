package com.oose2017.rdemo1.hareandhounds

import org.slf4j.LoggerFactory
import org.sqlite.SQLiteDataSource

import javax.sql.DataSource

import spark.Spark.*

import java.nio.file.Files
import java.nio.file.Paths

object Bootstrap {
    val IP_ADDRESS = "localhost"
    val PORT = 8080

    private val logger = LoggerFactory.getLogger(Bootstrap::class.java!!)

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        //Check if the database file exists in the current directory. Abort if not
        val dataSource = configureDataSource()
        if (dataSource == null) {
            System.out.printf("Could not find todo.db in the current directory (%s). Terminating\n",
                    Paths.get(".").toAbsolutePath().normalize())
            System.exit(1)
        }

        //Specify the IP address and Port at which the server should be run
        ipAddress(IP_ADDRESS)
        port(PORT)

        //Specify the sub-directory from which to serve static resources (like html and css)
        staticFileLocation("/public")

        //Create the model instance and then configure and start the web service
        try {
            val model = TodoService(dataSource)
            TodoController(model)
        } catch (ex: TodoService.TodoServiceException) {
            logger.error("Failed to create a GameService instance. Aborting")
        }

    }

    /**
     * Check if the database file exists in the current directory. If it does
     * create a DataSource instance for the file and return it.
     * @return javax.sql.DataSource corresponding to the todo database
     */
    private fun configureDataSource(): DataSource? {
        val dbPath = Paths.get(".", "harehound.db")
        if (!Files.exists(dbPath)) {
            try {
                Files.createFile(dbPath)
            } catch (ex: java.io.IOException) {
                logger.error("Failed to create harehound.db file in current directory. Aborting")
                return null
            }
        }

        val dataSource = SQLiteDataSource()
        dataSource.url = "jdbc:sqlite:todo.db"
        return dataSource
    }
}
