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
    runtimeOnly("com.h2database:h2:2.3.232")
}

jooq {
    database {
        embedded {
            type = "H2"
        }
    }
    migration {
        sql {
            scripts.from(
                "src/main/resources/db/migration/V1__create_users.sql",
                "src/main/resources/db/migration/V2__create_addresses.sql"
            )
        }
    }
    codegen {
        generator {
            database {
                inputSchema = "PUBLIC"
            }
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
