
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
    runtimeOnly("com.mysql:mysql-connector-j:9.6.0")
}

jooq {
    database {
        container {
            type = "MYSQL"
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
