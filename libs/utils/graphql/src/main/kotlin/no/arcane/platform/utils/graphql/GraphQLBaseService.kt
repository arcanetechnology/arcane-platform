package no.arcane.platform.utils.graphql

import graphql.GraphQL
import graphql.scalars.ExtendedScalars
import graphql.schema.DataFetcher
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.SchemaPrinter

class GraphQLBaseService(
    private val baseSchema: Lazy<String>
): GraphqlService {

    private val dataFetchersMap = mutableMapOf<String, DataFetcher<Any>>()

    private val stringSchemaLists = mutableListOf<String>()

    override fun registerSchema(schema: String) {
        stringSchemaLists.add(schema)
    }

    override fun registerDataFetcher(type: String, dataFetcher: DataFetcher<Any>) {
        dataFetchersMap[type] = dataFetcher
    }

    private fun getGraphqlSchema(): GraphQLSchema {
        val schemaParser = SchemaParser()
        val typeDefinitionRegistry = schemaParser.parse(baseSchema.value)

        // multiple schema files
        stringSchemaLists.forEach { stringSchema ->
            typeDefinitionRegistry.merge(schemaParser.parse(stringSchema))
        }


        val runtimeWiring: RuntimeWiring = RuntimeWiring
            .newRuntimeWiring()
            .scalar(ExtendedScalars.DateTime)
            .scalar(ExtendedScalars.GraphQLBigInteger)
            .type("Query") {
                it.dataFetchers(dataFetchersMap)
            }.build()

        return SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)
    }

    override fun getGraphQL(): GraphQL = GraphQL
        .newGraphQL(getGraphqlSchema())
        .build()

    override fun getSdl(): String {
        val schemaPrinterOptions = SchemaPrinter
            .Options
            .defaultOptions()
            .includeDirectives(false)

        return SchemaPrinter(schemaPrinterOptions).print(getGraphqlSchema())
    }
}