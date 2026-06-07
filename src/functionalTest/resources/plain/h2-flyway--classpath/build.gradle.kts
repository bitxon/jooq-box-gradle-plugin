
plugins {
    java
    id("dev.bitxon.jooq-box")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jooq:jooq:3.19.33")
    runtimeOnly("com.h2database:h2:2.3.232")

    // TODO: Flyway must be declared explicitly here because defaultDependencies fires only when the
    //  config has zero deps — adding project(":migration-scripts") suppresses it entirely.
    //  See inbox: "Fix defaultDependencies suppression for jooqMigration".
    jooqMigration("org.flywaydb:flyway-core:11.14.1")
    jooqMigration(project(":migration-scripts"))
}

jooq {
    database {
        embedded {
            type = "H2"
        }
    }
    migration {
        flyway {
            locations = listOf("classpath:db/migration")
        }
    }
    codegen {
        generator {
            database {
                inputSchema = "PUBLIC"
                excludes = "flyway_schema_history"
            }
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
