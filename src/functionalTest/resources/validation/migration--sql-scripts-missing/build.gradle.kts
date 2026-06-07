// Intentionally omits migration.sql.scripts.
// The plugin should reject this at task execution with a clear GradleException.
plugins {
    id("dev.bitxon.jooq-box")
}

jooq {
    database {
        embedded { type = "H2" }
    }
    migration {
        sql { } // scripts are intentionally not set
    }
    codegen {
        generator {
            target {
                packageName = "com.example.jooq"
            }
        }
    }
}
