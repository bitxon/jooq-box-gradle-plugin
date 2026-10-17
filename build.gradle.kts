import org.gradle.plugin.compatibility.compatibility

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.spotless)
}

group = "dev.bitxon"
version = providers.gradleProperty("version").getOrElse("0.0.0-SNAPSHOT")

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // Testcontainers — plugin starts containers directly in task action
    implementation(libs.testcontainers.core)
    implementation(libs.testcontainers.postgresql)
    implementation(libs.testcontainers.mysql)
    implementation(libs.testcontainers.mariadb)

    // Worker dependencies — compiled against these but NOT shipped with the plugin.
    // At runtime the user supplies own versions via jooqCodegen/jooqMigration/jooqDatabase.
    compileOnly(libs.jooq.codegen)
    compileOnly(libs.flyway.core)
    compileOnly(libs.liquibase.core)
}

gradlePlugin {
    website = "https://github.com/bitxon/jooq-box-gradle-plugin"
    vcsUrl = "https://github.com/bitxon/jooq-box-gradle-plugin"
    plugins {
        create("jooqBox") {
            id = "dev.bitxon.jooq-box"
            implementationClass = "dev.bitxon.gradle.jooq.JooqBoxPlugin"
            displayName = "jOOQ Box"
            description = "Generates jOOQ code by spinning up a real database via Testcontainers and running Flyway/Liquibase migrations"
            tags = listOf("jooq", "codegen", "testcontainers", "docker", "flyway", "liquibase")
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

// Writes plugin default versions into a .properties file bundled in the JAR.
// JooqBoxPlugin reads it at runtime — single source of truth in libs.versions.toml.
val generatePluginConstants = tasks.register("generatePluginConstants") {
    val defaults = mapOf(
        "jooq.version" to libs.versions.jooq.get(),
        "flyway.version" to libs.versions.flyway.get(),
        "liquibase.version" to libs.versions.liquibase.get(),
        "driver.postgres.version" to libs.versions.driver.postgres.get(),
        "driver.mysql.version" to libs.versions.driver.mysql.get(),
        "driver.mariadb.version" to libs.versions.driver.mariadb.get(),
        "driver.h2.version" to libs.versions.driver.h2.get(),
        "driver.sqlite.version" to libs.versions.driver.sqlite.get(),
        "driver.hsqldb.version" to libs.versions.driver.hsqldb.get(),
    )
    val outputDir = layout.buildDirectory.dir("generated/plugin-constants")
    inputs.properties(defaults)
    outputs.dir(outputDir)
    doLast {
        val props = outputDir.get().file("plugin-defaults.properties").asFile
        props.parentFile.mkdirs()
        props.writeText(defaults.entries.joinToString("\n") { (k, v) -> "$k=$v" })
    }
}

sourceSets.main {
    resources.srcDir(generatePluginConstants)
}

testing {
    suites {
        val test = getByName<JvmTestSuite>("test") {
            useJUnitJupiter(libs.versions.junit.jupiter.get())
            dependencies {
                implementation(gradleTestKit())
                implementation(libs.jooq.codegen)
                implementation(libs.junit.jupiter.params)
                implementation(libs.assertj.core)
            }
        }
        val functionalTest = register<JvmTestSuite>("functionalTest") {
            useJUnitJupiter(libs.versions.junit.jupiter.get())
            dependencies {
                implementation(gradleTestKit())
                implementation(libs.junit.jupiter.params)
                implementation(libs.assertj.core)
                implementation(project())
            }
            targets.all {
                testTask.configure {
                    shouldRunAfter(test)
                    failFast = providers.environmentVariable("FAIL_FAST").map { it == "true" }.getOrElse(false)
                }
            }
        }
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])
tasks.check { dependsOn(testing.suites.named("functionalTest")) }

spotless {
    kotlin {
        trimTrailingWhitespace()
        endWithNewline()
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard" to "disabled",
                "ktlint_standard_import-ordering" to "enabled",
                "ktlint_standard_no-unused-imports" to "enabled",
                "ktlint_standard_no-wildcard-imports" to "enabled"
            ),
        )
        target("src/**/*.kt","consumer/**/*.kt")
    }
    kotlinGradle {
        trimTrailingWhitespace()
        endWithNewline()
        target("*.gradle.kts", "src/**/*.gradle.kts", "consumer/**/*.gradle.kts")
    }
    java {
        trimTrailingWhitespace()
        endWithNewline()
        removeUnusedImports()
        importOrder()
        target("src/**/*.java", "consumer/**/*.java")
    }
}
