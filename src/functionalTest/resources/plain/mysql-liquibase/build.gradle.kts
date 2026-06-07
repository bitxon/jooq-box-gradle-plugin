
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
    implementation("org.liquibase:liquibase-core:5.0.2")
    runtimeOnly("com.mysql:mysql-connector-j:9.6.0")
}

jooq {
    database {
        container {
            type = "MYSQL"
        }
    }
    migration {
        liquibase {
            changeLogFile = "src/main/resources/db/changelog/db.changelog-root.xml"
            defaultSchemaName = "test"
        }
    }
    codegen {
        generator {
            database {
                inputSchema = "test"
                excludes = "databasechangelog|databasechangeloglock"
            }
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
