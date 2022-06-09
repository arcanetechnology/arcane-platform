package no.arcane.platform.app.trade.ledger.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource


object DataSourceProvider {

    internal var testDatasource: DataSource? = null

    private val dataSource: HikariDataSource by lazy {
        val config = HikariConfig("/db.properties")
        HikariDataSource(config)
    }

    fun getDataSource(): DataSource = testDatasource ?: dataSource
}
