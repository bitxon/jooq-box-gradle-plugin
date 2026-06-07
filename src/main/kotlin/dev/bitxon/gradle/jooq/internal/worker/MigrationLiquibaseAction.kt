package dev.bitxon.gradle.jooq.internal.worker

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.DirectoryResourceAccessor
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import java.io.File
import java.sql.DriverManager

abstract class MigrationLiquibaseAction : WorkAction<MigrationLiquibaseAction.Params> {

    interface Params : WorkParameters {
        val jdbcUrl: Property<String>
        val jdbcUser: Property<String>
        val jdbcPassword: Property<String>
        // Absolute path to the changelog file (resolved by the task before submission)
        val changeLogFile: Property<String>
        val defaultSchemaName: Property<String>
        val liquibaseSchemaName: Property<String>
        val databaseChangeLogTableName: Property<String>
        val databaseChangeLogLockTableName: Property<String>
        val parameters: MapProperty<String, String>
    }

    override fun execute() {
        val p = parameters
        val changeLogFile = File(p.changeLogFile.get())
        val resourceAccessor = DirectoryResourceAccessor(changeLogFile.parentFile.toPath())
        val connection = DriverManager.getConnection(p.jdbcUrl.get(), p.jdbcUser.get(), p.jdbcPassword.get())

        connection.use {
            val db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))

            p.defaultSchemaName.orNull?.let { db.defaultSchemaName = it }
            p.liquibaseSchemaName.orNull?.let { db.liquibaseSchemaName = it }
            p.databaseChangeLogTableName.orNull?.let { db.databaseChangeLogTableName = it }
            p.databaseChangeLogLockTableName.orNull?.let { db.databaseChangeLogLockTableName = it }

            Liquibase(changeLogFile.name, resourceAccessor, db).use { lb ->
                p.parameters.get().forEach { (k, v) -> lb.setChangeLogParameter(k, v) }
                lb.update()
            }
        }
    }
}
