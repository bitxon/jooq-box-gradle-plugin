package dev.bitxon.gradle.jooq.extension.migration

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

abstract class MigrationLiquibaseExtension {
    abstract val changeLogFile: Property<String>
    abstract val defaultSchemaName: Property<String>
    abstract val liquibaseSchemaName: Property<String>
    abstract val databaseChangeLogTableName: Property<String>
    abstract val databaseChangeLogLockTableName: Property<String>
    abstract val parameters: MapProperty<String, String>
}
