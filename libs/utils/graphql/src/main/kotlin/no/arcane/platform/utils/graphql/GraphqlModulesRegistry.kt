package no.arcane.platform.utils.graphql

import no.arcane.platform.utils.config.readResourceWithoutWhitespace

object GraphqlModulesRegistry: GraphqlService by GraphQLBaseService(
    baseSchema = lazy { GraphqlModulesRegistry.readResourceWithoutWhitespace("/schema.graphqls") }
) {
    init {
        GraphqlModulesRegistry.registerSchema(readResourceWithoutWhitespace("/apps.graphqls"))
    }
}