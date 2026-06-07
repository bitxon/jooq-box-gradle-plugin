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
    // Version-less declarations — no BOM present to supply the version
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
