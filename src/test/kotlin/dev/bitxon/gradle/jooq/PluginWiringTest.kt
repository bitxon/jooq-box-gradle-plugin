package dev.bitxon.gradle.jooq

import dev.bitxon.gradle.jooq.extension.JooqBoxExtension
import dev.bitxon.gradle.jooq.extension.database.DatabaseContainerType
import dev.bitxon.gradle.jooq.extension.database.DatabaseEmbeddedType
import dev.bitxon.gradle.jooq.extension.database.DatabaseKind
import dev.bitxon.gradle.jooq.extension.migration.MigrationKind
import dev.bitxon.gradle.jooq.task.GenerateJooqTask
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class PluginWiringTest {

    private val project = ProjectBuilder.builder().build()
    private val ext: JooqBoxExtension

    init {
        project.plugins.apply("dev.bitxon.jooq-box")
        ext = project.extensions.getByType(JooqBoxExtension::class.java)
        ext.database.container.type.set("POSTGRES")
    }

    private val task: GenerateJooqTask
        get() = project.tasks.named("generateJooq", GenerateJooqTask::class.java).get()

    // ── Database container ─────────────────────────────────────────────────────

    @Test
    fun `database container properties are wired to task`() {
        ext.database.container.image.set("postgres:15-alpine")
        ext.database.container.databaseName.set("mydb")
        ext.database.container.username.set("admin")
        ext.database.container.password.set("secret")
        ext.migration.flyway { }

        assertThat(task.dbContainerType.get()).isEqualTo(DatabaseContainerType.POSTGRES)
        assertThat(task.dbContainerImage.get()).isEqualTo("postgres:15-alpine")
        assertThat(task.dbContainerDatabaseName.get()).isEqualTo("mydb")
        assertThat(task.dbContainerUsername.get()).isEqualTo("admin")
        assertThat(task.dbContainerPassword.get()).isEqualTo("secret")
    }

    // ── Database embedded ─────────────────────────────────────────────────────

    @Test
    fun `embedded database properties are wired to task`() {
        val localProject = ProjectBuilder.builder().build()
        localProject.plugins.apply("dev.bitxon.jooq-box")
        val localExt = localProject.extensions.getByType(JooqBoxExtension::class.java)

        localExt.database.embedded { it.type.set("H2") }
        localExt.migration.flyway { }

        val localTask = localProject.tasks.named("generateJooq", GenerateJooqTask::class.java).get()

        assertThat(localTask.dbKind.get()).isEqualTo(DatabaseKind.EMBEDDED)
        assertThat(localTask.dbEmbeddedType.get()).isEqualTo(DatabaseEmbeddedType.H2)
    }

    // ── Generate flags ─────────────────────────────────────────────────────────

    @Test
    fun `all generate flags are wired from DSL to task`() {
        ext.migration.flyway { }
        val gen = ext.codegen.generator.generate
        gen.pojos.set(true)
        gen.immutablePojos.set(true)
        gen.daos.set(true)
        gen.interfaces.set(true)
        gen.records.set(false)
        gen.javaTimeTypes.set(false)
        gen.springAnnotations.set(true)
        gen.springDao.set(true)
        gen.fluentSetters.set(true)
        gen.globalObjectReferences.set(false)

        assertThat(task.jooqGeneratePojos.get()).isTrue()
        assertThat(task.jooqGenerateImmutablePojos.get()).isTrue()
        assertThat(task.jooqGenerateDaos.get()).isTrue()
        assertThat(task.jooqGenerateInterfaces.get()).isTrue()
        assertThat(task.jooqGenerateRecords.get()).isFalse()
        assertThat(task.jooqGenerateJavaTimeTypes.get()).isFalse()
        assertThat(task.jooqGenerateSpringAnnotations.get()).isTrue()
        assertThat(task.jooqGenerateSpringDao.get()).isTrue()
        assertThat(task.jooqGenerateFluentSetters.get()).isTrue()
        assertThat(task.jooqGenerateGlobalObjectReferences.get()).isFalse()
    }

    // ── Codegen: generator / database / strategy / target / logging ────────────

    @Test
    fun `codegen generator and schema properties are wired to task`() {
        ext.migration.flyway { }
        ext.codegen.generator.name.set("org.jooq.codegen.KotlinGenerator")
        ext.codegen.generator.database.name.set("org.jooq.meta.postgres.PostgresDatabase")
        ext.codegen.generator.database.inputSchema.set("myschema")
        ext.codegen.generator.database.includes.set("users|addresses")
        ext.codegen.generator.database.excludes.set("flyway_.*")
        ext.codegen.generator.strategy.name.set("com.example.MyStrategy")
        ext.codegen.logging.set("WARN")
        ext.codegen.onError.set("LOG")

        assertThat(task.jooqGeneratorName.get()).isEqualTo("org.jooq.codegen.KotlinGenerator")
        assertThat(task.jooqDatabaseName.get()).isEqualTo("org.jooq.meta.postgres.PostgresDatabase")
        assertThat(task.jooqInputSchema.get()).isEqualTo("myschema")
        assertThat(task.jooqIncludes.get()).isEqualTo("users|addresses")
        assertThat(task.jooqExcludes.get()).isEqualTo("flyway_.*")
        assertThat(task.jooqStrategyName.get()).isEqualTo("com.example.MyStrategy")
        assertThat(task.jooqLogging.get()).isEqualTo("WARN")
        assertThat(task.jooqOnError.get()).isEqualTo("LOG")
    }

    @Test
    fun `target package and directory are wired to task`() {
        ext.migration.flyway { }
        val absDir = project.layout.buildDirectory.dir("custom-jooq").get().asFile.absolutePath
        ext.codegen.generator.target.packageName.set("com.example.jooq")
        ext.codegen.generator.target.directory.set(absDir)

        assertThat(task.jooqTargetPackage.get()).isEqualTo("com.example.jooq")
        assertThat(task.jooqOutputDirectory.get().asFile.absolutePath).isEqualTo(absDir)
    }

    @Test
    fun `forced types are wired from DSL to task`() {
        ext.migration.flyway { }
        ext.codegen.generator.database.forcedTypes { scope ->
            scope.forcedType { ft ->
                ft.name.set("DECIMAL_INTEGER")
                ft.includeExpression.set(".*\\.points_amount")
                ft.includeTypes.set("BIGINT")
            }
        }

        val encoded = task.jooqForcedTypes.get()
        assertThat(encoded).hasSize(1)
        assertThat(encoded[0]).contains("name=DECIMAL_INTEGER")
        assertThat(encoded[0]).contains("includeExpression=.*\\.points_amount")
        assertThat(encoded[0]).contains("includeTypes=BIGINT")
    }

    // ── Flyway ─────────────────────────────────────────────────────────────────

    @Test
    fun `flyway properties are wired to task`() {
        ext.migration.flyway { flyway ->
            flyway.locations.set(listOf("filesystem:src/main/resources/db/migration"))
            flyway.defaultSchema.set("public")
            flyway.schemas.set(listOf("public", "app"))
            flyway.table.set("schema_version")
            flyway.properties.set(mapOf("outOfOrder" to "true"))
        }

        assertThat(task.migrationKind.get()).isEqualTo(MigrationKind.FLYWAY)
        assertThat(task.flywayLocations.get()).containsExactly("filesystem:src/main/resources/db/migration")
        assertThat(task.flywayDefaultSchema.get()).isEqualTo("public")
        assertThat(task.flywaySchemas.get()).containsExactly("public", "app")
        assertThat(task.flywayTable.get()).isEqualTo("schema_version")
        assertThat(task.flywayProperties.get()).containsEntry("outOfOrder", "true")
    }

    // ── Liquibase ──────────────────────────────────────────────────────────────

    @Test
    fun `liquibase properties are wired to task`() {
        ext.migration.liquibase { lb ->
            lb.changeLogFile.set("src/main/resources/db/changelog/root.xml")
            lb.defaultSchemaName.set("public")
            lb.liquibaseSchemaName.set("liquibase_meta")
            lb.databaseChangeLogTableName.set("my_changelog")
            lb.databaseChangeLogLockTableName.set("my_changelog_lock")
            lb.parameters.set(mapOf("env" to "test"))
        }

        assertThat(task.migrationKind.get()).isEqualTo(MigrationKind.LIQUIBASE)
        assertThat(task.liquibaseChangeLogFile.get()).isEqualTo("src/main/resources/db/changelog/root.xml")
        assertThat(task.liquibaseDefaultSchemaName.get()).isEqualTo("public")
        assertThat(task.liquibaseLiquibaseSchemaName.get()).isEqualTo("liquibase_meta")
        assertThat(task.liquibaseChangeLogTableName.get()).isEqualTo("my_changelog")
        assertThat(task.liquibaseChangeLogLockTableName.get()).isEqualTo("my_changelog_lock")
        assertThat(task.liquibaseParameters.get()).containsEntry("env", "test")
    }

    // ── SQL migration ─────────────────────────────────────────────────────────

    @Test
    fun `sql migration properties are wired to task`() {
        val sqlFile = project.projectDir.resolve("V1__init.sql")
        sqlFile.createNewFile()

        ext.migration.sql { sql -> sql.scripts.from(sqlFile) }

        assertThat(task.migrationKind.get()).isEqualTo(MigrationKind.SQL)
        assertThat(task.sqlScriptPaths.get()).contains(sqlFile.absolutePath)
        assertThat(task.migrationFiles.files).contains(sqlFile)
    }
}
