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
    jooqCodegen("org.jooq:jooq-codegen:3.18.31")
    jooqMigration("org.flywaydb:flyway-core:10.22.0")
    jooqMigration("org.flywaydb:flyway-database-postgresql:10.22.0")
    jooqDatabase("org.postgresql:postgresql:42.6.2")
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
