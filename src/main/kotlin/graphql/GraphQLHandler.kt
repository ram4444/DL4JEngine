package main.kotlin.graphql

import graphql.ExecutionResult
import graphql.GraphQL
import graphql.execution.SubscriptionExecutionStrategy
import graphql.schema.DataFetcher
import graphql.schema.GraphQLSchema
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import kotlinx.coroutines.future.await

class GraphQLHandler(private val schema:GraphQLSchema){
    constructor(schema:String, resolvers: Map<String, List<Pair<String, DataFetcher<*>>>>) :
            this(schema(schema, resolvers))

    companion object {
        fun schema(schema:String, resolvers: Map<String, List<Pair<String, DataFetcher<*>>>>): GraphQLSchema {
            val typeDefinitionRegistry = SchemaParser().parse(schema)
            val runtimeWiring = newRuntimeWiring()
                    .apply {resolvers.forEach {(type,fields) -> this.type(type)
                    {builder -> fields.forEach { (field,resolver)-> builder.dataFetcher(field,resolver)};builder}}}
                    .build()

            return SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)
        }
    }

    fun execute(query: String, params: Map<String, Any>, op:String?, ctx: Any?): ExecutionResult {
        val graphql = GraphQL.newGraphQL(schema).build()
        val executionResult = graphql.execute{
            builder -> builder.query(query).variables(params).operationName(op).context(ctx)
        }
        return executionResult
    }

    fun execute_react(query: String, params: Map<String, Any>, op:String?, ctx: Any?): ExecutionResult {
        val graphql = GraphQL.newGraphQL(schema).build()
        val executionResult = graphql.executeAsync{
            builder -> builder.query(query).variables(params).operationName(op).context(ctx)
        }
        return executionResult.get()
    }

    fun execute_subscription(query: String, params: Map<String, Any>, op:String?, ctx: Any?): ExecutionResult {
        val graphql = GraphQL.newGraphQL(schema).subscriptionExecutionStrategy(SubscriptionExecutionStrategy()).build()
        val executionResult = graphql.executeAsync{
            builder -> builder.query(query).variables(params).operationName(op).context(ctx)
        }
        return executionResult.get()
    }

}