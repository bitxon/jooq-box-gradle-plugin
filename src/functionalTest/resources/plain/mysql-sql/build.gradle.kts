
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
    runtimeOnly("com.mysql:mysql-connector-j:9.6.0")
}

jooq {
    database {
        container {
            type = "MYSQL"
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
                inputSchema = "test"
            }
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
