package dev.bitxon.gradle.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

abstract class AbstractFunctionalTest {

    @TempDir
    lateinit var projectDir: Path

    protected fun copyScenario(scenario: String) {
        val resourceUrl = javaClass.classLoader.getResource(scenario)
            ?: error("Functional test scenario not found on classpath: $scenario")
        val srcDir = File(resourceUrl.toURI())
        srcDir.listFiles()!!.forEach { src ->
            val dest = projectDir.resolve(src.name).toFile()
            if (src.isDirectory) src.copyRecursively(dest, overwrite = true)
            else src.copyTo(dest, overwrite = true)
        }
    }

    protected fun runTask(task: String): BuildResult = GradleRunner.create()
        .withProjectDir(projectDir.toFile())
        .withPluginClasspath()
        .withArguments(task, "--stacktrace", "--configuration-cache") // Gradle 9.3+ CC is enabled by default
        .forwardOutput()
        .build()

    protected fun runDependenciesTask(configuration: String): BuildResult = GradleRunner.create()
        .withProjectDir(projectDir.toFile())
        .withPluginClasspath()
        .withArguments("dependencies", "--configuration", configuration, "--stacktrace", "--no-configuration-cache")
        .forwardOutput()
        .build()

    protected fun assertTablesGenerated(
        tableNames: List<String>,
        outputDir: String = "build/generated-sources/jooq",
        packageName: String = "com.example.jooq",
    ) {
        val tablesDir = projectDir
            .resolve("$outputDir/${packageName.replace('.', '/')}/tables")
            .toFile()

        assertThat(tablesDir)
            .withFailMessage {
                "Expected tables directory at ${tablesDir.absolutePath} but it was not found.\n${generatedSourcesTree(outputDir)}"
            }
            .isDirectory()

        val actualNames = tablesDir.listFiles()!!.filter { it.isFile }.map { it.nameWithoutExtension }.sorted()
        assertThat(actualNames)
            .withFailMessage {
                "Expected tables $tableNames but found $actualNames in ${tablesDir.absolutePath}.\n${generatedSourcesTree(outputDir)}"
            }
            .containsExactlyInAnyOrderElementsOf(tableNames.sorted())
    }

    private fun generatedSourcesTree(outputDir: String = "build/generated-sources/jooq"): String {
        val root = projectDir.resolve(outputDir).toFile()
        if (!root.exists()) return "$outputDir/ does not exist"
        val files = root.walkTopDown().filter { it.isFile }.map { "  " + it.relativeTo(root).path }.sorted()
        return "Actual generated files:\n$outputDir/\n${files.joinToString("\n")}"
    }

    companion object {
        val JOOQ_DEFAULT = JooqBoxPlugin.JOOQ_VERSION
        val FLYWAY_DEFAULT = JooqBoxPlugin.FLYWAY_VERSION
        val POSTGRES_DEFAULT = JooqBoxPlugin.POSTGRES_DRIVER_VERSION

        fun discoverScenarios(group: String): List<String> {
            val url = AbstractFunctionalTest::class.java.classLoader.getResource(group)
                ?: return emptyList()
            return File(url.toURI())
                .listFiles()!!
                .filter { it.isDirectory }
                .map { it.name }
                .sorted()
        }
    }
}
