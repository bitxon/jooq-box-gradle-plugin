plugins {
    java
    id("dev.bitxon.jooq-box")
}

repositories {
    mavenCentral()
}

dependencies {
    // Application dependencies
    implementation(platform("org.jooq:jooq-bom:3.18.31"))
    implementation(platform("io.dropwizard.flywaydb:flyway-bom:11.11.1"))
    implementation("org.postgresql:postgresql:42.6.2") // no BOM available for the PostgreSQL JDBC driver

    // Plugin dependencies
    // Version-less declarations — user expects application platforms to supply versions.
    // Platforms on standard configurations are isolated from custom Gradle configurations.
    jooqCodegen("org.jooq:jooq-codegen")
    jooqMigration("org.flywaydb:flyway-core")
    jooqMigration("org.flywaydb:flyway-database-postgresql")
    jooqDatabase("org.postgresql:postgresql")
}

jooq {
    database {
        container {
            type = "POSTGRES"
        }
    }
    migration {
        flyway {}
    }
    codegen {
        generator {
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
