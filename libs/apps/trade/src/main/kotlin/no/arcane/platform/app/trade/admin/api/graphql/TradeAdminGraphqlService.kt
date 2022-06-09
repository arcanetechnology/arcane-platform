package no.arcane.platform.app.trade.admin.api.graphql

import no.arcane.platform.utils.config.readResourceWithoutWhitespace
import no.arcane.platform.utils.graphql.GraphQLBaseService
import no.arcane.platform.utils.graphql.GraphqlService

object TradeAdminGraphqlService : GraphqlService by GraphQLBaseService(
    baseSchema = lazy { TradeAdminGraphqlService.readResourceWithoutWhitespace("/trade.graphqls") }
) {
    init {
        GraphqlDataFetchers.registerAsDataFetcher()
    }
}