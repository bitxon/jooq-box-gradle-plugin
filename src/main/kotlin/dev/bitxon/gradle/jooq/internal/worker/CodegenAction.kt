package dev.bitxon.gradle.jooq.internal.worker

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.ForcedTypeObjectType
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.Nullability
import org.jooq.meta.jaxb.OnError
import org.jooq.meta.jaxb.Strategy
import org.jooq.meta.jaxb.Target
import java.io.File

abstract class CodegenAction : WorkAction<CodegenAction.Params> {

    interface Params : WorkParameters {
        val jdbcUrl: Property<String>
        val jdbcUser: Property<String>
        val jdbcPassword: Property<String>
        val jdbcDriver: Property<String>
        val generatorName: Property<String>
        val databaseName: Property<String>
        val inputSchema: Property<String>
        val includes: Property<String>
        val excludes: Property<String>
        val forcedTypes: ListProperty<String>
        val generatePojos: Property<Boolean>
        val generateImmutablePojos: Property<Boolean>
        val generateDaos: Property<Boolean>
        val generateInterfaces: Property<Boolean>
        val generateRecords: Property<Boolean>
        val generateJavaTimeTypes: Property<Boolean>
        val generateSpringAnnotations: Property<Boolean>
        val generateSpringDao: Property<Boolean>
        val generateFluentSetters: Property<Boolean>
        val generateGlobalObjectReferences: Property<Boolean>
        val strategyName: Property<String>
        val targetPackage: Property<String>
        val targetDirectory: Property<String>
        val logging: Property<String>
        val onError: Property<String>
        val configFile: RegularFileProperty
    }

    override fun execute() {
        GenerationTool.generate(
            buildJooqConfiguration(
                CodegenInput(
                    jdbcUrl = parameters.jdbcUrl.get(),
                    jdbcUser = parameters.jdbcUser.get(),
                    jdbcPassword = parameters.jdbcPassword.get(),
                    jdbcDriver = parameters.jdbcDriver.get(),
                    generatorName = parameters.generatorName.orNull,
                    databaseName = parameters.databaseName.orNull,
                    inputSchema = parameters.inputSchema.orNull,
                    includes = parameters.includes.orNull,
                    excludes = parameters.excludes.orNull,
                    forcedTypes = parameters.forcedTypes.orNull,
                    generatePojos = parameters.generatePojos.orNull,
                    generateImmutablePojos = parameters.generateImmutablePojos.orNull,
                    generateDaos = parameters.generateDaos.orNull,
                    generateInterfaces = parameters.generateInterfaces.orNull,
                    generateRecords = parameters.generateRecords.orNull,
                    generateJavaTimeTypes = parameters.generateJavaTimeTypes.orNull,
                    generateSpringAnnotations = parameters.generateSpringAnnotations.orNull,
                    generateSpringDao = parameters.generateSpringDao.orNull,
                    generateFluentSetters = parameters.generateFluentSetters.orNull,
                    generateGlobalObjectReferences = parameters.generateGlobalObjectReferences.orNull,
                    strategyName = parameters.strategyName.orNull,
                    targetPackage = parameters.targetPackage.orNull,
                    targetDirectory = parameters.targetDirectory.orNull,
                    logging = parameters.logging.orNull,
                    onError = parameters.onError.orNull,
                    configFile = parameters.configFile.asFile.orNull,
                )
            )
        )
    }
}

internal data class CodegenInput(
    val jdbcUrl: String,
    val jdbcUser: String,
    val jdbcPassword: String,
    val jdbcDriver: String,
    val generatorName: String? = null,
    val databaseName: String? = null,
    val inputSchema: String? = null,
    val includes: String? = null,
    val excludes: String? = null,
    val forcedTypes: List<String>? = null,
    val generatePojos: Boolean? = null,
    val generateImmutablePojos: Boolean? = null,
    val generateDaos: Boolean? = null,
    val generateInterfaces: Boolean? = null,
    val generateRecords: Boolean? = null,
    val generateJavaTimeTypes: Boolean? = null,
    val generateSpringAnnotations: Boolean? = null,
    val generateSpringDao: Boolean? = null,
    val generateFluentSetters: Boolean? = null,
    val generateGlobalObjectReferences: Boolean? = null,
    val strategyName: String? = null,
    val targetPackage: String? = null,
    val targetDirectory: String? = null,
    val logging: String? = null,
    val onError: String? = null,
    val configFile: File? = null,
)

internal fun buildJooqConfiguration(input: CodegenInput): Configuration {
    val config = input.configFile
        ?.let { GenerationTool.load(it.inputStream()) }
        ?: Configuration()

    val generator = config.generator ?: Generator()
    val database = generator.database ?: Database()
    val generate = generator.generate ?: Generate()
    val strategy = generator.strategy ?: Strategy()
    val target = generator.target ?: Target()

    input.generatorName?.let { generator.withName(it) }

    input.databaseName?.let { database.withName(it) }
    input.inputSchema?.let { database.withInputSchema(it) }
    input.includes?.let { database.withIncludes(it) }
    input.excludes?.let { database.withExcludes(it) }
    input.forcedTypes?.takeIf { it.isNotEmpty() }
        ?.let { database.withForcedTypes(it.map { s -> decodeForcedType(s) }) }

    input.generatePojos?.let { generate.withPojos(it) }
    input.generateImmutablePojos?.let { generate.withImmutablePojos(it) }
    input.generateDaos?.let { generate.withDaos(it) }
    input.generateInterfaces?.let { generate.withInterfaces(it) }
    input.generateRecords?.let { generate.withRecords(it) }
    input.generateJavaTimeTypes?.let { generate.withJavaTimeTypes(it) }
    input.generateSpringAnnotations?.let { generate.withSpringAnnotations(it) }
    input.generateSpringDao?.let { generate.withSpringDao(it) }
    input.generateFluentSetters?.let { generate.withFluentSetters(it) }
    input.generateGlobalObjectReferences?.let { generate.withGlobalObjectReferences(it) }

    input.strategyName?.let { strategy.withName(it) }

    input.targetPackage?.let { target.withPackageName(it) }
    input.targetDirectory?.let { target.withDirectory(it) }

    input.logging?.let { config.withLogging(Logging.fromValue(it)) }
    input.onError?.let { config.withOnError(OnError.fromValue(it)) }

    config.withJdbc(
        Jdbc()
            .withUrl(input.jdbcUrl)
            .withUser(input.jdbcUser)
            .withPassword(input.jdbcPassword)
            .withDriver(input.jdbcDriver)
    )
    config.withGenerator(
        generator
            .withDatabase(database)
            .withGenerate(generate)
            .withStrategy(strategy)
            .withTarget(target)
    )

    return config
}

internal fun decodeForcedType(encoded: String): ForcedType {
    val fields = encoded.lines()
        .filter { '=' in it }
        .associate { line ->
            val idx = line.indexOf('=')
            line.substring(0, idx) to line.substring(idx + 1)
        }
    return ForcedType().apply {
        fields["name"]?.let { withName(it) }
        fields["userType"]?.let { withUserType(it) }
        fields["includeExpression"]?.let { withIncludeExpression(it) }
        fields["excludeExpression"]?.let { withExcludeExpression(it) }
        fields["includeTypes"]?.let { withIncludeTypes(it) }
        fields["excludeTypes"]?.let { withExcludeTypes(it) }
        fields["converter"]?.let { withConverter(it) }
        fields["binding"]?.let { withBinding(it) }
        fields["nullability"]?.let { withNullability(Nullability.fromValue(it)) }
        fields["objectType"]?.let { withObjectType(ForcedTypeObjectType.fromValue(it)) }
    }
}
