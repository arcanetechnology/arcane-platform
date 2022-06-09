package no.arcane.platform.app.trade.admin

import no.arcane.platform.utils.config.readResourceWithoutWhitespace
import no.arcane.platform.utils.graphql.GraphQLBaseService
import no.arcane.platform.utils.graphql.GraphqlService

object TradeAdminGraphqlService : GraphqlService by GraphQLBaseService(
    baseSchema = lazy { TradeAdminGraphqlService.readResourceWithoutWhitespace("/trade.graphqls") }
) {
    init {
        GraphqlDummyDataFetcher.registerAsDataFetcher()
    }
}