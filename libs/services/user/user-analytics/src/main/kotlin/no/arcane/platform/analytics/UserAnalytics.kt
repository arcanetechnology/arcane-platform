package no.arcane.platform.analytics


class UserAnalytics {

    private suspend fun usersTimeline(map: (User) -> String) = FirebaseUsersFetcher
        .fetchUsers()
        .map(map)
        .groupBy { it }
        .mapValues { (_, values) -> values.count() }
        .toSortedMap()

    suspend fun usersCreatedTimeline() = usersTimeline(User::createdOn)

    suspend fun usersActiveTimeline() = usersTimeline(User::lastActive)

    suspend fun usersByProviders() = FirebaseUsersFetcher
        .fetchUsers()
        .groupBy { it.idProviders }
        .mapValues { (_, values) -> values.count() }
}