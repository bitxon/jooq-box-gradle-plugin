package dev.bitxon.gradle.jooq.internal.worker

import org.flywaydb.core.Flyway
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

abstract class MigrationFlywayAction : WorkAction<MigrationFlywayAction.Params> {

    interface Params : WorkParameters {
        val jdbcUrl: Property<String>
        val jdbcUser: Property<String>
        val jdbcPassword: Property<String>
        val locations: ListProperty<String>
        val defaultSchema: Property<String>
        val schemas: ListProperty<String>
        val table: Property<String>
        // Extra Flyway config as key→value pairs WITHOUT the "flyway." prefix.
        // Overrides the named properties above when the same key is present.
        val properties: MapProperty<String, String>
    }

    override fun execute() {
        val p = parameters
        val config = mutableMapOf<String, String>()
        config["locations"] = p.locations.get().joinToString(",")
        p.defaultSchema.orNull?.let { config["defaultSchema"] = it }
        p.schemas.orNull?.takeIf { it.isNotEmpty() }?.let { config["schemas"] = it.joinToString(",") }
        p.table.orNull?.let { config["table"] = it }
        config.putAll(p.properties.get())

        Flyway.configure()
            .dataSource(p.jdbcUrl.get(), p.jdbcUser.get(), p.jdbcPassword.get())
            .configuration(config.mapKeys { "flyway.${it.key}" })
            .load()
            .migrate()
    }
}
