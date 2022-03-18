package no.arcane.platform.utils.graphql

import graphql.GraphQL
import graphql.schema.DataFetcher
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.SchemaPrinter
import no.arcane.platform.utils.config.readResourceWithoutWhitespace

object GraphqlModulesRegistry {

    private val dataFetchersMap = mutableMapOf<String, DataFetcher<Any>>()

    private val stringSchemaLists = mutableListOf<String>()

    fun registerDataFetcher(type: String, dataFetcher: DataFetcher<Any>) {
        dataFetchersMap[type] = dataFetcher
    }

    fun registerSchema(schema: String) {
        stringSchemaLists.add(schema)
    }

    private fun getGraphqlSchema(): GraphQLSchema {
        val schemaParser = SchemaParser()
        val typeDefinitionRegistry = schemaParser.parse(readResourceWithoutWhitespace("/schema.graphqls"))

        // multiple schema files
        stringSchemaLists.forEach { stringSchema ->
            typeDefinitionRegistry.merge(schemaParser.parse(stringSchema))
        }


        val runtimeWiring: RuntimeWiring = RuntimeWiring.newRuntimeWiring()
            .type("Query") {
                it.dataFetchers(dataFetchersMap)
            }.build()

        return SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)
    }

    fun getGraphQL(): GraphQL = GraphQL
        .newGraphQL(getGraphqlSchema())
        .build()

    fun getSdl(): String {
        val schemaPrinterOptions = SchemaPrinter
            .Options
            .defaultOptions()
            .includeDirectives(false)

        return SchemaPrinter(schemaPrinterOptions).print(getGraphqlSchema())
    }
}