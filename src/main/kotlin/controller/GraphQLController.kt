package main.kotlin.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.JSONPObject
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import graphql.schema.DataFetcher
import graphql.schema.StaticDataFetcher
import main.kotlin.graphql.GraphQLHandler
import main.kotlin.graphql.GraphQLRequest
import main.kotlin.service.DL4JService
import mu.KotlinLogging
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import javax.annotation.PostConstruct

@RestController
class GraphQLController() {

    @Autowired
    val dl4jService: DL4JService = DL4JService()

    private val logger = KotlinLogging.logger {}

    //Initiate schema from somewhere
    val schema ="""
            type Query{
                queryModel: String
            }
            type Mutation{
                importModel(path: String!): String
                createSimpleModel(
                    path: String!,
                    labelIndex: Int,
                    numClasses: Int,
                    batchSize: Int,
                    seed: Int
                    ): String
                trainModel(
                    path: String!,
                    jsonPath: String!,
                    trainingBatchSize: Int!,
                    labelIndex: Int!,
                    numClasses: Int!,
                    fitTimes: Int!,
                    evalMetric: String!,
                    passRate: Float!
                    ): TrainingEvalResult
            }
            type TrainingEvalResult{
                id: String,
                model: String,
                accuracy: Float,
                precision: Float,
                f1: Float,
                recall: Float
            }"""

    lateinit var fetchers: Map<String, List<Pair<String, DataFetcher<out Any>>>>
    lateinit var handler:GraphQLHandler

    @PostConstruct
    fun init() {

        //initialize Fetchers
        fetchers = mapOf(
                "Query" to
                        listOf(
                                "queryModel" to DataFetcher{dl4jService.queryModel(it.getArgument("path"),it.getArgument("input"))}
                        ),
                "Mutation" to
                        listOf(
                                "importModel" to DataFetcher{dl4jService.importModel(it.getArgument("path"))},
                                "createSimpleModel" to DataFetcher{dl4jService.createSimpleModel(
                                        it.getArgument("path"),
                                        it.getArgument("labelIndex"),
                                        it.getArgument("numClasses"),
                                        it.getArgument("batchSize"),
                                        it.getArgument("seed")
                                        )},
                                "trainModel" to DataFetcher{dl4jService.trainModel(
                                        it.getArgument("path"),
                                        it.getArgument("jsonPath"),
                                        it.getArgument("trainingBatchSize"),
                                        it.getArgument("labelIndex"),
                                        it.getArgument("numClasses"),
                                        it.getArgument("fitTimes"),
                                        it.getArgument("evalMetric"),
                                        it.getArgument("passRate")
                                )}
                        )
        )

        handler = GraphQLHandler(schema, fetchers)
    }

    @RequestMapping("/")
    suspend fun pingcheck():String {
        println("ping")
        logger.debug { "Debugging" }
        return "success"
    }
    @CrossOrigin(origins = arrayOf("http://localhost:3000"))
    @PostMapping("/graphql")
    fun executeGraphQL(@RequestBody request:GraphQLRequest):Map<String, Any> {

        val result = handler.execute(request.query, request.params, request.operationName, ctx = null)

        return mapOf("data" to result.getData<Any>())
    }

}
