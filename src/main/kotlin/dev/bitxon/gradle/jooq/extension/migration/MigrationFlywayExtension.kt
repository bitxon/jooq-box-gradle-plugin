package dev.bitxon.gradle.jooq.extension.migration

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

abstract class MigrationFlywayExtension {
    // Default migration locations if not set:
    //   filesystem:<projectDir>/src/main/resources/db/migration
    abstract val locations: ListProperty<String>
    abstract val defaultSchema: Property<String>
    abstract val schemas: ListProperty<String>
    abstract val table: Property<String>

    // Any additional Flyway config as key→value pairs (keys WITHOUT the "flyway." prefix).
    // These are merged with the named properties above.
    abstract val properties: MapProperty<String, String>
}
