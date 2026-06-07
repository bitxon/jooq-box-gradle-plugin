package dev.bitxon.gradle.jooq.internal.database

import dev.bitxon.gradle.jooq.extension.database.DatabaseContainerType
import org.testcontainers.containers.JdbcDatabaseContainer

internal class ContainerDatabase(
    private val container: JdbcDatabaseContainer<*>,
    private val type: DatabaseContainerType,
) : Database {

    override val jdbcUrl: String get() = container.jdbcUrl
    override val username: String get() = container.username
    override val password: String get() = container.password
    override val jdbcDriver: String get() = ContainerDefaults[type].jdbcDriver
    override val description: String get() = "$type container"

    override fun start() = container.start()
    override fun stop() = container.stop()
}
