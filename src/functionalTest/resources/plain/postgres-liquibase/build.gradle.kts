
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
    runtimeOnly("org.postgresql:postgresql:42.7.10")
}

jooq {
    database {
        container {
            type = "POSTGRES"
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
                inputSchema = "public"
                excludes = "databasechangelog|databasechangeloglock"
            }
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
