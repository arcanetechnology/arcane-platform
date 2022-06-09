package no.arcane.platform.utils.graphql

import graphql.GraphQL
import graphql.schema.DataFetcher

interface GraphqlService {
    fun registerSchema(schema: String)
    fun registerDataFetcher(type: String, dataFetcher: DataFetcher<Any>)
    fun getGraphQL(): GraphQL
    fun getSdl(): String
}