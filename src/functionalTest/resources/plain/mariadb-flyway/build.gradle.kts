
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
    implementation(platform("org.jooq:jooq-bom:3.19.31"))
    implementation("org.jooq:jooq")
    implementation("org.flywaydb:flyway-mysql:11.14.1")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.5.3")
}

jooq {
    database {
        container {
            type = "MARIADB"
        }
    }
    migration {
        flyway {
            defaultSchema = "test"
        }
    }
    codegen {
        generator {
            database {
                inputSchema = "test"
                excludes = "flyway_schema_history"
            }
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
