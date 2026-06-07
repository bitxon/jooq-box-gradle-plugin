package dev.bitxon.gradle.jooq.internal.worker

import org.assertj.core.api.Assertions.assertThat
import org.jooq.meta.jaxb.ForcedTypeObjectType
import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.Nullability
import org.jooq.meta.jaxb.OnError
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CodegenConfigBuilderTest {

    private val minimalInput = CodegenInput(
        jdbcUrl = "jdbc:postgresql://localhost:5432/test",
        jdbcUser = "testuser",
        jdbcPassword = "testpass",
        jdbcDriver = "org.postgresql.Driver",
    )

    // ── JDBC ──────────────────────────────────────────────────────────────────

    @Test
    fun `jdbc credentials are always set`() {
        val config = buildJooqConfiguration(minimalInput)

        assertThat(config.jdbc.url).isEqualTo("jdbc:postgresql://localhost:5432/test")
        assertThat(config.jdbc.user).isEqualTo("testuser")
        assertThat(config.jdbc.password).isEqualTo("testpass")
        assertThat(config.jdbc.driver).isEqualTo("org.postgresql.Driver")
    }

    // ── Generate flags ─────────────────────────────────────────────────────────

    @Test
    fun `all generate flags are forwarded to Configuration`() {
        val input = minimalInput.copy(
            generatePojos = true,
            generateImmutablePojos = true,
            generateDaos = true,
            generateInterfaces = true,
            generateRecords = false,
            generateJavaTimeTypes = false,
            generateSpringAnnotations = true,
            generateSpringDao = true,
            generateFluentSetters = true,
            generateGlobalObjectReferences = false,
        )

        val generate = buildJooqConfiguration(input).generator.generate

        assertThat(generate.isPojos).isTrue()
        assertThat(generate.isImmutablePojos).isTrue()
        assertThat(generate.isDaos).isTrue()
        assertThat(generate.isInterfaces).isTrue()
        assertThat(generate.isRecords).isFalse()
        assertThat(generate.isJavaTimeTypes).isFalse()
        assertThat(generate.isSpringAnnotations).isTrue()
        assertThat(generate.isSpringDao).isTrue()
        assertThat(generate.isFluentSetters).isTrue()
        assertThat(generate.isGlobalObjectReferences).isFalse()
    }

    @Test
    fun `absent generate flags leave jOOQ defaults untouched`() {
        // No generate flags set — jOOQ's own defaults apply.
        // This verifies we don't accidentally force a value when the user didn't set one.
        val generate = buildJooqConfiguration(minimalInput).generator.generate

        // jOOQ 3.13+ defaults: pojos=false, daos=false, javaTimeTypes=true, records=true
        assertThat(generate.isPojos).isFalse()
        assertThat(generate.isDaos).isFalse()
        assertThat(generate.isJavaTimeTypes).isTrue()
        assertThat(generate.isRecords).isTrue()
    }

    // ── Generator / Database / Strategy / Target ───────────────────────────────

    @Test
    fun `generator name is forwarded`() {
        val input = minimalInput.copy(generatorName = "org.jooq.codegen.KotlinGenerator")

        assertThat(buildJooqConfiguration(input).generator.name)
            .isEqualTo("org.jooq.codegen.KotlinGenerator")
    }

    @Test
    fun `database schema properties are forwarded`() {
        val input = minimalInput.copy(
            databaseName = "org.jooq.meta.postgres.PostgresDatabase",
            inputSchema = "myschema",
            includes = "users|addresses",
            excludes = "flyway_.*",
        )

        val database = buildJooqConfiguration(input).generator.database

        assertThat(database.name).isEqualTo("org.jooq.meta.postgres.PostgresDatabase")
        assertThat(database.inputSchema).isEqualTo("myschema")
        assertThat(database.includes).isEqualTo("users|addresses")
        assertThat(database.excludes).isEqualTo("flyway_.*")
    }

    @Test
    fun `strategy name is forwarded`() {
        val input = minimalInput.copy(strategyName = "com.example.MyStrategy")

        assertThat(buildJooqConfiguration(input).generator.strategy.name)
            .isEqualTo("com.example.MyStrategy")
    }

    @Test
    fun `target package and directory are forwarded`() {
        val input = minimalInput.copy(
            targetPackage = "com.example.jooq",
            targetDirectory = "/tmp/generated",
        )

        val target = buildJooqConfiguration(input).generator.target

        assertThat(target.packageName).isEqualTo("com.example.jooq")
        assertThat(target.directory).isEqualTo("/tmp/generated")
    }

    // ── Logging / OnError ──────────────────────────────────────────────────────

    @Test
    fun `logging and onError enum values are forwarded`() {
        val input = minimalInput.copy(logging = "WARN", onError = "LOG")

        val config = buildJooqConfiguration(input)

        assertThat(config.logging).isEqualTo(Logging.WARN)
        assertThat(config.onError).isEqualTo(OnError.LOG)
    }

    // ── ForcedTypes ────────────────────────────────────────────────────────────

    @Test
    fun `forced types are decoded and forwarded`() {
        val encoded = "name=DECIMAL_INTEGER\nincludeExpression=.*\\.points_amount\nincludeTypes=BIGINT"
        val input = minimalInput.copy(forcedTypes = listOf(encoded))

        val forcedTypes = buildJooqConfiguration(input).generator.database.forcedTypes

        assertThat(forcedTypes).hasSize(1)
        assertThat(forcedTypes[0].name).isEqualTo("DECIMAL_INTEGER")
        assertThat(forcedTypes[0].includeExpression).isEqualTo(".*\\.points_amount")
        assertThat(forcedTypes[0].includeTypes).isEqualTo("BIGINT")
    }

    @Test
    fun `empty forced types list leaves database forcedTypes empty`() {
        val input = minimalInput.copy(forcedTypes = emptyList())

        val forcedTypes = buildJooqConfiguration(input).generator.database.forcedTypes

        assertThat(forcedTypes).isEmpty()
    }

    // ── decodeForcedType ───────────────────────────────────────────────────────

    @Nested
    inner class DecodeForcedTypeTest {

        @Test
        fun `decodes all string fields`() {
            val encoded = """
                name=BOOLEAN
                userType=com.example.MyType
                includeExpression=.*\.col
                excludeExpression=.*\.excl
                includeTypes=TINYINT
                excludeTypes=VARCHAR
                converter=com.example.MyConverter
                binding=com.example.MyBinding
            """.trimIndent()

            val ft = decodeForcedType(encoded)

            assertThat(ft.name).isEqualTo("BOOLEAN")
            assertThat(ft.userType).isEqualTo("com.example.MyType")
            assertThat(ft.includeExpression).isEqualTo(".*\\.col")
            assertThat(ft.excludeExpression).isEqualTo(".*\\.excl")
            assertThat(ft.includeTypes).isEqualTo("TINYINT")
            assertThat(ft.excludeTypes).isEqualTo("VARCHAR")
            assertThat(ft.converter).isEqualTo("com.example.MyConverter")
            assertThat(ft.binding).isEqualTo("com.example.MyBinding")
        }

        @Test
        fun `decodes nullability and objectType enums`() {
            val encoded = "nullability=NOT_NULL\nobjectType=COLUMN"

            val ft = decodeForcedType(encoded)

            assertThat(ft.nullability).isEqualTo(Nullability.NOT_NULL)
            assertThat(ft.objectType).isEqualTo(ForcedTypeObjectType.COLUMN)
        }

        @Test
        fun `value containing equals sign is preserved`() {
            // includeExpression values can contain regex with = in them
            val encoded = "includeExpression=.*\\.col=val"

            val ft = decodeForcedType(encoded)

            assertThat(ft.includeExpression).isEqualTo(".*\\.col=val")
        }

        @Test
        fun `unknown fields are silently ignored`() {
            val encoded = "name=BOOLEAN\nunknownField=something"

            val ft = decodeForcedType(encoded)

            assertThat(ft.name).isEqualTo("BOOLEAN")
        }
    }
}
