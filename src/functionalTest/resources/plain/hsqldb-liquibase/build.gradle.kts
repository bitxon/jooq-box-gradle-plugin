
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
    runtimeOnly("org.hsqldb:hsqldb:2.7.4")
}

jooq {
    database {
        embedded {
            type = "HSQLDB"
        }
    }
    migration {
        liquibase {
            changeLogFile = "src/main/resources/db/changelog/db.changelog-root.xml"
        }
    }
    codegen {
        generator {
            database {
                inputSchema = "PUBLIC"
                excludes = "databasechangelog|databasechangeloglock"
            }
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
