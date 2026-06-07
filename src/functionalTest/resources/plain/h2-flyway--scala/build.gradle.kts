
plugins {
    scala
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
    implementation("org.scala-lang:scala3-library_3:3.3.6")
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
        flyway {}
    }
    codegen {
        generator {
            name = "org.jooq.codegen.ScalaGenerator"
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
