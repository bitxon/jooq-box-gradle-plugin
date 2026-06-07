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
