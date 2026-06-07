
plugins {
    kotlin("jvm") version "2.2.0"
    id("dev.bitxon.jooq-box")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jooq:jooq:3.19.33")
    implementation("org.jooq:jooq-kotlin:3.19.33")
    runtimeOnly("com.h2database:h2:2.3.232")
}

jooq {
    database {
        embedded {
            type = "H2"
        }
    }
    migration {
        flyway {}
    }
    codegen {
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                inputSchema = "PUBLIC"
                excludes = "flyway_schema_history"
            }
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
