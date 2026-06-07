
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
    runtimeOnly("org.xerial:sqlite-jdbc:3.47.1.0")
}

jooq {
    database {
        embedded {
            type = "SQLITE"
        }
    }
    migration {
        flyway {}
    }
    codegen {
        generator {
            database {
                excludes = "flyway_schema_history"
            }
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
