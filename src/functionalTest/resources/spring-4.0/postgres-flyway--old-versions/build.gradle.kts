
plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
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

dependencyManagement {
    dependencies {
        dependency("org.jooq:jooq:3.18.31")
        dependency("org.jooq:jooq-codegen:3.18.31")
        dependency("org.jooq:jooq-meta:3.18.31")
        dependency("org.flywaydb:flyway-core:10.22.0")
        dependency("org.flywaydb:flyway-database-postgresql:10.22.0")
        dependency("org.postgresql:postgresql:42.6.2")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    jooqCodegen("org.jooq:jooq-codegen:3.18.31")
    jooqMigration("org.flywaydb:flyway-core:10.22.0")
    jooqMigration("org.flywaydb:flyway-database-postgresql:10.22.0")
    jooqDatabase("org.postgresql:postgresql:42.6.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jooq {
    database {
        container {
            type = "POSTGRES"
            image = "postgres:15-alpine"
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
                packageName = "com.example.jooq"
            }
        }
    }
}
