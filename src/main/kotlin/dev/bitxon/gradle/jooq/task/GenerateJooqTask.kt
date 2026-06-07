package dev.bitxon.gradle.jooq.task

import dev.bitxon.gradle.jooq.extension.database.DatabaseContainerType
import dev.bitxon.gradle.jooq.extension.database.DatabaseEmbeddedType
import dev.bitxon.gradle.jooq.extension.database.DatabaseKind
import dev.bitxon.gradle.jooq.extension.migration.MigrationKind
import dev.bitxon.gradle.jooq.internal.database.DatabaseFactory
import dev.bitxon.gradle.jooq.internal.worker.CodegenAction
import dev.bitxon.gradle.jooq.internal.worker.MigrationFlywayAction
import dev.bitxon.gradle.jooq.internal.worker.MigrationLiquibaseAction
import dev.bitxon.gradle.jooq.internal.worker.MigrationSqlAction
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
abstract class GenerateJooqTask : DefaultTask() {

    // Database — discriminator
    @get:Input abstract val dbKind: Property<DatabaseKind>
    // Database / Container
    @get:Input @get:Optional abstract val dbContainerType: Property<DatabaseContainerType>
    @get:Input @get:Optional abstract val dbContainerImage: Property<String>
    @get:Input @get:Optional abstract val dbContainerDatabaseName: Property<String>
    @get:Input @get:Optional abstract val dbContainerUsername: Property<String>
    @get:Input @get:Optional abstract val dbContainerPassword: Property<String>
    // Database / Embedded
    @get:Input @get:Optional abstract val dbEmbeddedType: Property<DatabaseEmbeddedType>

    // Migration — discriminator
    @get:Input abstract val migrationKind: Property<MigrationKind>
    // Migration / Flyway
    @get:Input @get:Optional abstract val flywayLocations: ListProperty<String>
    @get:Input @get:Optional abstract val flywayDefaultSchema: Property<String>
    @get:Input @get:Optional abstract val flywaySchemas: ListProperty<String>
    @get:Input @get:Optional abstract val flywayTable: Property<String>
    @get:Input @get:Optional abstract val flywayProperties: MapProperty<String, String>
    // Migration / SQL
    @get:Internal abstract val sqlScriptPaths: ListProperty<String>
    // Migration / Liquibase
    @get:Input @get:Optional abstract val liquibaseChangeLogFile: Property<String>
    @get:Input @get:Optional abstract val liquibaseDefaultSchemaName: Property<String>
    @get:Input @get:Optional abstract val liquibaseLiquibaseSchemaName: Property<String>
    @get:Input @get:Optional abstract val liquibaseChangeLogTableName: Property<String>
    @get:Input @get:Optional abstract val liquibaseChangeLogLockTableName: Property<String>
    @get:Input @get:Optional abstract val liquibaseParameters: MapProperty<String, String>

    // jOOQ generator / schema / target
    @get:Input @get:Optional abstract val jooqGeneratorName: Property<String>
    @get:Input @get:Optional abstract val jooqDatabaseName: Property<String>
    @get:Input @get:Optional abstract val jooqInputSchema: Property<String>
    @get:Input @get:Optional abstract val jooqIncludes: Property<String>
    @get:Input @get:Optional abstract val jooqExcludes: Property<String>
    @get:Input @get:Optional abstract val jooqForcedTypes: ListProperty<String>
    @get:Input @get:Optional abstract val jooqGeneratePojos: Property<Boolean>
    @get:Input @get:Optional abstract val jooqGenerateImmutablePojos: Property<Boolean>
    @get:Input @get:Optional abstract val jooqGenerateDaos: Property<Boolean>
    @get:Input @get:Optional abstract val jooqGenerateInterfaces: Property<Boolean>
    @get:Input @get:Optional abstract val jooqGenerateRecords: Property<Boolean>
    @get:Input @get:Optional abstract val jooqGenerateJavaTimeTypes: Property<Boolean>
    @get:Input @get:Optional abstract val jooqGenerateSpringAnnotations: Property<Boolean>
    @get:Input @get:Optional abstract val jooqGenerateSpringDao: Property<Boolean>
    @get:Input @get:Optional abstract val jooqGenerateFluentSetters: Property<Boolean>
    @get:Input @get:Optional abstract val jooqGenerateGlobalObjectReferences: Property<Boolean>
    @get:Input @get:Optional abstract val jooqStrategyName: Property<String>
    @get:Input @get:Optional abstract val jooqTargetPackage: Property<String>
    @get:Input @get:Optional abstract val jooqLogging: Property<String>
    @get:Input @get:Optional abstract val jooqOnError: Property<String>
    @get:InputFile @get:Optional @get:PathSensitive(PathSensitivity.NONE) abstract val jooqConfigFile: RegularFileProperty
    @get:OutputDirectory abstract val jooqOutputDirectory: DirectoryProperty

    // Never read by the task action — Gradle reads this via @InputFiles reflection
    // to fingerprint migration file content for UP_TO_DATE / FROM_CACHE checks.
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val migrationFiles: ConfigurableFileCollection

    // Used only for resolving relative Liquibase changeLogFile paths — not a content input.
    @get:Internal abstract val projectDirectory: DirectoryProperty
    // Build directory — used as the root for embedded DB temp files.
    @get:Internal abstract val buildDirectory: DirectoryProperty

    @get:Classpath abstract val jooqCodegenClasspath: ConfigurableFileCollection
    @get:Classpath abstract val jooqMigrationClasspath: ConfigurableFileCollection
    @get:Classpath abstract val jooqDatabaseClasspath: ConfigurableFileCollection

    @get:Inject abstract val workers: WorkerExecutor

    @TaskAction
    fun generate() {
        val migrationKind = migrationKind.get()
        val dbKind = dbKind.get()
        val targetPackage = jooqTargetPackage.orNull
            ?: if (!jooqConfigFile.isPresent)
                throw GradleException("jooq: codegen.generator.target.packageName is required")
               else null

        val targetDir = jooqOutputDirectory.get().asFile.absolutePath

        val db = when (dbKind) {
            DatabaseKind.CONTAINER -> DatabaseFactory.createContainer(
                type         = dbContainerType.orNull ?: throw GradleException("jooq: database.container.type is required"),
                image        = dbContainerImage.orNull,
                databaseName = dbContainerDatabaseName.orNull,
                username     = dbContainerUsername.orNull,
                password     = dbContainerPassword.orNull,
            )
            DatabaseKind.EMBEDDED  -> DatabaseFactory.createEmbedded(
                type     = dbEmbeddedType.orNull ?: throw GradleException("jooq: database.embedded.type is required"),
                buildDir = buildDirectory.get().asFile,
            )
        }

        try {
            logger.lifecycle("jooq: starting {}", db.description)
            db.start()

            val jdbcUrl      = db.jdbcUrl
            val jdbcUser     = db.username
            val jdbcPassword = db.password

            when (migrationKind) {
                MigrationKind.FLYWAY    -> submitFlywayMigration(jdbcUrl, jdbcUser, jdbcPassword)
                MigrationKind.LIQUIBASE -> submitLiquibaseMigration(jdbcUrl, jdbcUser, jdbcPassword)
                MigrationKind.SQL       -> submitSqlMigration(jdbcUrl, jdbcUser, jdbcPassword)
            }

            workers.await()
            logger.lifecycle("jooq: migrations complete, generating jOOQ sources")

            workers.processIsolation { spec ->
                spec.classpath.from(jooqCodegenClasspath, jooqDatabaseClasspath)
            }.submit(CodegenAction::class.java) { p ->
                p.jdbcUrl.set(jdbcUrl)
                p.jdbcUser.set(jdbcUser)
                p.jdbcPassword.set(jdbcPassword)
                p.jdbcDriver.set(db.jdbcDriver)
                p.generatorName.set(jooqGeneratorName)
                p.databaseName.set(jooqDatabaseName)
                p.inputSchema.set(jooqInputSchema)
                p.includes.set(jooqIncludes)
                p.excludes.set(jooqExcludes)
                p.forcedTypes.set(jooqForcedTypes)
                p.generatePojos.set(jooqGeneratePojos)
                p.generateImmutablePojos.set(jooqGenerateImmutablePojos)
                p.generateDaos.set(jooqGenerateDaos)
                p.generateInterfaces.set(jooqGenerateInterfaces)
                p.generateRecords.set(jooqGenerateRecords)
                p.generateJavaTimeTypes.set(jooqGenerateJavaTimeTypes)
                p.generateSpringAnnotations.set(jooqGenerateSpringAnnotations)
                p.generateSpringDao.set(jooqGenerateSpringDao)
                p.generateFluentSetters.set(jooqGenerateFluentSetters)
                p.generateGlobalObjectReferences.set(jooqGenerateGlobalObjectReferences)
                p.strategyName.set(jooqStrategyName)
                p.targetPackage.set(targetPackage)
                p.targetDirectory.set(targetDir)
                p.logging.set(jooqLogging)
                p.onError.set(jooqOnError)
                p.configFile.set(jooqConfigFile)
            }

            workers.await()
            logger.lifecycle("jooq: jOOQ sources generated in {}", targetDir)
        } finally {
            logger.lifecycle("jooq: stopping {}", db.description)
            db.stop()
        }
    }

    private fun submitFlywayMigration(jdbcUrl: String, jdbcUser: String, jdbcPassword: String) {
        workers.processIsolation { spec ->
            spec.classpath.from(jooqMigrationClasspath, jooqDatabaseClasspath)
        }.submit(MigrationFlywayAction::class.java) { p ->
            p.jdbcUrl.set(jdbcUrl)
            p.jdbcUser.set(jdbcUser)
            p.jdbcPassword.set(jdbcPassword)
            p.locations.set(flywayLocations)
            p.defaultSchema.set(flywayDefaultSchema)
            p.schemas.set(flywaySchemas)
            p.table.set(flywayTable)
            p.properties.set(flywayProperties.getOrElse(emptyMap()))
        }
    }

    private fun submitSqlMigration(jdbcUrl: String, jdbcUser: String, jdbcPassword: String) {
        workers.processIsolation { spec ->
            spec.classpath.from(jooqDatabaseClasspath)
        }.submit(MigrationSqlAction::class.java) { p ->
            p.jdbcUrl.set(jdbcUrl)
            p.jdbcUser.set(jdbcUser)
            p.jdbcPassword.set(jdbcPassword)
            p.scriptPaths.set(sqlScriptPaths)
        }
    }

    private fun submitLiquibaseMigration(jdbcUrl: String, jdbcUser: String, jdbcPassword: String) {
        val changeLogFileRel = liquibaseChangeLogFile.orNull
            ?: throw GradleException("jooq: liquibase.changeLogFile is required")
        // File.resolve() handles absolute paths by returning them unchanged.
        val changeLogFileAbs = projectDirectory.get().asFile.resolve(changeLogFileRel).absolutePath

        workers.processIsolation { spec ->
            spec.classpath.from(jooqMigrationClasspath, jooqDatabaseClasspath)
        }.submit(MigrationLiquibaseAction::class.java) { p ->
            p.jdbcUrl.set(jdbcUrl)
            p.jdbcUser.set(jdbcUser)
            p.jdbcPassword.set(jdbcPassword)
            p.changeLogFile.set(changeLogFileAbs)
            liquibaseDefaultSchemaName.orNull?.let { p.defaultSchemaName.set(it) }
            liquibaseLiquibaseSchemaName.orNull?.let { p.liquibaseSchemaName.set(it) }
            liquibaseChangeLogTableName.orNull?.let { p.databaseChangeLogTableName.set(it) }
            liquibaseChangeLogLockTableName.orNull?.let { p.databaseChangeLogLockTableName.set(it) }
            p.parameters.set(liquibaseParameters.getOrElse(emptyMap()))
        }
    }
}
