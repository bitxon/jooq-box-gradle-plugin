package dev.bitxon.gradle.jooq.internal.database

import dev.bitxon.gradle.jooq.extension.database.DatabaseContainerType

internal object ContainerDefaults {

    data class Profile(
        val image: String,
        val jdbcDriver: String,
        val jooqGenerator: String,
        val driverModule: String,
        val flywayModule: String,
    )

    private val profiles = mapOf(
        DatabaseContainerType.POSTGRES to Profile(
            image = "postgres:17-alpine",
            jdbcDriver = "org.postgresql.Driver",
            jooqGenerator = "org.jooq.meta.postgres.PostgresDatabase",
            driverModule = "org.postgresql:postgresql",
            flywayModule = "org.flywaydb:flyway-database-postgresql",
        ),
        DatabaseContainerType.MYSQL to Profile(
            image = "mysql:8.4",
            jdbcDriver = "com.mysql.cj.jdbc.Driver",
            jooqGenerator = "org.jooq.meta.mysql.MySQLDatabase",
            driverModule = "com.mysql:mysql-connector-j",
            flywayModule = "org.flywaydb:flyway-mysql",
        ),
        DatabaseContainerType.MARIADB to Profile(
            image = "mariadb:11.4",
            jdbcDriver = "org.mariadb.jdbc.Driver",
            jooqGenerator = "org.jooq.meta.mariadb.MariaDBDatabase",
            driverModule = "org.mariadb.jdbc:mariadb-java-client",
            flywayModule = "org.flywaydb:flyway-mysql",
        ),
    )

    operator fun get(type: DatabaseContainerType): Profile = profiles.getValue(type)
}
