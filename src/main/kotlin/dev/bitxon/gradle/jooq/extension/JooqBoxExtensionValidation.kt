package dev.bitxon.gradle.jooq.extension

import dev.bitxon.gradle.jooq.extension.database.DatabaseContainerType
import dev.bitxon.gradle.jooq.extension.database.DatabaseEmbeddedType
import dev.bitxon.gradle.jooq.extension.database.DatabaseKind
import dev.bitxon.gradle.jooq.extension.migration.MigrationKind
import org.gradle.api.GradleException

fun JooqBoxExtension.validate() {
    database.kind.orNull
        ?: throw GradleException("jooq: configure one of: database.container { } or database.embedded { }")
    migration.kind.orNull
        ?: throw GradleException("jooq: configure one of: migration.flyway { }, migration.liquibase { }, or migration.sql { }")

    validateDatabase()
    validateMigration()
}

private fun JooqBoxExtension.validateDatabase() {
    when (database.kind.get()) {
        DatabaseKind.CONTAINER -> {
            val typeStr = database.container.type.orNull
                ?: throw GradleException("jooq: database.container.type is required")
            runCatching { DatabaseContainerType.valueOf(typeStr.uppercase()) }
                .onFailure {
                    throw GradleException(
                        "jooq: unknown database.container.type '$typeStr'. Valid values: ${DatabaseContainerType.entries.joinToString()}"
                    )
                }
        }
        DatabaseKind.EMBEDDED -> {
            val typeStr = database.embedded.type.orNull
                ?: throw GradleException("jooq: database.embedded.type is required")
            runCatching { DatabaseEmbeddedType.valueOf(typeStr.uppercase()) }
                .onFailure {
                    throw GradleException(
                        "jooq: unknown database.embedded.type '$typeStr'. Valid values: ${DatabaseEmbeddedType.entries.joinToString()}"
                    )
                }
        }
    }
}

private fun JooqBoxExtension.validateMigration() {
    if (migration.kind.get() == MigrationKind.SQL && migration.sql.scripts.isEmpty)
        throw GradleException("jooq: migration.sql.scripts is required")
}
