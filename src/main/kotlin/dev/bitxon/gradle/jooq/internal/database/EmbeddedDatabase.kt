package dev.bitxon.gradle.jooq.internal.database

import dev.bitxon.gradle.jooq.extension.database.DatabaseEmbeddedType
import java.io.File
import java.util.UUID

internal class EmbeddedDatabase(
    private val type: DatabaseEmbeddedType,
    buildDir: File,
) : Database {

    private val profile = EmbeddedDefaults[type]
    private val dir = buildDir.resolve("tmp/generateJooq/db/${UUID.randomUUID()}")

    override val jdbcDriver: String get() = profile.jdbcDriver
    override val username: String get() = profile.username
    override val password: String get() = profile.password
    override val jdbcUrl: String = buildJdbcUrl(type, dir)
    override val description: String get() = "$type embedded database"

    override fun start() {
        dir.mkdirs()
    }

    override fun stop() {
        dir.deleteRecursively()
    }

    private fun buildJdbcUrl(type: DatabaseEmbeddedType, dir: File): String = when (type) {
        DatabaseEmbeddedType.H2     -> "jdbc:h2:file:${dir.absolutePath}/db"
        DatabaseEmbeddedType.SQLITE -> "jdbc:sqlite:${dir.absolutePath}/db.sqlite"
        // shutdown=true: HSQLDB performs a clean checkpoint+shutdown when the last connection closes,
        // removing the lock file. This lets the next worker (codegen) reconnect to the same files.
        DatabaseEmbeddedType.HSQLDB -> "jdbc:hsqldb:file:${dir.absolutePath}/db;shutdown=true"
    }
}
