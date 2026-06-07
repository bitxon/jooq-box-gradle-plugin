package dev.bitxon.gradle.jooq.internal.database

import dev.bitxon.gradle.jooq.extension.database.DatabaseContainerType
import dev.bitxon.gradle.jooq.extension.database.DatabaseContainerType.MARIADB
import dev.bitxon.gradle.jooq.extension.database.DatabaseContainerType.MYSQL
import dev.bitxon.gradle.jooq.extension.database.DatabaseContainerType.POSTGRES
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.mariadb.MariaDBContainer
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.postgresql.PostgreSQLContainer

internal object ContainerFactory {

    fun create(
        type: DatabaseContainerType,
        image: String?,
        databaseName: String?,
        username: String?,
        password: String?,
    ): JdbcDatabaseContainer<*> {
        val effectiveImage = image ?: ContainerDefaults[type].image
        return when (type) {
            POSTGRES -> SafePostgreSQLContainer(effectiveImage).applyConfig(databaseName, username, password)
            MYSQL    -> SafeMySQLContainer(effectiveImage).applyConfig(databaseName, username, password)
            MARIADB  -> SafeMariaDBContainer(effectiveImage).applyConfig(databaseName, username, password)
        }
    }

    private fun <T : JdbcDatabaseContainer<T>> T.applyConfig(
        databaseName: String?,
        username: String?,
        password: String?,
    ): T = apply {
        databaseName?.let { withDatabaseName(it) }
        username?.let { withUsername(it) }
        password?.let { withPassword(it) }
    }

    private class SafePostgreSQLContainer(image: String) : PostgreSQLContainer(image) {
        init { waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2)) }
    }

    // TC 2.x MySQLContainer/MariaDBContainer inherit JdbcDatabaseContainer.waitUntilContainerStarted()
    // which calls getJdbcDriverInstance() — fails because the JDBC driver lives in jooqDatabase,
    // not the plugin's classpath. PostgreSQLContainer in TC 2.x overrides this to only call
    // getWaitStrategy().waitUntilReady(this); we do the same for MySQL and MariaDB.
    private class SafeMySQLContainer(image: String) : MySQLContainer(image) {
        init { waitingFor(Wait.forLogMessage(".*ready for connections.*port: 3306.*", 1)) }
        override fun waitUntilContainerStarted() = getWaitStrategy().waitUntilReady(this)
    }

    private class SafeMariaDBContainer(image: String) : MariaDBContainer(image) {
        init { waitingFor(Wait.forLogMessage(".*ready for connections.*", 2)) }
        override fun waitUntilContainerStarted() = getWaitStrategy().waitUntilReady(this)
    }
}
