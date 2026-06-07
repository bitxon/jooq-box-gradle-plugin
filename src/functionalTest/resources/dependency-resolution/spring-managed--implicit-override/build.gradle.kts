plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("dev.bitxon.jooq-box")
}

repositories {
    mavenCentral()
}

dependencies {
    // Application dependencies
    // Spring Boot dependency-management plugin applies its BOM to all configurations

    // Plugin dependencies
    // Version-less declarations — user expects Spring BOM to supply versions
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
