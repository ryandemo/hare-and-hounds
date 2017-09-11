package com.oose2017.rdemo1.hareandhounds

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.sql2o.Connection
import org.sql2o.Sql2o
import org.sqlite.SQLiteDataSource
import spark.Spark
import spark.utils.IOUtils

import java.io.IOException
import java.io.OutputStreamWriter
import java.lang.reflect.Type
import java.net.URL
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.*

import org.junit.*
import org.junit.Assert.*

class TestGameServer {

    //------------------------------------------------------------------------//
    // Setup
    //------------------------------------------------------------------------//

    @Before
    fun setupTest() {
        // Start main server
        Bootstrap.main(null)
        Spark.awaitInitialization()
    }

    @After
    fun tearDownTest() {
        Spark.stop()
    }

    //------------------------------------------------------------------------//
    // Tests
    //------------------------------------------------------------------------//

    @Test
    fun testCreateGame() {

    }

    @Test
    fun testJoinGame() {

    }

    @Test
    fun testUpdateGame() {

    }

    @Test
    fun testGetGameState() {

    }

    @Test
    fun testIllegalMoves() {

    }

    @Test
    fun testGetGameBoard() {

    }

    @Test
    fun testWinStalling() {

    }

    @Test
    fun testWinHareEscape() {

    }

    @Test
    fun testHoundWinTrap() {

    }


    //------------------------------------------------------------------------//
    // Generic Helper Methods and classes
    //------------------------------------------------------------------------//

    private data class Response(val httpCode: Int, val content: String)

    private fun request(method: String, path: String, content: Object): Response? {
        try {
            val url = URL("http", Bootstrap.IP_ADDRESS, Bootstrap.PORT, path)
            println(url)
            val http = url.openConnection() as HttpURLConnection
            http.requestMethod = method
            http.doInput = true
            content.let {
                val contentAsJson = Gson().toJson(content)
                http.setDoOutput(true)
                http.setRequestProperty("Content-Type", "application/json")
                val output = OutputStreamWriter(http.outputStream)
                output.write(contentAsJson)
                output.flush()
                output.close()
            }

            val responseBody = IOUtils.toString(http.inputStream)
            return Response(http.responseCode, responseBody)
        } catch (e: IOException) {
            e.printStackTrace()
            fail("Sending request failed: " + e.message)
            return null
        }
    }

}
