plugins {
    java
    id("io.quarkus") version "3.35.3"
    id("dev.bitxon.jooq-box")
}

group = "com.example"
version = "0.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.35.3"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkiverse.jooq:quarkus-jooq:2.1.0")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

jooq {
    database {
        container {
            type = "postgres"
        }
    }
    migration {
        flyway {}
    }
    codegen {
        generator {
            database {
                inputSchema = "public"
                excludes = "flyway_schema_history"
            }
            target {
                packageName = "com.example.generated.jooq"
            }
        }
    }
}
