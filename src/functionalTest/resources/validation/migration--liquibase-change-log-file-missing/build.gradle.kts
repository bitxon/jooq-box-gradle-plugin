// Intentionally omits migration.liquibase.changeLogFile.
// The plugin should reject this at task execution with a clear GradleException.
plugins {
    java
    id("dev.bitxon.jooq-box")
}

repositories {
    mavenCentral()
}

jooq {
    database {
        embedded { type = "H2" }
    }
    migration {
        liquibase { } // changeLogFile is intentionally not set
    }
    codegen {
        generator {
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
