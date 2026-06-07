package dev.bitxon.gradle.jooq.internal.database

import dev.bitxon.gradle.jooq.extension.database.DatabaseContainerType
import dev.bitxon.gradle.jooq.extension.database.DatabaseEmbeddedType
import java.io.File

internal object DatabaseFactory {

    fun createContainer(
        type: DatabaseContainerType,
        image: String?,
        databaseName: String?,
        username: String?,
        password: String?,
    ): Database = ContainerDatabase(
        ContainerFactory.create(type, image, databaseName, username, password),
        type,
    )

    fun createEmbedded(
        type: DatabaseEmbeddedType,
        buildDir: File,
    ): Database = EmbeddedDatabase(type, buildDir)
}
