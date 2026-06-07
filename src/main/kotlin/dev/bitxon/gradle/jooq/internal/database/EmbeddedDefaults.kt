package dev.bitxon.gradle.jooq.internal.database

import dev.bitxon.gradle.jooq.extension.database.DatabaseEmbeddedType

internal object EmbeddedDefaults {

    data class Profile(
        val jdbcDriver: String,
        val jooqGenerator: String,
        val driverModule: String,
        val username: String,
        val password: String,
    )

    private val profiles = mapOf(
        DatabaseEmbeddedType.H2 to Profile(
            jdbcDriver    = "org.h2.Driver",
            jooqGenerator = "org.jooq.meta.h2.H2Database",
            driverModule  = "com.h2database:h2",
            username      = "sa",
            password      = "",
        ),
        DatabaseEmbeddedType.SQLITE to Profile(
            jdbcDriver    = "org.sqlite.JDBC",
            jooqGenerator = "org.jooq.meta.sqlite.SQLiteDatabase",
            driverModule  = "org.xerial:sqlite-jdbc",
            username      = "",
            password      = "",
        ),
        DatabaseEmbeddedType.HSQLDB to Profile(
            jdbcDriver    = "org.hsqldb.jdbc.JDBCDriver",
            jooqGenerator = "org.jooq.meta.hsqldb.HSQLDBDatabase",
            driverModule  = "org.hsqldb:hsqldb",
            username      = "sa",
            password      = "",
        ),
    )

    operator fun get(type: DatabaseEmbeddedType): Profile = profiles.getValue(type)
}
