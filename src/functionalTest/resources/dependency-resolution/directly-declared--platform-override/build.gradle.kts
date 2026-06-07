plugins {
    java
    id("dev.bitxon.jooq-box")
}

repositories {
    mavenCentral()
}

dependencies {
    // Application dependencies
    // (none)

    // Plugin dependencies
    jooqCodegen(platform("org.jooq:jooq-bom:3.18.31"))
    jooqCodegen("org.jooq:jooq-codegen")

    // io.dropwizard.flywaydb is used as a third-party Flyway BOM (Flyway has no official BOM)
    jooqMigration(platform("io.dropwizard.flywaydb:flyway-bom:11.11.1"))
    jooqMigration("org.flywaydb:flyway-core")
    jooqMigration("org.flywaydb:flyway-database-postgresql")

    // jooqDatabase: no official BOM available for the PostgreSQL JDBC driver
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
