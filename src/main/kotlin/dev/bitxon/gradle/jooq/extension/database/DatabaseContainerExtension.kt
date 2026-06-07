package dev.bitxon.gradle.jooq.extension.database

import org.gradle.api.provider.Property

abstract class DatabaseContainerExtension {
    abstract val type: Property<String>
    abstract val image: Property<String>
    abstract val databaseName: Property<String>
    abstract val username: Property<String>
    abstract val password: Property<String>
}
