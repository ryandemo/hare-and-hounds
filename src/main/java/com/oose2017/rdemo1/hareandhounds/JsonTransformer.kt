//-------------------------------------------------------------------------------------------------------------//
// Code based on a tutorial by Shekhar Gulati of SparkJava at
// https://blog.openshift.com/developing-single-page-web-applications-using-java-8-spark-mongodb-and-angularjs/
//-------------------------------------------------------------------------------------------------------------//

package com.oose2017.rdemo1.hareandhounds

import com.google.gson.Gson
import com.google.gson.JsonParseException
import spark.Response
import spark.ResponseTransformer

import java.util.HashMap

class JsonTransformer : ResponseTransformer {

    private val gson = Gson()

    override fun render(model: Any): String {
        return if (model is Response) {
            gson.toJson(HashMap<Any, Any>())
        } else gson.toJson(model)
    }

}

interface Validatable {
    @Throws(JsonParseException::class)
    fun validate()
}
