package dev.bitxon.gradle.jooq

import dev.bitxon.gradle.jooq.extension.JooqBoxExtension
import dev.bitxon.gradle.jooq.extension.codegen.encodeToString
import dev.bitxon.gradle.jooq.extension.database.DatabaseContainerType
import dev.bitxon.gradle.jooq.extension.database.DatabaseEmbeddedType
import dev.bitxon.gradle.jooq.extension.database.DatabaseKind
import dev.bitxon.gradle.jooq.extension.migration.MigrationKind
import dev.bitxon.gradle.jooq.extension.validate
import dev.bitxon.gradle.jooq.internal.database.ContainerDefaults
import dev.bitxon.gradle.jooq.internal.database.EmbeddedDefaults
import dev.bitxon.gradle.jooq.task.GenerateJooqTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import java.io.File
import java.util.Properties

class JooqBoxPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("jooq", JooqBoxExtension::class.java)
        project.afterEvaluate { extension.validate() }

        val jooqCodegen = project.configurations.create("jooqCodegen") {
            it.isCanBeResolved = true
            it.isCanBeConsumed = false
            it.description = "jOOQ codegen artifacts (jooq-codegen, meta extensions, custom generators). " +
                "Leave empty to use the plugin default. Declare to override the jOOQ version or add extensions."
        }

        val jooqMigration = project.configurations.create("jooqMigration") {
            it.isCanBeResolved = true
            it.isCanBeConsumed = false
            it.description = "Migration tool artifacts (Flyway or Liquibase and their extensions). " +
                "Leave empty to use the plugin default. Declare to override the version or add extensions."
        }

        val jooqDatabase = project.configurations.create("jooqDatabase") {
            it.isCanBeResolved = true
            it.isCanBeConsumed = false
            it.description = "JDBC driver and any connection-level artifacts (auth libs, license JARs). " +
                "Leave empty to use the default driver for the configured database type."
        }

        configureDefaultDependencies(project, extension, jooqCodegen, jooqMigration, jooqDatabase)
        configureExtensionConventions(project, extension)

        val projectDir = project.layout.projectDirectory.asFile
        val objects = project.objects

        val generateTask = project.tasks.register("generateJooq", GenerateJooqTask::class.java) { task ->
            task.group = "jooq"
            task.description = "Generates jOOQ sources from a database"

            task.dbKind.convention(extension.database.kind)

            task.dbContainerType.convention(extension.database.container.type.map { DatabaseContainerType.valueOf(it.uppercase()) })
            task.dbContainerImage.convention(extension.database.container.image)
            task.dbContainerDatabaseName.convention(extension.database.container.databaseName)
            task.dbContainerUsername.convention(extension.database.container.username)
            task.dbContainerPassword.convention(extension.database.container.password)

            task.dbEmbeddedType.convention(extension.database.embedded.type.map { DatabaseEmbeddedType.valueOf(it.uppercase()) })

            task.migrationKind.convention(extension.migration.kind)

            task.flywayLocations.convention(extension.migration.flyway.locations)
            task.flywayDefaultSchema.convention(extension.migration.flyway.defaultSchema)
            task.flywaySchemas.convention(extension.migration.flyway.schemas)
            task.flywayTable.convention(extension.migration.flyway.table)
            task.flywayProperties.convention(extension.migration.flyway.properties)

            task.sqlScriptPaths.convention(
                extension.migration.sql.scripts.elements.map { fcs ->
                    fcs.map { it.asFile.absolutePath }
                }
            )

            task.liquibaseChangeLogFile.convention(extension.migration.liquibase.changeLogFile)
            task.liquibaseDefaultSchemaName.convention(extension.migration.liquibase.defaultSchemaName)
            task.liquibaseLiquibaseSchemaName.convention(extension.migration.liquibase.liquibaseSchemaName)
            task.liquibaseChangeLogTableName.convention(extension.migration.liquibase.databaseChangeLogTableName)
            task.liquibaseChangeLogLockTableName.convention(extension.migration.liquibase.databaseChangeLogLockTableName)
            task.liquibaseParameters.convention(extension.migration.liquibase.parameters)

            task.jooqGeneratorName.convention(extension.codegen.generator.name)
            task.jooqDatabaseName.convention(extension.codegen.generator.database.name)
            task.jooqInputSchema.convention(extension.codegen.generator.database.inputSchema)
            task.jooqIncludes.convention(extension.codegen.generator.database.includes)
            task.jooqExcludes.convention(extension.codegen.generator.database.excludes)
            task.jooqForcedTypes.convention(
                project.provider { extension.codegen.generator.database.forcedTypeList.map { it.encodeToString() } }
            )
            task.jooqGeneratePojos.convention(extension.codegen.generator.generate.pojos)
            task.jooqGenerateImmutablePojos.convention(extension.codegen.generator.generate.immutablePojos)
            task.jooqGenerateDaos.convention(extension.codegen.generator.generate.daos)
            task.jooqGenerateInterfaces.convention(extension.codegen.generator.generate.interfaces)
            task.jooqGenerateRecords.convention(extension.codegen.generator.generate.records)
            task.jooqGenerateJavaTimeTypes.convention(extension.codegen.generator.generate.javaTimeTypes)
            task.jooqGenerateSpringAnnotations.convention(extension.codegen.generator.generate.springAnnotations)
            task.jooqGenerateSpringDao.convention(extension.codegen.generator.generate.springDao)
            task.jooqGenerateFluentSetters.convention(extension.codegen.generator.generate.fluentSetters)
            task.jooqGenerateGlobalObjectReferences.convention(extension.codegen.generator.generate.globalObjectReferences)
            task.jooqStrategyName.convention(extension.codegen.generator.strategy.name)
            task.jooqTargetPackage.convention(extension.codegen.generator.target.packageName)
            task.jooqLogging.convention(extension.codegen.logging)
            task.jooqOnError.convention(extension.codegen.onError)
            task.jooqConfigFile.convention(extension.codegen.configFile)
            task.jooqOutputDirectory.convention(
                extension.codegen.generator.target.directory.flatMap { dirStr ->
                    val file = File(dirStr).let { if (it.isAbsolute) it else projectDir.resolve(it) }
                    objects.directoryProperty().also { it.fileValue(file) }
                }
            )

            task.projectDirectory.set(project.layout.projectDirectory)
            task.buildDirectory.set(project.layout.buildDirectory)

            task.jooqCodegenClasspath.from(jooqCodegen)
            task.jooqMigrationClasspath.from(jooqMigration)
            task.jooqDatabaseClasspath.from(jooqDatabase)

            // Track migration file content so Gradle can skip the task (UP_TO_DATE / FROM_CACHE)
            // when nothing has changed. classpath: locations are not resolvable as files, so only
            // filesystem: entries are registered. Absent providers (e.g. changeLogFile when using
            // Flyway) contribute nothing to the collection.
            task.migrationFiles.from(
                extension.migration.flyway.locations.map { locations ->
                    locations.filter { it.startsWith("filesystem:") }
                        .map { project.fileTree(it.removePrefix("filesystem:")) }
                }
            )
            task.migrationFiles.from(
                project.provider {
                    extension.migration.liquibase.changeLogFile.orNull?.let { changeLogFileStr ->
                        val file = File(changeLogFileStr).let { if (it.isAbsolute) it else projectDir.resolve(it) }
                        project.fileTree(file.parentFile)
                    } ?: project.files()
                }
            )
            task.migrationFiles.from(extension.migration.sql.scripts)
        }

        // compileJava wiring
        project.plugins.withType(JavaPlugin::class.java) {
            project.extensions.getByType(JavaPluginExtension::class.java)
                .sourceSets
                .getByName("main")
                .java
                .srcDir(generateTask.flatMap { it.jooqOutputDirectory })

            project.tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME) {
                it.dependsOn(generateTask)
            }
        }

        // compileKotlin wiring
        // Kotlin plugin applies the Java plugin internally and compileKotlin includes mainSourceSet.java

        // compileScala wiring
        project.plugins.withId("scala") {
            val mainSourceSet = project.extensions
                .getByType(JavaPluginExtension::class.java)
                .sourceSets
                .getByName("main")

            (mainSourceSet.extensions.findByName("scala") as? SourceDirectorySet)
                ?.srcDir(generateTask.flatMap { it.jooqOutputDirectory })

            project.tasks.named("compileScala") {
                it.dependsOn(generateTask)
            }
        }
    }

    private fun configureExtensionConventions(
        project: Project,
        extension: JooqBoxExtension,
    ) {
        // Flyway API default (classpath:db/migration) doesn't work in Worker API process isolation
        // the worker classpath contains only jooqMigration artifacts, not project resources.
        extension.migration.flyway.locations.convention(
            listOf("filesystem:${project.projectDir.absolutePath}/src/main/resources/db/migration")
        )
        extension.codegen.generator.name.convention("org.jooq.codegen.JavaGenerator")
        extension.codegen.generator.database.name.convention(
            extension.database.kind.flatMap { type ->
                when (type) {
                    DatabaseKind.CONTAINER -> extension.database.container.type
                        .map { ContainerDefaults[DatabaseContainerType.valueOf(it.uppercase())].jooqGenerator }
                    DatabaseKind.EMBEDDED  -> extension.database.embedded.type
                        .map { EmbeddedDefaults[DatabaseEmbeddedType.valueOf(it.uppercase())].jooqGenerator }
                }
            }
        )
        // Override jOOQ's Maven-centric default (target/generated-sources/jooq) with Gradle-idiomatic path.
        extension.codegen.generator.target.directory.convention(
            project.layout.buildDirectory.dir("generated-sources/jooq").map { it.asFile.absolutePath }
        )
    }

    private fun configureDefaultDependencies(
        project: Project,
        extension: JooqBoxExtension,
        jooqCodegen: Configuration,
        jooqMigration: Configuration,
        jooqDatabase: Configuration,
    ) {
        // withDependencies instead of defaultDependencies so that a project() dep (e.g. a migrations
        // subproject JAR on jooqMigration, or a custom generator on jooqCodegen) does not suppress
        // default tool coordinates. defaultDependencies fires only when zero deps exist; withDependencies
        // fires on every resolution. We skip injection when the user already declared an ExternalDependency
        // for the relevant group — that is the override signal.

        jooqCodegen.withDependencies { deps ->
            val hasJooqDep = deps.filterIsInstance<ExternalDependency>().any { it.group == "org.jooq" }
            if (!hasJooqDep) {
                deps.add(project.dependencies.create("org.jooq:jooq-codegen:$JOOQ_VERSION"))
            }
        }

        jooqMigration.withDependencies { deps ->
            when (extension.migration.kind.get()) {
                MigrationKind.FLYWAY -> {
                    val hasFlywayDep = deps.filterIsInstance<ExternalDependency>().any { it.group == "org.flywaydb" }
                    if (!hasFlywayDep) {
                        deps.add(project.dependencies.create("org.flywaydb:flyway-core:$FLYWAY_VERSION"))
                        // Container DBs need a database-specific Flyway module; embedded DBs are covered by flyway-core.
                        if (extension.database.kind.get() == DatabaseKind.CONTAINER) {
                            val containerType = DatabaseContainerType.valueOf(extension.database.container.type.get().uppercase())
                            deps.add(project.dependencies.create("${ContainerDefaults[containerType].flywayModule}:$FLYWAY_VERSION"))
                        }
                    }
                }
                MigrationKind.LIQUIBASE -> {
                    val hasLiquibaseDep = deps.filterIsInstance<ExternalDependency>().any { it.group == "org.liquibase" }
                    if (!hasLiquibaseDep) {
                        deps.add(project.dependencies.create("org.liquibase:liquibase-core:$LIQUIBASE_VERSION"))
                    }
                }
                MigrationKind.SQL -> { /* no migration tool deps needed — plain JDBC only */ }
            }
        }

        jooqDatabase.withDependencies { deps ->
            when (extension.database.kind.get()) {
                DatabaseKind.CONTAINER -> {
                    val containerType = DatabaseContainerType.valueOf(extension.database.container.type.get().uppercase())
                    val driverGroup = ContainerDefaults[containerType].driverModule.substringBefore(":")
                    if (deps.filterIsInstance<ExternalDependency>().none { it.group == driverGroup }) {
                        val driverVersion = when (containerType) {
                            DatabaseContainerType.POSTGRES -> POSTGRES_DRIVER_VERSION
                            DatabaseContainerType.MYSQL    -> MYSQL_DRIVER_VERSION
                            DatabaseContainerType.MARIADB  -> MARIADB_DRIVER_VERSION
                        }
                        deps.add(project.dependencies.create("${ContainerDefaults[containerType].driverModule}:$driverVersion"))
                    }
                }
                DatabaseKind.EMBEDDED -> {
                    val embeddedType = DatabaseEmbeddedType.valueOf(extension.database.embedded.type.get().uppercase())
                    val driverGroup = EmbeddedDefaults[embeddedType].driverModule.substringBefore(":")
                    if (deps.filterIsInstance<ExternalDependency>().none { it.group == driverGroup }) {
                        val driverVersion = when (embeddedType) {
                            DatabaseEmbeddedType.H2     -> H2_DRIVER_VERSION
                            DatabaseEmbeddedType.SQLITE -> SQLITE_DRIVER_VERSION
                            DatabaseEmbeddedType.HSQLDB -> HSQLDB_DRIVER_VERSION
                        }
                        deps.add(project.dependencies.create("${EmbeddedDefaults[embeddedType].driverModule}:$driverVersion"))
                    }
                }
            }
        }
    }

    companion object {
        private val defaults: Properties by lazy {
            Properties().apply {
                (JooqBoxPlugin::class.java.getResourceAsStream("/plugin-defaults.properties")
                    ?: error("plugin-defaults.properties not found in plugin JAR"))
                    .use { load(it) }
            }
        }

        val JOOQ_VERSION: String get() = defaults.getProperty("jooq.version")
        val FLYWAY_VERSION: String get() = defaults.getProperty("flyway.version")
        val LIQUIBASE_VERSION: String get() = defaults.getProperty("liquibase.version")
        // Container drivers
        val POSTGRES_DRIVER_VERSION: String get() = defaults.getProperty("driver.postgres.version")
        val MYSQL_DRIVER_VERSION: String get() = defaults.getProperty("driver.mysql.version")
        val MARIADB_DRIVER_VERSION: String get() = defaults.getProperty("driver.mariadb.version")
        // Embedded drivers
        val H2_DRIVER_VERSION: String get() = defaults.getProperty("driver.h2.version")
        val SQLITE_DRIVER_VERSION: String get() = defaults.getProperty("driver.sqlite.version")
        val HSQLDB_DRIVER_VERSION: String get() = defaults.getProperty("driver.hsqldb.version")
    }
}
