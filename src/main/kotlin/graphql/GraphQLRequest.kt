package main.kotlin.graphql

data class GraphQLRequest(
        val query:String,
        val params:Map<String,Any>,
        val operationName: String?
)